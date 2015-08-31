package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.fromJson;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.versioning.OntologyVersionState;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.app.owl.versioning.change.VMetadataChange;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.AtActivity;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnActivityState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowState;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.io.StringDocumentSource;

/**
 * <p>
 * Used to perform an update, a partial or a complete synchronization of
 * the revision graph between two peers. This can be used to push changes
 * to a peer, or pull from a peer, or synchronize two peers (which might
 * entail both a pull and a push).
 * </p>
 *
 * TODO - revisit this for transaction boundaries!
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionUpdateActivity extends FSMActivity
{
	public static final String TYPENAME = "version-update-activity";
	
	public static final String ONTOLOGY_HANDLE = "ontologyHandle";
	public static final String REVISION_HEADS = "revisionHeads";
	public static final String REVISIONS = "revisions";
	public static final String LAST_META_CHANGE = "lastMetaChange";
	
	public static final WorkflowStateConstant WaitForRevisionChangeSet = WorkflowStateConstant.makeStateConstant("WaitForRevisionChangeSet");
	public static final WorkflowStateConstant WaitForRevisionObjects = WorkflowStateConstant.makeStateConstant("WaitForRevisionObjects");
	public static final WorkflowStateConstant PullRequested = WorkflowStateConstant.makeStateConstant("PullRequested");
	public static final WorkflowStateConstant PushAccepted = WorkflowStateConstant.makeStateConstant("PushAccepted");
	
	public static enum States 
	{
		WaitForChangeSet
		;
		
		private WorkflowStateConstant stateConstant;
		States()
		{
			stateConstant = WorkflowStateConstant.makeStateConstant(name());
		}
		public WorkflowStateConstant state() { return stateConstant; }
	}
		
	private OntologyVersionState.Delta delta = null;
	private List<VMetadataChange<VersionedOntology>> metaChanges = null;	
	private HGHandle remoteOntologyHandle;	
	private String action;
	private String completedMessage;

	private RemoteOntology remoteOnto() { return getThisPeer().getGraph().get(remoteOntologyHandle); }
	
	private WorkflowStateConstant startPulling()
	{
		getThisPeer().getActivityManager().initiateActivity(
				new GetNewRevisionsActivity(getThisPeer(), remoteOntologyHandle), this, null);
		return WaitForRevisionChangeSet;
	}
	
	private VersionManager versionManager() 
	{
		return new VersionManager(getThisPeer().getGraph(), "fixme-VHDBOntologyRepository");		
	}
	
	Set<String> checkBranchConflicts(RemoteOntology remoteOnto, List<VMetadataChange<VersionedOntology>> metaChanges)
	{
		HashSet<String> branchConflicts = new HashSet<String>();
		if (metaChanges == null)
			return branchConflicts;
		VersionManager vm = versionManager();
		if (!vm.isVersioned(remoteOnto.getOntologyHandle()))
			return branchConflicts;
		VersionedOntology vonto = vm.versioned(remoteOnto.getOntologyHandle());
		List<VMetadataChange<VersionedOntology>> localList = 
			ActivityUtils.collectMetaChanges(getThisPeer().getGraph(), vonto, remoteOnto.getLastMetaChange());
		localList = versioning.normalize(vonto, localList, false);
		metaChanges = versioning.normalize(vonto, metaChanges, false);
		for (Pair<VMetadataChange<VersionedOntology>, VMetadataChange<VersionedOntology>> conf : 
				versioning.findConflicts(localList, metaChanges))
			branchConflicts.add("Incoming change " + conf.getFirst() + 
								" conflicts with local change " + conf.getSecond());
		return branchConflicts;		
	}
	
	/**
	 * @param thisPeer
	 */
	public VersionUpdateActivity(HyperGraphPeer thisPeer)
	{
		super(thisPeer);
	}

	/**
	 * @param thisPeer
	 * @param id
	 */
	public VersionUpdateActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
	}
	
	public VersionUpdateActivity remoteOntology(HGHandle remoteOntology)
	{
		this.remoteOntologyHandle = remoteOntology;
		return this;
	}
	
	public VersionUpdateActivity action(String action)
	{
		this.action = action;
		return this;
	}
	
	public String completedMessage()
	{
		return completedMessage;
	}
	
	@Override
	public void initiate()
	{
		RemoteOntology remoteOntology = getThisPeer().getGraph().get(remoteOntologyHandle);
		Json msg;
		if ("pull".equals(action))
		{
			getState().assign(startPulling());
		}
		else if ("push".equals(action))
		{
			msg = createMessage(Performative.Request, 
								Json.object(ONTOLOGY_HANDLE, remoteOntology.getOntologyHandle(),
											"heads", remoteOntology.getRevisionHeads(),
											"action", "pull"));
			getState().assign(PullRequested);
			send(remoteOntology.getRepository().getPeer(), msg);				
		}
		else if ("synch".equals(action))
		{
			throw new UnsupportedOperationException("synch operation of version update not supported yet.");
		}
		else
			throw new IllegalArgumentException("Possible values for version update action are 'push', 'pull' or 'synch' and '" +
						action + "' is not one of them.");
	}
	
	/**
	 * Start pulling after the set of revisions to pull has been obtained.
	 */
	@FromState("WaitForRevisionChangeSet")
	@AtActivity(GetNewRevisionsActivity.TYPENAME)
	@OnActivityState("Completed")
	public WorkflowStateConstant askForChanges(GetNewRevisionsActivity revisionSetActivity)
	{
		RemoteOntology remoteOnto = remoteOnto();
		//System.out.println("got revision set " + getRevisionSetActivity.newRevisions());
		delta = revisionSetActivity.delta();
		metaChanges = revisionSetActivity.metaChanges();
		for (VMetadataChange<VersionedOntology> change : metaChanges)
			change.setHyperGraph(getThisPeer().getGraph());
		Set<String> branchConflicts = checkBranchConflicts(remoteOnto, metaChanges);
		if (branchConflicts.isEmpty())
		{
			send(remoteOnto.getRepository().getPeer(), 
				 createMessage(Performative.QueryRef, Json.object(REVISIONS, delta.revisions,
						 										  ONTOLOGY_HANDLE, remoteOnto.getOntologyHandle())));	
			return WaitForRevisionObjects;
		}
		else
		{
			if (branchConflicts.size() > 1)
				this.completedMessage = "" + branchConflicts.size() + 
						" branch conflicts found : " + branchConflicts.toString();
			else
				this.completedMessage = "1 branch conflict found : " + branchConflicts.toString();				
			return WorkflowState.Failed;
		}
	}
	
	@FromState("WaitForRevisionObjects")
	@OnMessage(performative="InformRef")
	public WorkflowStateConstant receiveChanges(Json msg)
	{
		System.out.println("Got changes " + msg.at(Messages.CONTENT).asString());
		HyperGraph graph = getThisPeer().getGraph();
		HGDBOntologyManager manager = HGOntologyManagerFactory.getOntologyManager(graph.getLocation());
		VOWLXMLDocument doc = ActivityUtils.parseVersionedDoc(manager, 
									new StringDocumentSource(msg.at(Messages.CONTENT).asString()));
		VersionedOntology vo = null;
		if (doc.getRevisionData().getOntologyID().isAnonymous()) // this is not a clone, so we have the IRI locally
		{
			HGHandle ontologyHandle = graph.getHandleFactory().makeHandle(doc.getOntologyID());
			vo = versionManager().versioned(ontologyHandle);
			ActivityUtils.updateVersionedOntology(manager, vo, doc);
		}
		else
			vo = ActivityUtils.storeClonedOntology(manager, doc);

		RemoteOntology remoteOnto = remoteOnto();		
		remoteOnto.setRevisionHeads(delta.heads);	
		if (metaChanges != null)
			remoteOnto.setLastMetaChange(vo.metadata().applyChanges(metaChanges));
		//System.out.println("New revision heads: " + delta.heads);
		getThisPeer().getGraph().update(remoteOnto);
		if (msg.has(Messages.REPLY_WITH))
			reply(msg, Performative.Confirm, Json.nil());
		return WorkflowState.Completed;
	}
	
	public String sendRequestedRevisions(Json msg)
	{
//		System.out.println("asked for revisions: " + msg.at(REVISIONS));
		HGHandle ontologyHandle = Messages.fromJson(msg.at(Messages.CONTENT).at(ONTOLOGY_HANDLE));
		Set<HGHandle> revisions = Messages.fromJson(msg.at(Messages.CONTENT).at(REVISIONS));
		VersionManager versionManager = new VersionManager(getThisPeer().getGraph(), 
														   "fixme-VHDBOntologyRepository");
		if (!versionManager.isVersioned(ontologyHandle))
		{
			reply(msg, Performative.Failure, 
					Json.object("error", "The ontology does not exist or is not versioned."));
			return null;
		}
		try
		{
			VersionedOntology versionedOntology = versionManager.versioned(ontologyHandle);
			System.out.println("At source parents " + versionedOntology.revision().parents());
			// If this is a clone, we send also the latest snapshot of the ontology, otherwise
			// we just send the revisions and change sets
			String serializedOntology = revisions.contains(versionedOntology.getRootRevision()) ?
					ActivityUtils.renderVersionedOntology(versionedOntology) :
					ActivityUtils.renderVersionedOntologyDelta(versionedOntology, revisions);
			return serializedOntology;
		}
		catch (Exception ex)
		{
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex);
		}		
	}
	
	/**
	 * Answer a pull request.
	 *  
	 * @param msg
	 * @return
	 */
	@FromState({"Started"})
	@OnMessage(performative="QueryRef")
	public WorkflowStateConstant pullChanges(Json msg)
	{
		String serializedOntology = sendRequestedRevisions(msg);
		if (serializedOntology == null)
			return WorkflowState.Failed;
		reply(msg, Performative.InformRef, serializedOntology);
		return WorkflowState.Completed;
	}
	
	/**
	 * Answer a pull request during push - we don't want to complete immediately, we
	 * wait for confirmation from peer before Activity can be marked as completed!
	 *  
	 * @param msg
	 * @return
	 */
	@FromState({"PushAccepted"})
	@OnMessage(performative="QueryRef")
	public WorkflowStateConstant pushChanges(Json msg)
	{
		String serializedOntology = sendRequestedRevisions(msg);
		if (serializedOntology == null)
			return WorkflowState.Failed;
		Json reply = getReply(msg, Performative.InformRef, serializedOntology)
				 // this is just to indicate that we want a confirmation
				 // any non-empty REPLY_WITH will force the receiveChanges to reply
							.set(Messages.REPLY_WITH, "confirmation");
        post(getSender(msg), reply);
		return PushAccepted;
	}	

	@FromState({"PushAccepted"})
	@OnMessage(performative="Confirm")
	public WorkflowStateConstant changesPushed(Json msg)
	{
		return WorkflowState.Completed;
	}	

	/**
	 * Answer a push request (peer is asking us to pull from it).
	 *  
	 * @param msg
	 * @return
	 */
	@FromState("Started")
	@OnMessage(performative="Request")
	public WorkflowStateConstant pushRequested(Json msg)
	{
		if (!msg.at(CONTENT).has("action"))
		{
			reply(msg, Performative.Failure, "Missing action operand of request performative.");
			return WorkflowStateConstant.Failed;
		}
		action = msg.at(CONTENT).at("action").asString();
		if (action.equals("pull"))
		{
			HGHandle ontologyHandle = fromJson(msg.at(CONTENT).at(ONTOLOGY_HANDLE));
			if (getThisPeer().getGraph().get(ontologyHandle) == null)
			{
				reply(msg, Performative.Refuse, "Unknown ontology.");
				return WorkflowStateConstant.Failed;
			}						
			Set<HGHandle> heads = fromJson(msg.at(CONTENT).at("heads"));			
			HGPeerIdentity otherPeer = getThisPeer().getIdentity(getSender(msg));			
			VDHGDBOntologyRepository repo = new VDHGDBOntologyRepository(getThisPeer());
			RemoteOntology remote = repo.remoteOnto(ontologyHandle, repo.remoteRepo(otherPeer));
			remote.setRevisionHeads(heads);
			getThisPeer().getGraph().update(remote);
			remoteOntologyHandle = getThisPeer().getGraph().getHandle(remote);
			reply(msg, Performative.Agree, Json.object());
			return startPulling();
		}
		else
		{
			reply(msg, Performative.Failure, "Unrecognized action '" + action + "' of request performative.");
			return WorkflowStateConstant.Failed;			
		}
	}
	
	@FromState("PullRequested")
	@OnMessage(performative="Refuse")
	public WorkflowStateConstant pushRefused(Json msg)
	{
		this.completedMessage = msg.at(CONTENT).asString();
		return WorkflowStateConstant.Failed;
	}

	@FromState("PullRequested")
	@OnMessage(performative="Agree")
	public WorkflowStateConstant pushAccepted(Json msg)
	{
		return PushAccepted;
	}
	
	@Override
	public String getType()
	{
		return TYPENAME;
	}

	/**
	 * This is to force complete static initialization which does not occur when just referring to the class during
	 * bootstrap.
	 */
	@SuppressWarnings({ "static-access", "unchecked" })
	public static Class<VersionUpdateActivity> initializedClass()
	{
		try
		{
			return (Class<VersionUpdateActivity>) VersionUpdateActivity.class.forName(VersionUpdateActivity.class.getName(), 
						true, VersionUpdateActivity.class.getClassLoader());
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	
}

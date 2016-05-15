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
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
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
	public static final WorkflowStateConstant CloneRequested = WorkflowStateConstant.makeStateConstant("CloneRequested");
	public static final WorkflowStateConstant PublishAccepted = WorkflowStateConstant.makeStateConstant("PublishAccepted");
	
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
		
	public static enum ActionType
	{
		pull,
		push,
		sync,
		clone,
		publish
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
		return new VersionManager(getThisPeer().getGraph(), 
								  getThisPeer().getPeerInterface().principal().getName());		
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
	
	public VersionUpdateActivity(HyperGraphPeer thisPeer)
	{
		super(thisPeer);
	}

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

	public VersionUpdateActivity action(ActionType action)
	{
		this.action = action.name();
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
		if (ActionType.clone.name().equals(action))
		{
			msg = createMessage(Performative.QueryRef, 
								Json.object(ONTOLOGY_HANDLE, remoteOntology.getOntologyHandle()));
			send(remoteOntology.getRepository().getPeer(), msg);							 										  
			getState().assign(WaitForRevisionObjects);			
		}
		else if (ActionType.publish.name().equals(action))
		{
			msg = createMessage(Performative.Request, 
					Json.object(ONTOLOGY_HANDLE, remoteOntology.getOntologyHandle(),
								"action", ActionType.clone.name()));
			send(remoteOntology.getRepository().getPeer(), msg);				
			getState().assign(CloneRequested);						
		}
		else if (ActionType.pull.name().equals(action))
		{
			getState().assign(startPulling());
		}
		else if (ActionType.push.name().equals(action))
		{
			msg = createMessage(Performative.Request, 
								Json.object(ONTOLOGY_HANDLE, remoteOntology.getOntologyHandle(),
//											"heads", remoteOntology.getOntologyHandle(),
											"action", ActionType.pull.name()));
			send(remoteOntology.getRepository().getPeer(), msg);				
			getState().assign(PullRequested);			
		}
		else if (ActionType.sync.name().equals(action))
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
		if (ActionType.pull.name().equals(action))
		{
			HGHandle ontologyHandle = graph.getHandleFactory().makeHandle(doc.getOntologyID());
			vo = versionManager().versioned(ontologyHandle);
			ActivityUtils.updateVersionedOntology(manager, vo, doc);
		}
		else
			vo = ActivityUtils.storeClonedOntology(manager, doc);

		RemoteOntology remoteOnto = remoteOnto();	
		if (delta != null)
			remoteOnto.updateRevisionHeads(graph, delta.heads);
		else
			remoteOnto.setRevisionHeads(vo.heads());
		if (metaChanges != null && !metaChanges.isEmpty())
			remoteOnto.setLastMetaChange(vo.metadata().applyChanges(metaChanges));
		else if (doc.getMetadata() != null && doc.getMetadata().lastChange() != null)
			remoteOnto.setLastMetaChange(doc.getMetadata().lastChange());
		//System.out.println("New revision heads: " + delta.heads);
		getThisPeer().getGraph().update(remoteOnto);
		if (msg.has(Messages.REPLY_WITH))
			reply(msg, Performative.Confirm, Json.nil());
		completedMessage = "ok";
		return WorkflowState.Completed;
	}
	
	private String changeData(Json msg)
	{
//		System.out.println("asked for revisions: " + msg.at(REVISIONS));
		HGHandle ontologyHandle = Messages.fromJson(msg.at(Messages.CONTENT).at(ONTOLOGY_HANDLE));
		Set<HGHandle> revisions = Messages.fromJson(msg.at(Messages.CONTENT).at(REVISIONS));
		VersionManager versionManager = new VersionManager(getThisPeer().getGraph(),
														   getThisPeer().getPeerInterface().principal().getName());	
		if (!versionManager.isVersioned(ontologyHandle))
		{
			reply(msg, Performative.Failure, 
					Json.object("error", "The ontology does not exist or is not versioned."));
			return null;
		}
		try
		{
			VersionedOntology versionedOntology = versionManager.versioned(ontologyHandle);
//			System.out.println("At source parents " + versionedOntology.revision().parents());
			String serializedOntology =
				// are we cloning?
				revisions == null || revisions.contains(versionedOntology.getRootRevision()) ?
					ActivityUtils.renderVersionedOntology(versionedOntology) :
					ActivityUtils.renderVersionedOntologyDelta(versionedOntology, revisions);
			// if we are cloning, we send all branches along, otherwise, the GetNewRevisions 
			// task would have been performed and the delta calculated already.
			return serializedOntology;
		}
		catch (Exception ex)
		{
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex);
		}		
	}
	
	/**
	 * Answer a pull or a clone request.
	 *  
	 * @param msg
	 * @return
	 */
	@FromState({"Started"})
	@OnMessage(performative="QueryRef")
	public WorkflowStateConstant pullChanges(Json msg)
	{
		String data = changeData(msg);
		if (data == null)
			return WorkflowState.Failed;
		reply(msg, Performative.InformRef, data);
		completedMessage = "ok";
		return WorkflowState.Completed;
	}
	
	/**
	 * Answer a pull request during push - we don't want to complete immediately, we
	 * wait for confirmation from peer before Activity can be marked as completed!
	 *  
	 * @param msg
	 * @return
	 */
	@FromState({"PushAccepted", "PublishAccepted"})
	@OnMessage(performative="QueryRef")
	public WorkflowStateConstant pushChanges(Json msg)
	{
		String data = changeData(msg);
		if (data == null)
			return WorkflowState.Failed;
		Json reply = getReply(msg, Performative.InformRef, data)
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
		completedMessage = "ok";
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
	public WorkflowStateConstant transferRequested(Json msg)
	{
		if (!msg.at(CONTENT).has("action"))
		{
			reply(msg, Performative.Failure, "Missing action operand of request performative.");
			return WorkflowStateConstant.Failed;
		}
		action = msg.at(CONTENT).at("action").asString();
		if (ActionType.pull.name().equals(action))
		{
			HGHandle ontologyHandle = fromJson(msg.at(CONTENT).at(ONTOLOGY_HANDLE));
			if (getThisPeer().getGraph().get(ontologyHandle) == null)
			{
				reply(msg, Performative.Refuse, "Unknown ontology.");
				return WorkflowStateConstant.Failed;
			}						
			//Set<HGHandle> heads = fromJson(msg.at(CONTENT).at("heads"));			
			HGPeerIdentity otherPeer = getThisPeer().getIdentity(getSender(msg));			
			OntologyDatabasePeer repo = new OntologyDatabasePeer(getThisPeer());
			RemoteOntology remote = repo.remoteOnto(ontologyHandle, repo.remoteRepo(otherPeer));
			//remote.setRevisionHeads(heads);
			//getThisPeer().getGraph().update(remote);
			remoteOntologyHandle = getThisPeer().getGraph().getHandle(remote);
			reply(msg, Performative.Agree, Json.object());
			return startPulling();
		}
		else if (ActionType.clone.name().equals(action))
		{
			HGHandle ontologyHandle = fromJson(msg.at(CONTENT).at(ONTOLOGY_HANDLE));
			if (getThisPeer().getGraph().get(ontologyHandle) != null)
			{
				reply(msg, Performative.Refuse, "Ontology already known.");
				return WorkflowStateConstant.Failed;
			}
			reply(msg, Performative.Agree, Json.object());			
			HGPeerIdentity otherPeer = getThisPeer().getIdentity(getSender(msg));			
			OntologyDatabasePeer repo = new OntologyDatabasePeer(getThisPeer());
			RemoteOntology remote = repo.remoteOnto(ontologyHandle, repo.remoteRepo(otherPeer));
			remoteOntologyHandle = getThisPeer().getGraph().getHandle(remote);
			msg = createMessage(Performative.QueryRef, 
							   Json.object(ONTOLOGY_HANDLE, remote.getOntologyHandle()));
			send(otherPeer, msg);							 										  
			return WaitForRevisionObjects;
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
	
	@FromState("CloneRequested")
	@OnMessage(performative="Refuse")
	public WorkflowStateConstant cloneRefused(Json msg)
	{
		this.completedMessage = msg.at(CONTENT).asString();
		return WorkflowStateConstant.Failed;
	}

	@FromState("CloneRequested")
	@OnMessage(performative="Agree")
	public WorkflowStateConstant cloneAccepted(Json msg)
	{
		return PublishAccepted;
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

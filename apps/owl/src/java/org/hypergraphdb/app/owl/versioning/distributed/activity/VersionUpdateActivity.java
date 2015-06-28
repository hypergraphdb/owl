package org.hypergraphdb.app.owl.versioning.distributed.activity;

import java.util.Set;
import java.util.UUID;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteRepository;
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
import org.semanticweb.owlapi.io.StringDocumentSource;

/**
 * <p>
 * Used to perform an update, a partial or a complete synchronization of
 * the revision graph between two peers. This can be used to push changes
 * to a peer, or pull from a peer, or synchronize two peers (which might
 * entail both a pull and a push).
 * </p>
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
	
	public static final WorkflowStateConstant WaitForRevisionChangeSet = WorkflowStateConstant.makeStateConstant("WaitForRevisionChangeSet");
	public static final WorkflowStateConstant WaitForRevisionObjects = WorkflowStateConstant.makeStateConstant("WaitForRevisionObjects");
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
		
	private RemoteOntology remoteOntology;	
	private String action;
	private String completedMessage;

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
	
	public VersionUpdateActivity remoteOntology(RemoteOntology remoteOntology)
	{
		this.remoteOntology = remoteOntology;
		return this;
	}
	
	public VersionUpdateActivity action(String action)
	{
		this.action = action;
		return this;
	}
	
	@Override
	public void initiate()
	{
		Json msg;
		if ("pull".equals(action))
		{
			getThisPeer().getActivityManager().initiateActivity(
				new GetNewRevisionsActivity(getThisPeer(), remoteOntology), this, null);
			getState().assign(WaitForRevisionChangeSet);
		}
		else if ("push".equals(action))
		{
			msg = createMessage(Performative.InformRef, Json.object());
			send(remoteOntology.getRepository().getPeer(), msg);				
		}
		else if ("synch".equals(action))
		{
			msg = createMessage(Performative.QueryRef, Json.object());			
			send(remoteOntology.getRepository().getPeer(), msg);				
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
	public WorkflowStateConstant askForChanges(GetNewRevisionsActivity getRevisionSetActivity)
	{
		System.out.println("got revision set " + getRevisionSetActivity.newRevisions());
		send(remoteOntology.getRepository().getPeer(), 
			 createMessage(Performative.QueryRef, Json.object(REVISIONS, getRevisionSetActivity.newRevisions(),
					 										  ONTOLOGY_HANDLE, remoteOntology.getOntologyHandle())));	
		return WaitForRevisionObjects;
	}
	
	@FromState("WaitForRevisionObjects")
	@OnMessage(performative="InformRef")
	public WorkflowStateConstant receiveChanges(Json msg)
	{
		System.out.println("Got changes " + msg.at(Messages.CONTENT).asString());
		VersionedOntology vo = ActivityUtils.storeVersionedOntology(
				new StringDocumentSource(msg.at(Messages.CONTENT).asString()), 
				HGOntologyManagerFactory.getOntologyManager(getThisPeer().getGraph().getLocation()));
		System.out.println(vo.revision().parents());
		return WorkflowState.Completed;
	}
	
	/**
	 * Answer a pull request.
	 *  
	 * @param msg
	 * @return
	 */
	@FromState("Started")
	@OnMessage(performative="QueryRef")
	public WorkflowStateConstant pullChanges(Json msg)
	{
//		System.out.println("asked for revisions: " + msg.at(REVISIONS));
		HGHandle ontologyHandle = Messages.fromJson(msg.at(Messages.CONTENT).at(ONTOLOGY_HANDLE));
		Set<HGHandle> revisions = Messages.fromJson(msg.at(Messages.CONTENT).at(REVISIONS));
		VersionManager versionManager = new VersionManager(getThisPeer().getGraph(), 
														   "fixme-VHDBOntologyRepository");
		if (!versionManager.isVersioned(ontologyHandle))
		{
			reply(msg, Performative.Failure, Json.object("error", "The ontology does not exist or is not versioned."));
			return WorkflowState.Failed;
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
			reply(msg, Performative.InformRef, serializedOntology);
		}
		catch (Exception ex)
		{
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex);
		}
		return WorkflowState.Completed;
	}
	
	/**
	 * Execute a push request
	 * @param msg
	 * @return
	 */
	@FromState("PushAccepted")
	@OnMessage(performative="InformRef")
	public WorkflowStateConstant pushChanges(Json msg)
	{
		return WorkflowStateConstant.Completed;
	}
	
	@Override
	public String getType()
	{
		return TYPENAME;
	}

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

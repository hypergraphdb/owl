package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.getPart;
import static org.hypergraphdb.peer.Structs.struct;
import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.RevisionID;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * GetRemoteOntologyRevisionsActivity gets a list of revision objects from a remote repository by UUID
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 06, 2012
 */
public class GetRemoteOntologyChangesetActivity extends FSMActivity {

    public static boolean DBG = true;   
    public static final String TYPENAME = "get-remote-ontology-changeset";

    private HGPeerIdentity targetPeerID;
    private VDHGDBOntologyRepository repository;
    private HyperGraph graph;
    private HGPersistentHandle sourceDistributedOntologyUUID;
    private RevisionID sourceDistributedOntologyRevision;
    private List<String> renderedChangesFromTarget;


	public GetRemoteOntologyChangesetActivity(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
        if(!thisPeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) thisPeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
        graph = repository.getHyperGraph();
    }

	/**
	 * @param thisPeer
	 */
	public GetRemoteOntologyChangesetActivity(HyperGraphPeer sourcePeer, HGPeerIdentity targetPeerID, HGPersistentHandle distributedOntologyUUID, RevisionID distributedOntologyRevision) {
		super(sourcePeer);
		this.targetPeerID = targetPeerID;
        if(!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
        graph = repository.getHyperGraph();
        sourceDistributedOntologyUUID = distributedOntologyUUID; 
        sourceDistributedOntologyRevision = distributedOntologyRevision; 
	}	

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.Activity#getType()
	 */
	@Override
	public String getType() {
		return TYPENAME;
	}	
	
    /* (non-Javadoc) // SOURCE
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate() {
        Message msg = createMessage(Performative.QueryIf, this);
        if (sourceDistributedOntologyUUID == null) throw new NullPointerException("sourceDistributedOntologyUUID must not be null");
        Object[] queryParams = new Object[] {sourceDistributedOntologyUUID, sourceDistributedOntologyRevision };
    	combine(msg, struct(CONTENT, queryParams));
        send(targetPeerID, msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //TARGET
    @OnMessage(performative="QueryIf")
    public WorkflowStateConstant targetQueryOntologyRevisions(final Message msg) throws Throwable {
		Object[] queryParams = getPart(msg, CONTENT);
		final HGPersistentHandle sourceUUID = (HGPersistentHandle)queryParams[0];
		final RevisionID revisionID = (RevisionID)queryParams[1];
		Message reply = graph.getTransactionManager().ensureTransaction(new Callable<Message>() {
			public Message call() {
				Message reply;
				HGDBOntology o = graph.get(sourceUUID);
				if (o != null) {
					DistributedOntology targetDistributedOnto = repository.getDistributedOntology(o);
					if (targetDistributedOnto != null) {
						VersionedOntology vo = targetDistributedOnto.getVersionedOntology();
						OWLOntology onto = vo.getWorkingSetData();
						ChangeSet cs = vo.getChangeSet(revisionID);
						//Render Changes and send 
						List<String> renderedChanges = new LinkedList<String>();
						if (cs != null) {
							for (VOWLChange voc : cs.getChanges()) {
								OWLOntologyChange change = VOWLChangeFactory.create(voc, onto, graph);
								renderedChanges.add(change.toString());
							}
							reply = getReply(msg, Performative.Inform);			        	
					        combine(reply, struct(CONTENT, renderedChanges));
					        return reply;
						} else {
							// Ontology but not changeset
				        	throw new RuntimeException(new VOWLException("Source ontology exists but changeset not for revision " + revisionID + " not found"));
						}
					} else {
							// Ontology but not shared 
				        	throw new RuntimeException(new VOWLException("Source ontology exists but is not shared."));
					}
				} else {
					// ontology unknown at source. Pull full.
		        	throw new RuntimeException(new VOWLException("Source ontology does not exist on server."));
		        	//TRANSACTION END
				}
			}});
		send(getSender(msg), reply);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //TARGET
    @OnMessage(performative="Inform")
    public WorkflowStateConstant sourceReceiveOntologyIds(final Message msg) throws Throwable {
		renderedChangesFromTarget = getPart(msg, CONTENT);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * To be called after action has completed.
	 * @return a list of revisions, null if failure
	 */
	public List<String> getRemoteChangeSetChangesRendered() {
		return renderedChangesFromTarget;
	}	
}
package org.hypergraphdb.app.owl.versioning.distributed.activity;


import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.getPart;
import static org.hypergraphdb.peer.Structs.struct;
import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.PossibleOutcome;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.StringDocumentSource;

/**
 * PullActivity. Pulls all changes from a target repository, which has the same VersionedOntology and 
 * a change history that is longer (newer) than the source (initiator).
 *  
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 19, 2012
 */
public class PullActivity extends FSMActivity {

    public static final String TYPENAME = "pull-VersionedOntology";

    public static final String KEY_LAST_MATCHING_REVISION = "LastMatchingRevision";

    public static boolean DBG = true;

    public static boolean DBG_RENDER_ONTOLOGIES_TO_FILE = true;
    
    public static final WorkflowStateConstant SendingInitial = WorkflowStateConstant.makeStateConstant("SendingInitial");
    public static final WorkflowStateConstant ReceivingInitial = WorkflowStateConstant.makeStateConstant("ReceivingInitial");
    public static final WorkflowStateConstant SendingDelta = WorkflowStateConstant.makeStateConstant("SendingDelta");
    public static final WorkflowStateConstant ReceivingDelta= WorkflowStateConstant.makeStateConstant("ReceivingDelta");
    
    private VDHGDBOntologyRepository repository;

    /**
     * The versioned ontology at the initiator that shall be modified by the pull.
     */
    private HGPersistentHandle ontologyUUID; //on source and target
    private HGPeerIdentity targetPeerID;
    private String completedMessage;
    private HyperGraph graph;
    
    private ActivityUtils activityUtils =  new ActivityUtils();
    
	public PullActivity(HyperGraphPeer thisPeer, UUID id)
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
	public PullActivity(HyperGraphPeer sourcePeer, HGPersistentHandle ontologyUUID, HGPeerIdentity targetPeerID) {
		super(sourcePeer);
		//this.sourceVersionedOnto = sourceVersionedOnto;
		this.ontologyUUID = ontologyUUID;
		this.targetPeerID = targetPeerID;
        if(!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
        graph = repository.getHyperGraph();
	}	
	
	/**
	 * @param completedMessage the completedMessage to set
	 */
	protected void setCompletedMessage(String completedMessage) {
		this.completedMessage = completedMessage;
		if (DBG) System.out.println("Pull Completed: " + completedMessage);
	}

	/**
	 * @return the completedMessage
	 */
	public String getCompletedMessage() {
		return completedMessage;
	}
	
    /* (non-Javadoc) // SOURCE
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate() {
		// Look up in repository
		//TRANSACTION START
		Message message;
		message = graph.getTransactionManager().transact(new Callable<Message>() {
			public Message call() {
				HGDBOntology o = graph.get(ontologyUUID);
				Message msg;
				if (o != null) {
					VersionedOntology sourceVersionedOnto = repository.getVersionControlledOntology(o); 
					if (sourceVersionedOnto != null) {
						// send Confirm with existing revisions objects
						// and tell if we have uncommitted changes.
						// TODO send content hash
				        if (sourceVersionedOnto.getWorkingSetChanges().isEmpty()) {
				        	msg = createMessage(Performative.Confirm, null);
				        	List<Revision> revList = sourceVersionedOnto.getRevisions();
					        combine(msg, struct(CONTENT, revList));
					        return msg;
					        // Started return ReceivingDelta;
				        } else {
				        	// Source has uncommitted - cannot pull
				        	throw new RuntimeException(new VOWLSourceTargetConflictException("Source has uncommitted changes. Cannot pull."));
						}
					} else {
							// Ontology but no versioning information, 
							// cannot determine
				        	// Source has uncommitted - cannot pull
				        	throw new RuntimeException(new VOWLSourceTargetConflictException("Source ontology exists without versioning. Cannot match revisions. Cannot pull."));
					}
				} else {
					// ontology unknown here.
					msg = createMessage(Performative.Disconfirm, ontologyUUID);
					return msg;
					//TRANSACTION END
				}
			}});
		send(targetPeerID, message);
	}

//	/**
//	 * 
//	 * @param msg
//	 * @return
//	 * @throws Throwable
//	 */
//	@FromState("Started") //TARGET
//    @OnMessage(performative="QueryIf")
//    @PossibleOutcome({"ReceivingDelta", "ReceivingInitial"})
//    //@AtActivity(CONTENT);
//    public WorkflowStateConstant targetExistsVersionedOntology(final Message msg) throws Throwable {
//    }
	
	//------------------------------------------------------------------------------------
	// SENDING / RECEIVING FULL VERSIONED ONTOLOGY
	//

	@FromState("Started") //TARGET
    @OnMessage(performative="Disconfirm")
    @PossibleOutcome({"SendingInitial"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant targetSendFullVersionedOntology(final Message msg) throws Throwable {
		// PROPOSE
		Message reply;
		String vowlxmlStringOntology  = graph.getTransactionManager().transact(new Callable<String>() {
			public String call() {
				VersionedOntology targetVersionedOnto;
				//TRANSACTION START	
				ontologyUUID = getPart(msg, CONTENT);
				HGDBOntology onto = graph.get(ontologyUUID);
				if (onto != null) {
					targetVersionedOnto = repository.getVersionControlledOntology(onto);
					if (targetVersionedOnto != null) {
						try {
							return activityUtils.renderVersionedOntology(targetVersionedOnto);
						} catch (OWLRendererException e) {
							throw new RuntimeException(e);
						}
					} else {
						throw new IllegalStateException("Ontology found at target, but not version controlled. Cannot Send.");
					}
				} else {
					throw new IllegalStateException("No Ontology found for : " + ontologyUUID + " Cannot Send.");
				}
				//TRANSACTION END
			}}, HGTransactionConfig.READONLY);
		reply = getReply(msg, Performative.Propose);
		// send full head revision data, not versioned yet.
        combine(reply, struct(CONTENT, vowlxmlStringOntology)); 
        send(getSender(msg), reply);
		return SendingInitial;
	}
	
	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //SOURCE
    @OnMessage(performative="Propose")
    //@PossibleOutcome({"Completed", "Failed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant sourceReceiveFullVersionedOntologyAsNew(Message msg) throws Throwable {
		final String vowlxmlStringOntology = getPart(msg, CONTENT);		
		graph.getTransactionManager().transact(new Callable<Object>() {
			public Object call() {
				//TRANSACTION START
				VersionedOntology voParsed;
				try {
					voParsed = activityUtils.storeVersionedOntology(new StringDocumentSource(vowlxmlStringOntology), repository.getOntologyManager());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				//TODO HANDLE EXCEPTION, GC created objects			
				// Neither Ontology, nor VersionedOntology was stored, 
				// but ontology axioms & entities, revisions and changeset, changes, axioms were.
				// Run a GC, which also collects dangling changesets, changes and revisions?
				if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
					repository.printAllOntologies();
					try {
						activityUtils.saveVersionedOntologyXML(voParsed, "FULL-RECEIVED-SOURCE" + getThisPeer().getIdentity().getId());
					} catch (OWLRendererException e) {
						//Ignore DBG Exceptions
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return null;
				//TRANSACTION END
		}}, HGTransactionConfig.DEFAULT);
		//RESPOND
        Message reply = getReply(msg, Performative.AcceptProposal);
        send(getSender(msg), reply);
        setCompletedMessage("Full versioned ontology received. Size: " + (vowlxmlStringOntology.length()/1024) + " kilo characters");
		return WorkflowStateConstant.Completed;
	}
	
	/**
	 * Exits activity.
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("SendingInitial") //TARGET
    @OnMessage(performative="AcceptProposal")
    //@PossibleOutcome({"Completed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant targetReceiveConfirmationForFullVersionedOntology(Message msg) throws Throwable {
        setCompletedMessage("Source reported: accepted full versioned ontology. All changes were applied.");
		return WorkflowStateConstant.Completed;
	}
	
	//------------------------------------------------------------------------------------
	// TARGET HAS ONTOLOGY -> VALIDATING AND PUSHING MISSING CHANGES
	// OR CANCEL -> has 
	//
	
	/**
	 * Receive a list of revisions from target and determine,
	 * A) if push is possible -> send Cancel
	 * B) which changesets to send -> send Rendered Missing Changesets
	 */
	@FromState("Started") //TARGET
    @OnMessage(performative="Confirm")
    @PossibleOutcome({"SendingDelta"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant targetSendVersionedOntologyDelta(final Message msg) throws Throwable {
		final List<Revision> sourceRevisions = getPart(msg, CONTENT);
		return graph.getTransactionManager().transact(new Callable<WorkflowStateConstant>() {
			public WorkflowStateConstant call() {
				Message reply; 
				WorkflowStateConstant nextState;
				int lastCommonRevisionIndex; 
				boolean allSourceRevisionsAreInTarget;
				boolean allTargetRevisionsAreInSource;
				String owlxmlStringOntology;
				//TRANSACTION START
				//VersionedOntology targetVersionedOnto = repository.getVersionControlledOntology(ontologyUUID);
				VersionedOntology targetVersionedOnto = repository.getVersionControlledOntology(sourceRevisions.get(0).getOntologyUUID());
				List<Revision> targetRevisions = targetVersionedOnto.getRevisions();
				lastCommonRevisionIndex = activityUtils.findLastCommonRevisionIndex(sourceRevisions, targetRevisions);
				allSourceRevisionsAreInTarget = lastCommonRevisionIndex + 1 == sourceRevisions.size();
				allTargetRevisionsAreInSource = lastCommonRevisionIndex + 1 == targetRevisions.size();
				if (lastCommonRevisionIndex >= 0) { 
					if (allSourceRevisionsAreInTarget) {
						if (!allTargetRevisionsAreInSource) {
							//S C0C1C2
							//T C0C1C2T3
							// send target's Revisions and changesets starting at sourceIndex, no data, no uncommitted
							// Send, including the LAST MATCHING REVISION at which index the first necessary 
							// delta changeset will be.
							reply = getReply(msg, Performative.Inform);
							try {
								owlxmlStringOntology = activityUtils.renderVersionedOntologyDelta(targetVersionedOnto, lastCommonRevisionIndex);
							} catch(Exception e) {
								throw new RuntimeException(e);
							}
					        combine(reply, struct(CONTENT, owlxmlStringOntology));
					        combine(reply, struct(KEY_LAST_MATCHING_REVISION, targetRevisions.get(lastCommonRevisionIndex)));
					        setCompletedMessage("Target sent " + (targetRevisions.size() - lastCommonRevisionIndex + 1) + " changesets to source." 
					        		+ " size was : " + (owlxmlStringOntology.length()/1024) + " kilo characters ");
					        nextState = SendingDelta;
					        if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
					        	try {
					        		activityUtils.saveStringXML(owlxmlStringOntology, "DELTA-SENT-BY-PULL-TARGET");
					        	} catch (Exception e) {
					        		System.err.println("Push: Exception during debug output ignored:");
					        		e.printStackTrace();
								}
					        }
						} else {
							//S C0C1C2
							//T C0C1C2
							// target equals source
							reply = getReply(msg, Performative.InformIf);
							combine(reply, struct(CONTENT, "Target and Source are equal."));
							setCompletedMessage("Target and Source are equal. Nothing to transmit.");
							nextState = WorkflowStateConstant.Completed;
						}
					} else {
						if (allTargetRevisionsAreInSource) {
							//S C0C1C2S4
							//T C0C1C2
							//Suggest Push
							//target has more than source, but some inital match
							reply = getReply(msg, Performative.InformIf);
							combine(reply, struct(CONTENT, "Source is newer than target."));
							setCompletedMessage("Source is newer than target. A push is suggested and possible.");
							nextState = WorkflowStateConstant.Completed;
						} else {
							//S C0C1C2S3
							//T C0C1C2T3 S3 <> T3  
							// Both have exclusive revisions after a common history,
							// push or pull only possible with branching, which is not available.
							// Here in the linear model it is a conflict.
							// Revert source or target and pull or push.
							throw new RuntimeException(new VOWLSourceTargetConflictException("Both have excusive revisions after a common history"));
						}
					}
				} else {
					// no shared history
					throw new RuntimeException(new VOWLSourceTargetConflictException("No common revision at beginning of source and target histories."));
				}
				//TRANSACTION END
		        send(getSender(msg), reply);
				return nextState;
			}}, HGTransactionConfig.READONLY);
	}
		
	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //SOURCE 
    @OnMessage(performative="Inform")
    //@PossibleOutcome({"Completed", "Failed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant sourceReceiveVersionedOntologyDelta(final Message msg) throws Throwable {
		//
		// Test if received last revision matches target head and all other prerequisites are still met.
		//
		final Revision lastMatchingRevision = getPart(msg, KEY_LAST_MATCHING_REVISION);
		// Validate if lastMatchingRevision still is target HEAD, keep UUID
		//Throws exceptions if not.
		String vowlxmlStringDelta = graph.getTransactionManager().transact(new Callable<String>() {
			public String call() {
				//TRANSACTION START
				VersionedOntology sourceVersionedOnto = activityUtils.getVersionedOntologyForDeltaFrom(lastMatchingRevision, repository);
				System.out.println("RECEIVING PULLED delta");
				String vowlxmlStringDelta = getPart(msg, CONTENT);
				OWLOntologyDocumentSource ds = new StringDocumentSource(vowlxmlStringDelta);
				// Parse, apply and append the delta
				try {
					activityUtils.appendDeltaTo(ds, sourceVersionedOnto);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				//assert targetVersionedOntology contains delta
				if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
					repository.printAllOntologies();
					try {
						activityUtils.saveVersionedOntologyXML(sourceVersionedOnto, "FULL-AFTER-DELTA-APPLIED-PULL-SOURCE");
					} catch (Exception e) {
						//DBG exception ignored.
						e.printStackTrace();
					}
				}
				return vowlxmlStringDelta;
				//TRANSACTION END
			}}, HGTransactionConfig.DEFAULT);
		Message reply = getReply(msg, Performative.AcceptProposal);
		send(getSender(msg), reply);
		setCompletedMessage("Delta received and applied. Size: " + (vowlxmlStringDelta.length()/1024) + " kilo characters");
		return WorkflowStateConstant.Completed;
	}

	@FromState("Started") //SOURCE 
    @OnMessage(performative="InformIf")
    public WorkflowStateConstant sourceReceiveVersionedOntologyDeltaCancelled(final Message msg) throws Throwable {
		String message = getPart(msg, CONTENT);
		setCompletedMessage(message);
		return WorkflowStateConstant.Completed;
	}
	
		@FromState("SendingDelta") //Source
    @OnMessage(performative="AcceptProposal")
    //@PossibleOutcome({"Completed"}) 
    public WorkflowStateConstant targetReceiveConfirmationForDelta(Message msg) throws Throwable {
		setCompletedMessage("All changes were applied to source.");
		return WorkflowStateConstant.Completed;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.Activity#getType()
	 */
	@Override
	public String getType() {
		return TYPENAME;
	}	
}
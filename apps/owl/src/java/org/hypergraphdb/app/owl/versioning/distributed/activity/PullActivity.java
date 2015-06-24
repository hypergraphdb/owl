package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;
import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.OntologyVersionState;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.PossibleOutcome;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;

/**
 * PullActivity. Pulls all changes from a target repository, which has the same
 * VersionedOntology and a change history that is longer (newer) than the source
 * (initiator).
 * 
 * target ->changes-> source
 * 
 * If the source has pending uncommitted workingset changes, they are merged
 * with the incoming delta in the following way: 1. pending are rolled back 2.
 * delta is applied 3. rolled back pending are reapplied (and can be committed
 * afterwards).
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 19, 2012
 */
public class PullActivity extends OntologyTransmitActivity
{

	public static final String TYPENAME = "pull-VersionedOntology";

	public static boolean DBG = true;

	public static boolean DBG_RENDER_ONTOLOGIES_TO_FILE = false;

	public static final WorkflowStateConstant SendingInitial = WorkflowStateConstant.makeStateConstant("SendingInitial");
	public static final WorkflowStateConstant ReceivingInitial = WorkflowStateConstant.makeStateConstant("ReceivingInitial");
	public static final WorkflowStateConstant SendingDelta = WorkflowStateConstant.makeStateConstant("SendingDelta");
	public static final WorkflowStateConstant ReceivingDelta = WorkflowStateConstant.makeStateConstant("ReceivingDelta");

	private VDHGDBOntologyRepository repository;

	/**
	 * The versioned ontology at the initiator that shall be modified by the
	 * pull.
	 */
	private HGPersistentHandle ontologyUUID; // on source and target
	private Set<HGHandle> referenceHeads = null;
	private HGPeerIdentity targetPeerID;
	private String completedMessage;
	private HyperGraph graph;

	private ActivityUtils activityUtils = new ActivityUtils();

	private boolean mergeWithUncommited;

	public PullActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
		if (!thisPeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (VDHGDBOntologyRepository) thisPeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
	}

	public PullActivity(HyperGraphPeer sourcePeer, 
						VersionedOntology localVersionedOntology, 
						HGPeerIdentity targetPeerID)
	{
		this(sourcePeer, localVersionedOntology, targetPeerID, null);
	}

	/**
	 * Use this constructor, if you have a localDistributedOntology. Early
	 * checks will be performed.
	 * 
	 * @param sourcePeer
	 * @param localDistributedOntology
	 * @param targetPeerID
	 */
	public PullActivity(HyperGraphPeer sourcePeer, 
						VersionedOntology localVersionedOntology, 
						HGPeerIdentity targetPeerID,
						Set<HGHandle> referenceHeads)
	{
		super(sourcePeer);
		if (!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		HGPersistentHandle ontologyUUID = localVersionedOntology.ontology().getAtomHandle().getPersistent();
		initialize(sourcePeer, ontologyUUID, targetPeerID, referenceHeads);
	}

	/**
	 * Use this constructor only, if you are pulling a new ontology and you do
	 * NOT yet have a shared localOntology to apply incoming delta to.
	 * 
	 * @param thisPeer
	 */
	public PullActivity(HyperGraphPeer sourcePeer, HGPersistentHandle ontologyUUID, HGPeerIdentity targetPeerID)
	{
		super(sourcePeer);
		initialize(sourcePeer, ontologyUUID, targetPeerID, null);
	}

	/**
	 * Use this constructor only, if you are pulling a new ontology and you do
	 * NOT yet have a shared localOntology to apply incoming delta to.
	 * 
	 * @param thisPeer
	 */
	public PullActivity(HyperGraphPeer sourcePeer, HGPersistentHandle ontologyUUID, Set<HGHandle> referenceHeads, HGPeerIdentity targetPeerID)
	{
		super(sourcePeer);
		initialize(sourcePeer, ontologyUUID, targetPeerID, referenceHeads);
	}

	protected void initialize(HyperGraphPeer sourcePeer, 
							  HGPersistentHandle ontologyUUID, 
							  HGPeerIdentity targetPeerID,
							  Set<HGHandle> referenceHeads)
	{
		this.ontologyUUID = ontologyUUID;
		this.targetPeerID = targetPeerID;
		this.referenceHeads = referenceHeads;
		if (!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
	}

	/**
	 * @param completedMessage
	 *            the completedMessage to set
	 */
	protected void setCompletedMessage(String completedMessage)
	{
		this.completedMessage = completedMessage;
		if (DBG)
			System.out.println("Pull Completed: " + completedMessage);
	}

	/**
	 * @return the completedMessage
	 */
	public String getCompletedMessage()
	{
		return completedMessage;
	}

	
	/**
	 * Initiation happens on the "client" side, pulling data from the peer that has it. The
	 * data is either a complete ontology or a revisions subgraph (a "delta").
	 */
	@Override
	public void initiate()
	{
		// Look up in repository
		// TRANSACTION START
		Json message = graph.getTransactionManager().ensureTransaction(new Callable<Json>()
		{
			public Json call()
			{
				HGDBOntology o = graph.get(ontologyUUID);
				Json msg;
				if (o != null)
				{
					if (repository.getOntologyManager().getVersionManager().isVersioned(ontologyUUID))
					{
						// Get Delta
						msg = createMessage(Performative.Confirm, null);
						List<Revision> revList = null;
						msg.set(CONTENT, revList);
						if (referenceHeads == null)
						{
							referenceHeads = new HashSet<HGHandle>();
							for (Revision rev : repository.getOntologyManager().getVersionManager().versioned(o.getAtomHandle()).heads())
								referenceHeads.add(graph.getHandle(rev));
						}
						msg.set(KEY_REFERENCE_HEADS, referenceHeads);
						return msg;
					}
					else
					{
						// Ontology but no versioning information - cannot pull
						// How does this case arise? Through an import of some sorts?
						throw new RuntimeException(new VOWLSourceTargetConflictException(
								"Source ontology exists but is not shared. Cannot pull."));
					}
				}
				else
				{
					// ontology unknown at source. Pull full or until
					msg = createMessage(Performative.Disconfirm, null);
					return msg;
				}
			}
		});
		message.set(KEY_ONTOLOGY_UUID, ontologyUUID);
		send(targetPeerID, message);
	}

	// ------------------------------------------------------------------------------------
	// SENDING / RECEIVING FULL VERSIONED ONTOLOGY
	//

	@FromState("Started")
	// TARGET
	@OnMessage(performative = "Disconfirm")
	@PossibleOutcome({ "SendingInitial" })
	// @AtActivity(CONTENT);
	public WorkflowStateConstant targetSendFullVersionedOntology(final Json msg) throws Throwable
	{
		// PROPOSE
		Json reply;
		String vowlxmlRendered = graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				// TRANSACTION START
				ontologyUUID = Messages.fromJson(msg.at(CONTENT));
				referenceHeads = Messages.fromJson(msg.at(KEY_REFERENCE_HEADS));
				HGDBOntology onto = graph.get(ontologyUUID);
				if (onto != null)
				{
					if (repository.getOntologyManager().getVersionManager().isVersioned(onto.getAtomHandle()))
					{
						try
						{
							// render FULL or render until last requested.
							VersionedOntology versionedOntology = repository.getOntologyManager().getVersionManager().versioned(onto.getAtomHandle());							
							Set<Revision> delta = new OntologyVersionState(referenceHeads).delta(versionedOntology);
							return activityUtils.renderVersionedOntologyDelta(versionedOntology, /*delta*/ null);
						}
						catch (Exception e)
						{
							throw new RuntimeException(e);
						}
					}
					else
					{
						throw new RuntimeException(new VOWLException("Ontology found at target, but not shared. Cannot Send."));
					}
				}
				else
				{
					throw new RuntimeException(new VOWLException("Ontology does not exist at target. Cannot Send. Query UUID was: "
							+ ontologyUUID));
				}
				// TRANSACTION END
			}
		}, HGTransactionConfig.READONLY);
		reply = getReply(msg, Performative.Propose);
		// send full head revision data, not versioned yet.
		reply.set(CONTENT, vowlxmlRendered);
		send(getSender(msg), reply);
		return SendingInitial;
	}

	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started")
	// SOURCE
	@OnMessage(performative = "Propose")
	// @PossibleOutcome({"Completed", "Failed"})
	// @AtActivity(CONTENT);
	public WorkflowStateConstant sourceReceiveFullVersionedOntologyAsNew(Json msg) throws Throwable
	{
		final String vowlxmlStringOntology = msg.at(CONTENT).asString();
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				// TRANSACTION START
				try
				{
					VersionedOntology vo = activityUtils.storeVersionedOntology(
							new StringDocumentSource(vowlxmlStringOntology), repository.getOntologyManager());
					graph.add(vo);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				// TODO HANDLE EXCEPTION, GC created objects
				// Neither Ontology, nor VersionedOntology was stored,
				// but ontology axioms & entities, revisions and changeset,
				// changes, axioms were.
				// Run a GC, which also collects dangling changesets, changes
				// and revisions?
				if (DBG_RENDER_ONTOLOGIES_TO_FILE)
				{
					repository.printAllOntologies();
					try
					{
//						activityUtils.saveVersionedOntologyXML(voParsed, 
//															   "FULL-RECEIVED-SOURCE" + getThisPeer().getIdentity().getId());
					}
					catch (Exception e)
					{
						// Ignore DBG Exceptions
						e.printStackTrace();
					}
				}
				return null;
				// TRANSACTION END
			}
		}, HGTransactionConfig.DEFAULT);
		// RESPOND
		Json reply = getReply(msg, Performative.AcceptProposal);
		send(getSender(msg), reply);
		setCompletedMessage("Full versioned ontology received. Size: " + 
							(vowlxmlStringOntology.length() / 1024)	+ " kilo characters");
		return WorkflowStateConstant.Completed;
	}

	/**
	 * Exits activity.
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("SendingInitial")
	// TARGET
	@OnMessage(performative = "AcceptProposal")
	// @PossibleOutcome({"Completed"})
	// @AtActivity(CONTENT);
	public WorkflowStateConstant targetReceiveConfirmationForFullVersionedOntology(Json msg) throws Throwable
	{
		setCompletedMessage("Source reported: accepted full distibuted ontology. All changes were applied.");
		return WorkflowStateConstant.Completed;
	}

	// ------------------------------------------------------------------------------------
	// TARGET HAS ONTOLOGY -> VALIDATING AND PUSHING MISSING CHANGES
	// OR CANCEL -> has
	//

	/**
	 * Receive a list of revisions from target and determine, A) if push is
	 * possible -> send Cancel B) which changesets to send -> send Rendered
	 * Missing Changesets
	 */
	@FromState("Started")
	// TARGET
	@OnMessage(performative = "Confirm")
	@PossibleOutcome({ "SendingDelta" })
	// @AtActivity(CONTENT);
	public WorkflowStateConstant targetSendVersionedOntologyDelta(final Json msg) throws Throwable
	{
		final List<Revision> sourceRevisions = new ArrayList<Revision>();
		for (Json x : msg.at(CONTENT).asJsonList())
			sourceRevisions.add((Revision) Messages.fromJson(x));
		this.referenceHeads = Messages.fromJson(msg.at(KEY_REFERENCE_HEADS));
		this.ontologyUUID = Messages.fromJson(msg.at(KEY_ONTOLOGY_UUID));
		return graph.getTransactionManager().ensureTransaction(new Callable<WorkflowStateConstant>()
		{
			public WorkflowStateConstant call()
			{
				Json reply;
				WorkflowStateConstant nextState;
				String owlxmlStringOntology;
				HGDBOntology ontology = graph.get(ontologyUUID);
				VersionedOntology versionedOntology = repository.getOntologyManager().getVersionManager().versioned(ontologyUUID);
				if (ontology == null)
					throw new IllegalStateException("Ontology " + ontologyUUID + " does not exist at target. Cannot send delta.");
				reply = getReply(msg, Performative.Inform);
				Set<Revision> delta = new OntologyVersionState(referenceHeads).delta(versionedOntology);
				if (delta.isEmpty())
				{
					// target equals source
					reply = getReply(msg, Performative.InformIf);
					reply.set(CONTENT, "Target and Source are equal.");
					setCompletedMessage("Target and Source are equal. Nothing to transmit.");
					nextState = WorkflowStateConstant.Completed;
				}
				try
				{
					owlxmlStringOntology = activityUtils.renderVersionedOntologyDelta(versionedOntology, /*delta*/ null);
				}
				catch (Exception ex)
				{
					// TODO - better, generic exception handling for when something like this goes wrong
					ex.printStackTrace();
					owlxmlStringOntology = "";
				}
				reply.set(CONTENT, owlxmlStringOntology);				
				setCompletedMessage("Target sent " + delta.size()
									+ " changesets to source." + " size was : " + (owlxmlStringOntology.length() / 1024)
									+ " kilo characters ");
				nextState = SendingDelta;
				if (DBG_RENDER_ONTOLOGIES_TO_FILE)
				{
					try
					{
						activityUtils.saveStringXML(owlxmlStringOntology, "DELTA-SENT-BY-PULL-TARGET");
					}
					catch (Exception e)
					{
						System.err.println("Pull: Exception during debug output ignored:");
						e.printStackTrace();
					}
				}
				// TRANSACTION END
				send(getSender(msg), reply);
				return nextState;
			}
		}, HGTransactionConfig.READONLY);
	}

	@FromState("Started")
	// SOURCE
	@OnMessage(performative = "Inform")
	// @PossibleOutcome({"Completed", "Failed"})
	// @AtActivity(CONTENT);
	public WorkflowStateConstant sourceReceiveVersionedOntologyDelta(final Json msg) throws Throwable
	{
		String vowlxmlStringDelta = graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				// TRANSACTION START
				VersionedOntology versionedOntology = repository.getOntologyManager().getVersionManager().versioned(ontologyUUID);
				if (DBG)
					System.out.println("RECEIVING PULLED delta");
				String vowlxmlStringDelta = msg.at(CONTENT).asString();
				OWLOntologyDocumentSource ds = new StringDocumentSource(vowlxmlStringDelta);
				// Parse, apply and append the delta
				try
				{
					activityUtils.appendDeltaTo(ds, versionedOntology, mergeWithUncommited);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				// assert targetVersionedOntology contains delta
				if (DBG_RENDER_ONTOLOGIES_TO_FILE)
				{
					repository.printAllOntologies();
					try
					{
						activityUtils.saveVersionedOntologyXML(versionedOntology, "FULL-AFTER-DELTA-APPLIED-PULL-SOURCE");
					}
					catch (Exception e)
					{
						// DBG exception ignored.
						e.printStackTrace();
					}
				}
				return vowlxmlStringDelta;
				// TRANSACTION END
			}
		}, HGTransactionConfig.DEFAULT);
		Json reply = getReply(msg, Performative.AcceptProposal);
		send(getSender(msg), reply);
		if (mergeWithUncommited)
		{
			setCompletedMessage("Delta received, applied and merged with uncommitted changes. Size: "
					+ (vowlxmlStringDelta.length() / 1024) + " kilo characters");
		}
		else
		{
			setCompletedMessage("Delta received and applied. Size: " + (vowlxmlStringDelta.length() / 1024) + " kilo characters");
		}
		return WorkflowStateConstant.Completed;
	}

	@FromState("Started")
	// SOURCE
	@OnMessage(performative = "InformIf")
	public WorkflowStateConstant sourceReceiveVersionedOntologyDeltaCancelled(final Json msg) throws Throwable
	{
		String message = msg.at(CONTENT).asString();
		setCompletedMessage(message);
		return WorkflowStateConstant.Completed;
	}

	@FromState("SendingDelta")
	// Target
	@OnMessage(performative = "AcceptProposal")
	// @PossibleOutcome({"Completed"})
	public WorkflowStateConstant targetReceiveConfirmationForDelta(Json msg) throws Throwable
	{
		setCompletedMessage("All changes were applied to source.");
		return WorkflowStateConstant.Completed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.peer.workflow.Activity#getType()
	 */
	@Override
	public String getType()
	{
		return TYPENAME;
	}
}
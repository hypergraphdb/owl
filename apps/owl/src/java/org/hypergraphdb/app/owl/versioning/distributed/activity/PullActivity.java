package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.RevisionID;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.PeerDistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ServerCentralizedOntology;
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
import org.semanticweb.owlapi.io.OWLRendererException;
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
	private RevisionID lastRequestedRevision; // on source and target
	// private DistributedOntology sourceDistributedOntology; //pull onto
	// existing
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
						DistributedOntology localDistributedOntology, 
						HGPeerIdentity targetPeerID)
	{
		this(sourcePeer, localDistributedOntology, targetPeerID, null);
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
						DistributedOntology localDistributedOntology, 
						HGPeerIdentity targetPeerID,
						RevisionID lastRevisionToPull)
	{
		super(sourcePeer);
		if (!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
		if (localDistributedOntology instanceof ServerCentralizedOntology)
		{
			throw new IllegalArgumentException("Cannot pull onto server ontology");
		}
		if (localDistributedOntology instanceof ClientCentralizedOntology)
		{
			ClientCentralizedOntology cco = (ClientCentralizedOntology) localDistributedOntology;
			if (!cco.getServerPeer().equals(targetPeerID))
			{
				throw new IllegalArgumentException("Cannot Pull from server " + targetPeerID
						+ "for ontology, because ClientCentralizedOntology is bound to server: " + cco.getServerPeer());
			}
		}
		// NPE if not stored:
		HGPersistentHandle ontologyUUID = repository.getOntologyUUID(localDistributedOntology.getWorkingSetData());
		if (lastRevisionToPull != null)
		{
			if (!ontologyUUID.equals(ontologyUUID))
			{
				throw new IllegalArgumentException("The OntologyUUID" + ontologyUUID + " does not match the last pull revision's "
						+ lastRevisionToPull.getOntologyUUID());
			}
		}
		initialize(sourcePeer, ontologyUUID, targetPeerID, lastRevisionToPull);
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
	public PullActivity(HyperGraphPeer sourcePeer, RevisionID lastRevisionToPull, HGPeerIdentity targetPeerID)
	{
		super(sourcePeer);
		initialize(sourcePeer, lastRevisionToPull.getOntologyUUID(), targetPeerID, lastRevisionToPull);
	}

	protected void initialize(HyperGraphPeer sourcePeer, 
							  HGPersistentHandle ontologyUUID, 
							  HGPeerIdentity targetPeerID,
							  RevisionID lastRevisionToPull)
	{
		this.ontologyUUID = ontologyUUID;
		this.targetPeerID = targetPeerID;
		this.lastRequestedRevision = lastRevisionToPull;
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
					DistributedOntology sourceDistributedOnto = repository.getDistributedOntology(o);
					if (sourceDistributedOnto != null)
					{
						// TODO send content hash
						// Get Delta
						msg = createMessage(Performative.Confirm, null);
						List<Revision> revList = null;//sourceDistributedOnto.getVersionedOntology().getRevisions();
						msg.set(CONTENT, revList);
						msg.set(KEY_DISTRIBUTION_MODE, getDistributionModeFor(sourceDistributedOnto));
						msg.set(KEY_LAST_REQUESTED_REVISION, lastRequestedRevision);
						return msg;
					}
					else
					{
						// Ontology but no versioning information,
						// cannot determine
						// Source has uncommitted - cannot pull
						throw new RuntimeException(new VOWLSourceTargetConflictException(
								"Source ontology exists but is not shared. Cannot pull."));
					}
				}
				else
				{
					// ontology unknown at source. Pull full or until
					// lastRequestedRevision.
					msg = createMessage(Performative.Disconfirm, ontologyUUID);
					msg.set(KEY_LAST_REQUESTED_REVISION, lastRequestedRevision);
					return msg;
					// TRANSACTION END
				}
			}
		});
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
		String[] vowlxmlRenderedAndMode = graph.getTransactionManager().ensureTransaction(new Callable<String[]>()
		{
			public String[] call()
			{
				// TRANSACTION START
				String[] results;
				ontologyUUID = Messages.fromJson(msg.at(CONTENT));
				lastRequestedRevision = Messages.fromJson(msg.at(KEY_LAST_REQUESTED_REVISION));
				HGDBOntology onto = graph.get(ontologyUUID);
				if (onto != null)
				{
					DistributedOntology targetDistributedOnto;
					targetDistributedOnto = repository.getDistributedOntology(onto);
					if (targetDistributedOnto != null)
					{
						try
						{
							results = new String[2];
							// render FULL or render until last requested.
							VersionedOntology vo = targetDistributedOnto.getVersionedOntology();							
//							if (lastRequestedRevision != null)
//							{								
//								int lastRevisionToPullIndex = vo.getRevisions().indexOf(lastRequestedRevision);
//								if (lastRevisionToPullIndex < 0)
//								{
//									throw new RuntimeException(new VOWLException(
//											"The requested last revision does not exist on target: " + lastRequestedRevision));
//								}
//								else
//								{
//									results[0] = activityUtils.renderVersionedOntology(
//											targetDistributedOnto.getVersionedOntology(), lastRevisionToPullIndex);
//								}
//							}
//							else
//							{
//								results[0] = activityUtils.renderVersionedOntology(targetDistributedOnto.getVersionedOntology());
//							}
							results[1] = getDistributionModeFor(targetDistributedOnto);
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
				return results;
				// TRANSACTION END
			}
		}, HGTransactionConfig.READONLY);
		reply = getReply(msg, Performative.Propose);
		// send full head revision data, not versioned yet.
		reply.set(CONTENT, vowlxmlRenderedAndMode[0]);
		reply.set(KEY_DISTRIBUTION_MODE, vowlxmlRenderedAndMode[1]);
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
		final String pullMode = msg.at(KEY_DISTRIBUTION_MODE).asString();

		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				// TRANSACTION START
				VersionedOntology voParsed = null;
				try
				{
//					voParsed = activityUtils.storeVersionedOntology(
//							new StringDocumentSource(vowlxmlStringOntology), repository.getOntologyManager());
					DistributedOntology newDO;
					HGHandle voParsedHandle = graph.getHandle(voParsed);
					if (pullMode.equals(VALUE_DISTRIBUTION_MODE_CLIENT_SERVER))
					{
						newDO = new ClientCentralizedOntology(voParsedHandle, targetPeerID);
					}
					else if (pullMode.equals(VALUE_DISTRIBUTION_MODE_PEER))
					{
						newDO = new PeerDistributedOntology(voParsedHandle);
					}
					else
					{
						throw new IllegalArgumentException("Pull mode not recognized. Abort transaction. Was:" + pullMode);
					}
					graph.add(newDO);
					// targetDistributedOnto = newDO;
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
		setCompletedMessage("Full versioned ontology received. Size: " + (vowlxmlStringOntology.length() / 1024)
				+ " kilo characters");
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
		final String pullMode = msg.at(KEY_DISTRIBUTION_MODE).asString();
		lastRequestedRevision = Messages.fromJson(msg.at(KEY_LAST_REQUESTED_REVISION));
		return graph.getTransactionManager().ensureTransaction(new Callable<WorkflowStateConstant>()
		{
			public WorkflowStateConstant call()
			{
				Json reply;
				WorkflowStateConstant nextState;
				int lastCommonRevisionIndex;
				boolean allSourceRevisionsAreInTarget;
				boolean allTargetRevisionsAreInSource;
				String owlxmlStringOntology;
				DistributedOntology targetDistributedOnto;
				// TRANSACTION START
				// VersionedOntology targetVersionedOnto =
				// repository.getVersionControlledOntology(ontologyUUID);
				HGPersistentHandle sourceUUID = sourceRevisions.get(0).getOntologyUUID();
				HGDBOntology ontology = graph.get(sourceUUID);
				if (ontology == null)
				{
					throw new IllegalStateException("Ontology " + sourceUUID + " does not exist at target. Cannot send delta.");
				}
				else
				{
					targetDistributedOnto = repository.getDistributedOntology(ontology);
					if (targetDistributedOnto == null)
					{
						throw new IllegalStateException("Ontology " + ontology + " is not shared at target. Cannot send delta.");
					}
				}
				targetAssertDistributionModeMatches(targetDistributedOnto, pullMode);
				List<Revision> targetRevisions = null;//targetDistributedOnto.getVersionedOntology().getRevisions();
				lastCommonRevisionIndex = activityUtils.findLastCommonRevisionIndex(sourceRevisions, targetRevisions);
				allSourceRevisionsAreInTarget = lastCommonRevisionIndex + 1 == sourceRevisions.size();
				allTargetRevisionsAreInSource = lastCommonRevisionIndex + 1 == targetRevisions.size();
				if (lastCommonRevisionIndex >= 0)
				{
					if (allSourceRevisionsAreInTarget)
					{
						if (!allTargetRevisionsAreInSource)
						{
							// S C0C1C2
							// T C0C1C2T3
							// send target's Revisions and changesets starting
							// at sourceIndex, no data, no uncommitted
							// Send, including the LAST MATCHING REVISION at
							// which index the first necessary
							// delta changeset will be.
							reply = getReply(msg, Performative.Inform);
//							try
//							{
//								if (lastRequestedRevision != null)
//								{
//									int lastRevisionToPullIndex = targetRevisions.indexOf(lastRequestedRevision);
//									if (lastRevisionToPullIndex >= 0)
//									{
//										owlxmlStringOntology = activityUtils.renderVersionedOntologyDelta(
//												targetDistributedOnto.getVersionedOntology(), lastCommonRevisionIndex,
//												lastRevisionToPullIndex);
//									}
//									else
//									{
//										throw new RuntimeException(new VOWLException(
//												"The requested last revision does not exist on target: " + lastRequestedRevision));
//									}
//								}
//								else
//								{
//									// Send ALL REVISIONS
//									owlxmlStringOntology = activityUtils.renderVersionedOntologyDelta(
//											targetDistributedOnto.getVersionedOntology(), lastCommonRevisionIndex);
//								}
//							}
//							catch (Exception e)
//							{
//								throw new RuntimeException(e);
//							}
//							reply.set(CONTENT, owlxmlStringOntology);
//							reply.set(KEY_LAST_MATCHING_REVISION, targetRevisions.get(lastCommonRevisionIndex));
//							setCompletedMessage("Target sent " + (targetRevisions.size() - lastCommonRevisionIndex - 1)
//									+ " changesets to source." + " size was : " + (owlxmlStringOntology.length() / 1024)
//									+ " kilo characters ");
							nextState = SendingDelta;
//							if (DBG_RENDER_ONTOLOGIES_TO_FILE)
//							{
//								try
//								{
//									activityUtils.saveStringXML(owlxmlStringOntology, "DELTA-SENT-BY-PULL-TARGET");
//								}
//								catch (Exception e)
//								{
//									System.err.println("Pull: Exception during debug output ignored:");
//									e.printStackTrace();
//								}
//							}
						}
						else
						{
							// S C0C1C2
							// T C0C1C2
							// target equals source
							reply = getReply(msg, Performative.InformIf);
							reply.set(CONTENT, "Target and Source are equal.");
							setCompletedMessage("Target and Source are equal. Nothing to transmit.");
							nextState = WorkflowStateConstant.Completed;
						}
					}
					else
					{
						if (allTargetRevisionsAreInSource)
						{
							// S C0C1C2S4
							// T C0C1C2
							// Suggest Push
							// target has more than source, but some inital
							// match
							reply = getReply(msg, Performative.InformIf);
							reply.set(CONTENT, "Source is newer than target.");
							setCompletedMessage("Source is newer than target. A push is suggested and possible.");
							nextState = WorkflowStateConstant.Completed;
						}
						else
						{
							// S C0C1C2S3
							// T C0C1C2T3 S3 <> T3
							// Both have exclusive revisions after a common
							// history,
							// push or pull only possible with branching, which
							// is not available.
							// Here in the linear model it is a conflict.
							// Revert source or target and pull or push.
							throw new RuntimeException(new VOWLSourceTargetConflictException(
									"Both have excusive revisions after a common history"));
						}
					}
				}
				else
				{
					// no shared history
					throw new RuntimeException(new VOWLSourceTargetConflictException(
							"No common revision at beginning of source and target histories."));
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
		//
		// Test if received last revision matches target head and all other
		// prerequisites are still met.
		//
		final Revision lastMatchingRevision = Messages.fromJson(msg.at(KEY_LAST_MATCHING_REVISION));
		// Validate if lastMatchingRevision still is target HEAD, keep UUID
		// Throws exceptions if not.
		String vowlxmlStringDelta = graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				// TRANSACTION START
				DistributedOntology sourceDistributedOnto = activityUtils.getDistributedOntologyForDeltaFrom(lastMatchingRevision,
						repository, true);
				VersionedOntology sourceVersionedOnto = sourceDistributedOnto.getVersionedOntology();
//				mergeWithUncommited = !sourceVersionedOnto.getWorkingSetChanges().isEmpty();
				if (DBG)
					System.out.println("RECEIVING PULLED delta");
				String vowlxmlStringDelta = msg.at(CONTENT).asString();
				OWLOntologyDocumentSource ds = new StringDocumentSource(vowlxmlStringDelta);
				// Parse, apply and append the delta
//				try
//				{
//					activityUtils.appendDeltaTo(ds, sourceVersionedOnto, mergeWithUncommited);
//				}
//				catch (Exception e)
//				{
//					throw new RuntimeException(e);
//				}
//				// assert targetVersionedOntology contains delta
//				if (DBG_RENDER_ONTOLOGIES_TO_FILE)
//				{
//					repository.printAllOntologies();
//					try
//					{
//						activityUtils.saveVersionedOntologyXML(sourceVersionedOnto, "FULL-AFTER-DELTA-APPLIED-PULL-SOURCE");
//					}
//					catch (Exception e)
//					{
//						// DBG exception ignored.
//						e.printStackTrace();
//					}
//				}
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
package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer.OBJECTCONTEXT_REPOSITORY;
import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;

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
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.PossibleOutcome;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.hypergraphdb.transaction.HGTransactionConfig;

/**
 * PushActivity. Pushes all changes to a target repository, which has the same
 * VersionedOntology and a change history that is shorter (older) than the
 * source (initiator).
 * 
 * Logical outcomes: - FAILED_VERSIONED_ONTOLOGY_DOES_NOT_EXIST - FAILED_No
 * Update Necessary - FAILED_Target_Newer - CONFLICT (Target history is
 * different than ours) -
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 17, 2012
 */
public class PushActivity extends OntologyTransmitActivity
{
	public static final String TYPENAME = "push-VersionedOntology";

	// public static final String ERROR_OBJECT_KEY = "ErrorObject";

	public static boolean DBG = true;

	public static boolean DBG_RENDER_ONTOLOGIES_TO_FILE = false;

	public static final WorkflowStateConstant SendingInitial = WorkflowStateConstant.makeStateConstant("SendingInitial");
	public static final WorkflowStateConstant ReceivingInitial = WorkflowStateConstant.makeStateConstant("ReceivingInitial");
	public static final WorkflowStateConstant SendingDelta = WorkflowStateConstant.makeStateConstant("SendingDelta");
	public static final WorkflowStateConstant ReceivingDelta = WorkflowStateConstant.makeStateConstant("ReceivingDelta");

	private OntologyDatabasePeer repository;
	private DistributedOntology sourceDistributedOnto;
	private boolean sourceDistributedExistsOnTarget;
	private int sourceNrOfRevisionsPushed;
	private long sourceSizeOfPushedDataChars;

	/**
	 * Might be created as new, if the pushed ontology does not exist on target.
	 */
	private DistributedOntology targetDistributedOnto;

	private HGPeerIdentity targetPeerID;
	private String completedMessage;
	private HyperGraph graph;

	private ActivityUtils activityUtils = new ActivityUtils();

	public PushActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
		if (!thisPeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (OntologyDatabasePeer) thisPeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
	}

	/**
	 * @param thisPeer
	 */
	public PushActivity(HyperGraphPeer sourcePeer, DistributedOntology sourceDistributedOnto, HGPeerIdentity targetPeerID)
	{
		super(sourcePeer);
		this.sourceDistributedOnto = sourceDistributedOnto;
		this.targetPeerID = targetPeerID;
		if (!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (OntologyDatabasePeer) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
	}

	/**
	 * @return the targetDistributedOnto
	 */
	public DistributedOntology getTargetDistributedOnto()
	{
		return targetDistributedOnto;
	}

	/**
	 * @param completedMessage
	 *            the completedMessage to set
	 */
	protected void setCompletedMessage(String completedMessage)
	{
		this.completedMessage = completedMessage;
		if (DBG)
			System.out.println("Push Completed: " + completedMessage);
	}

	/**
	 * @return the completedMessage
	 */
	public String getCompletedMessage()
	{
		return completedMessage;
	}

	/*
	 * (non-Javadoc) // SOURCE
	 * 
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate()
	{
		Json msg = createMessage(Performative.QueryIf, this);
		msg.set(CONTENT, sourceDistributedOnto.getVersionedOntology().getCurrentRevision());
		send(targetPeerID, msg);
		if (DBG)
		{
			getThisPeer().getGraph().getLogger()
					.trace("Query if target push : " + sourceDistributedOnto.getVersionedOntology().revision());
		}
	}

	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started")
	// TARGET
	@OnMessage(performative = "QueryIf")
	@PossibleOutcome({ "ReceivingDelta", "ReceivingInitial" })
	// @AtActivity(CONTENT);
	public WorkflowStateConstant targetQueryIfVersionedOntologyExists(final Json msg) throws Throwable
	{
		// msg parsing
		final HGPersistentHandle headRevisionOntologyID = Messages.fromJson(msg.at(CONTENT));
		// Look up in repository
		// TRANSACTION START
		return graph.getTransactionManager().ensureTransaction(new Callable<WorkflowStateConstant>()
		{
			public WorkflowStateConstant call()
			{
				HGDBOntology o = graph.get(headRevisionOntologyID);
				if (o != null)
				{
					VersionManager versionManager = new VersionManager(getThisPeer().getGraph(), "fixme-VHDBOntologyRepository");
					if (versionManager.isVersioned(o.getAtomHandle()))
					{
						VersionedOntology vo = versionManager.versioned(o.getAtomHandle());
						// send Confirm with existing revisions objects
						// and tell if we have uncommitted changes.
						// TODO send content hash
						if (vo.changes().isEmpty())
						{
							Json reply = getReply(msg, Performative.Confirm);
							Json revList = Json.array();
//							for (Revision r : targetDistributedOnto.getVersionedOntology().getRevisions())
//								revList.add(Json.make(r));
							reply.set(CONTENT, revList);
							send(getSender(msg), reply);
							return ReceivingDelta;
						}
						else
						{
							// Target has uncommitted - cannot push
							throw new RuntimeException(new VOWLSourceTargetConflictException(
									"Target has uncommitted changes. Cannot push."));
						}
					}
					else
					{
						// Ontology but no versioning information,
						// cannot determine
						// Target has uncommitted - cannot push
						throw new RuntimeException(
								new VOWLSourceTargetConflictException("Target ontology not shared. Cannot push."));
					}
				}
				else
				{
					// o null or targetDistributedOnto null
					// send Confirm
					Json reply = getReply(msg, Performative.Disconfirm);
					send(getSender(msg), reply);
					return ReceivingInitial;
					// TRANSACTION END
				}
			}
		});
	}

	// ------------------------------------------------------------------------------------
	// SENDING / RECEIVING FULL VERSIONED ONTOLOGY
	//

	@FromState("Started")
	// SOURCE
	@OnMessage(performative = "Disconfirm")
	@PossibleOutcome({ "SendingInitial" })
	// @AtActivity(CONTENT);
	public WorkflowStateConstant sourceSendFullVersionedOntology(Json msg) throws Throwable
	{
		// PROPOSE
		setSourceDistributedExistsOnTarget(false);
		String vowlxmlStringOntology = graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				// TRANSACTION START
//				try
//				{
//					return activityUtils.renderVersionedOntology(sourceDistributedOnto.getVersionedOntology());
//				}
//				catch (OWLRendererException e)
//				{
//					throw new RuntimeException(e);
//				}
				// TRANSACTION END
				return null;
			}
		});
		msg = createMessage(Performative.Propose, this);
		// send full head revision data, not versioned yet.
		msg.set(CONTENT, vowlxmlStringOntology);
		send(targetPeerID, msg);
//		setSourceNrOfRevisionsPushed(sourceDistributedOnto.getVersionedOntology().getNrOfRevisions());
		setSourceSizeOfPushedDataChars(vowlxmlStringOntology.length());
		return SendingInitial;
	}

	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("ReceivingInitial")
	// TARGET
	@OnMessage(performative = "Propose")
	// @PossibleOutcome({"Completed", "Failed"})
	// @AtActivity(CONTENT);
	public WorkflowStateConstant targetReceiveFullVersionedOntologyAsNew(Json msg) throws Throwable
	{
		final String vowlxmlStringOntology = msg.at(CONTENT).asString();
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				// TRANSACTION START
				VersionedOntology voParsed = null;
				try
				{
//					voParsed = activityUtils.storeVersionedOntology(new StringDocumentSource(vowlxmlStringOntology),
//							repository.getOntologyManager());
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
				DistributedOntology newDO;
				HGHandle voParsedHandle = graph.getHandle(voParsed);
//				graph.add(newDO);
//				targetDistributedOnto = newDO;
//				if (DBG_RENDER_ONTOLOGIES_TO_FILE)
//				{
//					repository.printAllOntologies();
//					try
//					{
//						activityUtils.saveVersionedOntologyXML(voParsed, "FULL-RECEIVED-TARGET"
//								+ getThisPeer().getIdentity().getId());
//					}
//					catch (OWLRendererException e)
//					{
//						// Ignore DBG exceptions
//						e.printStackTrace();
//					}
//					catch (IOException e)
//					{
//						e.printStackTrace();
//					}
//				}
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
	// SOURCE
	@OnMessage(performative = "AcceptProposal")
	@PossibleOutcome({ "Completed" })
	// @AtActivity(CONTENT);
	public WorkflowStateConstant sourceReceiveConfirmationForFullVersionedOntology(Json msg) throws Throwable
	{
		setCompletedMessage("Target reported: accepted full versioned ontology. All changes were applied.");
		sourceDistributedExistsOnTarget = true;
		return WorkflowStateConstant.Completed;
	}

	// ------------------------------------------------------------------------------------
	// TARGET HAS ONTOLOGY -> VALIDATING AND PUSHING MISSING CHANGES
	// OR CANCEL -> has
	//

	/**
	 * Receive a list of revisions from target and determine, A) if push is
	 * possible -> send Propose B) which changesets to send -> send Rendered
	 * Missing Changesets
	 */
	@FromState("Started")
	// SOURCE
	@OnMessage(performative = "Confirm")
	@PossibleOutcome({ "SendingDelta" })
	// @AtActivity(CONTENT);
	public WorkflowStateConstant sourceSendVersionedOntologyDelta(final Json msg) throws Throwable
	{
		setSourceDistributedExistsOnTarget(true);
		final List<Revision> targetRevisions = new ArrayList<Revision>();
		for (Json jrev : msg.at(CONTENT).asJsonList())
			targetRevisions.add((Revision) Messages.fromJson(jrev));
		return graph.getTransactionManager().ensureTransaction(new Callable<WorkflowStateConstant>()
		{
			public WorkflowStateConstant call()
			{
				Json reply;
				WorkflowStateConstant nextState = null;
				int lastCommonRevisionIndex;
				boolean allSourceRevisionsAreInTarget;
				boolean allTargetRevisionsAreInSource;
				String owlxmlStringOntology;
				// TRANSACTION START
				List<Revision> sourceRevisions = null;//sourceDistributedOnto.getVersionedOntology().getRevisions();
				lastCommonRevisionIndex = -1;//activityUtils.findLastCommonRevisionIndex(sourceRevisions, targetRevisions);
				allSourceRevisionsAreInTarget = lastCommonRevisionIndex + 1 == sourceRevisions.size();
				allTargetRevisionsAreInSource = lastCommonRevisionIndex + 1 == targetRevisions.size();
				if (lastCommonRevisionIndex >= 0)
				{
					if (allTargetRevisionsAreInSource)
					{
						if (!allSourceRevisionsAreInTarget)
						{
							// S C0C1C2S3
							// T C0C1C2
							// Source newer than target and all shared match
							// send Revisions and changeset starting
							// sourceIndex, no data, no uncommitted
							// Send, including the LAST MATCHING REVISION at
							// which index the first necessary
							// delta changeset will be.
							reply = getReply(msg, Performative.Propose);
//							try
//							{
//								owlxmlStringOntology = activityUtils.renderVersionedOntologyDelta(
//										sourceDistributedOnto.getVersionedOntology(), lastCommonRevisionIndex);
//							}
//							catch (Exception e)
//							{
//								throw new RuntimeException(e);
//							}
//							reply.set(CONTENT, owlxmlStringOntology);
//							reply.set(KEY_LAST_MATCHING_REVISION, sourceRevisions.get(lastCommonRevisionIndex));
//							int nrOfRevisionsSent = (sourceRevisions.size() - lastCommonRevisionIndex - 1);
//							setSourceNrOfRevisionsPushed(nrOfRevisionsSent);
//							setSourceSizeOfPushedDataChars(owlxmlStringOntology.length());
//							setCompletedMessage("Source sent " + nrOfRevisionsSent + " revisions to target." + " size was : "
//									+ (owlxmlStringOntology.length() / 1024) + " kilo characters ");
//							nextState = SendingDelta;
//							if (DBG_RENDER_ONTOLOGIES_TO_FILE)
//							{
//								try
//								{
//									activityUtils.saveStringXML(owlxmlStringOntology, "DELTA-SENT-BY-PUSH-SOURCE");
//								}
//								catch (Exception e)
//								{
//									System.err.println("Push: Exception during debug output ignored:");
//									e.printStackTrace();
//								}
//							}
						}
						else
						{
							// S C0C1C2
							// T C0C1C2
							// source equals target
							reply = getReply(msg, Performative.Confirm);
							reply.set(CONTENT, "Source and Target are equal.");
							setCompletedMessage("Source and Target are equal. Nothing to transmit.");
							nextState = WorkflowStateConstant.Completed;
						}
					}
					else
					{
						if (allSourceRevisionsAreInTarget)
						{
							// S C0C1C2
							// T C0C1C2T3
							// Suggest Pull
							// target has more than source, but all shared match
							reply = getReply(msg, Performative.Confirm);
							reply.set(CONTENT, "Target is newer than source.");
							setCompletedMessage("Target is newer than source. A pull is suggested and possible.");
							nextState = WorkflowStateConstant.Completed;
						}
						else
						{
							// S C0C1C2S3
							// T C0C1C2T3 S3 <> T3
							// Both have excusive revisions after a common
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
		});
	}

	@FromState("ReceivingDelta")
	// TARGET
	@OnMessage(performative = "Confirm")
	public WorkflowStateConstant targetReceiveVersionedOntologyDeltaNotNecessary(final Json msg) throws Throwable
	{
		String message = msg.at(CONTENT).asString();
		setCompletedMessage("Source reported that no delta is necessary. Reason: " + message);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("ReceivingDelta")
	// TARGET
	@OnMessage(performative = "Propose")
	// @PossibleOutcome({"Completed", "Failed"})
	// @AtActivity(CONTENT);
	public WorkflowStateConstant targetReceiveVersionedOntologyDelta(final Json msg) throws Throwable
	{
		//
		// Test if received last revision matches target head and all other
		// prerequisites are still met.
		//
		// Validate if lastMatchingRevision still is target HEAD, keep UUID
		// Throws exceptions if not.
		String vowlxmlStringDelta = graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				// TRANSACTION START
//				DistributedOntology targetDistributedOntology = activityUtils.getDistributedOntologyForDeltaFrom(
//						lastMatchingRevision, repository, false);
//				targetAssertDistributionModeMatches(targetDistributedOntology, pushMode);
//				VersionedOntology targetVersionedOntology = targetDistributedOntology.getVersionedOntology();
//				if (DBG)
//					System.out.println("RECEIVING delta");
//				String vowlxmlStringDelta = msg.at(CONTENT).asString();
//				OWLOntologyDocumentSource ds = new StringDocumentSource(vowlxmlStringDelta);
//				// Parse, apply and append the delta
//				try
//				{
//					activityUtils.appendDeltaTo(ds, targetVersionedOntology, false);
//				}
//				catch (Exception e)
//				{
//					throw new RuntimeException(e);
//				}
//				// assert targetDistributedOntology contains delta
//				if (DBG_RENDER_ONTOLOGIES_TO_FILE)
//				{
//					repository.printAllOntologies();
//					try
//					{
//						activityUtils.saveVersionedOntologyXML(targetVersionedOntology, "FULL-AFTER-DELTA-APPLIED-PUSH-TARGET");
//					}
//					catch (Exception e)
//					{
//						// DBG exception ignored.
//						e.printStackTrace();
//					}
//				}
//				return vowlxmlStringDelta;
				return null;
				// TRANSACTION END
			}
		});
		Json reply = getReply(msg, Performative.AcceptProposal);
		send(getSender(msg), reply);
		setCompletedMessage("Delta received and applied. Size: " + (vowlxmlStringDelta.length() / 1024) + " kilo characters");
		return WorkflowStateConstant.Completed;
	}

	@FromState("SendingDelta")
	// Source
	@OnMessage(performative = "AcceptProposal")
	@PossibleOutcome({ "Completed" })
	public WorkflowStateConstant sourceReceiveConfirmationForDelta(Json msg) throws Throwable
	{
		setCompletedMessage("All changes were applied to target.");
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

	/**
	 * @return the sourceDistributedExistsOnTarget
	 */
	public boolean isSourceDistributedExistsOnTarget()
	{
		return sourceDistributedExistsOnTarget;
	}

	public int getSourceNrOfRevisionsPushed()
	{
		return sourceNrOfRevisionsPushed;
	}

	public long getSourceSizeOfPushedDataChars()
	{
		return sourceSizeOfPushedDataChars;
	}

	/**
	 * @param sourceDistributedExistsOnTarget
	 *            the sourceDistributedExistsOnTarget to set
	 */
	protected void setSourceDistributedExistsOnTarget(boolean sourceDistributedExistsOnTarget)
	{
		this.sourceDistributedExistsOnTarget = sourceDistributedExistsOnTarget;
	}

	/**
	 * @param sourceNrOfRevisionsPushed
	 *            the sourceNrOfRevisionsPushed to set
	 */
	protected void setSourceNrOfRevisionsPushed(int sourceNrOfRevisionsPushed)
	{
		this.sourceNrOfRevisionsPushed = sourceNrOfRevisionsPushed;
	}

	/**
	 * @param sourceSizeOfPushedDataChars
	 *            the sourceSizeOfPushedDataChars to set
	 */
	protected void setSourceSizeOfPushedDataChars(long sourceSizeOfPushedDataChars)
	{
		this.sourceSizeOfPushedDataChars = sourceSizeOfPushedDataChars;
	}
}
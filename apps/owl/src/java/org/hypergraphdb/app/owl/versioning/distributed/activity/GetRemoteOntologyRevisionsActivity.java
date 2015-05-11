package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;

import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import mjson.Json;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;

/**
 * GetRemoteOntologyRevisionsActivity gets a list of revision objects from a
 * remote repository by UUID
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 06, 2012
 */
public class GetRemoteOntologyRevisionsActivity extends FSMActivity
{

	public static boolean DBG = true;
	public static final String TYPENAME = "get-remote-ontology-revisions";

	private HGPeerIdentity targetPeerID;
	private VDHGDBOntologyRepository repository;
	private HyperGraph graph;
	private HGPersistentHandle sourceDistributedOntologyUUID;
	private List<Revision> revisionsFromTarget;

	public GetRemoteOntologyRevisionsActivity(HyperGraphPeer thisPeer, UUID id)
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

	/**
	 * @param thisPeer
	 */
	public GetRemoteOntologyRevisionsActivity(HyperGraphPeer sourcePeer, HGPeerIdentity targetPeerID,
			HGPersistentHandle ontologyUUID)
	{
		super(sourcePeer);
		this.targetPeerID = targetPeerID;
		if (!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
		sourceDistributedOntologyUUID = ontologyUUID;
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

	/*
	 * (non-Javadoc) // SOURCE
	 * 
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate()
	{
		Json msg = createMessage(Performative.QueryIf, this);
		if (sourceDistributedOntologyUUID == null)
			throw new NullPointerException("sourceDistributedOntologyUUID must not be null");
		msg.set(CONTENT, sourceDistributedOntologyUUID);
		send(targetPeerID, msg);
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
	public WorkflowStateConstant targetQueryOntologyRevisions(final Json msg) throws Throwable
	{
		final HGPersistentHandle sourceUUID = Messages.fromJson(msg.at(CONTENT));
		Json reply = graph.getTransactionManager().ensureTransaction(new Callable<Json>()
		{
			public Json call()
			{
				Json reply;
				HGDBOntology o = graph.get(sourceUUID);
				if (o != null)
				{
					DistributedOntology sourceDistributedOnto = repository.getDistributedOntology(o);
					if (sourceDistributedOnto != null)
					{
						reply = getReply(msg, Performative.Inform);
						VersionedOntology vo = sourceDistributedOnto.getVersionedOntology();
						List<Revision> revList = graph.getAll(hg.apply(hg.targetAt(graph, 0), 
															hg.and(hg.type(Revision.class), 
															  	   hg.incident(vo.getAtomHandle()))));
						reply.set(CONTENT, revList);
						return reply;
					}
					else
					{
						// Ontology but not shared
						throw new RuntimeException(new VOWLSourceTargetConflictException(
								"Source ontology exists but is not shared."));
					}
				}
				else
				{
					// ontology unknown at source. Pull full.
					throw new RuntimeException(new VOWLSourceTargetConflictException("Source ontology does not exist on server."));
					// TRANSACTION END
				}
			}
		});
		send(getSender(msg), reply);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started")
	// TARGET
	@OnMessage(performative = "Inform")
	public WorkflowStateConstant sourceReceiveOntologyIds(final Json msg) throws Throwable
	{
		revisionsFromTarget = new ArrayList<Revision>();
		for (Json x : msg.at(CONTENT).asJsonList())
			revisionsFromTarget.add((Revision) Messages.fromJson(x));
		return WorkflowStateConstant.Completed;
	}

	/**
	 * To be called after action has completed.
	 * 
	 * @return a list of revisions, null if failure
	 */
	public List<Revision> getRemoteOntologyRevisions()
	{
		return revisionsFromTarget;
	}
}
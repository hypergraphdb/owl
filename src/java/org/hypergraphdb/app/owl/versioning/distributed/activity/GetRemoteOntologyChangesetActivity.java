package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer.OBJECTCONTEXT_REPOSITORY;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import mjson.Json;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.app.owl.versioning.change.VChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * GetRemoteOntologyRevisionsActivity gets a list of revision objects from a
 * remote repository by UUID
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 06, 2012
 */
public class GetRemoteOntologyChangesetActivity extends FSMActivity
{

	public static boolean DBG = true;
	public static final String TYPENAME = "get-remote-ontology-changeset";

	private HGPeerIdentity targetPeerID;
	private OntologyDatabasePeer repository;
	private HyperGraph graph;
	private HGPersistentHandle sourceDistributedOntologyUUID;
	private Revision sourceDistributedOntologyRevision;
	private Json renderedChangesFromTarget;

	public GetRemoteOntologyChangesetActivity(HyperGraphPeer thisPeer, UUID id)
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
	public GetRemoteOntologyChangesetActivity(HyperGraphPeer sourcePeer, HGPeerIdentity targetPeerID,
			HGPersistentHandle distributedOntologyUUID, Revision distributedOntologyRevision)
	{
		super(sourcePeer);
		this.targetPeerID = targetPeerID;
		if (!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (OntologyDatabasePeer) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		graph = repository.getHyperGraph();
		sourceDistributedOntologyUUID = distributedOntologyUUID;
		sourceDistributedOntologyRevision = distributedOntologyRevision;
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
		Object[] queryParams = new Object[] { sourceDistributedOntologyUUID, sourceDistributedOntologyRevision };
		msg.set(CONTENT, queryParams);
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
		Json queryParams = msg.at(CONTENT);
		final HGPersistentHandle sourceUUID = (HGPersistentHandle) Messages.fromJson(queryParams.at(0));
		final Revision revision = (Revision) Messages.fromJson(queryParams.at(1));
		Json reply = graph.getTransactionManager().ensureTransaction(new Callable<Json>()
		{
			public Json call()
			{
				Json reply;
				HGDBOntology o = graph.get(sourceUUID);
				if (o != null)
				{
					VersionManager versionManager = new VersionManager(getThisPeer().getGraph(), "fixme-VHDBOntologyRepository");
					if (versionManager.isVersioned(o.getAtomHandle()))
					{
						VersionedOntology vo = versionManager.versioned(o.getAtomHandle());
						OWLOntology onto = vo.ontology();
						ChangeSet<VersionedOntology> cs = versioning.changes(graph, revision.getAtomHandle(), 
										revision.parents().iterator().next()); //vo.changes(revision);// .getChangeSet(revisionID);
						// Render Changes and send
						List<String> renderedChanges = new LinkedList<String>();
						for (VChange<VersionedOntology> voc : cs.changes())
						{
							OWLOntologyChange change = VOWLChangeFactory.create((VOWLChange)voc, onto, graph);
							renderedChanges.add(change.toString());
						}
						reply = getReply(msg, Performative.Inform);
						reply.set(CONTENT, renderedChanges);
						return reply;						
					}
					else
					{
						// Ontology but not shared
						throw new RuntimeException(new VOWLException("Source ontology exists but is not shared."));
					}
				}
				else
				{
					// ontology unknown at source. Pull full.
					throw new RuntimeException(new VOWLException("Source ontology does not exist on server."));
					// TRANSACTION END
				}
			}
		});
		send(getSender(msg), reply);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started")
	// TARGET
	@OnMessage(performative = "Inform")
	public WorkflowStateConstant sourceReceiveOntologyIds(final Json msg) throws Throwable
	{
		renderedChangesFromTarget = msg.at(CONTENT);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * To be called after action has completed.
	 * 
	 * @return a list of revisions, null if failure
	 */
	public List<String> getRemoteChangeSetChangesRendered()
	{
		List<String> L = new ArrayList<String>();
		for (Json x : renderedChangesFromTarget.asJsonList())
			L.add(x.asString());
		return L;
	}
}
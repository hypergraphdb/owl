package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.OntologyVersionState;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;

import static org.hypergraphdb.peer.Messages.*;

/**
 * <p>
 * This activity is used to obtain the delta between the last time
 * changes were pulled from a remote repository and now. 
 * 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class GetNewRevisionsActivity extends FSMActivity
{
	private HGHandle remoteOntologyHandle;
	private OntologyVersionState.Delta delta = null;
	
	public static final String TYPENAME = "get-new-revisions";

	public GetNewRevisionsActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
	}

	public GetNewRevisionsActivity(HyperGraphPeer thisPeer, HGHandle remoteOntology)
	{
		super(thisPeer);
		this.remoteOntologyHandle = remoteOntology;
	}

	@Override
	public String getType()
	{
		return TYPENAME;
	}
	
	@Override
	public void initiate()
	{
		RemoteOntology remoteOntology = getThisPeer().getGraph().get(remoteOntologyHandle);
		Json msg = createMessage(Performative.QueryRef, this);
		msg.set(CONTENT, Json.object("ontology", remoteOntology.getOntologyHandle(), 
									 "heads", remoteOntology.getRevisionHeads()));
		send(remoteOntology.getRepository().getPeer(), msg);
	}
	
	@FromState("Started")
	@OnMessage(performative = "QueryRef")
	public WorkflowStateConstant getNewRevisions(final Json msg)
	{
		HGHandle ontologyHandle = Messages.fromJson(msg.at(CONTENT).at("ontology"));
		Set<HGHandle> revisionHeads = Messages.fromJson(msg.at(CONTENT).at("heads"));
		OntologyVersionState versionState = new OntologyVersionState(revisionHeads);
		VersionManager versionManager = new VersionManager(getThisPeer().getGraph(), "fixme-VHDBOntologyRepository");
		if (!versionManager.isVersioned(ontologyHandle))
			reply(msg, Performative.Failure, "The ontology does not exist or is not versioned.");
		else if (revisionHeads.isEmpty())
		{
			VersionedOntology vo = versionManager.versioned(ontologyHandle);
			OntologyVersionState.Delta delta = new OntologyVersionState.Delta();
			delta.heads = vo.heads();
			delta.roots = new HashSet<HGHandle>();
			delta.roots.add(vo.getRootRevision());
			delta.revisions = ActivityUtils.collectRevisions(vo, delta.roots, delta.heads); 
			reply(msg, Performative.InformRef, Json.object()
					.set("revisions", delta.revisions)
					.set("heads", delta.heads)
					.set("roots", delta.roots));
		}
		else
		{
			OntologyVersionState.Delta delta = versionState.findRevisionsSince(versionManager.versioned(ontologyHandle));
			reply(msg, Performative.InformRef, Json.object()
					.set("revisions", delta.revisions)
					.set("heads", delta.heads)
					.set("roots", delta.roots));
		}
		return WorkflowStateConstant.Completed;
	}

	@FromState("Started")
	@OnMessage(performative = "InformRef")
	public WorkflowStateConstant gotNewRevisions(final Json msg)
	{
		delta = new OntologyVersionState.Delta();
		delta.revisions = fromJson(msg.at(CONTENT).at("revisions"));
		delta.heads = fromJson(msg.at(CONTENT).at("heads"));
		delta.roots = fromJson(msg.at(CONTENT).at("roots"));
		//System.out.println("new revisions: " + newRevisions);
		return WorkflowStateConstant.Completed;
	}
	
	public Set<HGHandle> newRevisions() { return delta.revisions; }
	public Set<HGHandle> newRoots() { return delta.roots; }
	public Set<HGHandle> newHeads() { return delta.heads; }
	public OntologyVersionState.Delta delta() { return delta; }
}
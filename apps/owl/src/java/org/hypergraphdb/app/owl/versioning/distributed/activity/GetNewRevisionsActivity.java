package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import mjson.Json;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.OntologyVersionState;
import org.hypergraphdb.app.owl.versioning.Revision;
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
	private RemoteOntology remoteOntology;
	private Set<HGHandle> newRevisions;
	
	public static final String TYPENAME = "get-new-revisions";

	public GetNewRevisionsActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
	}

	public GetNewRevisionsActivity(HyperGraphPeer thisPeer, RemoteOntology remoteOntology)
	{
		super(thisPeer);
		this.remoteOntology = remoteOntology;
	}

	@Override
	public String getType()
	{
		return TYPENAME;
	}
	
	@Override
	public void initiate()
	{
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
			reply(msg, Performative.Failure, Json.object("error", "The ontology does not exist or is not versioned."));
		else if (revisionHeads.isEmpty())
		{
			VersionedOntology vo = versionManager.versioned(ontologyHandle);
			Set<HGHandle> heads = new HashSet<HGHandle>();
			for (Revision r : vo.heads()) heads.add(getThisPeer().getGraph().getHandle(r));
			reply(msg, 
				  Performative.InformRef, 
				  ActivityUtils.collectRevisions(vo, Collections.singleton(vo.getRootRevision()), heads));
		}
		else
			reply(msg, Performative.InformRef, versionState.findRevisionsSince(versionManager.versioned(ontologyHandle)));
		return WorkflowStateConstant.Completed;
	}

	@FromState("Started")
	@OnMessage(performative = "InformRef")
	public WorkflowStateConstant gotNewRevisions(final Json msg)
	{
		newRevisions = fromJson(msg.at(CONTENT));
		System.out.println("new revisions: " + newRevisions);
		return WorkflowStateConstant.Completed;
	}
	
	public Set<HGHandle> newRevisions() { return newRevisions; }
}
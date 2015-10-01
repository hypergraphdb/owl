package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer.OBJECTCONTEXT_REPOSITORY;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import mjson.Json;

import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.Activity;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;

/**
 * FindOntologyServersActivity.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 10, 2012
 * @deprecated No client-server model anymore, this activity is not needed
 */
public class FindOntologyServersActivity extends Activity
{
	public static int TIMEOUT_SECONDS = 30;
	public static final String TYPENAME = "Find-Ontology-Servers";

	OntologyDatabasePeer repository;
	Set<HGPeerIdentity> ontologyServers;
	Set<HGPeerIdentity> nonOntologyServers;

	private volatile int nrOfPeersInitiatorIsWaitingFor = Integer.MAX_VALUE;

	/**
	 * @param thisPeer
	 */
	public FindOntologyServersActivity(HyperGraphPeer thisPeer)
	{
		super(thisPeer);
		initialize();
	}

	/**
	 * @param thisPeer
	 * @param id
	 */
	public FindOntologyServersActivity(HyperGraphPeer thisPeer, UUID id)
	{
		super(thisPeer, id);
		initialize();
	}

	protected void initialize()
	{
		if (!getThisPeer().getObjectContext().containsKey(
				OBJECTCONTEXT_REPOSITORY))
		{
			System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
			throw new IllegalArgumentException(
					"Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
		}
		repository = (OntologyDatabasePeer) getThisPeer()
				.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
		ontologyServers = new HashSet<HGPeerIdentity>();
		nonOntologyServers = new HashSet<HGPeerIdentity>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.peer.workflow.Activity#initiate()
	 */
	@Override
	public void initiate()
	{
		this.getState().assign(WorkflowStateConstant.Started);
		Set<HGPeerIdentity> peers = getPeerInterface().getThisPeer()
				.getConnectedPeers();
		for (HGPeerIdentity peer : peers)
		{
			Json msg = createMessage(Performative.QueryIf, null);
			send(peer, msg);
		}
		nrOfPeersInitiatorIsWaitingFor = peers.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.peer.workflow.Activity#handleMessage(org.hypergraphdb
	 * .peer.Message)
	 */
	@Override
	public void handleMessage(Json msg)
	{
		Performative p = Performative.toConstant(msg.at(Messages.PERFORMATIVE)
				.asString());
		if (p == Performative.QueryIf)
		{
			targetHandleQueryIf(msg);
		}
		else if (p == Performative.Confirm)
		{
			initiatorHandleConfirm(msg);
		}
		else if (p == Performative.Disconfirm)
		{
			initiatorHandleDisconfirm(msg);
		}
		else if (p == Performative.NotUnderstood)
		{
			initiatorHandleNotUnderstood(msg);
		}
		else if (p == Performative.Failure)
		{
			System.err.println("Failure at find ontology server: " + msg);
			getState().assign(WorkflowStateConstant.Failed);
		}
		else
		{
			throw new RuntimeException(new VOWLException(
					"Performative not understood: " + p));
		}
	}

	public void targetHandleQueryIf(Json msg)
	{
		Performative response = Performative.Disconfirm;
//		if (repository.isOntologyServer())
//		{
//			response = Performative.Confirm;
//		}
//		else
//		{
//			response = Performative.Disconfirm;
//		}
		Future<Boolean> replyFuture = reply(msg, response, null);
		try
		{
			replyFuture.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			this.getState().assign(WorkflowStateConstant.Completed);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			this.getState().assign(WorkflowStateConstant.Failed);
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
			this.getState().assign(WorkflowStateConstant.Failed);
		}
		catch (TimeoutException e)
		{
			System.out.println("Was unable to reply within timeout: "
					+ TIMEOUT_SECONDS);
			this.getState().assign(WorkflowStateConstant.Failed);
		}
	}

	protected void initiatorReceivedTargetResponse()
	{
		nrOfPeersInitiatorIsWaitingFor--;
		if (nrOfPeersInitiatorIsWaitingFor == 0)
		{
			this.getState().assign(WorkflowStateConstant.Completed);
		}
	}

	public void initiatorHandleConfirm(Json msg)
	{
		Object sender = Messages.getSender(msg);
		System.out.println("Found ontolgy server: " + sender);
		ontologyServers.add(getThisPeer().getIdentity(sender));
		initiatorReceivedTargetResponse();
	}

	public void initiatorHandleDisconfirm(Json msg)
	{
		Object sender = Messages.getSender(msg);
		nonOntologyServers.add(getThisPeer().getIdentity(sender));
		initiatorReceivedTargetResponse();
	}

	public void initiatorHandleNotUnderstood(Json msg)
	{
		// System.out.println(Messages.getSender(msg));
		Object sender = Messages.getSender(msg);
		System.err
				.println("There is a client on the network that does not understand FindOntologyServers :"
						+ sender);
		initiatorReceivedTargetResponse();
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

	public Set<HGPeerIdentity> getOntologyServers()
	{
		return ontologyServers;
	}

	public Set<HGPeerIdentity> getNonOntologyServers()
	{
		return nonOntologyServers;
	}
}
package org.hypergraphdb.app.owl.versioning.distributed;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetNewRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetRemoteOntologyChangesetActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetRemoteOntologyRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.hypergraphdb.util.Constant;
import org.hypergraphdb.util.Ref;

/**
 * OntologyDatabasePeer extends the {@link OntologyDatabase} by adding 
 * P2P operations for {@link VersionedOntology}'s.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County), Borislav Iordanov
 * @created Feb 16, 2012
 */
public class OntologyDatabasePeer extends OntologyDatabase
{
	private static boolean DBG = false;
	public static final String OBJECTCONTEXT_REPOSITORY = "Repository";

	private Ref<HyperGraphPeer> peer;

	public OntologyDatabasePeer(String location, Ref<HyperGraphPeer> peer)
	{
		super(location);
		this.peer = peer;
	}
	
	public OntologyDatabasePeer(HyperGraphPeer peer)
	{
		super(peer.getGraph().getLocation());
		this.peer = new Constant<HyperGraphPeer>(peer);
	}
	
	public OntologyDatabasePeer(String location, String peerConnectionString)
	{
		super(location);
		peer = new Constant<HyperGraphPeer>(ImplUtils.peer(peerConnectionString, location));
		configurePeer();
	}	
	private void configurePeer()
	{
		peer.get().getObjectContext().put(OBJECTCONTEXT_REPOSITORY, this);
		if (DBG)
		{
			peer.get().addPeerPresenceListener(new PeerPresenceListener()
			{

				@Override
				public void peerLeft(HGPeerIdentity peer)
				{
					System.out.println("Peer left " + peer);
				}

				@Override
				public void peerJoined(HGPeerIdentity peer)
				{
					System.out.println("Peer Joined " + peer);
				}
			});
		}
	}

	/**
	 * Starts networking using the configured userName and password.
	 * 
	 * @return
	 */
	public boolean startNetworking()
	{
		if (peer.get().getPeerInterface() != null && peer.get().getPeerInterface().isConnected())
			throw new IllegalStateException("Peer already connected.");
		startNetworkingInternal();
		return peer.get().getPeerInterface().isConnected();
	}

	private boolean startNetworkingInternal()
	{
		// this will block
		Future<Boolean> f = peer.get().start();
		boolean success = false;
		try
		{
			success = f.get(35, TimeUnit.SECONDS);
			if (!success)
			{
				Exception e = peer.get().getStartupFailedException();
				System.out.println("FAILED TO START PEER: ");
				e.printStackTrace();
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			e.printStackTrace();
		}
		catch (TimeoutException e)
		{
			e.printStackTrace();
		}
		// Bootstrap Push:
		// Important: to cause static initialization.
		if (PushActivity.ReceivingInitial == null)
		{
		}
		
		peer.get().getActivityManager().registerActivityType(VersionUpdateActivity.TYPENAME, VersionUpdateActivity.initializedClass());
		peer.get().getActivityManager().registerActivityType(GetNewRevisionsActivity.TYPENAME, GetNewRevisionsActivity.class);		
//		peer.getActivityManager().registerActivityType(PushActivity.TYPENAME, PushActivity.class);
//		peer.getActivityManager().registerActivityType(PullActivity.TYPENAME, PullActivity.class);
		peer.get().getActivityManager().registerActivityType(BrowseRepositoryActivity.TYPENAME, BrowseRepositoryActivity.class);
//		peer.get().getActivityManager().registerActivityType(GetRemoteOntologyRevisionsActivity.TYPENAME,
//				GetRemoteOntologyRevisionsActivity.class);
//		peer.get().getActivityManager().registerActivityType(GetRemoteOntologyChangesetActivity.TYPENAME,
//				GetRemoteOntologyChangesetActivity.class);
		return success;
	}

	public boolean isNetworking()
	{
		return peer.get().getPeerInterface().isConnected();
	}

	public void stopNetworking()
	{
		peer.get().stop();
	}

	public HyperGraphPeer getPeer()
	{
		return peer.get();
	}

	/**
	 * Returns user@server for a given PeerIdentity.
	 * 
	 * @param peerId
	 * @return
	 */
	public String getPeerUserId(HGPeerIdentity peerId)
	{
		String s = "" + peer.get().getNetworkTarget(peerId);
		String[] parts = s.split("/");
		return parts[0];
	}

	public void printPeerInfo()
	{
		System.out.println("peer.getPeerInterface().isConnected() " + peer.get().getPeerInterface().isConnected());
		System.out.println("peer.peer.getConnectedPeers().size() " + peer.get().getConnectedPeers().size());
		Set<HGPeerIdentity> others = peer.get().getConnectedPeers();
		for (HGPeerIdentity pi : others)
		{
			System.out.println(" " + pi.getHostname());
			System.out.println(" " + pi.getIpAddress());
			System.out.println(" " + pi.getGraphLocation());
			System.out.println("NetworkTarget: " + peer.get().getNetworkTarget(pi));
		}
		System.out.println(peer.get().getIdentity());
	}

	/**
	 * 
	 * @param remote
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public BrowseRepositoryActivity browseRemote(HGPeerIdentity remote)
	{
		BrowseRepositoryActivity activity = new BrowseRepositoryActivity(peer.get(), remote);
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;
	}
	
	public RemoteRepository remoteRepo(final HGPeerIdentity id)
	{
		final HyperGraph graph = getHyperGraph();
		return graph.getTransactionManager().ensureTransaction(new Callable<RemoteRepository>(){
			public RemoteRepository call()
			{
				RemoteRepository remoteRepo = graph.getOne(hg.and(hg.type(RemoteRepository.class), hg.eq("peer", id)));
				if (remoteRepo != null)
					return remoteRepo;
				remoteRepo = new RemoteRepository();
				remoteRepo.setPeer(id);
				graph.add(remoteRepo);
				return remoteRepo;
			}
		});
	}
	
	public RemoteOntology remoteOnto(final HGHandle ontologyHandle, final RemoteRepository remoteRepository)
	{
		final HyperGraph graph = getHyperGraph();
		return graph.getTransactionManager().ensureTransaction(new Callable<RemoteOntology>(){
			public RemoteOntology call()
			{
				RemoteOntology remoteOnto = graph.getOne(hg.and(hg.type(RemoteOntology.class), 
																hg.eq("ontologyHandle", ontologyHandle),
																hg.eq("repository", remoteRepository)));
				if (remoteOnto != null)
					return remoteOnto;
				remoteOnto = new RemoteOntology();
				remoteOnto.setOntologyHandle(ontologyHandle);
				remoteOnto.setRepository(remoteRepository);
				graph.add(remoteOnto);
				return remoteOnto;
			}
		});		
	}
	
	/**
	 * <p>
	 * Clone an ontology with its full revision graph from another peer. An asynchronous
	 * activity is created and returned. The activity will most likely not have completed
	 * upon return of this method. To wait for it to complete call <code>getFuture().get()</code>.
	 * </p> 
	 * 
	 * @param ontologyUUID
	 * @param remote
	 * @return
	 */
	public VersionUpdateActivity clone(HGHandle ontologyHandle, HGPeerIdentity otherPeer)
	{
		if (DBG)
			System.out.println("Pulling versioned onto: " + ontologyHandle);
		final HyperGraph graph = getHyperGraph();
		if (getHyperGraph().get(ontologyHandle) != null)
			throw new IllegalArgumentException("Ontology " + 
											   getHyperGraph().get(ontologyHandle) + 
											   " already in local repository.");
		RemoteOntology remoteOnto = remoteOnto(ontologyHandle, remoteRepo(otherPeer)); 
		VersionUpdateActivity activity = new VersionUpdateActivity(peer.get())
			.remoteOntology(graph.getHandle(remoteOnto)).action(VersionUpdateActivity.ActionType.clone.name());
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;
	}

	public VersionUpdateActivity publish(HGHandle ontologyHandle, HGPeerIdentity otherPeer)
	{
		if (DBG)
			System.out.println("Publishing ontology: " + ontologyHandle + " to " + otherPeer);
		final HyperGraph graph = getHyperGraph();
		RemoteOntology remoteOnto = remoteOnto(ontologyHandle, remoteRepo(otherPeer)); 
		VersionUpdateActivity activity = new VersionUpdateActivity(peer.get())
			.remoteOntology(graph.getHandle(remoteOnto))
			.action("publish");
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;		
	}
	
	/**
	 * Pull onto an existing local distributed ontology.
	 * 
	 * @param dOnto
	 * @param remote
	 * @return
	 */
	public VersionUpdateActivity pull(VersionedOntology ontology, HGPeerIdentity remote)
	{
		if (DBG)
			System.out.println("Pulling distributed onto: " + ontology);
		final HyperGraph graph = getHyperGraph();		
		RemoteOntology remoteOnto = remoteOnto(ontology.getOntology(), remoteRepo(remote));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer.get())
											.remoteOntology(graph.getHandle(remoteOnto))
											.action("pull");		
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;
	}
	
	public VersionUpdateActivity push(HGHandle ontologyHandle, HGPeerIdentity otherPeer)
	{
		final HyperGraph graph = getHyperGraph();		
		RemoteOntology remoteOnto = remoteOnto(ontologyHandle, remoteRepo(otherPeer));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer.get())
				.remoteOntology(graph.getHandle(remoteOnto)).action("push");		
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;
	}

	public void printIdentity()
	{
		HGPeerIdentity me = getPeer().getIdentity();
		System.out.println("HGPeer   : " + me);
		PeerInterface pif = getPeer().getPeerInterface();
		if (pif instanceof XMPPPeerInterface)
		{
			XMPPPeerInterface xpif = (XMPPPeerInterface) pif;
			// System.out.println("Network identity: " +
			// getPeer().getNetworkTarget(me));
			System.out.println("P2P User   : " + xpif.getUser());
			System.out.println("P2P Server : " + xpif.getServerName());
			System.out.println("P2P Port   : " + xpif.getPort());
		}
	}
}
package org.hypergraphdb.app.owl.versioning.distributed;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetRemoteOntologyChangesetActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetRemoteOntologyRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PullActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.Constant;
import org.hypergraphdb.util.Ref;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VDHGDBOntologyRepository extends versioning by Peer2Peer and Client/Server
 * sharing of VersionedOntologies.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 16, 2012
 */
public class VDHGDBOntologyRepository extends HGDBOntologyRepository
{
	private static boolean DBG = false;
	public static final String OBJECTCONTEXT_REPOSITORY = "Repository";
	public static final String CONFIG_KEY_SERVER = "OntologyServer";

	/**
	 * Expected to be UTF-8 encoded. Located by this.getClass().getResource()
	 */
	//private static String PEER_CONFIGURATION_FILE = "/org/hypergraphdb/app/owl/versioning/distributed/VDHGDBConfig.p2p";

	Ref<HyperGraphPeer> peer;

	public VDHGDBOntologyRepository(String location, Ref<HyperGraphPeer> peer)
	{
		super(location);
		this.peer = peer;
	}
	
	public VDHGDBOntologyRepository(HyperGraphPeer peer)
	{
		super(peer.getGraph().getLocation());
		this.peer = new Constant<HyperGraphPeer>(peer);
	}
	
	public VDHGDBOntologyRepository(String location, String peerConnectionString)
	{
		super(location);
		peer = new Constant<HyperGraphPeer>(ImplUtils.peer(peerConnectionString, location));
		configurePeer();
	}

	public boolean isDistributed(HGDBOntology o)
	{
		return getDistributedOntology(o) != null;
	}

	public Set<DistributedOntology> getDistributedOntologies()
	{
		List<DistributedOntology> l = getHyperGraph().getAll(hg.typePlus(DistributedOntology.class));
		HashSet<DistributedOntology> s = new HashSet<DistributedOntology>(l);
		if (s.size() != l.size())
			throw new IllegalStateException("Duplicates.");
		return s;
	}

	/**
	 * Returns the distributed Ontology or null.
	 * 
	 * @param onto
	 * @return the versioned ontology or null, if not found.
	 */
	public DistributedOntology getDistributedOntology(final OWLOntology onto)
	{
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<DistributedOntology>()
		{
			public DistributedOntology call()
			{
				// TODO maybe not loaded here? -> NPE; Check out callers
				HGHandle ontoHandle = getHyperGraph().getHandle(onto);
				if (ontoHandle == null)
				{
					if (DBG)
						System.out.println("NULL for onto " + onto);
					return null;
				}
				else
				{
					HGPersistentHandle ontoPHandle = ontoHandle.getPersistent();
					for (DistributedOntology distO : getDistributedOntologies())
					{
						if (distO.getVersionedOntology().revision().versioned().equals(ontoPHandle))
						{
							return distO;
						}
					}
					return null;
				}
			}
		}, HGTransactionConfig.READONLY);
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
		;
		peer.get().getActivityManager().registerActivityType(VersionUpdateActivity.TYPENAME, VersionUpdateActivity.initializedClass());		
//		peer.getActivityManager().registerActivityType(PushActivity.TYPENAME, PushActivity.class);
//		peer.getActivityManager().registerActivityType(PullActivity.TYPENAME, PullActivity.class);
		peer.get().getActivityManager().registerActivityType(BrowseRepositoryActivity.TYPENAME, BrowseRepositoryActivity.class);
		peer.get().getActivityManager().registerActivityType(GetRemoteOntologyRevisionsActivity.TYPENAME,
				GetRemoteOntologyRevisionsActivity.class);
		peer.get().getActivityManager().registerActivityType(GetRemoteOntologyChangesetActivity.TYPENAME,
				GetRemoteOntologyChangesetActivity.class);
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

	/**
	 * Pull onto an existing local distributed ontology.
	 * 
	 * @param dOnto
	 * @param remote
	 * @return
	 */
	public PullActivity pull(VersionedOntology ontology, HGPeerIdentity remote)
	{
		if (DBG)
			System.out.println("Pulling distributed onto: " + ontology);
		PullActivity activity = new PullActivity(peer.get(), ontology, remote);
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
	public VersionUpdateActivity pullNew(HGPersistentHandle ontologyHandle, HGPeerIdentity otherPeer)
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
			.remoteOntology(graph.getHandle(remoteOnto)).action("pull");
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;
	}

	public PushActivity push(DistributedOntology dvo, HGPeerIdentity remote)
	{
		// 1) Target available
		// 2) Target has vo? No: Push everything vo + all changesets using
		// pushNew
		// 3) Can push == target master head revision is older and equal to one
		// revision in my
		// branches history
		// 4) find last Changeset that is not in target.
		// 5) push each changeset/Revision pair after that. This is the Delta.
		// 6) remote will receive one changeset/Revision, then apply those
		// changes within one transaction
		//
		//
		// System.out.println("Pushing versioned onto: " +
		// vo.getWorkingSetData().getOntologyID());
		PushActivity activity = new PushActivity(peer.get(), dvo, remote);
		peer.get().getActivityManager().initiateActivity(activity);
		return activity;
	}

	public void printDistributedOntologies()
	{
		Set<DistributedOntology> dontos = getDistributedOntologies();
		for (DistributedOntology donto : dontos)
		{
			System.out.println(donto.toString());
		}
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
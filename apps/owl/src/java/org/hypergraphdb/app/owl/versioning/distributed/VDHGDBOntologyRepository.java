package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hypergraphdb.peer.Structs.getPart;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.RevisionID;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator;
import org.hypergraphdb.app.owl.versioning.VersionedOntologyComparator.VersionedOntologyComparisonResult;
import org.hypergraphdb.app.owl.versioning.distributed.activity.*;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.peer.workflow.Activity;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.hypergraphdb.peer.xmpp.XMPPPeerInterface;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * VDHGDBOntologyRepository extends versioning by Peer2Peer and Client/Server sharing of VersionedOntologies.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 16, 2012
 */
public class VDHGDBOntologyRepository extends VHGDBOntologyRepository {

	public static final String OBJECTCONTEXT_REPOSITORY = "Repository";
	public static final String CONFIG_KEY_SERVER = "OntologyServer";

	/**
	 * Expected to be UTF-8 encoded.
	 * Located by this.getClass().getResource()
	 */
	public static String PEER_CONFIGURATION_FILE = "VDHGDBConfig.p2p";

	private HGDBOntologyManager ontologyManager;
	private boolean isOntologyServer = false;
	
	HyperGraphPeer peer;
	
	public static VDHGDBOntologyRepository getInstance() {
		if (!hasInstance()) {
			String hypergraphDBLocation = getHypergraphDBLocation();
			System.out.println("VDHGDB REPOSITORY AT: " + hypergraphDBLocation);
			VDHGDBOntologyRepository instance = new VDHGDBOntologyRepository(hypergraphDBLocation);
			//instance.setOntologyManager(manager);
			setInstance(instance);
			//instance.initializeActivities();
			//((VDHGDBOntologyRepository)getInstance()).initializeActivities();
		}
		HGDBOntologyRepository instance = HGDBOntologyRepository.getInstance(); 
		if (!(instance instanceof VDHGDBOntologyRepository)) throw new IllegalStateException("Instance requested not Versioned Repository type.: " + instance);
		return (VDHGDBOntologyRepository)instance;
	}	
	
	private VDHGDBOntologyRepository(String location) {
		super(location);
	}
	
	public HGDBOntologyManager getOntologyManager() {
		//if (ontologyManager == null) throw new IllegalArgumentException();
		return ontologyManager;
	}

	public void setOntologyManager(HGDBOntologyManager manager) {
		if (manager == null) throw new NullPointerException("Manager must not be null");
		ontologyManager = manager;
	}

	/**
	 * 
	 * @param ontology
	 * @return
	 */
	public HGPersistentHandle getOntologyUUID(HGDBOntology ontology) {
		return getHyperGraph().getHandle(ontology).getPersistent();
	}
	
	public boolean isDistributed(HGDBOntology o) {
		return getDistributedOntology(o) != null;
	}

	public Set<DistributedOntology> getDistributedOntologies() {
		 List<DistributedOntology> l = getHyperGraph().getAll(hg.typePlus(DistributedOntology.class));
		 HashSet<DistributedOntology> s = new HashSet<DistributedOntology>(l);
		 if (s.size() != l.size()) throw new IllegalStateException("Duplicates.");
		 return s;
	}

	/**
	 * Returns the distributed Ontology or null.
	 * @param onto
	 * @return the versioned ontology or null, if not found.
	 */
	public DistributedOntology getDistributedOntology(final OWLOntology onto) {
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<DistributedOntology>() {
			public DistributedOntology call() {
				//TODO maybe not loaded here? -> NPE; Check out callers
				HGHandle ontoHandle = getHyperGraph().getHandle(onto);
				if (ontoHandle == null) {
					if (DBG) System.out.println("NULL for onto " + onto);
					return null;
				} else {
					HGPersistentHandle ontoPHandle = ontoHandle.getPersistent();
					for (DistributedOntology distO : getDistributedOntologies()) {
						if (distO.getVersionedOntology().getHeadRevision().getOntologyUUID().equals(ontoPHandle)) {
							return distO;
						}
					}
					return null;
				}
			}}, HGTransactionConfig.READONLY);
	}
	
	
	private void createAndConfigurePeer(Map<String, Object> peerConfig) {
		peer = new HyperGraphPeer(peerConfig, getHyperGraph());
		peer.getActivityManager();
		peer.getObjectContext().put(OBJECTCONTEXT_REPOSITORY, this);
		setOntologyServer(Boolean.parseBoolean((String) peerConfig.get(CONFIG_KEY_SERVER)));
		
		if (DBG) {
			peer.addPeerPresenceListener(new PeerPresenceListener() {
			
				@Override
				public void peerLeft(HGPeerIdentity peer) {
					System.out.println("Peer left: " + peer);
				}
				
				@Override
				public void peerJoined(HGPeerIdentity peer) {
					System.out.println("Peer Joined" + peer);
				}
			});
		}
	}
	
	/**
	 * 
	 * @param parseBoolean
	 */
	public void setOntologyServer(boolean isOntologyServer) {
		this.isOntologyServer = isOntologyServer;
		if (DBG && isOntologyServer) {
			System.out.println("Repository configured as OntologyServer instance");
		}
	}
	
	public boolean isOntologyServer() {
		return isOntologyServer;
	}

	/** 
	 * 
	 * @return a default configuration or the configuration found in the configuration file.
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	private Map<String,Object> loadPeerConfig() {
		URL peerConfigFile = getClass().getResource(PEER_CONFIGURATION_FILE);
		Map<String,Object> peerConfig;
		if (peerConfigFile != null) {
			JSONReader reader = new JSONReader();
			System.out.println("LOADING PEER CONFIGURATION FROM: " + PEER_CONFIGURATION_FILE);
			try {
				String cur, configAsString = "";
				BufferedReader brr = new BufferedReader(new InputStreamReader(peerConfigFile.openStream(), "UTF-8"));
				while ((cur = brr.readLine()) != null) {
					configAsString = configAsString + cur + "\n"; 
				}
				peerConfig = (Map<String, Object>)getPart(reader.read(configAsString));
			} catch (IOException e) {
				throw new RuntimeException("Loading configuration failed: " + PEER_CONFIGURATION_FILE, e);
			}
//			peerConfig = HyperGraphPeer.loadConfiguration(new File (peerConfigFile.getFile()));
		} else {
			System.out.println("USING TEST PEER CONFIGURATION (W203-003.miamidade.gov)");
			peerConfig = new HashMap<String, Object>();
			List<Map<?,?>> bootstrapConfig = new ArrayList<Map<?,?>>();
			Map<String,Object> bootstrapConfigSub1 = new HashMap<String, Object>();
			Map<String,Object> bootstrapConfigSub2 = new HashMap<String, Object>();
			Map<String,Object> interfaceConfig = new HashMap<String, Object>();
			peerConfig.put("interfaceType", "org.hypergraphdb.peer.xmpp.XMPPPeerInterface");
			peerConfig.put("peerName", "VDHGDBOntologyRepository");
			peerConfig.put("interfaceConfig", interfaceConfig);
			peerConfig.put("bootstrap", bootstrapConfig);
			bootstrapConfig.add(bootstrapConfigSub1);
			bootstrapConfig.add(bootstrapConfigSub2);
			bootstrapConfigSub1.put("class", "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap");
			bootstrapConfigSub1.put("config", new HashMap<String, Object>());
			bootstrapConfigSub2.put("class", "org.hypergraphdb.peer.bootstrap.CACTBootstrap");
			bootstrapConfigSub2.put("config", new HashMap<String, Object>());
			//
			interfaceConfig.put("user", "hg1");
			interfaceConfig.put("password", "hg1");
			interfaceConfig.put("serverUrl", "W203-003.miamidade.gov");
			//peers from roster! interfaceConfig.put("room", "ontologyCM@conference.127.0.0.1");
			interfaceConfig.put("autoRegister", true);
			interfaceConfig.put("ignoreRoster", false);
			interfaceConfig.put("fileTransferThreshold", 100 * 1024); //default.
		}
		return peerConfig;
	}
	
	public boolean startNetworking(Map<String, Object> configuration) {
		if (peer != null && peer.getPeerInterface().isConnected()) {
			throw new IllegalStateException("Peer is currently connected. Disconnect first.");
		}
		createAndConfigurePeer(configuration);
		return startNetworkingInternal();
	}	
	
	public boolean startNetworking(String userName, String password, String serverUrl) {
		if (peer != null && peer.getPeerInterface().isConnected()) {
			throw new IllegalStateException("Peer is currently connected. Disconnect first.");
		}
		Map<String, Object> config = loadPeerConfig();
		@SuppressWarnings("unchecked")
		Map<String, Object> interFaceConfig = (Map<String, Object>)config.get("interfaceConfig");
		interFaceConfig.put("user", userName);
		interFaceConfig.put("password", password);
		interFaceConfig.put("serverUrl", serverUrl);
		createAndConfigurePeer(config);
		return startNetworkingInternal();
	}

	/**
	 * Starts networking using the configured userName and password.
	 * @return
	 */
	public boolean startNetworking() {
		if (peer != null && peer.getPeerInterface().isConnected()) {
			throw new IllegalStateException("Peer is currently connected. Disconnect first.");
		}
		//DBG_ClearDates();
		createAndConfigurePeer(loadPeerConfig());
		return startNetworkingInternal();
	}

	private boolean startNetworkingInternal() {
		//this will block
		Future<Boolean> f = peer.start();
		boolean success = false; 
		try {
			success = f.get(35, TimeUnit.SECONDS);
			if (!success) {
				Exception  e = peer.getStartupFailedException();
				System.out.println("FAILED TO START PEER: ");
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} 
		// Bootstrap Push:
		//Important: to cause static initialization.
		if (PushActivity.ReceivingInitial == null) {};
		peer.getActivityManager().registerActivityType(PushActivity.TYPENAME, PushActivity.class);
		peer.getActivityManager().registerActivityType(PullActivity.TYPENAME, PullActivity.class);
		peer.getActivityManager().registerActivityType(BrowseRepositoryActivity.TYPENAME, BrowseRepositoryActivity.class);
		peer.getActivityManager().registerActivityType(GetRemoteOntologyRevisionsActivity.TYPENAME, GetRemoteOntologyRevisionsActivity.class);
		peer.getActivityManager().registerActivityType(GetRemoteOntologyChangesetActivity.TYPENAME, GetRemoteOntologyChangesetActivity.class);
		peer.getActivityManager().registerActivityType(FindOntologyServersActivity.TYPENAME, FindOntologyServersActivity.class);
		return success;
	}

	public boolean isNetworking() {
		return peer.getPeerInterface().isConnected();
	}

	public void stopNetworking() {
		peer.stop();
	}
	public HyperGraphPeer getPeer() {
		return peer;
	}
	
	/**
	 * Returns all peers that respond to a multicast that they are configured as server.
	 * @return
	 */
	public Set<HGPeerIdentity> getOntologyServers() {
		FindOntologyServersActivity activity = new FindOntologyServersActivity(peer);
		peer.getActivityManager().initiateActivity(activity);
		Future<ActivityResult> future = activity.getFuture();
		try {
			future.get(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return activity.getOntologyServers();
	}
	
	public Set<HGPeerIdentity> getPeers() {
		return peer.getConnectedPeers();
	}
	
	/**
	 * Returns user@server for a given PeerIdentity.
	 * @param peerId
	 * @return
	 */
	public String getPeerUserId(HGPeerIdentity peerId) {
		String s = "" + peer.getNetworkTarget(peerId);
		String [] parts = s.split("/");
		return parts[0];
	}
	
	Activity a = null;
	public void printPeerInfo() {
		System.out.println("peer.getPeerInterface().isConnected() " + peer.getPeerInterface().isConnected());
		//peer.getPeerInterface().newFilterActivity(evaluator)
		System.out.println("peer.peer.getConnectedPeers().size() " + peer.getConnectedPeers().size());
		//if (a == null) {
		//	a = new AffirmIdentity(peer, UUID.randomUUID());
		//	peer.getActivityManager().initiateActivity(a);
		//	peer.getActivityManager().start();
		//}
		//peer.updateNetworkProperties();
		//System.out.println("Done yet?: " + a.getFuture().isDone());
		// Not working, always empty:
		Set<HGPeerIdentity> others = peer.getConnectedPeers();
		for (HGPeerIdentity pi : others) {
			System.out.println(" " + pi.getHostname());
			System.out.println(" " + pi.getIpAddress());
			System.out.println(" " + pi.getGraphLocation());
			System.out.println("NetworkTarget: " + peer.getNetworkTarget(pi));
		}
		System.out.println(peer.getIdentity());
		//peer
	}
	
//	public Future<RemoteRepositoryActionResult> fetch(VersionedOntology vo, HGPeerIdentity remote) {
//		return null;
//	}

	/**
	 * 
	 * @param remote
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public BrowseRepositoryActivity browseRemote(HGPeerIdentity remote) {
		BrowseRepositoryActivity activity = new BrowseRepositoryActivity(peer, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}

	public GetRemoteOntologyRevisionsActivity getRemoteRevisions(HGPersistentHandle ontologyUUID, HGPeerIdentity remote) {
		GetRemoteOntologyRevisionsActivity activity = new GetRemoteOntologyRevisionsActivity(peer, remote, ontologyUUID);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}

	public GetRemoteOntologyChangesetActivity getRemoteChangeset(HGPersistentHandle ontologyUUID, HGPeerIdentity remote, RevisionID revision) {
		GetRemoteOntologyChangesetActivity activity = new GetRemoteOntologyChangesetActivity(peer, remote, ontologyUUID, revision);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}

	/**
	 * Pull onto an existing local distributed ontology.
	 * @param dOnto
	 * @param remote
	 * @return
	 */
	public PullActivity pull(DistributedOntology dOnto, HGPeerIdentity remote) {
		if (DBG) System.out.println("Pulling distributed onto: " + dOnto);
		PullActivity activity = new PullActivity(peer, dOnto, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}

	public PullActivity pullUntilRevision(DistributedOntology dOnto, HGPeerIdentity remote, RevisionID revision) {
		if (DBG) System.out.println("Pulling distributed onto: " + dOnto);
		PullActivity activity = new PullActivity(peer, dOnto, remote, revision);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}

	public PullActivity pullNewUntilRevision(RevisionID revision, HGPeerIdentity remote) {
		if (DBG) System.out.println("Pulling distributed onto until: " + revision);
		PullActivity activity = new PullActivity(peer, revision, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}

	/**
	 * Pull NEW from remote. UUID allow to pull vo's not yet available.
	 * @param ontologyUUID
	 * @param remote
	 * @return
	 */
	public PullActivity pullNew(HGPersistentHandle ontologyUUID, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: no pull
		// 3) Can pull == local master head revision is older and equal to one revision in remote 
		//	  branches history 
		// 4) 
		// 
		if (DBG) System.out.println("Pulling versioned onto: " + ontologyUUID);
		PullActivity activity = new PullActivity(peer, ontologyUUID, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}
	
	public PushActivity push(DistributedOntology dvo, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: Push everything vo + all changesets using pushNew
		// 3) Can push == target master head revision is older and equal to one revision in my 
		//	  branches history 
		// 4) find last Changeset that is not in target.
		// 5) push each changeset/Revision pair after that. This is the Delta.
		// 6) remote will receive one changeset/Revision, then apply those changes within one transaction
		//  
		//
		//System.out.println("Pushing versioned onto: " + vo.getWorkingSetData().getOntologyID());
		PushActivity activity = new PushActivity(peer, dvo, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}
	
	/**
	 * Get a version controlled ontology by UUID.
	 * @param ontologyUUID the ontology data uuid
	 * @return
	 */
	public VersionedOntology getVersionControlledOntology(final HGPersistentHandle ontologyUUID) {
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<VersionedOntology>() {
			public VersionedOntology call() {
				//TODO maybe not loaded here? -> NPE; Check out callers
				HGDBOntology onto = getHyperGraph().get(ontologyUUID);
				if (onto == null) {
					System.out.println("No ontology at " + ontologyUUID);
					return null;
				} else {
					return getVersionControlledOntology(onto);
				}
			}}, HGTransactionConfig.READONLY);
	}

	//public enum RemoteRepositoryActionResult {SUCCESS, DENIED_LOCAL_OUT_OF_DATE, DENIED_WERE_EQUAL, DENIED_REMOTE_OUT_OF_DATE}; 
	
	public ClientCentralizedOntology shareRemoteInServerMode(final VersionedOntology vo, final HGPeerIdentity remote) {
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<ClientCentralizedOntology>() {
			public ClientCentralizedOntology call() {
				//1. push in server mode to remote
				//2. create and add clien
				HGHandle voHandle = getHyperGraph().getHandle(vo);
				if (voHandle == null) throw new IllegalStateException("VersionedOntology to push is not stored in graph. Was: " + vo);
				if (isDistributed(vo.getWorkingSetData())) throw new VOWLOntologyAlreadySharedException();
				ClientCentralizedOntology newCdo = new ClientCentralizedOntology(voHandle);
				newCdo.setHyperGraph(getHyperGraph());
				PushActivity pushActivity = push(newCdo, remote);
				//Block till conversation over.
				try {
					ActivityResult result = pushActivity.getFuture().get();
					if (result.getException() != null) {
						throw result.getException();
					}
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
				if (pushActivity.getState().isCompleted() &&
						pushActivity.isSourceDistributedExistsOnTarget()) {
					newCdo.setServerPeer(remote);
					getHyperGraph().add(newCdo);
				}
				return newCdo;
			}}, HGTransactionConfig.DEFAULT);
	}
	
	public ServerCentralizedOntology shareLocalInServerMode(final VersionedOntology vo) {
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<ServerCentralizedOntology>() {
			public ServerCentralizedOntology call() {
				//TODO maybe not loaded here? -> NPE; Check out callers
				if(isDistributed(vo.getWorkingSetData())) {
					throw new VOWLOntologyAlreadySharedException();
				}
				HGHandle voHandle = getHyperGraph().getHandle(vo);
				if (voHandle == null) {
					System.out.println("Versionedontology handle not found in repo" + vo);
					return null;
				} else {
					ServerCentralizedOntology pdvo = new ServerCentralizedOntology(voHandle);
					getHyperGraph().add(pdvo);
					return pdvo;
				}
			}}, HGTransactionConfig.DEFAULT);
	}
	
	public PeerDistributedOntology shareLocalInPeerMode(final VersionedOntology vo) {
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<PeerDistributedOntology>() {
			public PeerDistributedOntology call() {
				//TODO maybe not loaded here? -> NPE; Check out callers
				if(isDistributed(vo.getWorkingSetData())) {
					throw new VOWLOntologyAlreadySharedException();
				}
				HGHandle voHandle = getHyperGraph().getHandle(vo);
				if (voHandle == null) {
					System.out.println("Versionedontology handle not found in repo" + vo);
					return null;
				} else {
					PeerDistributedOntology pdvo = new PeerDistributedOntology(voHandle);
					getHyperGraph().add(pdvo);
					return pdvo;
				}
			}}, HGTransactionConfig.DEFAULT);
	}
	
	/**
	 * Cancels the sharing of the Versioned Ontology by removing its decorator, the DistributedOntology object from the local repository.
	 * The versioned ontology and it's full history will remain unchanged and can 
	 * easily be shared again.
	 * 
	 * @param dvo
	 */
	public void cancelSharing(final DistributedOntology dvo) {
		getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				HGHandle dvoHandle = getHyperGraph().getHandle(dvo);
				getHyperGraph().remove(dvoHandle);
				return null;
			}}, HGTransactionConfig.DEFAULT);
	}
	
	/**
	 * 
	 * @param cco
	 * @return null, if server not accessible, ontology not on server.
	 */
	public VersionedOntologyComparisonResult compareOntologyToRemote(DistributedOntology dOnto, HGPeerIdentity peer) {
		List<Revision> local = dOnto.getVersionedOntology().getRevisions();
		List<Revision> remote;
		HGPersistentHandle uuid = getOntologyUUID(dOnto.getWorkingSetData());
		GetRemoteOntologyRevisionsActivity rra = getRemoteRevisions(uuid, peer);
		try {
			ActivityResult ar = rra.getFuture().get(30, TimeUnit.SECONDS);
			if (ar.getException() != null) throw ar.getException();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		remote = rra.getRemoteOntologyRevisions();
		VersionedOntologyComparator c = new VersionedOntologyComparator();
		return c.compare(local, remote);
	}
	
	/**
	 * Deletes sharing information, if distributed and delegatest to superclass.
	 */
	public boolean deleteOntology(final OWLOntologyID ontologyId) {
		return getHyperGraph().getTransactionManager().ensureTransaction(new Callable<Boolean>() {
			public Boolean call() {
				HGHandle ontologyHandle = getOntologyHandleByID(ontologyId);
				HGDBOntology ontology = getHyperGraph().get(ontologyHandle);
				if (isDistributed(ontology)) {
					DistributedOntology dOntology = getDistributedOntology(ontology);
					cancelSharing(dOntology);
				}
				return VDHGDBOntologyRepository.super.deleteOntology(ontologyId);
			}});
	}

	public void printDistributedOntologies() {
		Set<DistributedOntology> dontos = getDistributedOntologies();
		for (DistributedOntology donto : dontos) {
			System.out.println(donto.toString());
		}
	}
	
	public void printIdentity() {
		HGPeerIdentity me = getPeer().getIdentity();
		System.out.println("HGPeer   : " + me);
		PeerInterface pif = getPeer().getPeerInterface();
		if (pif instanceof XMPPPeerInterface) {
			XMPPPeerInterface xpif = (XMPPPeerInterface)pif;
			//System.out.println("Network identity: " + getPeer().getNetworkTarget(me));
			System.out.println("P2P User   : " + xpif.getUser());
			System.out.println("P2P Server : " + xpif.getServerName());
			System.out.println("P2P Port   : " + xpif.getPort());
		}
		System.out.println("Ontology Server : " + isOntologyServer());
	} 
}
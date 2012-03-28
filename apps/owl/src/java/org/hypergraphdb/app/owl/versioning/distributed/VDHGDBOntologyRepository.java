package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hypergraphdb.peer.Structs.getPart;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PullActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushActivity;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.serializer.JSONReader;
import org.hypergraphdb.peer.workflow.Activity;
import org.hypergraphdb.transaction.HGTransactionConfig;

/**
 * VDHGDBOntologyRepository.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 16, 2012
 */
public class VDHGDBOntologyRepository extends VHGDBOntologyRepository {

	public static final String OBJECTCONTEXT_REPOSITORY = "Repository";

	/**
	 * Expected to be UTF-8 encoded.
	 * Located by this.getClass().getResource()
	 */
	public static String PEER_CONFIGURATION_FILE = "VDHGDBConfig.p2p";

	private HGDBOntologyManager ontologyManager;
	
	HyperGraphPeer peer;
	
	//public static String PEER_USERNAME = "hg1"; 
	//public static String PEER_PASSWORD = "hg1"; 
	
	public static VDHGDBOntologyRepository getInstance() {
		if (!hasInstance()) {
			String hypergraphDBLocation = getHypergraphDBLocation();
			System.out.println("HGDB REPOSITORY AT: " + hypergraphDBLocation);
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
	
	private void createAndConfigurePeer(Map<String, Object> peerConfig) {
		peer = new HyperGraphPeer(peerConfig, getHyperGraph());
		peer.getActivityManager();
		peer.getObjectContext().put(OBJECTCONTEXT_REPOSITORY, this);
		
		//peer.addPeerPresenceListener(this);
		//Activity a;
		//peer.getNetworkTarget(id)
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
	
	/** 
	 * 
	 * @return a default configuration or the configuration found in the configuration file.
	 * @throws IOException 
	 */
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
	
//	public void DBG_ClearDates() {
//		List<HGHandle> L = getHyperGraph().findAll(hg.type(Date.class));
//		for (HGHandle h : L) {
//			getHyperGraph().remove(h);
//		}
//	}
	
	public boolean startNetworking(String userName, String password, String serverUrl) {
		if (peer != null && peer.getPeerInterface().isConnected()) {
			throw new IllegalStateException("Peer is currently connected. Disconnect first.");
		}
		Map<String, Object> config = loadPeerConfig();
		Map<String, Object> interFaceConfig = (Map<String, Object>)config.get("interfaceConfig");
		interFaceConfig.put("user", userName);
		interFaceConfig.put("password", password);
		interFaceConfig.put("serverUrl", serverUrl);
		createAndConfigurePeer(config);
		return startNetworkingInternal();
	}

	/**
	 * Starts networking using the file configured userName and password.
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
			success = f.get(1000, TimeUnit.SECONDS);
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
		peer.getActivityManager().registerActivityType(BrowserRepositoryActivity.TYPENAME, BrowserRepositoryActivity.class);

		return success;

	}

	public void stopNetworking() {
		peer.stop();
	}
	public HyperGraphPeer getPeer() {
		return peer;
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
	public BrowserRepositoryActivity browseRemote(HGPeerIdentity remote) {
		BrowserRepositoryActivity activity = new BrowserRepositoryActivity(peer, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}
	
	public PullActivity pull(VersionedOntology vo, HGPeerIdentity remote) {
		return pull(vo.getBaseRevision().getOntologyUUID(), remote);
	}
	
	/**
	 * Pull from remote. UUID allow to pull vo's not yet available.
	 * @param ontologyUUID
	 * @param remote
	 * @return
	 */
	public PullActivity pull(HGPersistentHandle ontologyUUID, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: no pull
		// 3) Can pull == local master head revision is older and equal to one revision in remote 
		//	  branches history 
		// 4) 
		// 
		System.out.println("Pulling versioned onto: " + ontologyUUID);
		PullActivity activity = new PullActivity(peer, ontologyUUID, remote);
		peer.getActivityManager().initiateActivity(activity);
		return activity;
	}
	
	public PushActivity push(VersionedOntology vo, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: Push everything vo + all changesets using pushNew
		// 3) Can push == target master head revision is older and equal to one revision in my 
		//	  branches history 
		// 4) find last Changeset that is not in target.
		// 5) push each changeset/Revision pair after that. This is the Delta.
		// 6) remote will receive one changeset/Revision, then apply those changes within one transaction
		//  
		//
		System.out.println("Pushing versioned onto: " + vo.getWorkingSetData().getOntologyID());
		PushActivity activity = new PushActivity(peer, vo, remote);
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
	

}
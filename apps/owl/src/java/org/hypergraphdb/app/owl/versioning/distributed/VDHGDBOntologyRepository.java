package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.cact.AddAtom;
import org.hypergraphdb.peer.cact.DefineAtom;
import org.hypergraphdb.peer.workflow.Activity;
import org.hypergraphdb.peer.workflow.ActivityManager;
import org.hypergraphdb.peer.workflow.AffirmIdentity;
//import org.hypergraphdb.p

/**
 * VDHGDBOntologyRepository.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 16, 2012
 */
public class VDHGDBOntologyRepository extends VHGDBOntologyRepository {
	
	HyperGraphPeer peer;
	
	public static String PEER_USERNAME = "hg1"; 
	public static String PEER_PASSWORD = "hg1"; 
	
	public static VDHGDBOntologyRepository getInstance() {
		if (!hasInstance()) {
			String hypergraphDBLocation = getHypergraphDBLocation();
			System.out.println("HGDB REPOSITORY AT: " + hypergraphDBLocation);
			setInstance(new VDHGDBOntologyRepository(hypergraphDBLocation));
			((VDHGDBOntologyRepository)getInstance()).initializeActivities();
		}
		HGDBOntologyRepository instance = HGDBOntologyRepository.getInstance(); 
		if (!(instance instanceof VDHGDBOntologyRepository)) throw new IllegalStateException("Instance requested not Versioned Repository type.: " + instance);
		return (VDHGDBOntologyRepository)instance;
	}
	
	private VDHGDBOntologyRepository(String location) {
		super(location);
	}
	
	private void initializeActivities() {
		Map<String, Object> peerConfig = getPeerConfig();
		peer = new HyperGraphPeer(peerConfig, getHyperGraph());
		peer.getActivityManager();
		
		//peer.addPeerPresenceListener(this);
		//Activity a;
		//peer.getNetworkTarget(id)
		peer.addPeerPresenceListener(new PeerPresenceListener() {
			
			@Override
			public void peerLeft(HGPeerIdentity peer) {
				System.out.println("Heeeellooo " + peer);
			}
			
			@Override
			public void peerJoined(HGPeerIdentity peer) {
				System.out.println("Bye bye " + peer);
			}
		});
				

	}
	
	private Map<String,Object> getPeerConfig() {
		Map<String,Object> peerConfig = new HashMap<String, Object>();
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
		interfaceConfig.put("user", PEER_USERNAME);
		interfaceConfig.put("password", PEER_PASSWORD);
		interfaceConfig.put("serverUrl", "localhost");
		//peers from roster! interfaceConfig.put("room", "ontologyCM@conference.127.0.0.1");
		interfaceConfig.put("autoRegister", true);
		interfaceConfig.put("ignoreRoster", false);
		interfaceConfig.put("fileTransferThreashold", 100 * 1024); //default.
		return peerConfig;
	}
	
	public boolean startNetworking() {
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
	
	public Future<RemoteRepositoryActionResult> fetch(VersionedOntology vo, HGPeerIdentity remote) {
		return null;
	}

	public Future<RemoteRepositoryActionResult> pull(VersionedOntology vo, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: no pull
		// 3) Can pull == local master head revision is older and equal to one revision in remote 
		//	  branches history 
		// 4) 
		// 
		return null;
		
	}
	
	public Future<RemoteRepositoryActionResult> push(VersionedOntology vo, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: Push everything vo + all changesets using pushNew
		// 3) Can push == target master head revision is older and equal to one revision in my 
		//	  branches history 
		// 4) find last Changeset that is not in target.
		// 5) push each changeset/Revision pair after that. This is the Delta.
		// 6) remote will receive one changeset/Revision, then apply those changes within one transaction
		//  
		//
		if (peer.getIdentity().)
			HGHandle atom = getHyperGraph().add(new Date());
			Activity a = new DefineAtom(peer, atom, target)
			peer.getActivityManager().initiateActivity(activity);
		return null;
	}
	
	
	public enum RemoteRepositoryActionResult {SUCCESS, DENIED_LOCAL_OUT_OF_DATE, DENIED_WERE_EQUAL, DENIED_REMOTE_OUT_OF_DATE}; 
	
	public static void main(String[] argv) {
		VDHGDBOntologyRepository.PEER_USERNAME = argv[0];
		VDHGDBOntologyRepository.PEER_PASSWORD = argv[1];
		File dir = new File ("C:\\temp\\hypergraph-" + VDHGDBOntologyRepository.PEER_USERNAME);
		if (!dir.exists()) dir.mkdir();
		VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());
		
		VDHGDBOntologyRepository dr = VDHGDBOntologyRepository.getInstance();
		dr.startNetworking();
		dr.printPeerInfo();
		try {
			while (true) {
				Thread.sleep((long) 5000);
				dr.printPeerInfo();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			dr.stopNetworking();
		}
	}
	
}

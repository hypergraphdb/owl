package org.hypergraph.app.owl.versioning.distributed;

import java.io.File;
import java.util.Date;
import java.util.Set;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.jivesoftware.smack.XMPPConnection;

/**
 * TestVDHGDBIdle starts idle at an repository at C:\\temp\\hypergraph-" + PEER_USERNAME and waits for push or pull.
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 12, 2012
 */
public class TestVDHGDBIdle {

	static {
	    XMPPConnection.DEBUG_ENABLED = false;
	}

	/**
	 * Delete all Ontologies at repository on startup?
	 */
	public static boolean DELETE_ALL_ONTOLOGIES = false;
	
	public static String REPOSITORY_LOCATION = "C:\\temp\\hypergraph-"; //+ PEER_USERNAME

	
	public static String PEER_SERVERNAME = "W203-003.miamidade.gov";
	public static String PEER_USERNAME;
	public static String PEER_PASSWORD;

	/**
	 * 
	 * @param argv call with username [0] password [1].
	 */
	public static void main(String[] argv) {
		PEER_USERNAME = argv[0];
		PEER_PASSWORD = argv[1];
		File dir = new File (REPOSITORY_LOCATION + PEER_USERNAME);
		System.out.println("STARTING IDLE AT: " + dir);
		if (!dir.exists()) dir.mkdir();
		VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());
		System.out.println("Repository at : " + dir);
		HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
		VDHGDBOntologyRepository dr = (VDHGDBOntologyRepository)manager.getOntologyRepository();
		System.out.println("INIT LOCAL IDLE PEER REPOSITORY: " + PEER_USERNAME);
		initializeVDRepository(dr);			
		waitForOnePeer(dr);
		try {
			while  (true) {
				System.out.println("Sleeping 10 mins. At: " + new Date());
				Thread.sleep(10 * 60 * 1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();		
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			dr.stopNetworking();
		}
		System.out.println("BYE BYE " + PEER_USERNAME);
	}
	
	/**
	 * @param dr
	 */
	private static void initializeVDRepository(VDHGDBOntologyRepository dr) {
		if (DELETE_ALL_ONTOLOGIES &&  dr.getOntologies().size() > 0) {
			dr.deleteAllOntologies();
			dr.getGarbageCollector().runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);
		} else {
			dr.printAllOntologies();
		}
		dr.printStatistics();
		dr.startNetworking(PEER_USERNAME, PEER_PASSWORD, PEER_SERVERNAME);
	}

	/**
	 * @param dr 
	 * 
	 */
	private static void waitForOnePeer(VDHGDBOntologyRepository dr) {
		System.out.println("WAIT FOR PEERS: START");
		Set<HGPeerIdentity> connectedPeers;
		do {
			connectedPeers = dr.getPeer().getConnectedPeers();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (connectedPeers.isEmpty()); 
		System.out.println("WAIT FOR PEERS: DONE, I have : " + dr.getPeer().getConnectedPeers().size());
		printConnectedPeers(dr.getPeer());
	}
	
	public static void printConnectedPeers(HyperGraphPeer peer) {
		for (HGPeerIdentity p : peer.getConnectedPeers()) {
			System.out.println("ID: " + p.getId() + " Host:" + p.getHostname() + " Graph: " + p.getGraphLocation());
		} 
	}
	
}

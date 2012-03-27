package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.usage.ImportOntologies;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowserRepositoryActivity.BrowseEntry;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PullActivity;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.jivesoftware.smack.XMPPConnection;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

/**
 * TesVDHGDBIdle starts and idle and empty repository at C:\\temp\\hypergraph-" + PEER_USERNAME.
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
	 * This ontology will be imported.
	 */
	public static String TEST_ONTOLOGY = "C:\\_CiRM\\testontos\\County.owl";
	
	public static String PEER_HOSTNAME = "W203-003.miamidade.gov";
	public static String PEER_USERNAME;
	public static String PEER_PASSWORD;

	
	private static VersionedOntology versionedOntology;
	
	/**
	 * 
	 * @param argv call with username.
	 */
	public static void main(String[] argv) {
		PEER_USERNAME = argv[0];
		PEER_PASSWORD = argv[1];
		File dir = new File ("C:\\temp\\hypergraph-" + PEER_USERNAME);
		System.out.println("STARTING IDLE AT: " + dir);
		if (!dir.exists()) dir.mkdir();
		VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());
		System.out.println("Creating Repository : " + dir);
		HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
		VDHGDBOntologyRepository dr = (VDHGDBOntologyRepository)manager.getOntologyRepository();
		System.out.println("INIT LOCAL IDLE PEER REPOSITORY: " + PEER_USERNAME);
		initializeVDRepository(dr);			
		waitForOnePeer(dr);
		try {
			while  (true) {
				System.out.println("Sleeping 5 mins. AT: " + new Date());
				Thread.sleep(5 * 60 * 1000);
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
		if (dr.getOntologies().size() > 0) {
			dr.deleteAllOntologies();
			dr.getGarbageCollector().runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);
		}
		dr.startNetworking(PEER_USERNAME, PEER_PASSWORD, PEER_HOSTNAME);
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

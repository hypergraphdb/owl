package org.hypergraph.app.owl.versioning.distributed;

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
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.BrowseRepositoryActivity.BrowseEntry;
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
 * TestPush.
 * 
 * The passed name containing "1" pulls from the other. 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 12, 2012
 */
public class TestVDHGDBPull {

	static {
	    XMPPConnection.DEBUG_ENABLED = false;
	}

	public static int TARGET_MODIFICATION_LIMIT = 500;
	/**
	 * This ontology will be imported.
	 */
	public static String TEST_ONTOLOGY = "C:\\_CiRM\\testontos\\County.owl";
	
	/**
	 * if false the first one found will be used.
	 */
	public static boolean IMPORT_TEST_ONTOLOGY = true;
	
	/**
	 * This directory will contain test output (Rendered ontologies)
	 */
	public static String RENDER_DIRECTORY = "C:\\_CiRM\\testontos\\TestVDHGDBPush\\";
		
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
		System.out.println("STARTING PULL TEST: " + dir);
		//System.out.println("Dropping Hypergraph at : " + dir);
		//HGUtils.dropHyperGraphInstance(dir.getAbsolutePath());
		if (!dir.exists()) dir.mkdir();
		VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());
		System.out.println("Creating Repository : " + dir);
		HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
		VDHGDBOntologyRepository dr = (VDHGDBOntologyRepository)manager.getOntologyRepository();
		if (argv[0].contains("1")) {
			System.out.println("INIT LOCAL PULL INITIATOR: " + PEER_USERNAME);
			initializePullInitiator(dr);			
			waitForOnePeer(dr);
			HGPeerIdentity targetPeer = dr.getPeer().getConnectedPeers().iterator().next();
			//Start Pull Loop.
			StopWatch stopWatch = new StopWatch(false);
				try {
					while  (true) {
					System.out.print("BROWSING PEER...");
					BrowseRepositoryActivity browseAct = dr.browseRemote(targetPeer);
					//block
					ActivityResult browseResult = browseAct.getFuture().get();
					if (browseResult.getException() != null) {
						throw browseResult.getException();
					}
					List<BrowseEntry> l = browseAct.getRepositoryBrowseEntries();
					System.out.println("Done. Got: " + l.size() + " remote ontologies.");
					for (BrowseEntry entry : l) {
						dr.printStatistics();
						stopWatch.start();
						PullActivity pullAct = dr.pullNew(entry.getUuid(), targetPeer);
						//block
						System.out.print("PULLING Ontology" + entry.getOwlOntologyIRI() + " UUID: " + entry.getUuid() + " ...");
						ActivityResult pullResult = pullAct.getFuture().get();
						System.out.println("Done. Final State: " + pullAct.getState());
						System.out.println("Completedmessage: " + pullAct.getCompletedMessage());
						stopWatch.stop("PULL TIME: ");
						if (pullResult.getException() != null) {
							throw pullResult.getException();
						}
					}
					//Wait 10 seconds after each pull cycle.
					System.out.println("Sleeping 20 secs.");
					Thread.sleep(20000);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				} finally {
				}
				dr.stopNetworking();
		} else {
			System.out.println("INIT REMOTE PULL TARGET" + PEER_USERNAME);
			initializePullTarget(manager, dr);
			waitForOnePeer(dr);
			for (int i = 0; i < TARGET_MODIFICATION_LIMIT; i ++) {
				System.out.println("TARGET MODIFICATION: " + i);
				modifyAndCommitTarget();
				try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("REVERTING TARGET REVISIONS " + PEER_USERNAME);
			for (int i = 0; i < TARGET_MODIFICATION_LIMIT; i ++) {
				//versionedOntology.revertHeadOneRevision();
				System.err.println("CURRENTLY DISABLED TEST");
			}
			System.out.println("REVERTING TARGET REVISIONS FINISHED " + PEER_USERNAME);
			try {
				Thread.sleep(100 * 60 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			dr.stopNetworking();
			System.out.println("BYE BYE " + PEER_USERNAME);
		}
	}

	/**
	 * Creates five class declaration axioms.
	 */
	private static void modifyAndCommitTarget() {
		OWLOntology onto =  versionedOntology.getWorkingSetData();
		OWLOntologyManager manager =  onto.getOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		for (int i = 0; i < 5; i ++) {
			OWLClass newClass = df.getOWLClass(IRI.create("" + new Date().getTime() + "-" + i));
			OWLDeclarationAxiom newAx = df.getOWLDeclarationAxiom(newClass);
			manager.applyChange(new AddAxiom(onto, newAx));
		}
		versionedOntology.commit("Automated User", "Time was: " +  new Date().getTime());
		System.out.println("TARGET REVISION COUNT: " + versionedOntology.getNrOfRevisions());
	}

	/**
	 * @param dr
	 */
	private static void initializePullTarget(HGDBOntologyManager manager, VDHGDBOntologyRepository dr) {
		//Ensure Test ontology loaded		
		System.out.println("INIT PULL TARGET");
		dr.printStatistics();
		if (IMPORT_TEST_ONTOLOGY) {
			System.out.println("IMPORTING: " + TEST_ONTOLOGY);
			System.out.println("deleteAllOntologies");
			dr.deleteAllOntologies();
			dr.printStatistics();
			System.out.println("ImportOntologies.importOntology " + TEST_ONTOLOGY);
			IRI targetIRI = ImportOntologies.importOntology(new File(TEST_ONTOLOGY), dr.getOntologyManager());
			HGDBOntology o;
			try {
				o = (HGDBOntology)manager.loadOntologyFromOntologyDocument(targetIRI);
			} catch (OWLOntologyCreationException e) {
				throw new RuntimeException("load Failes", e);
			}
			System.out.println("LOADED ONTOLOLGY ID: " + o.getOntologyID());
			System.out.println("addVersionControl: " + o);
			versionedOntology = dr.addVersionControl(o, "distributedTestUser");
			// MANIPULATE REMOVE CHANGED
			Object[] axioms = o.getAxioms().toArray();
			//remove all axioms 10.
			for (int i = 0; i < axioms.length / 10; i ++) {
				System.out.println("Creating Revision: " + versionedOntology.getNrOfRevisions());
				int j = i;
				for (;j < i + axioms.length / 100; j++) {
					if (j < axioms.length) {
						manager.applyChange(new RemoveAxiom(o, (OWLAxiom)axioms[j]));
					}
				}
				i = j;
				versionedOntology.commit("SameUser", " commit no " + i);
			}
		} else {
			versionedOntology = dr.getVersionControlledOntologies().get(0);
			//the workingsetdata is not loaded by the manager, we need to set it.
			versionedOntology.getWorkingSetData().setOWLOntologyManager(manager);
			if (versionedOntology == null) throw new IllegalStateException("We have NOT found a versioned ontololgy in the repository.");
		}
		dr.startNetworking(PEER_USERNAME, PEER_PASSWORD, PEER_HOSTNAME);
	}

	/**
	 * @param dr
	 */
	private static void initializePullInitiator(VDHGDBOntologyRepository dr) {
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

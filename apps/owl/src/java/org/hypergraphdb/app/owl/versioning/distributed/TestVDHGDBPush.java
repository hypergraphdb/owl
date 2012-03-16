package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.usage.ImportOntologies;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushActivity;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.workflow.Activity;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.hypergraphdb.util.HGUtils;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * TestPush.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 12, 2012
 */
public class TestVDHGDBPush {
	/**
	 * This ontology will be imported.
	 */
	public static String TEST_ONTOLOGY = "C:\\_CiRM\\testontos\\County.owl";
	
	/**
	 * if false the first one found will be used.
	 */
	public static boolean IMPORT_TEST_ONTOLOGY = false;
	
	/**
	 * This directory will contain test output (Rendered ontologies)
	 */
	public static String RENDER_DIRECTORY = "C:\\_CiRM\\testontos\\TestVDHGDBPush\\";
		
	
	private static VersionedOntology versionedOntology;
	
	/**
	 * 
	 * @param argv call with username.
	 */
	public static void main(String[] argv) {
		VDHGDBOntologyRepository.PEER_USERNAME = argv[0];
		VDHGDBOntologyRepository.PEER_PASSWORD = argv[1];
		File dir = new File ("C:\\temp\\hypergraph-" + VDHGDBOntologyRepository.PEER_USERNAME);
		System.out.println("STARTING TEST: " + dir);
		//System.out.println("Dropping Hypergraph at : " + dir);
		//HGUtils.dropHyperGraphInstance(dir.getAbsolutePath());
		if (!dir.exists()) dir.mkdir();
		VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());
		System.out.println("Creating Repository : " + dir);
		HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
		VDHGDBOntologyRepository dr = (VDHGDBOntologyRepository)manager.getOntologyRepository();
		if (argv[0].contains("1")) {
			initializePushInitiator(manager, dr);
			waitForOnePeer(dr);
			HGPeerIdentity targetPeer = dr.getPeer().getConnectedPeers().iterator().next();
			PushActivity a = dr.push(versionedOntology, targetPeer);
			try {
				ActivityResult r = a.getFuture().get();
				for (int i = 0; i < 10; i ++) {
					System.out.println("PUSHING NEW COMMIT: " + i);
					modifyAndCommitSource();
					a = dr.push(versionedOntology, targetPeer);
					//block till done
					r = a.getFuture().get();
					System.out.println("RESULT State : " + i + " " + a.getState());
					System.out.println("RESULT Message : " + i + " " + a.getCompletedMessage());
					System.out.println("RESULT: " + i + " Exception:" + r.getException());
					System.out.println("FINISHED: PUSHING NEW COMMIT: " + i + "");
				}
				for (int i = 0; i < 10; i ++) {
					versionedOntology.revertHeadOneRevision();
				}
				try {
					a = dr.push(versionedOntology, targetPeer);
					r = a.getFuture().get();
					System.out.println("RESULT: " + "X" + " " + a.getCompletedMessage());
					System.out.println("RESULT: " + "X" + " Exception:" + r.getException());
					System.out.println(a.getFuture().get().getException());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			initializePushReceiver(dr);
			waitForOnePeer(dr);
			//dr.getPeer().getActivityManager().getActivity(null).getState().
		}
		try {
			Thread.sleep(15 * 60 * 1000);
		} catch (InterruptedException e) {
			//ignored
		}
		
		dr.stopNetworking();
//		dr.printPeerInfo();
//		try {
//			while (true) {
//				Thread.sleep((long) 10000);
//				dr.printPeerInfo();
//				dr.push(null, null);
//			}
//			
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			dr.stopNetworking();
//		}
	}

	/**
	 * Creates five class declaration axioms.
	 */
	private static void modifyAndCommitSource() {
		OWLOntology onto =  versionedOntology.getWorkingSetData();
		OWLOntologyManager manager =  onto.getOWLOntologyManager();
		OWLDataFactory df = manager.getOWLDataFactory();
		for (int i = 0; i < 5; i ++) {
			OWLClass newClass = df.getOWLClass(IRI.create("" + new Date().getTime() + "-" + i));
			OWLDeclarationAxiom newAx = df.getOWLDeclarationAxiom(newClass);
			manager.applyChange(new AddAxiom(onto, newAx));
		}
		versionedOntology.commit("Automated User", "Time was: " +  new Date().getTime());
	}

	/**
	 * @param dr
	 */
	private static void initializePushInitiator(HGDBOntologyManager manager, VDHGDBOntologyRepository dr) {
		//Ensure Test ontology loaded		
		System.out.println("INIT PUSH");
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
		dr.startNetworking();
	}

	/**
	 * @param dr
	 */
	private static void initializePushReceiver(VDHGDBOntologyRepository dr) {
		if (dr.getOntologies().size() > 0) {
			dr.deleteAllOntologies();
			dr.getGarbageCollector().runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);
		}
		dr.startNetworking();
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
	
	/**
	 * @param dr
	 */
	private static void ensureOneVersionedOntology(VDHGDBOntologyRepository dr) {	
		List <File> renderedFiles = new ArrayList<File>();
		HGDBOntologyManagerImpl manager = HGDBOWLManager.createOWLOntologyManager();
		//
		// IMPORT AND RENDER
		//
//		try {
//			VDHGDBOntologyRepository repo = (VDHGDBOntologyRepository)manager.getOntologyRepository();
//			//repo.dropHypergraph();
//			repo.deleteAllOntologies();
//			//System.out.println("Running GC");
//			//CANNOT RUN GC nullHANDLE problem !!! repo.runGarbageCollector();
//			File f = new File("C:\\_CiRM\\testontos\\County.owl");
//			IRI targetIRI = ImportOntologies.importOntology(f, manager);
//			//File f2 = new File("C:\\_CiRM\\testontos\\1 csr.owl");
//			//IRI targetIRI = ImportOntologies.importOntology(f2, manager);
//			HGDBOntology o = (HGDBOntology)manager.loadOntologyFromOntologyDocument(targetIRI);
//			VersionedOntology vo = repo.addVersionControl(o, "distributedTestUser");
//			// MANIPULATE REMOVE CHANGED
//			Object[] axioms = o.getAxioms().toArray();
//			//remove all axioms 10.
//			for (int i = 0; i < axioms.length / 10; i ++) {
//				int j = i;
//				for (;j < i + axioms.length / 100; j++) {
//					if (j < axioms.length) {
//						manager.applyChange(new RemoveAxiom(o, (OWLAxiom)axioms[j]));
//					}
//				}
//				i = j;
//				vo.commit("SameUser", " commit no " + i);
//			}
//			// RENDER VERSIONED ONTOLOGY, includes data
//			for (int i = 0; i < vo.getArity(); i ++) {
//				VOWLXMLRenderConfiguration c = new VOWLXMLRenderConfiguration();
//				c.setLastRevisionIndex(i);
//				VOWLXMLVersionedOntologyRenderer r = new VOWLXMLVersionedOntologyRenderer(manager);
//				File fx = new File("C:\\_CiRM\\testontos\\CountyVersioned-Rev-"+ i + ".vowlxml");
//				renderedFiles.add(fx);
//				//File fx = new File("C:\\_CiRM\\testontos\\1 csr-Rev-"+ i + ".vowlxml");
//				FileWriter fwriter = new FileWriter(fx);
//				//	Full export
//				r.render(vo, fwriter, c);
//			}
//		} catch (OWLOntologyCreationException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OWLRendererException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		//
		// PARSE
		//
		//for (File f : renderedFiles) {
		File f = new File("C:\\_CiRM\\testontos\\CountyVersioned-Rev-"+ 10 + ".vowlxml");
			System.out.println("Parsing: " + f + " length: " + (f.length() / 1024) + " kB");
			OWLOntologyDocumentSource source = new FileDocumentSource(f);
			VOWLXMLParser parser = new VOWLXMLParser();
			OWLOntology onto = new OWLOntologyImpl(manager, new OWLOntologyID());
			// must have onto for manager in super class
			VOWLXMLDocument versionedOntologyRoot = new VOWLXMLDocument(onto);
			try {
				parser.parse(source, versionedOntologyRoot, new OWLOntologyLoaderConfiguration());
				System.out.println("PARSING FINISHED.");
			} catch (OWLOntologyChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnloadableImportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OWLParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}
	}
}

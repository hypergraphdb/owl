package org.hypergraph.app.owl.versioning.distributed;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyFactory;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.core.OWLTempOntologyImpl;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.usage.ImportOntologies;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVersionedOntologyRenderer;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * TestVersionedOntoRenderAndParse.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 9, 2012
 */
@Deprecated
public class TestVersionedOntoRenderAndParse {

	public static File TESTFILE = new File("C:\\_CiRM\\testontos\\County.owl");

	public static void main(String args[]) {
		TestVersionedOntoRenderAndParse t = new TestVersionedOntoRenderAndParse();
		t.testRenderAndParse();
	}
	
	public void testRenderAndParse() {
		List <File> renderedFiles = new ArrayList<File>();
		HGDBOntologyManagerImpl manager = HGDBOWLManager.createOWLOntologyManager();
		//
		// IMPORT AND RENDER
		//
		try {
			VDHGDBOntologyRepository repo = (VDHGDBOntologyRepository)manager.getOntologyRepository();
			//repo.dropHypergraph();
			repo.deleteAllOntologies();
			//System.out.println("Running GC");
			//CANNOT RUN GC nullHANDLE problem !!! repo.runGarbageCollector();
			File f = new File("C:\\_CiRM\\testontos\\County.owl");
			IRI targetIRI = ImportOntologies.importOntology(f, manager);
			//File f2 = new File("C:\\_CiRM\\testontos\\1 csr.owl");
			//IRI targetIRI = ImportOntologies.importOntology(f2, manager);
			HGDBOntology o = (HGDBOntology)manager.loadOntologyFromOntologyDocument(targetIRI);
			VersionedOntology vo = repo.addVersionControl(o, "distributedTestUser");
			// MANIPULATE REMOVE CHANGED
			Object[] axioms = o.getAxioms().toArray();
			//remove all axioms 10.
			for (int i = 0; i < axioms.length / 10; i ++) {
				int j = i;
				for (;j < i + axioms.length / 100; j++) {
					if (j < axioms.length) {
						manager.applyChange(new RemoveAxiom(o, (OWLAxiom)axioms[j]));
					}
				}
				i = j;
				vo.commit("SameUser", " commit no " + i);
			}
			// RENDER VERSIONED ONTOLOGY, includes data
			for (int i = 0; i < vo.getArity(); i ++) {
				VOWLXMLRenderConfiguration c = new VOWLXMLRenderConfiguration();
				c.setLastRevisionIndex(i);
				VOWLXMLVersionedOntologyRenderer r = new VOWLXMLVersionedOntologyRenderer(manager);
				File fx = new File(TESTFILE.getAbsolutePath() + " Revision-" + i + ".xml");
				//File fx = new File("C:\\_CiRM\\testontos\\CountyVersioned-Rev-"+ i + ".vowlxml");
				renderedFiles.add(fx);
				//File fx = new File("C:\\_CiRM\\testontos\\1 csr-Rev-"+ i + ".vowlxml");
				Writer fwriter = new OutputStreamWriter(new FileOutputStream(fx), Charset.forName("UTF-8"));
				//	Full export
				r.render(vo, fwriter, c);
			}
			System.out.println("DELETE ALL ONTOLOGIES");
			repo.deleteAllOntologies();
			repo.getGarbageCollector().runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLRendererException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//
		// PARSE
		//
		File f = new File(TESTFILE.getAbsolutePath() + " Revision-" + 10 + ".xml");
			System.out.println("PARSING: " + f + " length: " + (f.length() / 1024) + " kB");
			OWLOntologyDocumentSource source = new FileDocumentSource(f);
			VOWLXMLParser parser = new VOWLXMLParser();
			OWLOntologyEx onto = new OWLTempOntologyImpl(manager, new OWLOntologyID());
			// must have onto for manager in super class
			VOWLXMLDocument versionedOntologyRoot = new VOWLXMLDocument(onto);
			//
			// Create VersionedOntology Revision 10			
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
			if (versionedOntologyRoot.isCompleteVersionedOntology()) {
				OWLOntologyID ontologyID = versionedOntologyRoot.getRevisionData().getOntologyID();
				IRI documentIRI = IRI.create("hgdb://" + ontologyID.getDefaultDocumentIRI().toString().substring(7));
				HGPersistentHandle ontologyUUID = versionedOntologyRoot.getVersionedOntologyID();
				try {
					System.out.println("Storing ontology data for : " + ontologyUUID);
					HGDBOntology o = manager.getOntologyRepository().createOWLOntology(ontologyID, documentIRI, ontologyUUID);
					storeFromTo(versionedOntologyRoot.getRevisionData(), o);
				} catch (HGDBOntologyAlreadyExistsByDocumentIRIException e) {
					e.printStackTrace();
				} catch (HGDBOntologyAlreadyExistsByOntologyIDException e) {
					e.printStackTrace();
				} catch (HGDBOntologyAlreadyExistsByOntologyUUIDException e) {
					e.printStackTrace();
				}
				HyperGraph graph = manager.getOntologyRepository().getHyperGraph();
				//Add version control with full matching history.
				System.out.println("Adding version control history to : " + ontologyUUID);
				VersionedOntology voParsed = new VersionedOntology(versionedOntologyRoot.getRevisions(), versionedOntologyRoot.getChangesets(), graph);
				VHGDBOntologyRepository vrepo = (VHGDBOntologyRepository)manager.getOntologyRepository();
				System.out.println("Versioned Repository Contents: ");
				for (VersionedOntology vox : vrepo.getVersionControlledOntologies()) {
					System.out.println("Versioned Ontology: " + vox.getWorkingSetData());
					System.out.println("Versioned Ontology Revs: " + vox.getNrOfRevisions());
				}
				//
				// Rendering FULL Versioned Ontology
				//
				System.out.println("Rendering full versioned ontology after parse and store: " + ontologyUUID);
				VOWLXMLVersionedOntologyRenderer r = new VOWLXMLVersionedOntologyRenderer(manager);
				File fx = new File(TESTFILE.getAbsolutePath() + "FULL-afterParse.xml");
				Writer fwriter;
				try {
					fwriter = new OutputStreamWriter(new FileOutputStream(fx), Charset.forName("UTF-8"));
					r.render(voParsed, fwriter);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (OWLRendererException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("ERROR: EXPECTING COMPLETE VERSIONED ONTOLOGY");
			}
		//}
	}

	public void storeFromTo(OWLOntology from, HGDBOntology to) {
		final Set<OWLAxiom> axioms = from.getAxioms();
		int i = 0;
		for (OWLAxiom axiom : axioms) {
			to.applyChange(new AddAxiom(to, axiom));
			i++;
			if (i % 5000 == 0) {
				System.out.println("storeFromTo: Axioms: " + i);
			}
		}		
		System.out.println("storeFromTo: Axioms: " + i);
		//manager.addAxioms(newOnto, axioms);
		// Add Ontology Annotations
		for (OWLAnnotation a : from.getAnnotations()) {
			to.applyChange(new AddOntologyAnnotation(to, a));
		}
		// Add Import Declarations
		for (OWLImportsDeclaration im : from.getImportsDeclarations()) {
			to.applyChange(new AddImport(to, im));
		}
	}
}


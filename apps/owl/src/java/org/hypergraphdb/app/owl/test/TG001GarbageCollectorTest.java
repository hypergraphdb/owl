package org.hypergraphdb.app.owl.test;


import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.gc.GarbageCollectorStatistics;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import com.sun.corba.se.impl.orbutil.graph.Graph;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TG001GarbageCollectorTest.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 4, 2012
 */
@RunWith(value = Parameterized.class)
public class TG001GarbageCollectorTest extends OntologyManagerTest {

	GarbageCollector gc = null;
	/**
	 * @param useImplementation
	 */
	public TG001GarbageCollectorTest(int useImplementation) {
		super(useImplementation);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		super.setUp();
		gc = super.isHypergraphMode()? r.getGarbageCollector() : null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		//we drop the whole repository after each case.
		//tryCleanUp();
	}

	public void testGC0CollectEmptyOnto() throws Exception {
		if (gc == null) return;
		long atoms1 = r.getNrOfAtoms();
		GarbageCollectorStatistics statsA1 = gc.runGarbageAnalysis(GarbageCollector.MODE_FULL);	
		GarbageCollectorStatistics statsC1 = gc.runGarbageCollection(GarbageCollector.MODE_FULL);	
		assertStatsEqual(statsA1, statsC1);
		long atoms2 = r.getNrOfAtoms(); 
		assertTrue(atoms1 == atoms2);
		//
		m.removeOntology(o);
		r.deleteOntology(o.getOntologyID());
		long atoms3 = r.getNrOfAtoms(); 
		assertTrue(atoms3 == atoms2);
		GarbageCollectorStatistics statsA2 = gc.runGarbageAnalysis(GarbageCollector.MODE_FULL);	
		GarbageCollectorStatistics statsC2 = gc.runGarbageCollection(GarbageCollector.MODE_FULL);	
		assertStatsEqual(statsA2, statsC2);
		assertTrue(statsC2.getTotalAtoms() == statsC1.getTotalAtoms() + 1);
		long atoms4 = r.getNrOfAtoms(); 
		assertTrue(atoms4 == atoms3 - statsC2.getTotalAtoms());
		createOntologyO();
		long atoms5 = r.getNrOfAtoms();
		GarbageCollectorStatistics statsA3 = gc.runGarbageAnalysis(GarbageCollector.MODE_FULL);	
		GarbageCollectorStatistics statsC3 = gc.runGarbageCollection(GarbageCollector.MODE_FULL);	
		assertStatsEqual(statsA3, statsC3);
		long atoms6 = r.getNrOfAtoms();
		assertTrue(atoms6 == atoms5);assertTrue(atoms6 == atoms2);
		m.removeOntology(o);
		r.deleteOntology(o.getOntologyID());
		long atoms7 = r.getNrOfAtoms();
		assertTrue(atoms7 == atoms6);
		GarbageCollectorStatistics statsA4 = gc.runGarbageAnalysis(GarbageCollector.MODE_FULL);	
		GarbageCollectorStatistics statsC4 = gc.runGarbageCollection(GarbageCollector.MODE_FULL);	
		assertStatsEqual(statsA4, statsC4);
		long atoms8 = r.getNrOfAtoms();
		assertTrue(atoms8 == atoms7 - statsC4.getTotalAtoms());				
	}
	
	/**
	 * Creates one ontology using TestData class. Marks it for deletion, gc analyses and collects it in DELETED_ONTOLOGIES MODE.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGC01CollectTestOnto() throws Exception {
		if (gc == null) return;
		ensureOntology();
		long atoms1 = r.getNrOfAtoms(); //2011.01.04 1:14PM 282 Atoms
		TestData.fillOntology(df, o);
		int ontoAxiomCount = o.getAxiomCount();
		int ontoSignatureCount = o.getSignature().size();
		System.out.println("Ontolgy created: Axioms: " + ontoAxiomCount + " Signature: " + ontoSignatureCount);
		long atoms2 = r.getNrOfAtoms(); //2011.01.04 1:14PM 437 Atoms if onto not in DB, 477 if in DB; 478 2nd run.
		GarbageCollectorStatistics statsA1 = gc.runGarbageAnalysis(GarbageCollector.MODE_DELETED_ONTOLOGIES);	
		GarbageCollectorStatistics statsC1 = gc.runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);	
		assertStatsEqual(statsA1, statsC1); assertTrue(statsC1.getTotalAtoms() == 0);
		long atoms3 = r.getNrOfAtoms(); 
		assertTrue(atoms3 == atoms2);
		System.out.println("-----------BEFORE DELETE ---------------");
		System.out.println("Ontology Axioms: " + o.getAxiomCount());
		System.out.println("Ontology Entities: " + o.getSignature().size());
		System.out.println("Graph Atoms: " + atoms3);
		m.removeOntology(o);
		r.deleteOntology(o.getOntologyID());
		long atoms4 = r.getNrOfAtoms(); 
		assertTrue(atoms4 == atoms2);
		System.out.println("----------- ANALYZE ---------------");
		GarbageCollectorStatistics statsA2 = gc.runGarbageAnalysis(GarbageCollector.MODE_DELETED_ONTOLOGIES); //39 axioms, 1 onto, 40 total	
		System.out.println("-----------AFTER ANALYZE ---------------");
		System.out.println("Graph Atoms: " + r.getNrOfAtoms());
		System.out.println("Ontology Axioms: " + o.getAxiomCount());
		System.out.println("Ontology Entities: " + o.getSignature().size());
		System.out.println("GC STATS: " + statsA2.toString());
		System.out.println("----------- GC ---------------");
		GarbageCollectorStatistics statsC2 = gc.runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);	
		System.out.println("-----------AFTER GC ---------------");
		System.out.println("Graph Atoms: " + r.getNrOfAtoms());
		System.out.println("Ontology Axioms: " + o.getAxiomCount());
		System.out.println("Ontology Entities: " + o.getSignature().size());
		System.out.println("GC STATS: " + statsC2.toString());
		assertStatsEqual(statsA2, statsC2);
		assertTrue(statsC2.getTotalAtoms() == statsA2.getTotalAtoms());
		long atoms5 = r.getNrOfAtoms();
		assertTrue(statsC2.getTotalAtoms() > ontoAxiomCount + ontoSignatureCount);
		assertTrue(atoms5 == atoms4 - statsC2.getTotalAtoms());
	}

	/**
	 * Creates one ontology using TestData class. Copys all axioms into another. Marks both for deletion, 
	 * gc analyses and collects both in DELETED_ONTOLOGIES MODE.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGC02Collect2TestOntoSharedAxioms() throws Exception {
		if (gc == null) return;
		ensureOntology();
		long atoms1 = r.getNrOfAtoms(); //2011.01.04 1:14PM 282 Atoms
		TestData.fillOntology(df, o);
		OWLOntology o2 = m.createOntology(IRI.create("hgdb://www.miamidade.gov/GCTest2"));
		m.addAxioms(o2, o.getAxioms());
		int ontoAxiomCount = o2.getAxiomCount();
		int ontoSignatureCount = o2.getSignature().size();
		System.out.println("Ontolgies created: Axioms 2: " + ontoAxiomCount + " Signature 2: " + ontoSignatureCount);
		long atoms2 = r.getNrOfAtoms(); //2011.01.04 1:14PM 437 Atoms if onto not in DB, 477 if in DB; 478 2nd run.
		GarbageCollectorStatistics statsA1 = gc.runGarbageAnalysis(GarbageCollector.MODE_DELETED_ONTOLOGIES);	
		GarbageCollectorStatistics statsC1 = gc.runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);	
		assertStatsEqual(statsA1, statsC1); assertTrue(statsC1.getTotalAtoms() == 0);
		long atoms3 = r.getNrOfAtoms(); 
		assertTrue(atoms3 == atoms2);
		System.out.println("-----------BEFORE DELETE ---------------");
		System.out.println("Ontology Axioms: " + o.getAxiomCount());
		System.out.println("Ontology Entities: " + o.getSignature().size());
		System.out.println("Graph Atoms: " + atoms3);
		m.removeOntology(o);
		r.deleteOntology(o.getOntologyID());
		m.removeOntology(o2);
		r.deleteOntology(o2.getOntologyID());
		long atoms4 = r.getNrOfAtoms(); 
		assertTrue(atoms4 == atoms2);
		System.out.println("----------- ANALYZE ---------------");
		GarbageCollectorStatistics statsA2 = gc.runGarbageAnalysis(GarbageCollector.MODE_DELETED_ONTOLOGIES); //39 axioms, 1 onto, 40 total	
		System.out.println("-----------AFTER ANALYZE ---------------");
		System.out.println("Graph Atoms: " + r.getNrOfAtoms());
		System.out.println("Ontology Axioms: " + o.getAxiomCount());
		System.out.println("Ontology Entities: " + o.getSignature().size());
		System.out.println("GC STATS: " + statsA2.toString());
		System.out.println("----------- GC ---------------");
		GarbageCollectorStatistics statsC2 = gc.runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);	
		System.out.println("-----------AFTER GC ---------------");
		System.out.println("Graph Atoms: " + r.getNrOfAtoms());
		System.out.println("Ontology Axioms: " + o.getAxiomCount());
		System.out.println("Ontology Entities: " + o.getSignature().size());
		System.out.println("GC STATS: " + statsC2.toString());
		assertStatsEqual(statsA2, statsC2);
		assertTrue(statsC2.getTotalAtoms() == statsA2.getTotalAtoms());
		long atoms5 = r.getNrOfAtoms();
		assertTrue(statsC2.getTotalAtoms() > ontoAxiomCount + ontoSignatureCount);
		assertTrue(atoms5 == atoms4 - statsC2.getTotalAtoms());
	}

	public void ensureOntology() throws Exception {
		if (o == null) {
			super.createOntologyO();
		} else {
			if (!(r.existsOntology(o.getOntologyID()))) {
				createOntologyO();
			}
		}
	}
	
	public void assertStatsEqual(GarbageCollectorStatistics s, GarbageCollectorStatistics t) {
		assertTrue(s.getOntologies() == t.getOntologies());
		assertTrue(s.getAxioms() == t.getAxioms());
		assertTrue(s.getEntities() == t.getEntities());
		assertTrue(s.getOtherObjects() == t.getOtherObjects());
		assertTrue(s.getIris() == t.getIris());
		assertTrue(s.getAxiomNotRemovableCases() == t.getAxiomNotRemovableCases());
		assertTrue(s.getTotalAtoms() == t.getTotalAtoms());
	}
}

package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import junit.framework.TestCase;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * HGDBOntologyRepositoryTest.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 6, 2011
 */
public class HGDBOntologyRepositoryTest extends TestCase {
	private StopWatch s = new StopWatch();

	OWLOntology o;
	OWLOntologyManager m;
	OWLDataFactory df;
	// HGDBOntologyRepository r;
	IRI ontoIRI = IRI.create("hgdb://UNITTESTONTO1");

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// r = new HGDBOntologyRepository();
		// m = r.createOWLOntologyManager();
		m = HGDBOntologyRepository.createOWLOntologyManager();
		df = m.getOWLDataFactory();
		IRI ontoIRI = IRI.create("hgdb://UNITTESTONTO 1");
		o = m.createOntology(ontoIRI);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		// r.dispose();
	}

	/**
	 * Test method for
	 * {@link org.hypergraphdb.app.owl.HGDBOntologyRepository#HGDBOntologyRepository()}
	 * .
	 */
	@Test
	public void testHGDBOntologyRepository() {
		int n = 100;
		while (n < 1E5) {
			System.out.println(new Date());
			long startT = System.nanoTime();
			createClassAndSubclassAxioms(n);
			s.stop("WHOLE CALL TOOK: ", startT);
			n = 2 * n;
		}
	}

	public double createClassAndSubclassAxioms(int n) {
		// System.setOut(new PrintStream(NULL));
		double durationSecs;
		System.out.println("-------------------------------------------------------");
		System.out.print("Creating Classes : " + n);
		OWLClass last = null;
		s.start();
		for (int i = 0; i < n; i++) {
			// if we create a class with the same name each time, we will not
			// find out!!
			//
			OWLClass c = df
					.getOWLClass(IRI.create(ontoIRI.toString() + "#TEST" + n + "-" + i + ""));
			OWLDeclarationAxiom axiomD = df.getOWLDeclarationAxiom(c);
			m.addAxiom(o, axiomD);
			if (last != null) {
				OWLSubClassOfAxiom axiomS = df.getOWLSubClassOfAxiom(c, last);
				m.addAxiom(o, axiomS);
			}
			last = c;
		}
		durationSecs = s.stop(" \t");
		int expectedAxiomCount = n * 2 - 1;
		int expectedEntityCount = n;
		int actualAxiomCount = o.getAxiomCount();
		s.stop("o.getAxiomCount()");
		int actualEntityCount = o.getClassesInSignature().size();
		s.stop("o.getClassesInSignature()");
		System.out.print("Actual: AX: " + actualAxiomCount);
		System.out.println(" \t EN: " + actualEntityCount);
		assertTrue("Axioms expected:" + expectedAxiomCount, actualAxiomCount == expectedAxiomCount);
		assertTrue("Entities expected:" + expectedEntityCount,
				actualEntityCount == expectedEntityCount);
		// clear ontology, assert 0 entities
		s.start();
		m.removeAxioms(o, o.getAxioms());
		s.stop("Get+Remove All Ax");
		assertTrue("Ontolgy empty true:", o.isEmpty());
		s.stop("o.isEmpty()");
		assertTrue("Ontolgy ClassesInSign isEmpty:", o.getClassesInSignature().isEmpty());
		s.stop("o.getClassesInSignature()");
		assertTrue("Ontolgy Signature isEmpty:", o.getSignature().isEmpty());
		s.stop("o.getSignature().isEmpty()");
		assertTrue("Ontolgy Axioms isEmpty:", o.getAxioms().isEmpty());
		s.stop("o.getAxioms()");
		assertTrue("Ontolgy AxiomsCount zero:", o.getAxiomCount() == 0);
		s.stop(" o.getAxiomCount()");
		// Graph objects
		// long c = r.getHyperGraph().count(hg.all());
		// s.stop("Hypergraph all object count: " + c);
		return durationSecs;
	}

}

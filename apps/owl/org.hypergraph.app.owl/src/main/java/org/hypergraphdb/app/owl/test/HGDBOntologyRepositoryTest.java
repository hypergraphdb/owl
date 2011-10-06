package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.*;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * HGDBOntologyRepositoryTest.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 6, 2011
 */
public class HGDBOntologyRepositoryTest {

	HGDBOntology o;
	HGDBOntologyRepository r;
	HGDBOntologyManager m;
	OWLDataFactoryHGDB df =  OWLDataFactoryHGDB.getInstance();
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
		r = new HGDBOntologyRepository();
		m = r.createOWLOntologyManager();
		IRI ontoIRI = IRI.create("hgdb://UNITTESTONTO 1");
		o = (HGDBOntology) m.createOntology(ontoIRI);
		 OWLDataFactoryHGDB.getInstance();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		r.dispose();
	}

	/**
	 * Test method for {@link org.hypergraphdb.app.owl.HGDBOntologyRepository#HGDBOntologyRepository()}.
	 */
	@Test
	public void testHGDBOntologyRepository() {
		createClassAndSubclassAxioms(100);
		
	}
	
	public void createClassAndSubclassAxioms(int n) {
		System.out.println("Creating Classes : " + n);
		OWLClass last = null;
		long start = System.nanoTime();
		for (int i = 0; i< n; i++) {
			OWLClass c = df.getOWLClass(IRI.create(ontoIRI.toString() + "#TEST" + n + ""));
			OWLDeclarationAxiom axiomD = df.getOWLDeclarationAxiom(c);
			m.addAxiom(o, axiomD);
			if (last != null) {
				OWLSubClassOfAxiom axiomS = df.getOWLSubClassOfAxiom(c, last);
				m.addAxiom(o, axiomS);
			}
			last = c;
		}
		long stop = System.nanoTime();
		System.out.println("Time: sec " + (stop - start) / 10E9d);
		int expectedAxiomCount = n * 2 - 1;
		int expectedEntityCount = n;
		int actualAxiomCount = o.getAxiomCount();
		int actualEntityCount = o.getEntitiesInSignature(ontoIRI).size();
		System.out.println("Actual AX: " + actualAxiomCount);
		System.out.println("Actual Ent: " + actualEntityCount);
		assertTrue("Axioms expected:" + expectedAxiomCount, actualAxiomCount == expectedAxiomCount);
		assertTrue("Entities expected:" + expectedEntityCount, actualEntityCount == expectedEntityCount);
		
	}


}

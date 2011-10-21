package org.hypergraphdb.app.owl.test;

import java.util.Arrays;
import java.util.Collection;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * OntologyManagerTest an abstract parameterized base class that will enable JUNIT 4 test runners to call each test twice.
 * Once against our OWL API Hypergraph implementation, and once against the original Manchester OWL API implementation. 
 * 
 * All necessary fields will be initialized to either API implentation after the first setup() call:
 *   
 * A subclass may call disposeHypergraph(), whenever it needs a freshly initialized HG store and an empty ontology o.
 * This will happen automatically at tearDownAfterClass().
 *   
 * o ... empty ontology
 * m ... OWLOntologyManager
 * df... Data Factory
 * s ... StopWatch
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 14, 2011
 */
@RunWith(value = Parameterized.class)
public abstract class OntologyManagerTest {
	public final static int USE_HYPERGRAPH_IMPLEMENTATION = 0; 
	public final static int USE_MANCHESTER_IMPLEMENTATION = 1; 
	
	private static boolean USE_HYPERGRAPH_ONTOLOGY = true;
	
	/** 
	 * An empty ontology.
	 */
	protected static OWLOntology o;
	
	/**
	 * An OwlOntologymanager to add axioms.
	 */
	protected static OWLOntologyManager m;
	
	/**
	 * A Datafactory. Never use new XXX to create Owl constructs or Axioms.
	 */
	protected static OWLDataFactory df;
	
	protected final static IRI ontoIRI = IRI.create("hgdb://UNITTESTONTO1");

	protected StopWatch s = new StopWatch();

	public OntologyManagerTest(int useImplementation) {
		USE_HYPERGRAPH_ONTOLOGY = (useImplementation == USE_HYPERGRAPH_IMPLEMENTATION);
	}
	
	/**
	 * This enables our runner to call each test twice, with and without Hypergraph. 
	 * Hypergraph and once using the original Manchester API implementation.
	 * @return
	 */
	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { USE_HYPERGRAPH_IMPLEMENTATION }, { USE_MANCHESTER_IMPLEMENTATION } };
		//Object[][] data = new Object[][] { { USE_HYPERGRAPH_IMPLEMENTATION } };
		return Arrays.asList(data);
	}


	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		disposeHypergraph();
	}
	
	/**
	 * Drops the whole hypergraphstore, if m is a HGDBOntologyManager.
	 */
	public static void disposeHypergraph() {
		if (m instanceof HGDBOntologyManager) {
			HGDBOntologyManager mHgdb = (HGDBOntologyManager)m;
			HGDBOntologyRepository r = mHgdb.getOntologyRepository();
			System.out.println("-------------------------------------------------------------------------------");
			System.out.print("Dropping Hypergraph containing " + r.getOntologies().size() + " Ontologies and ");
			System.out.print(r.getHyperGraph().count(hg.all()));
			System.out.println(" total Atoms.") ;
			r.dropHypergraph();
			r.dispose();
		}		
		df = null;
		m = null;
		o = null;
	}

	/**
	 * Initiializes o, m, df if o is null.
	 * o will be set to null after teardownAfterClass or after disposeHypergraph.
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		if (!isInitialized()) {
			System.out.println("-------------------------------------------------------------------------------");
			System.out.print("Creating Ontology Manager : ");
			if (USE_HYPERGRAPH_ONTOLOGY) {
				System.out.println(" HGDBOntologyManager ");
				m = HGDBOntologyRepository.createOWLOntologyManager();
			} else {
				System.out.println(" OWLOntologyManager (no Hypergraph) ");
				m = OWLManager.createOWLOntologyManager();
			}
			df = m.getOWLDataFactory();
			o = m.createOntology(ontoIRI);
		}
	}

	public boolean isInitialized() {
		return o != null;
	}
}

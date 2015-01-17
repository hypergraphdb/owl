package org.hypergraphdb.app.owl.test;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.hypergraphdb.util.HGUtils;
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
 * OntologyManagerTest an abstract parameterized base class that will enable
 * JUNIT 4 test runners to call each test twice. Once against our OWL API
 * Hypergraph implementation, and once against the original Manchester OWL API
 * implementation.
 * 
 * All necessary fields will be initialized to either API implentation after the
 * first setup() call:
 * 
 * A subclass may call tryCleanUp(), whenever it needs a freshly initialized HG
 * store and an empty ontology o. This will happen automatically at
 * tearDownAfterClass().
 * 
 * o ... empty ontology m ... OWLOntologyManager (static) df... Data Factory s
 * ... StopWatch
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 14, 2011
 */
@RunWith(value = Parameterized.class)
public abstract class OntologyManagerTest
{

	public final static int USE_HYPERGRAPH_IMPLEMENTATION = 0;
	public final static int USE_MANCHESTER_IMPLEMENTATION = 1;

	private boolean usingHypergraphMode;

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

	/**
	 * A HGDBOntologyRepository (Only use during usingHypergraphMode).
	 */
	protected static HGDBOntologyRepository r;

	protected final static IRI ontoIRI = IRI.create("hgdb://UNITTESTONTO1");

	protected StopWatch s = new StopWatch();

	public OntologyManagerTest(int useImplementation)
	{
		usingHypergraphMode = (useImplementation == USE_HYPERGRAPH_IMPLEMENTATION);
	}

	/**
	 * This enables our runner to call each test twice, with and without
	 * Hypergraph. Hypergraph and once using the original Manchester API
	 * implementation.
	 * 
	 * @return
	 */
	@Parameters
	public static Collection<Object[]> data()
	{
		// Object[][] data = new Object[][] { { USE_MANCHESTER_IMPLEMENTATION }
		// };
		Object[][] data = new Object[][] { { USE_HYPERGRAPH_IMPLEMENTATION }, { USE_MANCHESTER_IMPLEMENTATION } };
		return Arrays.asList(data);
	}

	/**
	 * Might be called in both modes.
	 * 
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		tryCleanUp();
	}

	/**
	 * Drops the whole hypergraphstore, if m is a PHGDBOntologyManagerImpl.
	 */
	public static void tryCleanUp()
	{
		if (m instanceof HGDBOntologyManager)
		{
			HGDBOntologyManager mHgdb = (HGDBOntologyManager) m;
			HGDBOntologyRepository r = mHgdb.getOntologyRepository();
			System.out.println("-------------------------------------------------------------------------------");
			System.out.print("Dropping Hypergraph containing " + r.getOntologies().size() + " Ontologies and ");
			System.out.print(r.getHyperGraph().count(hg.all()));
			System.out.println(" total Atoms.");
			r.dispose();
			HGUtils.dropHyperGraphInstance(r.getHyperGraph().getLocation());
		}
		else			
			HGUtils.dropHyperGraphInstance(System.getProperty("java.io.tmpdir") + File.separator + "hgdbtest");
		df = null;
		m = null;
		o = null;
		r = null;
	}

	/**
	 * Initiializes o, m, df if o is null. o will be set to null after
	 * teardownAfterClass or after disposeHypergraph.
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		if (!isInitialized())
		{
			// we either start or switch modes
			tryCleanUp();
			System.out.println("--------------------------------------------------------------------------------");
			if (usingHypergraphMode)
			{
				initializeHypergraphMode();
			}
			else
			{
				initializeManchesterMode();
			}
			df = m.getOWLDataFactory();
			createOntologyO();
		} // else another method in the same class in the same mode gets called.
			// with the same potentially modified testdata.
	}

	public void createOntologyO() throws Exception
	{
		o = m.createOntology(ontoIRI);
	}

	public void initializeHypergraphMode()
	{
		System.out.print("TESTRUN in HYPERGRAPH MODE: ");
		System.out.println(" PHGDBOntologyManagerImpl ");
		String dblocation = System.getProperty("java.io.tmpdir") + File.separator + "hgdbtest";
		m = new HGOntologyManagerFactory().getOntologyManager(dblocation); 
		r = ((HGDBOntologyManager) m).getOntologyRepository();
	}

	public void initializeManchesterMode()
	{
		System.out.print("TESTRUN in MANCHESTER MODE: ");
		System.out.println(" OWLOntologyManager (no Hypergraph) ");
		m = OWLManager.createOWLOntologyManager();
		r = null;
	}

	public boolean isHypergraphMode()
	{
		return o instanceof HGDBOntologyImpl;
	}

	/**
	 * @return true, if we have a valid ontology o, whose class matches the
	 *         current mode.
	 */
	public boolean isInitialized()
	{
		if (o != null)
		{
			if (usingHypergraphMode)
			{
				return o instanceof HGDBOntology;
			}
			else
			{
				return !(o instanceof HGDBOntology);
			}
		}
		else
		{
			return false;
		}
	}
}

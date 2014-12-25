package org.hypergraphdb.app.owl.test;

import java.io.File;
import java.util.Date;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.HGDBOntologyOutputTarget;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Issue sent by Boris 2012.03.01. Text: See attached test program. The main has
 * 3 lines: first is to create and populate HGDB, second to run the test against
 * HGDB and 3d to run the test against OWL files. Changes paths in the source to
 * match your local paths. You can get the actual ontology files from
 * \\olsportaldev\work\cirmservices\src\ontology. The problem is the following:
 * ontology O2 imports ontology O1. I have an individual in O1 and I want the
 * reasoner to give me all classes this indivudal belongs to. If I initialize
 * the reasoner with O1, it works no problem. If I initialize with O2 (which
 * imports O1), it doesn't work with HGDB, it only works with the default OWLAPI
 * loading the ontology from the files.
 * 
 * TestOWLHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 1, 2012
 */
public class TestOWLHGDB
{
	// public static String ONTOLOGY_DIRECTORY =
	// "c:/work/cirmservices/src/ontology";
	public static String ONTOLOGY_DIRECTORY = "c:/_CIRM/testontos/2012.03 Boris";
	private static OWLOntologyManager manager;
	private static HGDBOntologyRepository repository;

	private static String databaseLocation = "c:/temp/ontdb";

	static IRI getHGDBIRI(IRI ontologyIRI)
	{
		String iriNoScheme = ontologyIRI.toString();
		String scheme = ontologyIRI.getScheme();
		iriNoScheme = iriNoScheme.substring(scheme.length());
		IRI docIRI = IRI.create("hgdb" + iriNoScheme);
		return docIRI;
	}

	public static void registerRepoToResolveImports()
	{
		manager.addIRIMapper(new OWLOntologyIRIMapper()
		{
			@Override
			public IRI getDocumentIRI(IRI ontologyIRI)
			{
				IRI docIRI = getHGDBIRI(ontologyIRI);
				System.out.println("HGDBIRIMapper: " + ontologyIRI + " -> " + docIRI);
				if (repository.existsOntologyByDocumentIRI(docIRI))
				{
					return docIRI;
				}
				else
				{
					return null;
				}
			}
		});
	}

	public static void importOntology(File ontologyFile)
	{
		// 1) Load in Memory
		OWLOntology loadedOntology = null;
		try
		{
			System.out.print("Loading Ontology from file: " + ontologyFile.getAbsolutePath() + " ...");
			loadedOntology = manager.loadOntologyFromOntologyDocument(ontologyFile);
			System.out.println("Done.");
		}
		catch (OWLOntologyCreationException ocex)
		{
			System.err.println("Error loading ontology from: " + ontologyFile.getAbsolutePath());
			ocex.printStackTrace();
			System.exit(-1);
		}
		// 2) Change Format, create repo url with hgdb://
		// Define a repository document IRI for our ontology
		IRI targetIRI = getHGDBIRI(loadedOntology.getOntologyID().getOntologyIRI());
		HGDBOntologyOutputTarget target = new HGDBOntologyOutputTarget(targetIRI);
		// Manager will find our HGDBStorer based on the format and
		// import the ontology into our repo. Same for other Formats.
		try
		{
			System.out.print("Importing: " + loadedOntology.getOntologyID().getOntologyIRI() + " -> " + target + " ...");
			manager.saveOntology(loadedOntology, new HGDBOntologyFormat(), targetIRI);
			System.out.print("Done.");
		}
		catch (OWLOntologyStorageException e)
		{
			System.err.println("Error saving ontology: " + ontologyFile.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public static void initDatabase()
	{
		System.out.println("Init Database :" + new Date());
		manager = HGDBOWLManager.createOWLOntologyManager(databaseLocation);
		manager.setSilentMissingImportsHandling(false);
		repository = ((HGDBOntologyManagerImpl) manager).getOntologyRepository();
		registerRepoToResolveImports();
		repository.printAllOntologies();
		repository.printStatistics();
		System.out.println("End Init Database :" + new Date());
	}

	public static void populateDatabase()
	{
		importOntology(new File(ONTOLOGY_DIRECTORY + "/County_Working.owl"));
		importOntology(new File(ONTOLOGY_DIRECTORY + "/csr.owl"));
	}

	public static void testWithDatabase() throws Exception
	{
		System.out.println("Test with Database :" + new Date());
		OWLOntology o1 = manager.loadOntology(IRI.create("http://www.miamidade.gov/ontology")), o2 = manager.loadOntology(IRI
				.create("http://www.miamidade.gov/cirm/legacy"));

		OWLNamedIndividual ind = manager.getOWLDataFactory().getOWLNamedIndividual(
				IRI.create("http://www.miamidade.gov/ontology#LegacyServiceCaseListInput"));
		OWLReasonerFactory reasonerFactory = (OWLReasonerFactory) Class
				.forName("com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory").getMethod("getInstance", new Class[0])
				.invoke(null, new Object[0]);

		o2.getOWLOntologyManager().setSilentMissingImportsHandling(false);
		System.out.println(o2.getOWLOntologyManager());
		System.out.println(o2.getOWLOntologyManager().getOWLDataFactory());
		System.out.println(o2.getOWLOntologyManager().getImportsClosure(o2));
		OWLReasoner reasoner = reasonerFactory.createReasoner(o2);
		System.out.println(reasoner.getTypes(ind, false));
		System.out.println("END: Test with Database :" + new Date());
	}

	public static void testWithFiles() throws Exception
	{

		System.out.println("Test with Files :" + new Date());
		File f1 = new File(ONTOLOGY_DIRECTORY + "/County_Working.owl"), f2 = new File(ONTOLOGY_DIRECTORY + "/csr.owl");
		manager = OWLManager.createOWLOntologyManager();
		OWLOntology o1 = manager.loadOntologyFromOntologyDocument(f1), o2 = manager.loadOntologyFromOntologyDocument(f2);

		OWLNamedIndividual ind = manager.getOWLDataFactory().getOWLNamedIndividual(
				IRI.create("http://www.miamidade.gov/ontology#LegacyServiceCaseListInput"));
		OWLReasonerFactory reasonerFactory = (OWLReasonerFactory) Class
				.forName("com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory").getMethod("getInstance", new Class[0])
				.invoke(null, new Object[0]);

		o2.getOWLOntologyManager().setSilentMissingImportsHandling(false);
		System.out.println(o2.getOWLOntologyManager());
		System.out.println(o2.getOWLOntologyManager().getOWLDataFactory());
		System.out.println(o2.getOWLOntologyManager().getImportsClosure(o2));
		OWLReasoner reasoner = reasonerFactory.createReasoner(o2);
		org.semanticweb.owlapi.reasoner.NodeSet<?> n = reasoner.getTypes(ind, false);
		System.out.println(n);
		System.out.println("END: Test with Files :" + new Date());
	}

	public static void main(String[] args)
	{
		try
		{
			// initDatabase(); populateDatabase();
			initDatabase();
			testWithDatabase();
			testWithFiles();
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
	}
}

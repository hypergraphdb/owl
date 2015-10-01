package org.hypergraphdb.app.owl.usage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyOutputTarget;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLRuntimeException;

/**
 * ImportOntologies.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 26, 2012
 */
public class ImportOntologies
{

	private static HGDBOntologyManager manager;
	private static OntologyDatabase repository;

	/**
	 * [0]...Repository Folder [0..n]...Ontology files to import (Topologically
	 * sorted by import declarations.)
	 * 
	 * @param argv
	 */
	public static void main(String[] argv)
	{
		if (argv.length < 2)
		{
			printHelp();
			System.exit(-1);
		}
		File[] files = createAndValidateFileArray(argv);
		System.out.println("Initializing Ontology Manager and  Repository...");
		manager = HGOntologyManagerFactory.getOntologyManager(files[0].getAbsolutePath());
		// repository = (VHGDBOntologyRepository)
		// manager.getOntologyRepository();
		if (files.length == 1)
		{
			printRepository();
		}
		else
		{
			// Import
			registerRepoToResolveImports();
			for (int i = 1; i < files.length; i++)
			{
				importOntology(files[i]);
			}
		}
	}

	/**
	 * 
	 */
	private static void printRepository()
	{
		repository.printAllOntologies();
		repository.printStatistics();

	}

	/**
	 * If an import needs to be resolved by the manager, we can look up our
	 * repository. This will be called before all other IRImappers and only if:
	 * - The manager has not loaded an ontology with the ontologyIRI already;
	 * loaded will be used first.
	 * 
	 */
	public static void registerRepoToResolveImports()
	{
		manager.addIRIMapper(new OWLOntologyIRIMapper()
		{

			@Override
			public IRI getDocumentIRI(IRI ontologyIRI)
			{
				String iriNoScheme = ontologyIRI.toString();
				String scheme = ontologyIRI.getScheme();
				iriNoScheme = iriNoScheme.substring(scheme.length());
				IRI docIRI = IRI.create("hgdb" + iriNoScheme);
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
		importOntology(ontologyFile, manager);
	}

	public static IRI importOntology(File ontologyLocation, OWLOntologyManager manager)
	{
		try
		{
			return importOntology(ontologyLocation.toURI().toURL(), manager);
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 
	 * @param ontologyFile
	 * @param manager
	 * @return target generated documentIRI
	 */
	public static IRI importOntology(URL ontologyLocation, OWLOntologyManager manager)
	{
		// 1) Load in Memory
		OWLOntology loadedOntology = null;
		InputStream inputStream = null;
		try
		{			
			System.out.print("Loading Ontology from : " + ontologyLocation + " ...");
			inputStream = ontologyLocation.openStream();
			loadedOntology = manager.loadOntologyFromOntologyDocument(inputStream);
			System.out.println("Done.");
		}
		catch (OWLOntologyCreationException ocex)
		{
			throw new OWLRuntimeException("Error loading ontology from: " + ontologyLocation, ocex);
		}
		catch (IOException ex)
		{
			throw new RuntimeException("Failed to read from URL " + ontologyLocation);
		}
		finally
		{
			if (inputStream != null) try { inputStream.close(); } catch (Throwable t) { }
		}
		// 2) Change Format, create repo url with hgdb://
		// Define a repository document IRI for our ontology
		IRI targetIRI = IRI.create("hgdb://" + loadedOntology.getOntologyID().getOntologyIRI());
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
			throw new OWLRuntimeException("Error saving ontology: " + ontologyLocation, e);
		}
		return targetIRI;
	}

	public static File[] createAndValidateFileArray(String[] argv)
	{
		File[] files = new File[argv.length];
		for (int i = 0; i < argv.length; i++)
		{
			files[i] = new File(argv[i]);
		}
		// Create Repo directory if not exists.
		if (!files[0].exists())
		{
			files[0].mkdirs();
		}
		for (File f : files)
		{
			if (!f.exists())
			{
				throw new IllegalArgumentException("File does not exist: " + f);
			}
		}
		return files;
	}

	/**
	 * 
	 */
	private static void printHelp()
	{
		System.out.println("***** ImportOntologies usage ********");
		System.out.println("Parameters: ");
		System.out.println("[0] First parameter is repository directory path (Will be used or created)");
		System.out
				.println("[1] Ontology file to be imported into the repository. All dependent ontologies need to be in the repository or accessible via http.");
		System.out
				.println("[2..m] More Ontology files to be imported. All dependent ontologies need to be in the repository or accessible via http.");
		System.out.println("A high index ontology may import a low index ontology; not vice versa unless accessible via http.");
		System.out.println("Therefore, it's best to sort the ontolgies to be imported topologically by import declarations.");
		System.out.println("Call with first parameter only to print a list of stored ontologies.");
	}
}

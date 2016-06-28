package org.hypergraphdb.app.owl.usage;

import java.io.File;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.ActivityUtils;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.semanticweb.owlapi.io.FileDocumentSource;

/**
 * 
 * <p>
 * Utility methods and a command line tool to perform one time import/export operations on HGDB-backed ontologies. 
 * </p>
 *
 * TODO: this was create to simplify HGDBOntologyManager and consolidate the OntologyImporter and
 * ImportOntologies classes in this package under one class.
 *  
 * @author Borislav Iordanov
 *
 */
public class ExportImport
{
	private String graphLocation;
	
	HyperGraph graph() 
	{
		return ImplUtils.owldb(graphLocation);
	}
	
	public ExportImport(String graphLocation)
	{
		
	}
	
	/**
	 * Imports a versioned Ontology from a VOWLXMLFormat saved file into the
	 * repository. All revisions, changesets and the full workingset will be
	 * stored.
	 * 
	 * TODO - this method should either be removed from this interface, or a corresponding
	 * exportVersionedOntology should be added!
	 * 
	 * @param vowlxmlFile An XML file containing the version ontology with its full version
	 * history.
	 * @return The runtime object representing the newly loaded {@link org.hypergraphdb.app.owl.versioning.VersionedOntology}.
	 */
	public VersionedOntology importVersionedOntology(File vowlxmlFile)
	{
		OntologyDatabase ontologyRepository = new OntologyDatabase(graphLocation);
		if (!vowlxmlFile.exists())
			throw new IllegalArgumentException("File does not exist: " + vowlxmlFile);
		final HGDBOntologyManager manager = HGOntologyManagerFactory.getOntologyManager(graphLocation);
		final FileDocumentSource fds = new FileDocumentSource(vowlxmlFile);
		HyperGraph graph = ontologyRepository.getHyperGraph();
		return graph.getTransactionManager().ensureTransaction(new Callable<VersionedOntology>()
		{
			public VersionedOntology call()
			{
				try
				{
					VOWLXMLDocument doc = ActivityUtils.parseVersionedDoc(manager, fds);
					return ActivityUtils.storeClonedOntology(manager, doc);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			}
		});		
	}
	
	public static void main(String[] args)
	{
	}

}

package org.hypergraphdb.app.owl;

import java.io.File;

import java.io.IOException;

import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * HGDBOntologyManager.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface HGDBOntologyManager extends OWLOntologyManager
{

	/**
	 * @return the dbRepository
	 */
	OntologyDatabase getOntologyRepository();

	VersionManager getVersionManager();	

	/**
	 * Determines if at least one In Memory ontology is managed.
	 * 
	 * @return
	 */
	boolean hasInMemoryOntology();

	/**
	 * Imports a versioned Ontology from a VOWLXMLFormat saved file into the
	 * repository. All revisions, changesets and the full workingset will be
	 * stored.
	 * 
	 * @param vowlxmlFile
	 * @return
	 * @throws OWLOntologyChangeException
	 * @throws UnloadableImportException
	 * @throws HGDBOntologyAlreadyExistsByDocumentIRIException
	 * @throws HGDBOntologyAlreadyExistsByOntologyIDException
	 * @throws HGDBOntologyAlreadyExistsByOntologyUUIDException
	 * @throws OWLParserException
	 * @throws IOException
	 */
	VersionedOntology importVersionedOntology(File vowlxmlFile) throws OWLOntologyChangeException,
			UnloadableImportException, HGDBOntologyAlreadyExistsByDocumentIRIException,
			HGDBOntologyAlreadyExistsByOntologyIDException, HGDBOntologyAlreadyExistsByOntologyUUIDException, OWLParserException,
			IOException;


	/**
	 * <p>
	 * Import an ontology from the given ontology document.
	 * </p>
	 * 
	 * <p>
	 * For example, to import an ontology from a file, do:
	 * </p>
	 * <pre><code>
	 * OntologyDatabase db = new OntologyDatabase(hypergraphLocation);
	 * db.importInDatabase(IRI.create(new File("/home/me/myontology.owl");
	 * </code></pre>
	 * 
	 * @param documentIRI The IRI identifying the physical location of the ontology
	 * to import. 
	 * @return The newly created, database-backed 
	 * {@link org.hypergraphdb.appl.owl.HGDBOntology} instance.
	 */
	HGDBOntology importOntology(IRI documentIRI);
	
	/**
	 * Gets the current long task Size for saveAs and open. This is thread safe.
	 * The underlying fields need to be volatile.
	 * 
	 * @return
	 */
	int getCurrentTaskSize();

	/**
	 * Gets the current progress task value for saveAs and open.
	 * 
	 * @return a value that is lower or equal to CurrentTaskSize
	 */
	int getCurrentTaskProgress();
}
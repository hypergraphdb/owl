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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * <p>
 * A HyperGraphDB extension of the OWLAPI <code>OWLOntologyManager<code>.
 * This class provides access to the underlying {@link OntologyDatabase} repository,
 * {@link VersionManager} and the HyperGraphDB-bound OWL data factory. 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County), Borislav Iordanov
 * @created Jan 13, 2012
 */
public interface HGDBOntologyManager extends OWLOntologyManager
{
	/**
	 * @return The {@link OntologyDatabase} repository where ontologies are persisted.
	 */
	OntologyDatabase getOntologyRepository();

	/**
	 * <p>Return the {@link org.hypergraphdb.app.owl.versioning.VersionManager} instance associated
	 * with this ontology manager. There is always a <code>VersionManager</code> available even if
	 * no ontologies in the database are currently under version control.
	 */
	VersionManager getVersionManager();	

	/**
	 * Imports a versioned Ontology from a VOWLXMLFormat saved file into the
	 * repository. All revisions, changesets and the full workingset will be
	 * stored.
	 * 
	 * TODO - this method should either be removed from this interface, or a corresponding
	 * exportVersionedOntology should be added!
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
	 * Import an ontology into the database from the given ontology document.
	 * </p>
	 * 
	 * <p>
	 * For example, to import an ontology from a file, do:
	 * </p>
	 * <pre><code>
	 * importOntology(IRI.create(new File("/home/me/myontology.owl");
	 * </code></pre>
	 * 
	 * @param documentIRI The IRI identifying the physical location of the ontology
	 * to import. 
	 * @return The newly created, database-backed 
	 * {@link org.hypergraphdb.appl.owl.HGDBOntology} instance.
	 */
	HGDBOntology importOntology(IRI documentIRI);
	
	/**
	 * Return the <code>OWLDataFactory</code> bound to the HyperGraphDB instance 
	 * of this ontology manager. Each graph instance gets its own data factory, which
	 * immediately perists all objects created through it.
	 */
	OWLDataFactory getDataFactory();
	
	/**
	 * <p>
	 * Create a new ontology and store it immediately in the database. The document IRI of the 
	 * ontology will be constructed by replacing the schema of the provided ontology ID IRI with
	 * <code>hgdb</code> as all HyperGraphDB-backed document IRIs are.
	 * </p> 
	 * 
	 * <p>
	 * Note that the normal <code>OWLOntologyManager.createOntology(IRI)</code> method will just
	 * create an in-memory ontology that will not be stored in the database. We don't  
	 * overwriting that method, even though it would seem to be the logical thing to do, because
	 * tools such as Protege like to freely create temporary/new ontology without too much concern
	 * of memory or side-effects.
	 * </p>
	 */
	HGDBOntology createOntologyInDatabase(IRI ontologyIRI) throws OWLOntologyCreationException;
	
}
package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGPersistentHandle;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * VersionedOntologyID.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VersionedOntologyID {

	/**
	 * One id that is equal for all Revisions of the ontology and survives downtime.
	 * Unique between Ontologies.
	 * @return
	 */
	HGPersistentHandle getPersistentID();

	/**
	 * A unique
	 * @return
	 */
	RevisionID getRevisionID();
	
}

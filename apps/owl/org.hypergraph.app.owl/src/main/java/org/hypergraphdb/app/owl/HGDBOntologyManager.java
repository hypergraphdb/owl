package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * HGDBOntologyManager.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface HGDBOntologyManager extends OWLOntologyManager {

	/**
	 * @return the dbRepository
	 */
	public abstract HGDBOntologyRepository getOntologyRepository();

	/**
	 * Determines if at least one In Memory ontology is managed.
	 * @return
	 */
	public abstract boolean hasInMemoryOntology();

}
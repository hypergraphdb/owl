package org.hypergraphdb.app.owl.exception;

import org.hypergraphdb.HGPersistentHandle;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * HGDBOntologyAlreadyExistsByOntologyUUIDException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 9, 2012
 */
public class HGDBOntologyAlreadyExistsByOntologyUUIDException extends OWLOntologyCreationException {
	
	private static final long serialVersionUID = 1L;

	private HGPersistentHandle ontologyUUID;
	
	public HGDBOntologyAlreadyExistsByOntologyUUIDException(HGPersistentHandle ontologyUUID) {
	    this.ontologyUUID = ontologyUUID;
	}
	
	public HGPersistentHandle getOntologyUUID() {
	    return ontologyUUID;
	}
}
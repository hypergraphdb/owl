package org.hypergraphdb.app.owl.exception;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * HGDBOntologyAlreadyExistsByOntologyUUIDException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 9, 2012
 */
public class HGDBOntologyAlreadyExistsByOntologyUUIDException extends OWLOntologyCreationException {
	
	private static final long serialVersionUID = 1L;

	private HGHandle ontologyUUID;
	
	public HGDBOntologyAlreadyExistsByOntologyUUIDException(HGHandle ontologyUUID) {
	    this.ontologyUUID = ontologyUUID;
	}
	
	public HGHandle getOntologyUUID() {
	    return ontologyUUID;
	}
}
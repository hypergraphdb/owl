package org.hypergraphdb.app.owl.exception;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * HGDBOntologyAlreadyExistsByOntologyIDException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 3, 2012
 */
public class HGDBOntologyAlreadyExistsByOntologyIDException extends OWLOntologyCreationException {
	
	private OWLOntologyID ontologyID;
	
	public HGDBOntologyAlreadyExistsByOntologyIDException(OWLOntologyID ontologyID) {
	    this.ontologyID = ontologyID;
	}
	
	public OWLOntologyID getOntologyID() {
	    return ontologyID;
	}
	

}

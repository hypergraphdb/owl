package org.hypergraphdb.app.owl.exception;

import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * HGDBOntologyAlreadyExistsByOntologyIDException.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 3, 2012
 */
public class HGDBOntologyAlreadyExistsByOntologyIDException extends OWLOntologyCreationException {
	
	private static final long serialVersionUID = 6791502581072294124L;

	private OWLOntologyID ontologyID;
	
	public HGDBOntologyAlreadyExistsByOntologyIDException(OWLOntologyID ontologyID) {
	    this.ontologyID = ontologyID;
	}
	
	public OWLOntologyID getOntologyID() {
	    return ontologyID;
	}
	

}

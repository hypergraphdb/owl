package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VHGDBOntologyChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VHGDBOntologyChange extends HGLink {	
	
	public static enum ChangeType{
		ADD, REMOVE, MODIFY
	}
	
	ChangeType getChangeType();
	
	OWLOntologyChange convertToOWLOntologyChange();
	
	OWLOntologyChange convertToRevertOWLOntologyChange();
}

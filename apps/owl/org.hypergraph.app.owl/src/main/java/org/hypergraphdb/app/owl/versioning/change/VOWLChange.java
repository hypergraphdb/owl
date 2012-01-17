package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * Change.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VOWLChange extends HGLink {	
	
	OWLOntologyChange convertToOWLOntologyChange();
	
	OWLOntologyChange convertToInverseOWLOntologyChange();
}

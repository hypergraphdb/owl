package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * OntologyAnnotationChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface OntologyAnnotationChange extends VOWLChange {
	
	HGHandle getOntologyAnnotation();
	
}

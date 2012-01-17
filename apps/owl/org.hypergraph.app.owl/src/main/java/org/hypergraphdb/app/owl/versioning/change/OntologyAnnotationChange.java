package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * OntologyAnnotationChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class OntologyAnnotationChange extends VOWLChange {
	
	private HGHandle ontologyAnnotationHandle;
	
	public OntologyAnnotationChange(HGHandle...args) {
		ontologyAnnotationHandle = args[0];
    }

	HGHandle getOntologyAnnotation() {
		return ontologyAnnotationHandle;
	}
	
}

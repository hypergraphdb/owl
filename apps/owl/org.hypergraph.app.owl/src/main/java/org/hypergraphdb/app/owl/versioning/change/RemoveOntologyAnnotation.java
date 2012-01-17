package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * RemoveOntologyAnnotation.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class RemoveOntologyAnnotation extends OntologyAnnotationChange {
	
	public RemoveOntologyAnnotation(HGHandle...args) {
    	super(args[0]);
    }

}

package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VRemoveOntologyAnnotationChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveOntologyAnnotationChange extends VOntologyAnnotationChange {
	
	public VRemoveOntologyAnnotationChange(HGHandle...args) {
    	super(args[0]);
    }
}
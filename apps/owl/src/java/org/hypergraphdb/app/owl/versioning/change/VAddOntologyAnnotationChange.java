package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VAddOntologyAnnotationChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddOntologyAnnotationChange extends VOntologyAnnotationChange {
	
	public VAddOntologyAnnotationChange(HGHandle...args) {
    	super(args[0]);
    }
}

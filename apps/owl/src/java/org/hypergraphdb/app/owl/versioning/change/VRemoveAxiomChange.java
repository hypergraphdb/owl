package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VRemoveAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveAxiomChange extends VAxiomChange {
	
	public VRemoveAxiomChange(HGHandle...args) {
    	super(args[0]);
    }
}
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VAddAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddAxiomChange extends VAxiomChange {
    
	public VAddAxiomChange(HGHandle...args) {
    	super(args[0]);
    }
}

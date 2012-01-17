package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * AddAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class AddAxiomChange extends AxiomChange {
    
	public AddAxiomChange(HGHandle...args) {
    	super(args[0]);
    }
}

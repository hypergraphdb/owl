package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * RemoveAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class RemoveAxiomChange extends AxiomChange {
	
	public RemoveAxiomChange(HGHandle...args) {
    	super(args[0]);
    }

}

package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VAddImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddImportChange extends VImportChange {
	
	public VAddImportChange(HGHandle...args) {
    	super(args[0]);
    }

}

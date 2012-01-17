package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * AddImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class AddImportChange extends ImportChange {
	
	public AddImportChange(HGHandle...args) {
    	super(args[0]);
    }

}

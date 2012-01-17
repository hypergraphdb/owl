package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * RemoveImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class RemoveImportChange extends ImportChange {
		
	public RemoveImportChange(HGHandle...args) {
    	super(args[0]);
    }
}

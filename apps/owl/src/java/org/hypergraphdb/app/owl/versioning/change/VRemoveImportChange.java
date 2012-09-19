package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VRemoveImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveImportChange extends VImportChange {
		
	public VRemoveImportChange(HGHandle...args) {
    	super(args[0]);
    }
}
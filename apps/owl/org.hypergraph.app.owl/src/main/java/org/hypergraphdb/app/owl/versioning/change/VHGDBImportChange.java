package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VHGDBImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VHGDBImportChange extends VHGDBOntologyChange {
	
	HGHandle getImportDeclaration();
	
}

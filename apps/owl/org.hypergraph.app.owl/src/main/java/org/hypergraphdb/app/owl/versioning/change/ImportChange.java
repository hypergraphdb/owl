package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * ImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class ImportChange extends VOWLChange {
	
	private HGHandle importDeclarationHandle;
	
	public ImportChange(HGHandle...args) {
		importDeclarationHandle = args[0];
    }
	
	HGHandle getImportDeclaration() {
		return importDeclarationHandle;
	}
	
}

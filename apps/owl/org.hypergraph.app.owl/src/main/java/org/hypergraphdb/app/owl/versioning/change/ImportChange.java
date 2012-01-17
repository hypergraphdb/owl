package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * ImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class ImportChange extends VOWLChange {
	
	private HGHandle importDeclarationHandle;
	
	HGHandle getImportDeclaration() {
		return importDeclarationHandle;
	}
	
}

package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * ModifyOntologyIDChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class ModifyOntologyIDChange extends VOWLChange {
	
	private HGHandle oldOntologyIDHandle;

	private HGHandle newOntologyIDHandle;
	
	HGHandle getOldOntologyID() {
		return oldOntologyIDHandle;
	}
	
	HGHandle getNewOntologyID() {
		return newOntologyIDHandle;
	}
	
}

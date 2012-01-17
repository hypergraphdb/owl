package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * ModifyOntologyIDChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface ModifyOntologyIDChange extends VOWLChange {
	
	HGHandle getOldOntologyID();
	
	HGHandle getNewOntologyID();
	
}

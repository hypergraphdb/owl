package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;

/**
 * VHGDBSetOntologyID.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VHGDBSetOntologyID extends VHGDBOntologyChange {
	
	HGHandle getOldOntologyID();
	
	HGHandle getNewOntologyID();
	
}

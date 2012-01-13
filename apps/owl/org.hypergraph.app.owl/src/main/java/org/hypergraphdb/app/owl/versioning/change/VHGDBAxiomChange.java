package org.hypergraphdb.app.owl.versioning.change;

import java.util.Set;

import org.hypergraphdb.HGHandle;

/**
 * VHGDBAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VHGDBAxiomChange extends VHGDBOntologyChange {
	
	HGHandle getAxiom();
	
	Set<HGHandle> getEntities();
}

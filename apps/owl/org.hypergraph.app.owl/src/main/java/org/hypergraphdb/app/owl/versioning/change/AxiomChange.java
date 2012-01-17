package org.hypergraphdb.app.owl.versioning.change;

import java.util.Set;

import org.hypergraphdb.HGHandle;

/**
 * AxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface AxiomChange extends Change {
	
	HGHandle getAxiom();
	
	Set<HGHandle> getEntities();
}

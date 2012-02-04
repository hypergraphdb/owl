package org.hypergraphdb.app.owl.core;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

/**
 * HGChangeableLink allows setting a target.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 27, 2011
 */
public interface HGChangeableLink extends HGLink {

	/**
	 * Sets the target at position i to a new atom handle.
	 * User needs to call graph.update after calling this method. 
	 * 
	 * @param i index between 0 and arity-1 of the link.
	 * @param handle non-null handle.
	 */			
	void setTargetAt(int i, HGHandle handle);
}

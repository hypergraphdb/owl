package org.hypergraphdb.app.owl.core;

import java.util.Map;

/**
 * PrefixHolder.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 3, 2012
 */
public interface PrefixHolder {

	/**
	 * Returns a map of prefixNames to namespace.
	 * @return
	 */
	Map<String, String> getPrefixes();
	
	/**
	 * Sets a Map of PrefixNames to Prefixes.
	 * @return
	 */
	void setPrefixesFrom(Map<String, String> prefixMap);
	

}

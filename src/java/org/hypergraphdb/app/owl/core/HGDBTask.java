package org.hypergraphdb.app.owl.core;

/**
 * HGTask.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 6, 2012
 */
public interface HGDBTask {
	
	/**
	 * @return the taskTotalAxioms (volatile)
	 */
	int getTaskSize();

	/**
	 * @return the taskCurrentAxioms (volatile)
	 */
	int getTaskProgess();

	void cancelTask();
}

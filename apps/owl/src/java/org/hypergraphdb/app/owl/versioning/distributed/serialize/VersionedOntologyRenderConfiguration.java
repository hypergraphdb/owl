package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import org.hypergraphdb.app.owl.versioning.VersioningObject;
import org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor;

/**
 * VersionedOntologyRenderConfiguration.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VersionedOntologyRenderConfiguration implements VersioningObject {

	private int firstRevisionIndex;
	private int lastRevisionIndex;
	private boolean lastRevisionData;
	private boolean uncommittedChanges;

	/**
	 * Default is render all revisions, head data and uncommitted.
	 */
	public VersionedOntologyRenderConfiguration() {
		setFirstRevisionIndex(0);
		setLastRevisionIndex(Integer.MAX_VALUE);
		setLastRevisionData(true);
		setUncommittedChanges(true);
	}
	
	/**
	 * Revisions and Changesets only, starting with first, no uncommitted, no data.
	 * @param first
	 */
	public VersionedOntologyRenderConfiguration(int first) {
		setFirstRevisionIndex(first);
		setLastRevisionIndex(Integer.MAX_VALUE);
		setLastRevisionData(false);
		setUncommittedChanges(false);
	}
		
	/**
	 * @return the firstRevisionIndex
	 */
	public int getFirstRevisionIndex() {
		return firstRevisionIndex;
	}
	
	/**
	 * Set the first revision's index to be configured. No value lower than 0 tolerated. 
	 * @param firstRevisionIndex the firstRevisionIndex to set 0..default
	 */
	public void setFirstRevisionIndex(int firstRevisionIndex) {
		if (firstRevisionIndex < 0) throw new IllegalArgumentException("firstRevisionIndex < 0, was " + firstRevisionIndex);
		this.firstRevisionIndex = firstRevisionIndex;
	}
	
	/**
	 * @return the index of the lastRevision, a higher number or Integer.Max
	 */
	public int getLastRevisionIndex() {
		return lastRevisionIndex;
	}

	/**
	 * Set the last revision index of the revision to be included.
	 * If a the value set is higher than the number of revisions in the versioned ontology,
	 * no error will be thrown and head will be included. 
	 * @param lastRevisionIndex the lastRevisionIndex to configure. Integer.Max is default.
	 */
	public void setLastRevisionIndex(int lastRevisionIndex) {
		this.lastRevisionIndex = lastRevisionIndex;
	}
	/**
	 * @return the lastRevisionData
	 */
	public boolean isLastRevisionData() {
		return lastRevisionData;
	}
	/**
	 * Sets if the ontology data of the last included revision in this configuration will be rendered.
	 *  
	 * If uncommited is set to false, 
	 * no uncommitted changes will be included, if the last revision is the head.
	 * A in memory rollback shall be performed before rendering.
	 * @param lastRevisionData the lastRevisionData to set default: false
	 */
	public void setLastRevisionData(boolean lastRevisionData) {
		this.lastRevisionData = lastRevisionData;
	}
	
	/**
	 * On false, neither the head changeset (after head) will be rendered, nor
	 * will the lastRevisionData include any uncommitted changes.
	 * 
	 * @return the uncommittedChanges default: false
	 */
	public boolean isUncommittedChanges() {
		return uncommittedChanges;
	}
	/**
	 * Get whether both, the head changeset (after head) will be rendered, and 
	 * the lastRevisionData will include any uncommitted changes.
	 * 
	 * @param uncommittedChanges the uncommittedChanges to set
	 */
	public void setUncommittedChanges(boolean includeUncommited) {
		this.uncommittedChanges = includeUncommited;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor)
	 */
	@Override
	public void accept(VersioningObjectVisitor visitor) {
		visitor.visit(this);
	}
}

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
	private boolean headRevisionData;
	private boolean uncommittedChanges;

	/**
	 * Default is render all revisions, head data and uncommitted.
	 */
	public VersionedOntologyRenderConfiguration() {
		setFirstRevisionIndex(0);
		setLastRevisionIndex(Integer.MAX_VALUE);
		setHeadRevisionData(true);
		setUncommittedChanges(true);
	}
	
	/**
	 * Revisions and Changesets only, starting with first, no uncommitted, no data.
	 * @param first
	 */
	public VersionedOntologyRenderConfiguration(int first) {
		setFirstRevisionIndex(first);
		setLastRevisionIndex(Integer.MAX_VALUE);
		setHeadRevisionData(false);
		setUncommittedChanges(false);
	}
		
	/**
	 * @return the firstRevisionIndex
	 */
	public int getFirstRevisionIndex() {
		return firstRevisionIndex;
	}
	/**
	 * @param firstRevisionIndex the firstRevisionIndex to set 0..default
	 */
	public void setFirstRevisionIndex(int firstRevisionIndex) {
		this.firstRevisionIndex = firstRevisionIndex;
	}
	/**
	 * @return the lastRevisionIndex or Integer.Max
	 */
	public int getLastRevisionIndex() {
		return lastRevisionIndex;
	}
	/**
	 * @param lastRevisionIndex the lastRevisionIndex to set Integer.Max..default
	 */
	public void setLastRevisionIndex(int lastRevisionIndex) {
		this.lastRevisionIndex = lastRevisionIndex;
	}
	/**
	 * @return the headRevisionData
	 */
	public boolean isHeadRevisionData() {
		return headRevisionData;
	}
	/**
	 * Sets if the HeadrevisionData will be rendered. If uncommited is set to false, 
	 * no uncommitted changes will be included in the head revision data.
	 * A in memory rollback shall be performed before rendering.
	 * @param headRevisionData the headRevisionData to set default: false
	 */
	public void setHeadRevisionData(boolean headRevisionData) {
		this.headRevisionData = headRevisionData;
	}
	
	/**
	 * On false, neither the head changeset (after head) will be rendered, nor
	 * will the headRevisionData include any uncommitted changes.
	 * 
	 * @return the uncommittedChanges default: false
	 */
	public boolean isUncommittedChanges() {
		return uncommittedChanges;
	}
	/**
	 * Get whether both, the head changeset (after head) will be rendered, and 
	 * the headRevisionData will include any uncommitted changes.
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

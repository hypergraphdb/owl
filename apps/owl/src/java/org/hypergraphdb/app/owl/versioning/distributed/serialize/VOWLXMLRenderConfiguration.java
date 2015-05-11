package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;

/**
 * VOWLXMLRenderConfiguration.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLRenderConfiguration
{
	HGHandle firstRevision;
	HGHandle revisionSnapshot;
	Set<HGHandle> heads = new HashSet<HGHandle>();
	Set<HGHandle> roots = new HashSet<HGHandle>();
	int maxDepth = Integer.MAX_VALUE;
	boolean uncommittedChanges;
	
	/**
	 * Default is render all revisions, head data but no uncommitted changes.
	 */
	public VOWLXMLRenderConfiguration()
	{
		uncommittedChanges(false);
	}
	
	public int maxDepth()
	{
		return maxDepth;
	}
	

	public VOWLXMLRenderConfiguration maxDepth(int maxDepth)
	{
		this.maxDepth = maxDepth;
		return this;
	}
	
	public HGHandle revisionSnapshot()
	{
		return revisionSnapshot;
	}
	
	public VOWLXMLRenderConfiguration revisionSnapshot(HGHandle revisionSnapshot)
	{
		this.revisionSnapshot = revisionSnapshot;
		return this;
	}
	
	public HGHandle firstRevision()
	{
		return firstRevision;
	}
	
	public VOWLXMLRenderConfiguration firstRevision(HGHandle firstRevision)
	{
		this.firstRevision = firstRevision;
		return this;
	}
	
	public Set<HGHandle> roots()
	{
		return roots;
	}
	
	public Set<HGHandle> heads()
	{
		return heads;
	}

	/**
	 * Revisions and Changesets only, starting with first, no uncommitted, no
	 * data.
	 * 
	 * @param first
	 */
	public VOWLXMLRenderConfiguration(int first)
	{
		uncommittedChanges(false);
	}

	/**
	 * On false, neither the head changeset (after head) will be rendered, nor
	 * will the lastRevisionData include any uncommitted changes.
	 * 
	 * @return the uncommittedChanges default: false
	 */
	public boolean isUncommittedChanges()
	{
		return uncommittedChanges;
	}

	/**
	 * Get whether both, the head changeset (after head) will be rendered, and
	 * the lastRevisionData will include any uncommitted changes.
	 * 
	 * @param uncommittedChanges
	 *            the uncommittedChanges to set
	 */
	public VOWLXMLRenderConfiguration uncommittedChanges(boolean includeUncommited)
	{
		this.uncommittedChanges = includeUncommited;
		return this;
	}
}

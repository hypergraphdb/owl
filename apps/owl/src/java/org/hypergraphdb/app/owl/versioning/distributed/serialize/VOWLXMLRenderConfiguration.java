package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;

/**
 * <p>
 * Configures how a versioned ontology is serialized. One can configure what parts of the 
 * revision graph to serialize as well as transmitting a given snapshot (as of a 
 * specific revision). 
 * </p>
 * <p>
 * To configure the graph portions to serialize, specify a set
 * of revision "roots" and a set of revision "heads". The serialization will then
 * collect all revisions starting from the roots and stopping at any of the heads. 
 * Another way to stop the revision traversal in this process is with 
 * the <code>maxDepth</code> parameter. When one of the roots is the same as
 * the <code>firstRevision</code> parameter and the <code>heads</code> set is 
 * empty, then everything is being serialized.
 * </p>
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @author borislav
 * @created Feb 24, 2012
 */
public class VOWLXMLRenderConfiguration
{
	HGHandle firstRevision;
	HGHandle revisionSnapshot;
	Set<HGHandle> heads = new HashSet<HGHandle>();
	Set<HGHandle> roots = new HashSet<HGHandle>();
	int maxDepth = Integer.MAX_VALUE;
	
	/**
	 * Default is render all revisions, head data but no uncommitted changes.
	 */
	public VOWLXMLRenderConfiguration()
	{
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
	
	public Set<HGHandle> roots()
	{
		return roots;
	}
	
	public Set<HGHandle> heads()
	{
		return heads;
	}
}

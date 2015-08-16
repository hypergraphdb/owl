package org.hypergraphdb.app.owl.versioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;

/**
 * <p>
 * Represents a version of a versioned object. The representation is indirect
 * in that a revision does not matierialize the state of an object at a given
 * time. Rather, it is a point in the version graph that can be used to 
 * re-construct the state of the versioned object when the revision was created.
 * </p>
 * 
 * <p>
 * A revision is associated one or more {@link ChangeRecord}s that "lead" to it.
 * The association is represented with the {@link RevisionMark} link. 
 * There may be more than one change mark (a.k.a. "commit") associated with a
 * revision when the versioned object is {@link VersionedProject} for example.  
 * </p>
 *
 * <p>
 * <b>NOTE:</b> For property access this class supports a "fluid" pattern where
 * the setter returns <code>this</code> and both getter and setter are just the name
 * of the property. This is a departure from Java Beans conventions and therefore
 * HyperGraphDB doesn't not automatically generate a type for the class. So, we've
 * included standard Java Bean style property setter&getter as well, but that is 
 * temporary, so please don't use them.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class Revision implements HGHandleHolder, HGGraphHolder, HGLink
{
	private HyperGraph graph;
	private HGHandle thisHandle;
	private HGHandle versioned;
	private HGHandle branch;
	private long timestamp;
	private String user;	
	private String comment;
		
	public Revision()
	{	
	}
	
	public Revision(HGHandle...targets)
	{
		assert targets.length == 1 || targets.length == 2;
		versioned = targets[0];
		if (targets.length > 1)
			branch = targets[1];
	}

	public Revision versioned(HGHandle version)
	{
		this.versioned = version;
		return this;
	}
	
	public HGHandle versioned()
	{
		return versioned;
	}
	
	public HGHandle getTargetAt(int i)
	{
		if (i == 0)
			return versioned;
		else if (i == 1)
			if (branch != null)
				return branch;
		throw new IllegalArgumentException("Target " + i + " is out of bands, revision may not on a branch.");
	}
	
	public int getArity()
	{
		return branch == null ? 1 : 2;
	}
		
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			versioned = handle;
		else if (i == 1)
			branch = handle;
		else 
			throw new IllegalArgumentException("Target " + i + " out of bounds.");
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i == 0)
			versioned = null;
		else if (i == 1)
			branch = null;
		else 
			throw new IllegalArgumentException("Target " + i + " out of bounds.");		
	}

	public String comment()
	{
		return comment;
	}

	public Revision comment(String comment)
	{
		this.comment = comment;
		return this;
	}

	public String user()
	{
		return user;
	}

	public Revision user(String user)
	{
		this.user = user;
		return this;
	}

	public long timestamp()
	{
		return timestamp;
	}

	public Revision timestamp(long timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}

	/**
	 * Return the revision marks associated this revision to the change sets
	 * that led to its creation. Note that when the versioned object being tracked
	 * is not a compound object, e.g. a project made of multiple modules, there will
	 * be only one revision mark per revision. 
	 */
	public Collection<HGHandle> revisionMarks()
	{
		return graph.findAll(hg.and(hg.type(RevisionMark.class), hg.incident(thisHandle)));
	}

	/**
	 * Return the {@link org.hypergraphdb.app.owl.versioning.ChangeRecord}s of the
	 * {@link org.hypergraphdb.app.owl.versioning.ChangeSet}s that led to this revision.
	 */
	public Collection<HGHandle> changeRecords()
	{
		return graph.findAll(hg.apply(hg.targetAt(graph, 1), 
				hg.and(hg.type(RevisionMark.class), hg.incident(thisHandle))));
	}
	
	/**
	 * Return the set of direct ancestors of this revision. Under normal
	 * circumstances, a revision will have only one parent. When there are 
	 * multiple parents, it means diverging heads/branches had to be merged.  
	 */
	@SuppressWarnings("unchecked")
	public Set<HGHandle> parents()
	{
		HashSet<HGHandle> S = new HashSet<HGHandle>();
		S.addAll((List<HGHandle>)(List<?>)graph.findAll(
				hg.apply(hg.targetAt(graph, 1), 
						 hg.and(hg.type(ParentLink.class), 
								hg.orderedLink(thisHandle, hg.anyHandle())))));
		return S;
	}

	public Branch branch()
	{
		if (branch == null)
			return null;
		return (Branch)graph.get(branch);
	}
	
	public HGHandle branchHandle()
	{
		return branch;
	}

	public Revision branchHandle(HGHandle branch)
	{
		this.branch = branch;
		return this;
	}
	
	/**
	 * Return the set of child revisions that "branch off" this revision. A head
	 * revision will have no branches at all.  
	 */
	@SuppressWarnings("unchecked")
	public Set<HGHandle> children()
	{
		HashSet<HGHandle> S = new HashSet<HGHandle>();
		S.addAll((List<HGHandle>)(List<?>)graph.findAll(
				hg.apply(hg.targetAt(graph, 0), 
						 hg.and(hg.type(ParentLink.class), 
								hg.orderedLink(hg.anyHandle(), thisHandle)))));
		return S;		
	}	
	
	@Override
	public HGHandle getAtomHandle()
	{
		return thisHandle;
	}

	@Override
	public void setAtomHandle(HGHandle handle)
	{
		this.thisHandle = handle;
	}

	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	public String toString()
	{
		return "Revision[" + user() + "," + comment() + "," + new java.util.Date(timestamp) + "]"
				+ "/" + getAtomHandle().getPersistent().toString();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((thisHandle == null) ? 0 : thisHandle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Revision other = (Revision) obj;
		if (thisHandle == null)
		{
			if (other.thisHandle != null)
				return false;
		}
		else if (!thisHandle.equals(other.thisHandle))
			return false;
		return true;
	}
	
	// TEMPORARY - conventional setters and getters
	
	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}				
}
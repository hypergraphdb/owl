package org.hypergraphdb.app.owl.versioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;

/**
 * <p>
 * Represents the application of a set of changes to a versioned object. After
 * the change set has been applied, it becomes immutable and cannot be 
 * modified any further. The versioned object itself can be positioned 
 * at different point in the version graph which means change sets will be
 * rolled back and re-applied, however after they are marked (i.e. after
 * there is at least one ChangeRecord link pointing to a changeset), they
 * are readonly and cannot be altered.
 * </p>
 * 
 * <p>
 * The "target" of a change record is the versioned object to which the
 * change set is applied. The versioned object is the 1st target of the link
 * and the change set is the 2nd target.  
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ChangeRecord implements HGLink, HGGraphHolder, HGHandleHolder
{
	private HGHandle thisHandle;
	private HyperGraph graph;
	private HGHandle versioned, changeset;
	private long timestamp;
	
	public ChangeRecord()
	{		
	}
	
	public ChangeRecord(HGHandle...targets)
	{
		if (targets.length != 2)
			throw new IllegalArgumentException("Expecting exactly 2 targets: target object and changeset");
		versioned = targets[0];
		changeset = targets[1];
	}
	
	/**
	 * Which revisions are associated with this change mark. Usually it will be
	 * just one revision, but the framework allows for more. For example, a project
	 * made up of several versioned entities could have a revision with this mark
	 * as well as one of the components in the project.
	 * 
	 */
	@SuppressWarnings("unchecked")
	public Set<HGHandle> revisions()
	{
		HashSet<HGHandle> S = new HashSet<HGHandle>();
		S.addAll((List<HGHandle>)(List<?>)hg.findAll(graph, hg.apply(hg.targetAt(graph, 0), 
				hg.and(hg.type(RevisionMark.class), hg.incident(thisHandle)))));
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
	
	/**
	 * <p>Return the revision with which this <code>ChangeRecord</code> is
	 * associated or <code>null</code> if it is not associated with any revision
	 * (e.g. it is an intermediary change flush). 
	 */
	public HGHandle revision()
	{
		return graph.findOne(hg.apply(hg.targetAt(graph, 0), 
				hg.and(hg.type(RevisionMark.class), hg.incident(thisHandle))));
	}
	
	public ChangeRecord versioned(HGHandle versioned)
	{
		this.versioned = versioned;
		return this;
	}
	
	public HGHandle versioned()
	{
		return getTargetAt(0);
	}

	public ChangeRecord changeSet(HGHandle changeSet)
	{
		this.changeset = changeSet;
		return this;
	}
	
	public HGHandle changeset()
	{
		return getTargetAt(1);
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	@Override
	public int getArity()
	{
		return 2;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i > 1)
			throw new IndexOutOfBoundsException(" Target index " + i + " must 0 or 1");
		return i == 0 ? versioned : changeset;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			versioned = handle;
		else
			changeset = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		throw new UnsupportedOperationException("Can't remove a commit link target, first delete the commit atom.");
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((thisHandle == null) ? 0 : thisHandle.hashCode());
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
		ChangeRecord other = (ChangeRecord) obj;
		if (thisHandle == null)
		{
			if (other.thisHandle != null)
				return false;
		}
		else if (!thisHandle.equals(other.thisHandle))
			return false;
		return true;
	}	
	
	public String toString()
	{
		return "ChangeRecord[" + versioned + ", " + changeset + "]"; 
	}
	
}
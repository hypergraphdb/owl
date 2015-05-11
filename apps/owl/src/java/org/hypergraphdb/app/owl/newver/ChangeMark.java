package org.hypergraphdb.app.owl.newver;

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
 * there is at least one ChangeMark link pointing to a changeset), they
 * are readonly and cannot be altered.
 * </p>
 * 
 * <p>
 * The "target" of a change mark is the versioned object to which the
 * change set is applied. The versioned object is the 1st target of the link
 * and the change set is the 2nd target.  
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ChangeMark implements HGLink, HGGraphHolder, HGHandleHolder
{
	private HGHandle thisHandle;
	private HyperGraph graph;
	private HGHandle target, changeset;
	private long timestamp;
	
	public ChangeMark(HGHandle...targets)
	{
		if (targets.length != 2)
			throw new IllegalArgumentException("Expecting exactly 2 targets: target object and changeset");
		target = targets[0];
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
						 hg.and(hg.type(MarkParent.class), 
								hg.orderedLink(thisHandle, hg.anyHandle())))));
		return S;
	}
	
	public HGHandle revision()
	{
		return graph.findOne(hg.apply(hg.targetAt(graph, 0), 
				hg.and(hg.type(RevisionMark.class), hg.incident(thisHandle))));
	}
	
	public HGHandle target()
	{
		return getTargetAt(0);
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
		return i == 0 ? target : changeset;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			target = handle;
		else
			changeset = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		throw new UnsupportedOperationException("Can't remove a commit link target, first delete the commit atom.");
	}
}
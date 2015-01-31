package org.hypergraphdb.app.owl.newver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;

public class Revision implements HGHandleHolder, HGGraphHolder, HGLink
{
	private HyperGraph graph;
	private HGHandle thisHandle;
	private HGHandle versioned;
	private long timestamp;
	private String user;	
	private String comment;
		
	public Revision(HGHandle...targets)
	{
		assert targets.length == 1;
	}
	
	public HGHandle getTargetAt(int i)
	{
		assert i == 0;
		return versioned;
	}
	public int getArity()
	{
		return 1;
	}
		
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		versioned = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public HGHandle changeMark()
	{
		return graph.findOne(hg.apply(hg.targetAt(graph, 1), 
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
						 hg.and(hg.type(MarkParent.class), 
								hg.orderedLink(thisHandle, hg.anyHandle())))));
		return S;
	}

	/**
	 * Return the set of child revisions that "branch off" this revision. A head
	 * revision will have no branches at all.  
	 */
	@SuppressWarnings("unchecked")
	public Set<Revision> branches()
	{
		HashSet<Revision> S = new HashSet<Revision>();
		S.addAll((List<Revision>)(List<?>)graph.getAll(
				hg.apply(hg.targetAt(graph, 0), 
						 hg.and(hg.type(MarkParent.class), 
								hg.orderedLink(hg.anyHandle(), thisHandle)))));
		return S;		
	}
	
	/**
	 * <p>
	 * Tag this revision with some meaningful string. Multiple tags 
	 * per revision are possible. If the revision is already tagged with
	 * the specified tag, nothing happens. If another revision is already
	 * tag with the given tag, an <code>IllegalArgumentException</code> 
	 * is thrown.
	 * </p>
	 * @param tag The revision tag. A revision tag must be unique: only one revision
	 * can be tagged with a given tag.
	 * @return this
	 * @throws IllegalArgumentException if the specified tag is not unique.
	 */
	public Revision tag(final String tag)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call()
			{
				HGHandle revisionTag = graph.findOne(hg.and(
						hg.type(TagRevision.class), hg.eq("tag", tag)));
				if (revisionTag == null)
					graph.add(new TagRevision(thisHandle, tag));
				else if (!thisHandle.equals(((TagRevision)graph.get(revisionTag)).revision()))
					throw new IllegalArgumentException("The specified tag " + tag + " is already used.");
				return null;
			}
		});
		return this;
	}
	
	/**
	 * Remove a tag from a revision. If the revision is not tagged with
	 * the specified tag, nothing happens.
	 * @param tag
	 * @return this.
	 */
	public Revision untag(final String tag)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call()
			{
				
				HGHandle revisionTag = graph.findOne(hg.and(
						hg.type(TagRevision.class), hg.incident(thisHandle), hg.eq("tag", tag)));
				if (revisionTag != null)
					graph.remove(revisionTag);
				return null;
			}
		});
		return this;
	}
	
	/**
	 * Return the set of all tags with which this revision is tagged (possibly an
	 * empty set). 
	 */
	public Set<String> tags()
	{
		HashSet<String> S = new HashSet<String>();
		List<TagRevision> allTags = graph.getAll(hg.and(hg.type(TagRevision.class), hg.incident(thisHandle)));
		for (TagRevision tagLink : allTags)
			S.add(tagLink.getTag());
		return S;
	}
	
	/**
	 * <p>
	 * Label this revision with some meaningful string. Multiple labels 
	 * per revision are possible. If the revision is already labeled with
	 * the specified label, nothing happens.
	 * </p>
	 * @param label The revision label. The same label can used for more than one 
	 * revision. 
	 * @return this
	 */	
	public Revision label(final String label)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call()
			{
				HGHandle labelHandle = hg.assertAtom(graph, label);
				HGHandle labelLink = graph.findOne(hg.and(
						hg.type(LabelLink.class), hg.incident(labelHandle), hg.incident(thisHandle)));
				if (labelLink == null)
					graph.add(new LabelLink(labelHandle, thisHandle));
				return null;
			}
		});
		return this;		
	}
	
	/**
	 * Remove a label from a revision. If the revision is not labeled with
	 * the specified label, nothing happens. 
	 * @param label
	 * @return this.
	 */	
	public Revision unlabel(final String label)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call()
			{		
				HGHandle labelHandle = graph.findOne(hg.eq(label));
				if (labelHandle == null)
					return this;
				HGHandle labelLink = graph.findOne(hg.and(
						hg.type(LabelLink.class), hg.incident(labelHandle), hg.incident(thisHandle)));
				if (labelLink != null)
					graph.remove(labelLink);
				return null;
			}
		});
		return this;					
	}
	
	/**
	 * Return the set of all labels with which this revision is labeled (possibly an
	 * empty set). 
	 */	
	public Set<String> labels()
	{
		HashSet<String> S = new HashSet<String>();
		List<LabelLink> allLabels = graph.getAll(hg.and(hg.type(LabelLink.class), hg.incident(thisHandle)));
		for (LabelLink ll : allLabels)
			S.add((String)graph.get(ll.label()));		
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
}
package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.transaction.HGTransactionConfig;

/**
 * 
 * A ChangeSet contains changes affecting a {@link Versioned} object.
 * 
 * The changeset must be added to the graph, before changes are added.
 * 
 * 
 * @created Jan 13, 2015
 */
public class ChangeSet<V extends Versioned<V>> implements HGLink, HGGraphHolder, HGHandleHolder
{
	private HGHandle thisHandle;
	private long timestamp;
	private List<HGHandle> changes;
	private HyperGraph graph;

	public ChangeSet()
	{
		timestamp(System.currentTimeMillis());
		changes = new ArrayList<HGHandle>(100);
	}

	public ChangeSet(HGHandle... args)
	{
		changes = new ArrayList<HGHandle>(Arrays.asList(args));
	}

	public ChangeSet(List<HGHandle> changes)
	{
		this.changes = new ArrayList<HGHandle>(changes);
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

	/**
	 * The Date this changeset was created or last cleared.
	 * 
	 * @return
	 */
	public long timestamp()
	{
		return timestamp;
	}

	public ChangeSet<V> timestamp(long timestamp)
	{
		this.timestamp = timestamp;
		return this;
	}	
	
	/**
	 * Stores a change in the graph and adds it to the changeset. The changeset
	 * will be updated in the graph. Should be called within HGTransaction.
	 * 
	 * @param change
	 */
	public ChangeSet<V> add(final VChange<V> change)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				HGHandle changeHandle = graph.add(change);
				changes.add(changeHandle);
				graph.update(ChangeSet.this);
				return null;
			}
		});
		return this;
	}

	/**
	 * Stores a list of changes in the graph and add them to the changeset. The changeset
	 * will be updated in the graph. Should be called within HGTransaction.
	 * 
	 * @param change
	 */
	public ChangeSet<V> add(final List<VChange<V>> changeList)
	{
//		if (changeList.isEmpty())
//			return this;
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (VChange<V> change : changeList)
				{
					HGHandle changeHandle = graph.add(change);
					changes.add(changeHandle);
				}
				graph.update(ChangeSet.this);
				return null;
			}
		});
		return this;
	}
	
	public ChangeSet<V> remove(final VChange<V> change)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				HGHandle changeHandle = graph.getHandle(change);
				if (changeHandle == null)
					throw new IllegalArgumentException("Can't remove change that's not in the database - " + change);
				if (changes.remove(changeHandle))
					graph.update(ChangeSet.this);
				return null;
			}
		});
		return this;
	}

	/**
	 * removes
	 * 
	 * @param indices
	 *            a sorted list of index positions that need to be removed.
	 */
	public ChangeSet<V> removeAt(final SortedSet<Integer> indices)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				int removedChanges = 0;
				for (int i : indices)
				{
					i = i - removedChanges;
					changes.remove(i);
					removedChanges++;
				}
				graph.update(ChangeSet.this);
				return null;
			}
		});
		return this;
	}

	public VOWLChange getAt(int index)
	{
		return graph.get(changes.get(index));
	}

	/**
	 * Returns a list of changes at the specified indices.
	 * 
	 * @param indices
	 * @return
	 */
	public List<VChange<V>> getAt(final Set<Integer> indices)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<VChange<V>>>()
		{
			public List<VChange<V>> call()
			{
				List<VChange<V>> changesLoaded = new ArrayList<VChange<V>>(indices.size());
				for (int i : indices)
				{
					try
					{
						VChange<V> cur = graph.get(changes.get(i));
						changesLoaded.add(cur);
					}
					catch (RuntimeException e)
					{
						e.printStackTrace();
						throw e;
					}
				}
				return changesLoaded;
			}
		}, HGTransactionConfig.READONLY);
	}

	public List<VChange<V>> changes()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<VChange<V>>>()
		{
			public List<VChange<V>> call()
			{
				List<VChange<V>> changesLoaded = new ArrayList<VChange<V>>(size());
				for (HGHandle h : changes)
				{
					VChange<V> cur = graph.get(h);
					changesLoaded.add(cur);
				}
				return changesLoaded;
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Clears the changeset by removing all changes from graph. The changeset
	 * will be updated in the graph. The changeset may be removed from the graph
	 * after this operation.
	 */
	public ChangeSet<V> clear()
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				List<HGHandle> changesCopy = new ArrayList<HGHandle>(changes);
				for (HGHandle ch : changesCopy)
				{
					// we could check for incidence set size 1 here.
					graph.remove(ch, true);
				}
				// changes.clear();
				graph.update(ChangeSet.this);
				return null;
			}
		});
		return this;
	}

	public boolean isEmpty()
	{
		return changes.isEmpty();
	}

	public int size()
	{
		return changes.size();
	}

	List<HGHandle> handles()
	{
		return changes;
	}

	static <V extends Versioned<?>> 
	List<VChange<V>> normalize(V versioned, List<VChange<V>> L)
	{
		Set<Integer> toremove = new HashSet<Integer>();		
		for (int i = 0; i < L.size(); i++)
		{
			if (toremove.contains(i))
				continue;
			VChange<V> c = L.get(i);
			if (!c.isEffective(versioned))
				toremove.add(i);
			VChange<V> ic = c.inverse();			
			if (c.isIdempotent())
			{
				if (ic == null || ic.isIdempotent())
				{
					// We want to keep only the last change equal to 'c' or to 
					// its inverse 'ic' - all the initial ones will be overridden
					// by the last.
					int last = i;
					for (int j = i + 1; j < L.size(); j++)
					{
						VChange<V> next = L.get(j);
						if (next.equals(c) || next.equals(ic))
						{
							toremove.add(last);
							last = j;
						}
					}
				}
				else // non-idempotent inverse 
				{
					for (int j = i + 1; j < L.size(); j++)
					{
						VChange<V> next = L.get(j);
						if (next.equals(c))
						{
							toremove.add(i);
							break;
						}
						else if (next.equals(ic))
						{
							toremove.add(i);
							toremove.add(j);
							break;
						}
					}					
				}
			}
			else if (ic != null)
			{
				for (int j = i + 1; j < L.size(); j++)
				{
					VChange<V> next = L.get(j);
					if (next.equals(ic))
					{
						toremove.add(i);
						toremove.add(j);
						break;
					}
				}									
			}
			// else it's non-idempotent and it has no inverse, can't remove			
		}
		List<VChange<V>> normal = new ArrayList<VChange<V>>();		
		for (int i = 0; i < L.size(); i++)
		{
			if (toremove.contains(i))
				continue;
			VChange<V> c = L.get(i);
			normal.add(c);
		}		
		return normal;
	}
	
	static <V extends Versioned<?>> 
	List<VChange<V>> merge(V versioned, List<VChange<V>> one, List<VChange<V>> two)
	{
		ArrayList<VChange<V>> result = new ArrayList<VChange<V>>();
		result.addAll(one);
		result.addAll(two);
		return result;
	}
	
	/**
	 * Finds and eliminates changes that became obsolete due to later changes.
	 */
	public List<VChange<V>> packed(V versioned)
	{
		/*
		Set<Integer> toremove = new HashSet<Integer>();		
		for (int i = 0; i < getArity(); i++)
		{
			if (toremove.contains(i))
				continue;
			VChange<V> c = graph.get(getTargetAt(i));
			if (!c.isEffective(versioned))
				toremove.add(i);
			VChange<V> ic = c.inverse();			
			if (c.isIdempotent())
			{
				if (ic == null || ic.isIdempotent())
				{
					// We want to keep only the last change equal to 'c' or to 
					// its inverse 'ic' - all the initial ones will be overridden
					// by the last.
					int last = i;
					for (int j = i + 1; j < getArity(); j++)
					{
						VChange<V> next = graph.get(getTargetAt(j));
						if (next.equals(c) || next.equals(ic))
						{
							toremove.add(last);
							last = j;
						}
					}
				}
				else // non-idempotent inverse 
				{
					for (int j = i + 1; j < getArity(); j++)
					{
						VChange<V> next = graph.get(getTargetAt(j));
						if (next.equals(c))
						{
							toremove.add(i);
							break;
						}
						else if (next.equals(ic))
						{
							toremove.add(i);
							toremove.add(j);
							break;
						}
					}					
				}
			}
			else if (ic != null)
			{
				for (int j = i + 1; j < getArity(); j++)
				{
					VChange<V> next = graph.get(getTargetAt(j));
					if (next.equals(ic))
					{
						toremove.add(i);
						toremove.add(j);
						break;
					}
				}									
			}
			// else it's non-idempotent and it has no inverse, can't remove			
		}
		List<VChange<V>> changes = changes();
		for (int i = 0; i < getArity(); i++)
		{
			if (toremove.contains(i))
				continue;
			VChange<V> c = graph.get(getTargetAt(i));
			changes.add(c);
		}		
		return changes;
*/		
		return normalize(versioned, changes());
	}
	
	public Set<ChangeConflict<V>> findConflicts(ChangeSet<V> otherSet)
	{
		Set<ChangeConflict<V>> conflicts = new HashSet<ChangeConflict<V>>();
		return conflicts;
	}
	
	/**
	 * Applies the changes of this changeset, leaving out conflicting changes.
	 * The changes will be applied through the manager to notify the reasoner.
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param o
	 * @return a sorted list of ascending indices of changes in this changeset
	 *         that conflict with the given ontology o.
	 */
	public ChangeSet<V> apply(final V o)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (VChange<V> change : changes())
				{
					change.apply(o);
				}
				return null;
			}
		});
		return this;
	}

	/**
	 * Applies inverted changes of this changeset in inverse order (undo). 
	 * 
	 * @param o
	 */
	public ChangeSet<V> reverseApply(final V o)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (VChange<V> change : changes())
				{
					change.inverse().apply(o);
				}
				return null;
			}
		});
		return this;		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.HGGraphHolder#setHyperGraph(org.hypergraphdb.HyperGraph)
	 */
	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return changes.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		return changes.get(i);
	}

	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		changes.set(i, handle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		changes.remove(i);
	}
	
	public String toString()
	{
		return getAtomHandle().toString();
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

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeSet<V> other = (ChangeSet<V>) obj;
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
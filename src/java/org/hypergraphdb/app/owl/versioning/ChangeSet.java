package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.transaction.TxList;

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
	 * will be updated in the graph.
	 * 
	 * @param change
	 */
	public ChangeSet<V> add(final Change<V> change)
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
	 * will be updated in the graph.
	 * 
	 * @param changeList
	 * @return <code>this</code>
	 */
	public ChangeSet<V> add(final List<Change<V>> changeList)
	{
		if (changeList.isEmpty())
			return this;
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (Change<V> change : changeList)
				{
					HGHandle changeHandle = graph.add(change);
					changes.add(changeHandle);
				}
				graph.replace(getAtomHandle(), ChangeSet.this);
				return null;
			}
		});
		return this;
	}
	
	public ChangeSet<V> remove(final Change<V> change)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				HGHandle changeHandle = graph.getHandle(change);
				if (changeHandle == null)
					throw new IllegalArgumentException("Can't remove change that's not in the database - " + change);
				if (changes.remove(changeHandle))
				{
					graph.remove(changeHandle, true);
					graph.update(ChangeSet.this);
				}
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
					graph.remove(changes.remove(i), true);
					removedChanges++;
				}
				graph.update(ChangeSet.this);
				return null;
			}
		});
		return this;
	}

	/**
	 * <p>
	 * Delete this <code>ChagneSet</code> and all individual {@link Change}s
	 * in it from the database.
	 * </p>
	 * 
	 * @return <code>this</code>
	 */
	public ChangeSet<V> drop()
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (HGHandle change : changes)
					graph.remove(change, true);
				graph.remove(thisHandle, true);
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
	public List<Change<V>> getAt(final Set<Integer> indices)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<Change<V>>>()
		{
			public List<Change<V>> call()
			{
				List<Change<V>> changesLoaded = new ArrayList<Change<V>>(indices.size());
				for (int i : indices)
				{
					try
					{
						Change<V> cur = graph.get(changes.get(i));
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

	/**
	 * <p>Return a copy of the list of changes in this change set. Because a copy
	 * is returned, one is free to modify the resulting list as one pleases.</p> 
	 */
	public List<Change<V>> changes()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<Change<V>>>()
		{
			public List<Change<V>> call()
			{
				List<Change<V>> changesLoaded = new ArrayList<Change<V>>(size());
				for (HGHandle h : changes)
				{
					Change<V> cur = graph.get(h);
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

	static <V extends Versioned<V>> 
	List<Change<V>> merge(V versioned, List<Change<V>> one, List<Change<V>> two)
	{
		ArrayList<Change<V>> result = new ArrayList<Change<V>>();
		result.addAll(one);
		result.addAll(two);
		return result;
	}
	
	/**
	 * <p>
	 * Simplify this change set by removing all changes that will not be effective 
	 * </p>
	 * 
	 * @param versioned
	 * @return
	 */
	public ChangeSet<V> pack(final V versioned)
	{		
		final ChangeSet<V> self = this;
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				List<Change<V>> changeList = changes();
				Set<Integer> toremove = versioning.collectSuperfluous(versioned, changeList, true);
				for (Integer i : toremove)
				{
					Change<V> change = changeList.get(i);
					HGHandle changeHandle = graph.getHandle(change);
					if (changes.remove(changeHandle))
					{
						graph.remove(changeHandle, true);
					}					
				}
				graph.update(self);				
				return null;
			}
		});
		return this;
	}
	
	/**
	 * Finds and eliminates changes that became obsolete due to later changes.
	 */
	public List<Change<V>> packed(V versioned)
	{	
		return versioning.normalize(versioned, changes());
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
				for (Change<V> change : changes())
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
				for (Change<V> change : changes())
				{
					change.inverse().apply(o);
				}
				return null;
			}
		});
		return this;		
	}
	
	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
		if (! (changes instanceof TxList)) // it will be already a TxList if we've done graph.update(this)!
			this.changes = new TxList<HGHandle>(graph.getTransactionManager(), this.changes);
	}

	@Override
	public int getArity()
	{
		return changes.size();
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		return changes.get(i);
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		changes.set(i, handle);
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		changes.remove(i);
	}
	
	public String toString()
	{
		return getAtomHandle() != null ? getAtomHandle().toString() : null;
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
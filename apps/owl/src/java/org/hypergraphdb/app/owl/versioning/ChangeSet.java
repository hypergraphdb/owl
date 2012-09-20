package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * A ChangeSet contains changes affecting one ontology only.
 * 
 * The changeset must be added to the graph, before changes are added.
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class ChangeSet implements HGLink, HGGraphHolder, VersioningObject
{

	/**
	 * 2012.06.01 Issue with broken database. One change had no axiom in
	 * targetset.
	 */
	private static boolean REMOVE_CHANGES_THAT_FAIL_TO_LOAD = false;

	private static boolean DBG_CONFLICTS = true;

	private Date createdDate;
	private List<HGHandle> changes;

	private HyperGraph graph;

	/**
	 * The Date this changeset was created or last cleared.
	 * 
	 * @return
	 */
	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public ChangeSet()
	{
		setCreatedDate(new Date());
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

	/**
	 * Stores a change in the graph and adds it to the changeset. The changeset
	 * will be updated in the graph. Should be called within HGTransaction.
	 * 
	 * @param change
	 */
	public void addChange(final VOWLChange change)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				HGHandle changeHandle = graph.add(change);
				changes.add(changeHandle);
				graph.update(ChangeSet.this);
				return null;
			}});
	}

	public void removeChange(final VOWLChange change)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				HGHandle changeHandle = graph.getHandle(change);
				if (changeHandle == null)
					throw new IllegalArgumentException("Can't remove change that's not in the database - " + change);
				if (changes.remove(changeHandle))
					graph.update(ChangeSet.this);
				return null;
			}});
	}

	/**
	 * removes 
	 * @param indices a sorted list of index positions that need to be removed.
	 */
	public void removeChangesAt(final SortedSet<Integer> indices)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				int removedChanges = 0;
				for (int i : indices) {
					i = i - removedChanges;
					changes.remove(i);
					removedChanges ++;
				}
				graph.update(ChangeSet.this);
				return null;
			}});
	}

	public VOWLChange getChangeAt(int index) {
		return graph.get(changes.get(index));
	}

	/**
	 * Returns a list of changes at the specified indices.
	 * @param indices
	 * @return
	 */
	public List<VOWLChange> getChangesAt(final Set<Integer> indices) {
		return graph.getTransactionManager().ensureTransaction(new Callable<List<VOWLChange>>() {
			public List<VOWLChange> call() {
				List<VOWLChange> changesLoaded = new ArrayList<VOWLChange>(indices.size());
				for (int i : indices)
				{
					try
					{
						VOWLChange cur = graph.get(changes.get(i));
						changesLoaded.add(cur);
					}
					catch (RuntimeException e)
					{
						e.printStackTrace();
						throw e;
					}
				}
				return changesLoaded;
			}}, HGTransactionConfig.READONLY);
	}

	public List<VOWLChange> getChanges()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<List<VOWLChange>>() {
			public List<VOWLChange> call() {
				List<VOWLChange> changesLoaded = new ArrayList<VOWLChange>(size());
				for (HGHandle h : changes)
				{
					try
					{
						VOWLChange cur = graph.get(h);
						changesLoaded.add(cur);
					}
					catch (RuntimeException e)
					{
						if (REMOVE_CHANGES_THAT_FAIL_TO_LOAD)
						{
							e.printStackTrace();
							System.out.println("REMOVING FAILED CHANGE");
							changes.remove(h);
							graph.update(ChangeSet.this);
							graph.remove(h, true);
							changesLoaded = new ArrayList<VOWLChange>();
							break;
						}
						else
						{
							throw e;
						}
					}
				}
				return changesLoaded;
			}}, HGTransactionConfig.READONLY);
	}

	/**
	 * Clears the changeset by removing all changes from graph. The changeset
	 * will be updated in the graph. The changeset may be removed from the graph
	 * after this operation. Should be called within HGTransaction.
	 */
	void clear()
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				List<HGHandle> changesCopy = new ArrayList<HGHandle>(changes);
					for (HGHandle ch : changesCopy)
					{
						// we could check for incidence set size 1 here.
						graph.remove(ch, true);
					}
					// changes.clear();
					graph.update(ChangeSet.this);
					return null;
			}});
	}

	public boolean isEmpty()
	{
		return changes.isEmpty();
	}

	public int size()
	{
		return changes.size();
	}

	List<HGHandle> getChangesHandles()
	{
		return changes;
	}

	/**
	 * Finds and eliminates changes that became obsolete due to later changes.
	 */
	void pack()
	{
		// TODO
	}

	/**
	 * Applies the changes of this changeset, leaving out conflicting changes. This method ensures a
	 * HGTransaction.
	 * 
	 * @param o
	 * @param useManager if false, apply changes to ontology directly, not using it's owl ontology manager. Always use true if you're using a reasoner.
	 * @return a sorted list of ascending indices of changes in this changeset that conflict with the given ontology o.
	 */
	public SortedSet<Integer> applyTo(final OWLMutableOntology o, final boolean useManager)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<SortedSet<Integer>>()
		{
			public SortedSet<Integer> call()
			{
				if (useManager) VDHGDBOntologyRepository.getInstance().ignoreChangeEvents(true);
				SortedSet<Integer> conflicts = new TreeSet<Integer>();
				//OWLChangeConflictDetector detector = new OWLChangeConflictDetector(o);
				try
				{
					int i = 0;
					for (OWLOntologyChange oc : VOWLChangeFactory.create(getChanges(), o, graph))
					{		
						List<OWLOntologyChange> appliedChanges;
						if (useManager) {
							appliedChanges = o.getOWLOntologyManager().applyChange(oc);
						} else {
							appliedChanges = o.applyChange(oc);
						}
						if (appliedChanges.isEmpty()) {
							System.out.println("Conflict detected: " + i + " " + oc);
							conflicts.add(i);
						}
						i++;
					}
					return conflicts;
				}
				finally
				{
					if (useManager) VDHGDBOntologyRepository.getInstance().ignoreChangeEvents(false);
				}
			}
		});
	}
	
	/**
	 * Applies the changes of this changeset, leaving out conflicting changes.
	 * The changes will be applied through the manager to notify the reasoner. 
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param o
	 * @return a sorted list of ascending indices of changes in this changeset that conflict with the given ontology o.
	 */
	public SortedSet<Integer> applyTo(final OWLMutableOntology o)	
	{
		return applyTo(o, true);
	}

	/**
	 * Applies inverted changes of this changeset in inverse order (undo). The
	 * changes are applied through the ontology's OwlOntologyManager.
	 * Do not call this method, if the changeset are the workingsetchanges of a versioned ontology 
	 * with working set conflicts as the history before this changeset might not match the state
	 * of the ontology after applying workingsetchanges with conflicts. 
	 * 
	 * eg. ORIG: 1 add A, 2 modify A to A', 3 remove A' --> UNDO: 3 add A', 2
	 * modify A' to A, 1 remove A
	 * 
	 * 
	 * @param o
	 */
	public void reverseApplyTo(final OWLMutableOntology o) {
		reverseApplyTo(o, new TreeSet<Integer>());
	}
	
	/**
	 * Reverse applies all changes, except those at the conflict indices.
	 * 
	 * @param o
	 * @param conflicts a 
	 */
	public void reverseApplyTo(final OWLMutableOntology o, final SortedSet<Integer> conflicts)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				VDHGDBOntologyRepository.getInstance().ignoreChangeEvents(true);
				try
				{								
					ListIterator<HGHandle> li = changes.listIterator(changes.size());
					while (li.hasPrevious())
					{
						int index = li.previousIndex();
						VOWLChange vc = graph.get(li.previous());
						OWLOntologyChange c = VOWLChangeFactory.createInverse(vc,
								o, graph);
						//TODO add conflict detection
						if (conflicts == null || !conflicts.contains(index)) {
							o.getOWLOntologyManager().applyChange(c);
						} else {
							if (DBG_CONFLICTS) {
								System.out.println("Avoiding change: " + c + " idx: " + index);
							}
						}
					}
					return null;
				}
				finally
				{
					VDHGDBOntologyRepository.getInstance().ignoreChangeEvents(false);
				}
			}
		});
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb
	 * .app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}
}
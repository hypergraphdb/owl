package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
import org.semanticweb.owlapi.model.OWLAxiomChange;
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
	public void addChange(VOWLChange change)
	{
		HGHandle changeHandle = graph.add(change);
		changes.add(changeHandle);
		graph.update(this);
	}

	public void removeChange(VOWLChange change)
	{
		HGHandle changeHandle = graph.getHandle(change);
		if (changeHandle == null)
			throw new IllegalArgumentException("Can't remove change that's not in the database - " + change);
		if (changes.remove(changeHandle))
			graph.update(this);
	}
	
	public List<VOWLChange> getChanges()
	{
		List<VOWLChange> changesLoaded = new ArrayList<VOWLChange>(size());
		for (HGHandle h : changes)
		{
			try
			{
				// System.out.print("Change at: " + h);
				VOWLChange cur = graph.get(h);
				changesLoaded.add(cur);
				// System.out.println(" Loaded: " + cur +
				// ((cur instanceof VAxiomChange)? " Axiom: " +
				// ((VAxiomChange)cur).getAxiom() : ""));
			}
			catch (RuntimeException e)
			{
				if (REMOVE_CHANGES_THAT_FAIL_TO_LOAD)
				{
					e.printStackTrace();
					System.out.println("REMOVING FAILED CHANGE");
					changes.remove(h);
					graph.update(this);
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
	}

	/**
	 * Clears the changeset by removing all changes from graph. The changeset
	 * will be updated in the graph. The changeset may be removed from the graph
	 * after this operation. Should be called within HGTransaction.
	 */
	void clear()
	{
		List<HGHandle> changesCopy = new ArrayList<HGHandle>(changes);
		for (HGHandle ch : changesCopy)
		{
			// we could check for incidence set size 1 here.
			graph.remove(ch, true);
		}
		// changes.clear();
		graph.update(this);
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
	 * Applies the changes of this changeset. This method ensures a
	 * HGTransaction.
	 * 
	 * @param o
	 */
	public void applyTo(final OWLMutableOntology o)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				for (HGHandle vchangeHandle : changes)
				{
					VOWLChange vc = graph.get(vchangeHandle);
					OWLOntologyChange c = VOWLChangeFactory
							.create(vc, o, graph);
					// applies the change directy, no manager involved, no
					// events issued.
					// manager needs to reload.
					o.applyChange(c);
				}
				return null;
			}
		});
	}

	/**
	 * Applies inverted changes of this changeset in inverse order (undo). The
	 * changes are applied to the ontology directly. Caller needs to tell the
	 * manager.
	 * 
	 * eg. ORIG: 1 add A, 2 modify A to A', 3 remove A' --> UNDO: 3 add A', 2
	 * modify A' to A, 1 remove A
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param o
	 */
	public void reverseApplyTo(final OWLMutableOntology o)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				ListIterator<HGHandle> li = changes.listIterator(changes.size());
				while (li.hasPrevious())
				{
					VOWLChange vc = graph.get(li.previous());
					OWLOntologyChange c = VOWLChangeFactory.createInverse(vc,
							o, graph);
					// applies the change directly, no manager involved, no
					// events issued.
					// manager needs to reload.
					o.applyChange(c);					
				}
				return null;
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
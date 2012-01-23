package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
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
public class ChangeSet implements HGLink, HGGraphHolder {
	
	private Date createdDate;
	private List <HGHandle> changes;

	private HyperGraph graph;

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	
	public ChangeSet() {
		createdDate = new Date();
	}
	
	public ChangeSet(HGHandle...args) {
		changes = new ArrayList<HGHandle>(Arrays.asList(args));
    }
	
	/**
	 * Stores a change in the graph and adds it to the changeset. 
	 * The changeset will be updated in the graph.
	 * 
	 * @param change
	 */
	void addChange(VOWLChange change) {
		HGHandle changeHandle = graph.add(change);
		changes.add(changeHandle);
		graph.update(this);
	}
	
	/**
	 * Clears the changeset by removing all changes.
	 */
	void clear() {
		changes.clear();
		graph.update(this);
	}


	List<HGHandle> getChangesHandles() {
		return changes;
	}

	/**
	 * Finds and eliminates changes that became obsolete due to later changes.
	 */
	void pack() {
		//TODO 
	}

	/**
	 * Applies the changes of this changeset.
	 * @param o
	 */
	void applyTo(OWLMutableOntology o) {
		for (HGHandle vchangeHandle : changes) {
			VOWLChange vc = graph.get(vchangeHandle);
			OWLOntologyChange c = VOWLChangeFactory.create(vc, o, graph);
			// applies the change directy, no manager involved, no events issued.
			// manager needs to reload.
			o.applyChange(c);
		}
	}
	
	/**
	 * Applies inverted changes of this changeset in inverse order (undo). 
	 * The changes are applied to the ontology directly. Caller needs to tell the manager.
	 * 
	 * eg. ORIG: 1 add A, 2 modify A to A', 3 remove A'  -->
	 * 	   UNDO: 3 add A', 2 modify A' to A, 1 remove A
	 * @param o 
	 */
	void reverseApplyTo(OWLMutableOntology o) {
		ListIterator<HGHandle> li = changes.listIterator(changes.size());
		while (li.hasPrevious()) {
			VOWLChange vc = graph.get(li.previous());
			OWLOntologyChange c = VOWLChangeFactory.createInverse(vc, o, graph);
			// applies the change directly, no manager involved, no events issued.
			// manager needs to reload.
			o.applyChange(c);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGGraphHolder#setHyperGraph(org.hypergraphdb.HyperGraph)
	 */
	@Override
	public void setHyperGraph(HyperGraph graph) {
		this.graph = graph;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return changes.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		return changes.get(i);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		changes.set(i, handle);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		changes.remove(i);
	}
}
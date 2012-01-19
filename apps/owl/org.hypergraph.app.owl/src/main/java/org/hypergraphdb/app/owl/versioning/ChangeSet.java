package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * ChangeSet contains changes affecting one ontology only.
 * A changeset is closed after a commit.
 * A commit causes a new revision of the ontology.
 * 
 * The changeset shall be added to the graph, before changes are added.
 *  
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class ChangeSet implements HGLink, HGGraphHolder {
	
	HyperGraph graph;
	Date createdDate;
	List <HGHandle> changes;
	
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
	void applyTo(OWLOntology o) {
		//TODO ;
	}
	
	/**
	 * Reverse applies (undoes) the changes of this changeset. 
	 * @param o
	 */
	void reverseApplyTo(OWLOntology o) {
		//TODO;
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
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.newver.VisitableObject;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * Change.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VOWLChange implements VChange<VersionedOntology>, HGLink, VisitableObject, HGGraphHolder
{
	HyperGraph graph;

	public static boolean isModifyChange(VOWLChange c)
	{
		return c instanceof VModifyOntologyIDChange;
	}

	public abstract OWLOntologyChange toOWLChange(VersionedOntology versioned);
	
	@Override
	public void apply(VersionedOntology versioned)
	{
		versioned.ontology().applyChange(toOWLChange(versioned));
	}
	
	@Override
	public boolean conflictsWith(VChange<VersionedOntology> other)
	{
		return !other.equals(this.inverse());
	}
	
	/**
	 * All possible changes defined by the OWL API are idempotent.
	 */
	@Override
	public boolean isIdempotent()
	{
		return true;
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

	public HyperGraph getHyperGraph()
	{
		return graph;
	}
}
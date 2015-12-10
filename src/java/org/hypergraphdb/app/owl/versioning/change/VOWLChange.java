package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGGraphHolder;

import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * Change.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VOWLChange implements Change<VersionedOntology>, HGLink, HGGraphHolder
{
	HyperGraph graph;

	public static boolean isModifyChange(VOWLChange c)
	{
		return c instanceof VModifyOntologyIDChange;
	}

	public abstract OWLOntologyChange toOWLChange(VersionedOntology versioned);
	public abstract void accept(VOWLChangeVisitor visitor);
	
	@Override
	public void apply(VersionedOntology versioned)
	{
		versioned.ontology().applyChange(toOWLChange(versioned));
	}
	
	@Override
	public Change<VersionedOntology> reduce(Change<VersionedOntology> previous)
	{
		return null;
	}
	
	@Override
	public boolean conflictsWith(Change<VersionedOntology> other)
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
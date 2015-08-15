package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Versioned;

public abstract class VBranchChange<T extends Versioned<T>> implements VChange<T>, HGGraphHolder
{
	HyperGraph graph;

	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}		
}

package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Versioned;

public abstract class VMetadataChange<T extends Versioned<T>> implements Change<T>, HGGraphHolder, HGHandleHolder
{
	HyperGraph graph;
	HGHandle atomHandle;
	
	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	public HGHandle getAtomHandle()
	{
		return atomHandle;
	}

	public void setAtomHandle(HGHandle atomHandle)
	{
		this.atomHandle = atomHandle;
	}	
}
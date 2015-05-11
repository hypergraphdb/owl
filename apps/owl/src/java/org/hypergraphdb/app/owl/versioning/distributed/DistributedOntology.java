package org.hypergraphdb.app.owl.versioning.distributed;

import org.hypergraphdb.HGGraphHolder;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.newver.VersionedOntology;

/**
 * DistributedOntology.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 23, 2012
 */
public abstract class DistributedOntology implements HGLink, HGGraphHolder
{
	private HyperGraph graph;
	private HGHandle versionedOntologyHandle;

	public VersionedOntology getVersionedOntology()
	{
		return graph.get(getTargetAt(0));
	}

	public HGDBOntology getWorkingSetData()
	{
		return getVersionedOntology().ontology();
	}

	public DistributedOntology(HGHandle... args)
	{
		if (args.length != 1)
		{
			throw new IllegalArgumentException("Exactly one argument expected.");
		}
		versionedOntologyHandle = args[0];
	}

	// ------------------------------------------------------------------------------
	// Hypergraph Interfaces Implementation
	// ------------------------------------------------------------------------------

	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}

	@Override
	public int getArity()
	{
		return 1;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i != 0)
		{
			throw new IllegalArgumentException("Only one target supported, the associated VersionedOntology.");
		}
		return versionedOntologyHandle;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i != 0)
		{
			throw new IllegalArgumentException("Only one target supported, the associated VersionedOntology.");
		}
		versionedOntologyHandle = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i != 0)
		{
			throw new IllegalArgumentException("Only one target supported, the associated VersionedOntology.");
		}
		versionedOntologyHandle = null;
	}
}
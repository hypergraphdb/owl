package org.hypergraphdb.app.owl.versioning.distributed;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;

/**
 * DistributedOntology.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 23, 2012
 */
public abstract class DistributedOntology implements HGLink, HGGraphHolder {

	private HyperGraph graph;
	private HGHandle versionedOntologyHandle;
	
	public VersionedOntology getVersionedOntology() {
		return graph.get(getTargetAt(0));
	}
	
	public HGDBOntology getWorkingSetData() {
		return getVersionedOntology().getWorkingSetData();
	}
	
	public DistributedOntology(HGHandle...args) {
		if (args.length != 1) {
			throw new IllegalArgumentException("Exactly one argument expected.");
		}
		versionedOntologyHandle = args[0];
    }

	//------------------------------------------------------------------------------
	// Hypergraph Interfaces Implementation
	//------------------------------------------------------------------------------
	
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
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i != 0) {
			throw new IllegalArgumentException("Only one target supported, the associated VersionedOntology.");
		}
		return versionedOntologyHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0) {
			throw new IllegalArgumentException("Only one target supported, the associated VersionedOntology.");
		}
		versionedOntologyHandle = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0) {
			throw new IllegalArgumentException("Only one target supported, the associated VersionedOntology.");
		}
		versionedOntologyHandle = null;
	}
}
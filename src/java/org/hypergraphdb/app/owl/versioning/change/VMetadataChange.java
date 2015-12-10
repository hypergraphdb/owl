package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.Versioned;

/**
 * <p>
 * Metadata changes are changes that are not part of the revision graph because
 * they reflect the evolution of, well, meta data about the revision graph. Examples
 * of such meta data are branches and labels. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
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
package org.hypergraphdb.app.owl.query;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.atom.HGSubgraph;
import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.query.HGQueryCondition;


/**
 * AnySubgraphMemberCondition checks, if an atom is a member in any subgraph by using the reverse index of HGSubgraph.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 27, 2011
 */
public class AnySubgraphMemberCondition implements HGQueryCondition, HGAtomPredicate {
	
	/**
	 * Index from subgraphmember to subgraph.
	 */
	private HGIndex<HGPersistentHandle, HGPersistentHandle> subgraphsReverseIndex;

	private HyperGraph graph;

	public AnySubgraphMemberCondition()
	{
	}
	
	public AnySubgraphMemberCondition(HyperGraph graph)
	{
		this.graph = graph;
		subgraphsReverseIndex = HGSubgraph.getReverseIndex(graph);
	}

	public boolean satisfies(HyperGraph graph, HGHandle handle)
	{
		if (graph != this.graph) throw new IllegalArgumentException("Graph must be same.");
		HGRandomAccessResult<HGPersistentHandle> rs = subgraphsReverseIndex.find(handle.getPersistent());
		try 
		{
			return rs.hasNext();
		}
		finally
		{
		    rs.close();
		}
	}
}
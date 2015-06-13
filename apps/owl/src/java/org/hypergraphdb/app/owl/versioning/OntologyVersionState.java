package org.hypergraphdb.app.owl.versioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.algorithms.HGTraversal;
import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.util.Pair;

/**
 * <p>
 * Captures the version state of an ontology.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class OntologyVersionState implements VersionState<VersionedOntology>
{
	private Set<HGHandle> heads;
	
	private HGHandle closestAncestor(HyperGraph graph, HGHandle r1, HGHandle r2)
	{
		HGTraversal t1 = new HGBreadthFirstTraversal(r1, 
			new DefaultALGenerator(graph, hg.type(ParentLink.class), null, true, false, false));
		HGTraversal t2 = new HGBreadthFirstTraversal(r2, 
			new DefaultALGenerator(graph, hg.type(ParentLink.class), null, true, false, false));
		
		while (true)
		{
			if (t1.hasNext())
			{
				Pair<HGHandle, HGHandle> x = t1.next();
				if (t2.isVisited(x.getSecond()))
					return x.getSecond();
			}
			else if (!t2.hasNext())
				break;
			if (t2.hasNext())
			{
				Pair<HGHandle, HGHandle> y = t2.next();			
				if (t1.isVisited(y.getSecond()))
					return y.getSecond();
			}
		}
		return null;
	}
	
	public OntologyVersionState(Set<HGHandle> heads)
	{
		this.heads = heads;
	}

	@Override
	public Compared compare(VersionState<VersionedOntology> otherState)
	{
		return null;
	}

	@Override
	public Set<Revision> delta(Versioned<VersionedOntology> otherOntology)
	{
		HyperGraph graph = ((VersionedOntology)otherOntology).graph();
		HashSet<Revision> result = new HashSet<Revision>();
		for (final HGHandle myHead : heads)
		{
			for (final Revision theirHeadRevision : otherOntology.heads())
			{
				final HGHandle theirHead = graph.getHandle(theirHeadRevision);
				final HGHandle parent = this.closestAncestor(graph, myHead, theirHead);
				if (parent.equals(theirHead))
					continue; // we are have a more recent revision
				HGAtomPredicate nodePredicate = new HGAtomPredicate() {
					public boolean satisfies(HyperGraph graph, HGHandle atom)
					{
						return !atom.equals(theirHead) && graph.get(atom) instanceof Revision;
					}
				};
				List<Revision> L = graph.getAll(hg.bfs(parent, 
													   hg.type(ParentLink.class), 
													   nodePredicate));
				result.addAll(L);
				result.add(theirHeadRevision);
			}
		}
		return result;
	}
}
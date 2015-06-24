package org.hypergraphdb.app.owl.versioning;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.algorithms.HGTraversal;
import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.util.HGUtils;
import org.hypergraphdb.util.Pair;
import org.hypergraphdb.util.SimpleStack;

/**
 * <p>
 * Captures the version state of an ontology as a snapshot. A version state is intended
 * to be used for comparison with other version states to find out if they diverge and if
 * so, what the difference is.
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
	
	/**
	 * <p>
	 * Assuming the <code>otherOntology</code> argument represents a more recent ontology
	 * than the version captured by <code>this</code> as the set <code>Heads</code>, 
	 * return all newer revisions. 
	 * </p>
	 * <p>
	 * The returned set will contain all revisions that do not lie on a path from the 
	 * root to any of the head revisions in <code>this</code>' <code>Heads</code>.
	 * </p>
	 * <p>
	 * The algorithm proceeds by constructing a delta <code>Delta</code> revisions set 
	 * in two phases:
	 * <ul>
	 * <li>Phase 1: starting from the head revisions of <code>otherOntology</code>, 
	 * traverse all predecessors until we reach either (1) a member of <code>Heads</code>
	 * or (2) a revision with children not all in <code>Delta</code>. In case of (2),
	 * we've hit a revision that could potentially have a successor merged with some 
	 * member of <code>Heads</code> so we add it to a set of <code>Pending</code> revisions
	 * to traverse in opposite direction in Phase 2.  </li>
	 * <li>Phase 2: for each revision R in the <code>Pending</code> set built in 
	 * the previous phase, let S = set of successors of R (descendents in the revision graph);
	 * if S and <code>Heads</code> have empty intersection, add all of S to Delta. </li>
	 * </ul>
	 * Note that we could theoretically just do Phase 2 with the pending set taken as the
	 * whole graph. But phase 1 reduced that set of hopefully either empty or very small. It
	 * will not be empty if there is a new branch created in the ontology that doesn't not
	 * descend for the previously known head revisions.  
	 * </p>
	 * 
	 * Hope this works... :)
	 * 
	 * @param otherOntology
	 * @return
	 */
	public Set<HGHandle> findRevisionsSince(VersionedOntology otherOntology)
	{
		HyperGraph graph = ((VersionedOntology)otherOntology).graph();
		final HashSet<HGHandle> delta = new HashSet<HGHandle>();
		final HashSet<HGHandle> pending = new HashSet<HGHandle>();		
		final SimpleStack<Revision> toexamine = new SimpleStack<Revision>();
		for (Revision rev : otherOntology.heads())
		{
			if (heads.contains(rev.getAtomHandle()))
				continue;
			toexamine.push(rev);
		}
		while (!toexamine.isEmpty())
		{
			Revision current = toexamine.pop();
			delta.add(current.getAtomHandle());
			for (HGHandle parentHandle : current.parents())
			{
				if (heads.contains(parentHandle))
					continue;
				Revision parent = graph.get(parentHandle);
				if (delta.containsAll(parent.branches()))
				{
					toexamine.push(parent);
					delta.add(parentHandle);
				}
				else
					pending.add(parentHandle);
			}
		}
		for (HGHandle revisionHandle : pending)
		{
			// A pending revision is one where we couldn't determine for sure in the previous whether
			// it's a new or not. So we traverse all its descendants and if we hit a head from version
			// state we are comparing against, then no it's not new, otherwise it's new and all of its
			// descendants are new so we add them to delta.
			HGSearchResult<HGHandle> successors = graph.find(hg.bfs(revisionHandle, hg.type(ParentLink.class), hg.type(Revision.class)));
			HashSet<HGHandle> accumulate = new HashSet<HGHandle>();
			try
			{
				while (successors.hasNext())
				{
					if (heads.contains(successors.next()))
					{
						accumulate = null;
						break;
					}
					else
						accumulate.add(revisionHandle);
				}
			}
			finally
			{
				HGUtils.closeNoException(successors);
			}
			if (accumulate != null)
				delta.addAll(accumulate);
		}
		return delta;
	}

	/**
	 * TODO - this is a draft implementation of a general delta between
	 * the state this object captures and some other version of the same
	 * ontology (older, newer or parallel)
	 */
	@Override
	public Set<Revision> delta(Versioned<VersionedOntology> otherOntology)
	{
		HyperGraph graph = ((VersionedOntology)otherOntology).graph();
		final HashSet<Revision> result = new HashSet<Revision>();
		for (final HGHandle myHead : heads)
		{
			for (final Revision theirHeadRevision : otherOntology.heads())
			{
				final HGHandle theirHead = graph.getHandle(theirHeadRevision);
				final HGHandle parent = this.closestAncestor(graph, myHead, theirHead);
				if (parent.equals(theirHead))
					continue; // we have a more recent revision
				HGAtomPredicate nodePredicate = new HGAtomPredicate() {
					public boolean satisfies(HyperGraph graph, HGHandle atom)
					{
						Object a = graph.get(atom);
						if (! (a instanceof Revision ) )
							return false;
						return !atom.equals(theirHead) && !result.contains(a);
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
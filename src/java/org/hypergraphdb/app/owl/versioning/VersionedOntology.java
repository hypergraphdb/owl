package org.hypergraphdb.app.owl.versioning;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.GraphClassics;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.versioning.change.VChange;
import org.hypergraphdb.query.IndexCondition;
import org.hypergraphdb.util.HGUtils;
import org.hypergraphdb.util.Mapping;

/**
 * <p>
 * Represents an active working copy of a versioned ontology. There may be
 * multiple such active copies within a repository. They are distinguished by
 * different version IRIs.
 * </p>
 * 
 * TODO - revisit transaction boundaries for methods, some don't even have transactions...
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionedOntology implements Versioned<VersionedOntology>, HGGraphHolder
{
	private HyperGraph graph;
	private HGHandle thisHandle;
	private HGHandle ontology;
	private HGHandle rootRevision;
	private HGHandle bottomRevision;
	private HGHandle currentRevision;
	private HGHandle workingChanges;
	private VersionedMetadata<VersionedOntology> metadata;
		
	/**
	 * Get all change marks applied after the revision argument. 
	 * 
	 * @return The list of change mark handles.
	 */
	private List<HGHandle> marksFrom(HGHandle revisionHandle)
	{
		ArrayList<HGHandle> L = new ArrayList<HGHandle>(); 
		HGHandle lastMark = getRevisionMark(revisionHandle).changeRecord();
		HGSearchResult<HGHandle> rs = graph.find(hg.dfs(lastMark, 
				hg.type(ParentLink.class), null, true, false));		
		try
		{
			while (rs.hasNext())
			{
				lastMark = rs.next();
				L.add(lastMark);
			}
		}
		finally
		{
			rs.close();
		}
		return L;
	}
	
	/**
	 * Return the {@link RevisionMark} for a revision
	 * that asserts what change mark created the given revision for this ontology that.
	 * 
	 * @param revisionHandle The handle of the revision whose {@link ChangeRecord} 
	 * association is desired.
	 */
	public RevisionMark getRevisionMark(HGHandle revisionHandle)
	{
		List<RevisionMark> L = graph.getAll(hg.and(hg.type(RevisionMark.class), hg.incident(revisionHandle)));
		for (RevisionMark markLink : L)
		{
			ChangeRecord mark = graph.get(markLink.changeRecord());
			if (mark.versioned().equals(ontology))
				return markLink;
		}
		return null;
	}
	
	/**
	 * Return the latest {@link ChangeRecord} representing the last change flush operation.
	 */
	public ChangeRecord latestChangeRecord()
	{
		HGHandle handleCurrent = getRevisionMark(currentRevision).changeRecord();
		
		// Traverse to find the most recent change mark, the "youngest"
		// descendent of the ChangeRecord that created the current revision.
		HGSearchResult<HGHandle> rs = graph.find(hg.dfs(handleCurrent, 
							hg.type(ParentLink.class), null, true, false));
		try
		{
			while (rs.hasNext())
				handleCurrent = rs.next();
		}
		finally
		{
			rs.close();
		}
		return graph.get(handleCurrent);
	}
	
	/**
	 * Collect all {@link ChangeRecord}s between two adjacent revisions.
	 * 
	 * @param startRevision The parent revision
	 * @param endRevision The child revision
	 * @return A list of ChangeRecord handles.
	 */
	public List<HGHandle> marksBetweenAdjacent(HGHandle startRevision, HGHandle endRevision)
	{
		ArrayList<HGHandle> L = new ArrayList<HGHandle>();
		if (startRevision.equals(endRevision))
			return L; 
		HGHandle lastMark = getRevisionMark(startRevision).changeRecord();
		HGSearchResult<HGHandle> rs = graph.find(hg.dfs(lastMark, 
				hg.type(ParentLink.class), null, true, false));		
		try
		{
			while (rs.hasNext())
			{
				lastMark = rs.next();
				L.add(lastMark);
				if (graph.findOne(hg.and(hg.type(RevisionMark.class), 
								         hg.link(lastMark, endRevision))) != null)
					break;
			}
		}
		finally
		{
			rs.close();
		}
		return L;		
	}
	
	/**
	 * Constructs a list of changes to be applied from <code>from</code>
	 * to reach the state of <code>to</code>.
	 */
	public List<VChange<VersionedOntology>> collectChanges(HGHandle from, HGHandle to)
	{
		Map<HGHandle, HGHandle> predecessorMatrix = new HashMap<HGHandle, HGHandle>();		
		if (GraphClassics.dijkstra(
			   from, 
			   to, 
			   new DefaultALGenerator(graph, 
						  hg.type(ParentLink.class),
						  hg.type(Revision.class)),
			   new Mapping<HGHandle, Double>() {
				   public Double eval(HGHandle parentLink)
				   {
					   ParentLink link = graph.get(parentLink);
					   return (double)collectChangesAdjacent(link.parent(), 
							   								 link.child()).size();
				   }
			   },
		       null,
		       predecessorMatrix) == null)
			throw new IllegalArgumentException("Revisions " + from + " and " + to + 
					" are not connected - are they part of the same version history?");
		ArrayList<VChange<VersionedOntology>> result = new ArrayList<VChange<VersionedOntology>>();
		HGHandle hCurrent = to;		
		do
		{
			HGHandle hPrev = predecessorMatrix.get(hCurrent);
			Revision current = graph.get(hCurrent);			
			List<HGHandle> L = null;
			if (current.parents().contains(hPrev))
			{
				L = marksBetweenAdjacent(hPrev, hCurrent);				
				Collections.reverse(L);
				for (HGHandle h : L)
				{
					ChangeRecord mark = graph.get(h);
					ChangeSet<VersionedOntology> cs = graph.get(mark.changeset());
					List<VChange<VersionedOntology>> changeList = cs.changes();
					Collections.reverse(changeList);
					for (VChange<VersionedOntology> change : changeList)
						result.add(change.inverse());
				}				
			}
			else
			{
				L = marksBetweenAdjacent(hCurrent, hPrev);
				for (HGHandle h : L)
				{
					ChangeRecord mark = graph.get(h);
					ChangeSet<VersionedOntology> cs = graph.get(mark.changeset());
					result.addAll(cs.changes());
				}
			}
			hCurrent = hPrev;	
		} while (!hCurrent.equals(from));
		return result;
	}
	
	/**
	 * Collect the normalized list of changes needed to reach the state of 
	 * revision <code>end</code>, starting from revision <code>start</code>.
	 * It is assumed that start is a parent revision of end.
	 */
	private List<VChange<VersionedOntology>> collectChangesAdjacent(HGHandle start, HGHandle end)
	{
		List<VChange<VersionedOntology>> changes = new ArrayList<VChange<VersionedOntology>>();
		for (HGHandle h : marksBetweenAdjacent(start, end))
		{
			ChangeRecord mark = graph.get(h);
			ChangeSet<VersionedOntology> current = graph.get(mark.changeset()); 			
			for (VChange<VersionedOntology> change : current.changes())
				changes.add(change);
		}
		return changes; // ChangeSet.normalize(this, changes);
	}
	
	public VersionedOntology()
	{
	}

	public VersionedOntology(HyperGraph graph, HGHandle ontology, HGHandle currentRevision, HGHandle changes)
	{
		this.graph = graph;
		this.ontology = ontology;
		this.currentRevision = currentRevision;
		this.workingChanges = changes;
	}

	@Override
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
		this.metadata = new VersionedMetadata<VersionedOntology>(graph, this);
	}

	public VersionedMetadata<VersionedOntology> metadata()
	{
		return this.metadata;
	}
	
	public HyperGraph graph()
	{
		return graph;
	}
		
	public HGHandle getRootRevision()
	{
		return rootRevision;
	}

	public void setRootRevision(HGHandle rootRevision)
	{
		this.rootRevision = rootRevision;
	}

	public Revision revision()
	{
		return graph.get(currentRevision);
	}

	private HGHandle makeRevision(String user, String comment, HGHandle branch)
	{		
		ChangeRecord mark = latestChangeRecord();
		if (currentRevision.equals(mark.revision()))
			mark = flushChanges();
		Revision revision = new Revision(thisHandle);
		if (branch != null)
			revision.branchHandle(branch);
		revision.user(user).comment(comment).timestamp(System.currentTimeMillis());
		HGHandle revisionHandle = graph.add(revision);
		graph.add(new RevisionMark(revisionHandle, graph.getHandle(mark)));
		graph.add(new ParentLink(revisionHandle, currentRevision));
		workingChanges = graph.add(new ChangeSet<VersionedOntology>());
		currentRevision = revisionHandle;
		graph.update(this);
		return this.currentRevision;
	}
	
	@Override
	public Revision commit(final String user, final String comment)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>(){
		public HGHandle call()
		{
			return makeRevision(user, comment, revision().branchHandle());
		}
		});
		return revision();
	}

	@Override
	public Revision commit(final String user, final String comment, final String branch)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>(){
		public HGHandle call()
		{
			HGHandle existingBranch = branch != null ? metadata.findBranchHandle(branch) : null;
			if (existingBranch != null && !existingBranch.equals(revision().branchHandle()))
				throw new IllegalArgumentException("Branch already exists: '" + branch + "'.");
			if (existingBranch != null)
				System.out.println("Committing on existing branch "+ existingBranch + " with name " + branch);
			HGHandle revhandle = makeRevision(user, comment, existingBranch);
			if (branch != null && existingBranch == null)
			{
				System.out.println("Creaning new branch  with name " + branch);
				metadata.createBranch(revhandle, branch, user);
			}
			return revhandle;
		}
		});
		return revision();
	}
	
	/**
	 * Construct a change list that represents the difference left-right. That is, the return change list
	 * is such if it is applied after the 'right' change list, that would be equivalent to applied the 'left'
	 * change list. That is 'result = left - right'. It is expected that both left and right arguments are
	 * normalized. This means that all changes in them are commutative (order of application doesn't matter).
	 * And in particular there aren't any inverse pairs (a pair consistent of a change ands its inverse). 
	 */
	private List<VChange<VersionedOntology>> changeListDiff(List<VChange<VersionedOntology>> left, 
															List<VChange<VersionedOntology>> right)
	{
		List<VChange<VersionedOntology>> result = new ArrayList<VChange<VersionedOntology>>();
		for (VChange<VersionedOntology> change : left)
		{
			if (!right.contains(change))
				result.add(change);
		}
		return result;
	}
	
	private HGHandle addChangeSet(List<VChange<VersionedOntology>> changeList)
	{
		HGHandle [] changes = new HGHandle[changeList.size()];
		int i = 0;
		for (VChange<VersionedOntology> c : changeList)
			changes[i++] = hg.assertAtom(graph, c);
		return graph.add(new ChangeSet<VersionedOntology>(changes));
	}
	
	private Revision createMergedRevision(String user,
										  String comment,
										  HGHandle commonAncestor, 
										  List<VChange<VersionedOntology>> mergeChangeList,
										  Revision...revisions)
	{
//		System.out.println("CA:" + commonAncestor);
//		for (Revision r: revisions)
//			System.out.println("M - " + graph.getHandle(r));
		goTo((Revision)graph.get(commonAncestor));
		// now we can normalize so only changes effective from the common
		// merge ancestor will be recorded
		mergeChangeList = versioning.normalize(this, mergeChangeList);
//		HGHandle [] mergeChanges = new HGHandle[mergeChangeList.size()];
//		int i = 0;
//		for (VChange<VersionedOntology> c : mergeChangeList)
//			mergeChanges[i++] = hg.assertAtom(graph, c);
//		ChangeSet<VersionedOntology> changeSet = new ChangeSet<VersionedOntology>(mergeChanges);
		HGHandle hChangeSet = addChangeSet(mergeChangeList); 
		ChangeSet<VersionedOntology> changeSet = graph.get(hChangeSet);
		Revision revision = new Revision(thisHandle);
		revision.user(user);
		revision.comment(comment);
		revision.timestamp(System.currentTimeMillis());
		HGHandle revisionHandle = graph.add(revision);		
		changeSet.apply(this);
		HGHandle mark = graph.add(new ChangeRecord(ontology, hChangeSet));
		graph.add(new ParentLink(mark, this.getRevisionMark(commonAncestor).changeRecord()));
		graph.add(new RevisionMark(revisionHandle, mark));
		
		// Create a parent-child relationship between each of the revisions being
		// merged and the resulting "merge" revision. Not only each of those revisions
		// must be declared as a parent, but we must also make sure that it is
		// possible to move the state of the versioned ontology from parent to child
		// and vice versa easily. Therefore, we must compute the change sets between
		// each parent revision and the merge result.		
		for (Revision rev : revisions)
		{ 
			List<VChange<VersionedOntology>> diff = this.changeListDiff(mergeChangeList, 
																		collectChanges(commonAncestor, 
																					   rev.getAtomHandle()));			
			mark = graph.add(new ChangeRecord(ontology, addChangeSet(diff)));
			graph.add(new ParentLink(mark, this.getRevisionMark(rev.getAtomHandle()).changeRecord()));
			graph.add(new RevisionMark(revisionHandle, mark));			
			graph.add(new ParentLink(revisionHandle, graph.getHandle(rev)));
		}
		workingChanges = graph.add(new ChangeSet<VersionedOntology>());
		currentRevision = revisionHandle;
		graph.update(this);
		return revision();
	}
	
	@Override
	public Revision merge(String user, String comment, Revision... revisions)
	{
		if (revisions.length < 2)
			return null;
		
		// Here, we are going to find the common ancestor of all revisions that
		// we are merging. For that, the algorithm starts by putting all revisions
		// in a set and iteratively replacing them with their immediate parents
		// until there is only one element in the set: the common ancestor. This
		// is guaranteed to terminate because the revision graph is a DAG.
		//
		// In addition, we want to construct a list of changes that, when applied to
		// that common ancestor will result in the desired state D of merged revisions.
		// 
		// The above mentioned set is actually a map. The keys are revisions
		// iteratively replaced with their parents and the values are change lists.
		// A change list associated with revision R, at any given stage, contains all
		// the changes that need to be applied starting from R to reach
		// the state of the revisions we are merging and whose common ancestor
		// is R. That is, we are merging change sets at each iteration and at the end 
		// we have the list of changes to complete the merge already built.
		//
		// What happens at each iteration is the following: we collect parents of
		// of current set of revisions. And for each parent we want to construct
		// the full change list to reach the desired state D. We do this by merging
		// the changes from a parent to all revisions in the current set and also
		// by merging the changes from multiple parents of a single revision.
		//
		// Suppose at a given stage, M[R] = L, that is revision R in M is associated
		// with a list of changes L. If R has multiple parents, say P1 and P2, we accumulate
		// both the changes from P1 to R and from P2 to R
		//
		// are inserted into M as a replacement for r, where the change set p1->r is merged
		HashMap<HGHandle, List<VChange<VersionedOntology>>> M = 
				new HashMap<HGHandle, List<VChange<VersionedOntology>>>();
		for (Revision r : revisions)
		{
			// Sanity check that all are head revisions with no changes after that.
			if (!r.children().isEmpty())
				throw new IllegalArgumentException("Revision " + r + " is not a head revision.");			
			
			if (r.equals(currentRevision) && (!changes().isEmpty() || 
											  !marksFrom(currentRevision).isEmpty()))				
				throw new IllegalArgumentException("Cannot merge current head revision with uncomitted changes.");
			
			M.put(r.getAtomHandle(), new ArrayList<VChange<VersionedOntology>>());
		}
		
		while (M.size() > 1)
		{
			HashMap<HGHandle, List<VChange<VersionedOntology>>> parents = 
					new HashMap<HGHandle, List<VChange<VersionedOntology>>>();
			for (HGHandle hRev : M.keySet())
			{				
				Revision rev = graph.get(hRev);
				for (HGHandle p : rev.parents())
				{
					List<VChange<VersionedOntology>> changes = collectChangesAdjacent(p, hRev);
					List<VChange<VersionedOntology>> accumulated = parents.get(p);
					if (accumulated != null)
						changes = ChangeSet.merge(this, accumulated, changes);
					parents.put(p, changes);
				}				
			}
			HashMap<HGHandle, List<VChange<VersionedOntology>>> newM = 
					new HashMap<HGHandle, List<VChange<VersionedOntology>>>();
			for (HGHandle hRev : M.keySet())
			{
				Revision rev = graph.get(hRev);
				if (parents.containsKey(hRev))
				{
					List<VChange<VersionedOntology>> changes = 
							ChangeSet.merge(this, M.get(hRev), parents.get(hRev));
					List<VChange<VersionedOntology>> accumulated = newM.get(hRev);
					newM.put(hRev, accumulated == null ? 
								  changes : ChangeSet.merge(this, accumulated, changes)
					);
				}
				else for (HGHandle p : rev.parents())
				{
					List<VChange<VersionedOntology>> accumulated = newM.get(p);
					if (accumulated == null)
						accumulated = M.get(hRev);
					else
						accumulated = ChangeSet.merge(this, M.get(hRev), accumulated);
					newM.put(p,  ChangeSet.merge(this, accumulated, parents.get(p)));
				}
			}			
			M = newM;
		}
		
		// Here M contains 1 element, which is the common ancestor of all revisions.
		HGHandle commonAncestor = M.keySet().iterator().next();
		return createMergedRevision(user, 
									comment, 
									commonAncestor, 
									M.get(commonAncestor), 
									revisions);
	}

	/**
	 * Dropped all recent working changes.
	 */
	public VersionedOntology undo()
	{
		return graph.getTransactionManager().transact(new Callable<VersionedOntology>() {
			public VersionedOntology call()
			{
				ChangeSet<VersionedOntology> changeSet = graph.get(workingChanges);
				changeSet.reverseApply(VersionedOntology.this);
				graph.remove(workingChanges);
				workingChanges = graph.add(new ChangeSet<VersionedOntology>());
				graph.update(VersionedOntology.this);
				return VersionedOntology.this;
			}			
		});
	}
	
	/**
	 * Freeze the latest working changes on this ontology as an immutable change set.
	 * Thus a new {@link ChangeRecord} is created as a result of the flush operation and
	 * made a child (via a {@link ParentLink} link) to the most recent such ChangeRecord.  
	 */
	public ChangeRecord flushChanges()
	{ 
		ChangeRecord current = latestChangeRecord();
		ChangeRecord newmark = new ChangeRecord(ontology, workingChanges);
		newmark.setTimestamp(System.currentTimeMillis());
		HGHandle markHandle = graph.add(newmark);
		graph.add(new ParentLink(markHandle, current.getAtomHandle()));
		workingChanges = graph.add(new ChangeSet<VersionedOntology>());
		graph.update(this);
		return newmark;
	}

	/**
	 * Return the working {@link ChangeSet} which holds the set of changes since the last
	 * commit or flush (see {@link #flushChanges()}.
	 */
	@Override
	public ChangeSet<VersionedOntology> changes()
	{
		return graph.get(workingChanges);
	}

	@SuppressWarnings("unchecked")
	private void changes(ChangeRecord from, List<ChangeSet<VersionedOntology>> L)
	{
		L.add((ChangeSet<VersionedOntology>)graph.get(from.changeset()));		
		for (HGHandle h : from.parents())
		{
			ChangeRecord parentMark = graph.get(h);
			if (parentMark.revision() == null)
				changes(parentMark, L);
		}
	}
	
	public List<ChangeSet<VersionedOntology>> changes(Revision revision)
	{
		ArrayList<ChangeSet<VersionedOntology>> L = new ArrayList<ChangeSet<VersionedOntology>>();
		ChangeRecord mark = graph.get(getRevisionMark(graph.getHandle(revision)).changeRecord());
		changes(mark, L);
		return L;
	}

	public List<Revision> revisions()
	{
		List<Revision> L = hg.getAll(graph(), hg.and(hg.type(Revision.class), hg.incident(thisHandle)));
		for (Iterator<Revision> iter = L.iterator(); iter.hasNext(); )
			if (iter.next().getAtomHandle().equals(this.bottomRevision))
				iter.remove();
		Collections.sort(L, new Comparator<Revision>(){
			public int compare(Revision left, Revision right)
			{
				return Long.compare(left.timestamp(), right.timestamp());
			}
		});
		return L;
	}	
	
	/**
	 * <p>
	 * Return the head revision of a given branch.
	 * </p>
	 * 
	 * @param branchHandle
	 * @return
	 */
	public Revision branchHead(HGHandle branchHandle)
	{
		List<Revision> allheads = graph.getAll(hg.and(new IndexCondition<HGPersistentHandle, HGPersistentHandle>(
					TrackRevisionStructure.revisionChildIndex(graph), 
					getBottomRevision().getPersistent()), 
					hg.incident(branchHandle)));
		if (allheads.size() > 1)
			throw new IllegalArgumentException("Branch " + graph.get(branchHandle) + " has multiple heads. They must be merged.");
		else
			return allheads.get(0);
	}
	
	/**
	 * <p>
	 * Position the current working set to whatever is indicated by <code>pointer</code>.
	 * </p>
	 * @param pointer Could be pointing to a branch or a revision. 
	 * @return <code>this</code>
	 */
	public VersionedOntology goTo(HGHandle pointer)
	{
		Object o = graph.get(pointer);
		if (o instanceof Branch)
			return goTo((Branch)o);
		else if (o instanceof Revision)
			return goTo((Revision)o);
		else 
			throw new IllegalArgumentException("Handle " + 
					pointer + " doesn't point to anything identifying a revision.");
	}
	
	public VersionedOntology goTo(final Branch branch)
	{
		HGHandle branchHandle = graph.getHandle(branch);
		if (branchHandle == null)
			branchHandle = metadata.findBranchHandle(branch.getName());
		if (branchHandle == null)
			throw new IllegalArgumentException("Could not find branch locally: " + branch.getName());		
		Revision branchHead = branchHead(branchHandle);
		return goTo(branchHead);
	}

	public VersionedOntology goTo(String branchName)
	{
		HGHandle branchHandle = metadata.findBranchHandle(branchName);
		if (branchHandle == null)
			throw new IllegalArgumentException("Could not find branch locally: " + branchName);
		Revision branchHead = branchHead(branchHandle);
		return goTo(branchHead);
	}
	
	/**
	 * <p>
	 * Position the working copy of the ontology to a specific revision.
	 * </p>
	 * 
	 * @return this
	 */
	public VersionedOntology goTo(final Revision revision)
	{
		if (revision.getAtomHandle().equals(currentRevision))
			return this;	
		final List<VChange<VersionedOntology>> changes = 
				collectChanges(currentRevision, revision.getAtomHandle());					
		return graph.getTransactionManager().ensureTransaction(new Callable<VersionedOntology>() {
			public VersionedOntology call()
			{
				// Stash working changes
				ChangeSet<VersionedOntology> workingSet = graph.get(workingChanges); 
				if (workingSet.size() > 0)
					throw new RuntimeException("Refusing to move to a different revision " + 
							"with uncommitted working changes. Please commit, undo or stash/shelve them.");
//				{
//					workingSet.reverseApply(VersionedOntology.this);					
//					graph.add(new StashLink(workingChanges, currentRevision));
//				}
				
				for (VChange<VersionedOntology> c : changes)
					c.inverse().apply(VersionedOntology.this);
				currentRevision = revision.getAtomHandle();
				// reset working set, or restore working set at that
				// revision
				graph.update(VersionedOntology.this);
				return VersionedOntology.this;
			}
		});
	}
	
	public OWLOntologyEx getRevisionData(HGHandle revisionHandle)
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * <p>
	 * Return the set of head revisions for all branches. A revision may have
	 * descendants of a different branch and it still be the head and the last
	 * revision of its own branch. Thus one should assume that a head revision
	 * has no children. 
	 * </p>
	 */
	public Set<HGHandle> heads()
	{
		HashSet<HGHandle> result = new HashSet<HGHandle>();
//		// This is rather inefficient...we are traversing the whole revision graph to get to the end.
//		// We should have some more efficient query to start from the "leafs" somehow. That will probably
//		// entail an adjustment of the representation.
//		HGSearchResult<HGHandle> rs = graph.find(hg.bfs(this.getRootRevision(), hg.type(ParentLink.class), null));
		HGSearchResult<HGPersistentHandle> rs = TrackRevisionStructure.revisionChildIndex(graph).find(getBottomRevision().getPersistent());
		try
		{
			while (rs.hasNext())
			{
//				HGHandle rev = rs.next();
//				if (graph.findAll(hg.and(hg.type(ParentLink.class), 
//										 hg.orderedLink(hg.anyHandle(), rev))).isEmpty())
					result.add(rs.next());				
			}
		}
		finally
		{
			HGUtils.closeNoException(rs);
		}
		return result;
	}
	
	public HGDBOntology ontology()
	{
		return graph.get(ontology);
	}

	public HGHandle getOntology()
	{
		return ontology;
	}

	public void setOntology(HGHandle ontology)
	{
		this.ontology = ontology;
	}
	
	public HGHandle getBottomRevision()
	{
		return bottomRevision;
	}

	public void setBottomRevision(HGHandle bottomRevision)
	{
		this.bottomRevision = bottomRevision;
	}

	public HGHandle getCurrentRevision()
	{
		return currentRevision;
	}

	public void setCurrentRevision(HGHandle currentRevision)
	{
		this.currentRevision = currentRevision;
	}

	public HGHandle getWorkingChanges()
	{
		return workingChanges;
	}

	public void setWorkingChanges(HGHandle workingChanges)
	{
		this.workingChanges = workingChanges;
	}
	
	public void setAtomHandle(HGHandle atomHandle)
	{
		this.thisHandle = atomHandle;
	}

	@Override
	public HGHandle getAtomHandle()
	{
		return thisHandle;
	}
	
	public String toString()
	{
		String s = "";
		if (graph != null)
		{
			HGDBOntology O = graph.get(ontology);
			s += O.getOntologyID().getOntologyIRI().getFragment() + 
					" (" + O.getOntologyID().getOntologyIRI() + ")";
		}
		return s + " At " + revision() + ", with " + changes().size() + " working changes.";		
	}
}
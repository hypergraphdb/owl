package org.hypergraphdb.app.owl.versioning;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGValueLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.change.VAddBranchChange;
import org.hypergraphdb.app.owl.versioning.change.VAddLabelChange;
import org.hypergraphdb.app.owl.versioning.change.VBranchRenameChange;
import org.hypergraphdb.app.owl.versioning.change.VMetadataChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveLabelChange;

/**
 * <p>
 * Manages metadata about a versioned object. Metadata is special in that change
 * history needs to be preserved just like for the regular versioned object, but
 * it is hidden from the user and there is no notion of revisions etc. The
 * history is needed because each change is a transaction that potentially has
 * to be propagated to other peers. When a user set the working copy of a
 * versioned to a certain revision, this should not modify the metadata view at
 * all. That is, branches and labels should remain the same regardless of where
 * in the main revision graph one decides to work. And in that respect, branches
 * and labels behave similarly.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class VersionedMetadata<T extends Versioned<T>>
{
	private static final String METACHANGE_ROOT = "METACHANGE_ROOT";
	private HyperGraph graph;
	private T versioned;

	// assuming we're in a transaction already
	private void lastChange(HGHandle lastChange)
	{
		HGHandle current = graph.findOne(hg.and(hg.eq(METACHANGE_ROOT), hg.incident(versioned.getAtomHandle())));
		if (current != null)
		{
			HGValueLink link = graph.get(current);
			graph.remove(current);
			graph.add(new ParentLink(lastChange, link.getTargetAt(0)));
		}
		graph.add(new HGValueLink(METACHANGE_ROOT, lastChange, versioned.getAtomHandle()));
	}

	private void performChange(VMetadataChange<T> change)
	{
		change.setHyperGraph(graph);
		change.apply(versioned);
		if (change.getAtomHandle() != null)
			graph.define(change.getAtomHandle(), change);
		else
			graph.add(change);
		lastChange(change.getAtomHandle());
	}

	public VersionedMetadata(HyperGraph graph, T versioned)
	{
		this.graph = graph;
		this.versioned = versioned;
	}

	public HGHandle lastChange()
	{
		HGLink link = graph.getOne(hg.and(hg.eq(METACHANGE_ROOT), hg.incident(versioned.getAtomHandle())));
		if (link == null)
			return null;
		else
			return link.getTargetAt(0);
	}

	public HGHandle createBranch(final HGHandle revhandle, final String name, final String user)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>()
		{
			public HGHandle call()
			{
				VAddBranchChange<T> change = new VAddBranchChange<T>();
				change.setHyperGraph(graph);
				change.setRevision(revhandle);
				change.setName(name);
				change.setCreatedBy(user);
				change.setCreatedOn(System.currentTimeMillis());
				performChange(change);
				return hg.findOne(graph,
						hg.and(hg.type(Branch.class), 
							   hg.eq("name", name), 
							   hg.eq("versioned", versioned.getAtomHandle())));
			}
		});
	}

	/**
	 * <p>
	 * Lookup a revision branch for this versioned ontology by name.
	 * </p>
	 * 
	 * @param name
	 *            The name of the branch.
	 * @return The {@link Branch} instance of <code>null</code> if not found.
	 */
	public Branch findBranch(String name)
	{
		return graph.getOne(hg.and(hg.type(Branch.class), 
								   hg.eq("name", name), 
								   hg.eq("versioned", versioned.getAtomHandle())));
	}

	public HGHandle findBranchHandle(String name)
	{
		return hg.findOne(graph, hg.and(hg.type(Branch.class),
										hg.eq("versioned", versioned.getAtomHandle()),
										hg.eq("name", name)));		
	}
	
	/**
	 * Change the name of this branch. For proper revisiont tracking, this
	 * method must be used instead of {@link #setName(String)} which is only a
	 * bean setter to be used for DB persistence.
	 * 
	 * @param newname
	 *            The new branch name. The only restriction is that the new name
	 *            be unique for the versioned object.
	 * @return <code>this</code>
	 */
	public Branch renameBranch(final Branch branch, final String newname)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Branch>()
		{
			public Branch call()
			{
				if (graph.findOne(hg.and(hg.type(Branch.class), hg.eq("name", newname), hg.eq("versioned", branch.getVersioned()))) != null)
					throw new IllegalArgumentException("Duplicate branch name '" + newname + "'.");
				VBranchRenameChange<T> change = new VBranchRenameChange<T>();
				change.setCurrentName(branch.getName());
				change.setNewname(newname);
				performChange(change);
				return branch;
			}
		});
	}

	/**
	 * Return a collection of all branches for this versioned object.
	 */
	public Collection<Branch> allBranches()
	{
		return graph.getAll(hg.and(hg.type(Branch.class), hg.eq("versioned", versioned.getAtomHandle())));
	}

	/**
	 * Return the set of all revisions tagged with the given label (a possible
	 * empty set).
	 */
	public Set<Revision> revisionsWithLabel(final String label)
	{
		return graph.getTransactionManager().transact(new Callable<Set<Revision>>()
		{
			public Set<Revision> call()
			{
				HashSet<Revision> S = new HashSet<Revision>();
				HGHandle labelHandle = graph.findOne(hg.eq(label));
				if (labelHandle == null)
					return S;
				for (HGHandle handle : graph.findAll(hg.and(hg.type(LabelLink.class), hg.incident(labelHandle))))
				{
					LabelLink labelLink = graph.get(handle);
					Revision rev = graph.get(labelLink.atom());
					if (rev.versioned().equals(versioned.getAtomHandle()))
						S.add(rev);
				}
				return S;
			}
		});
	}

	/**
	 * <p>
	 * Label this revision with some meaningful string. Multiple labels per
	 * revision are possible. If the revision is already labeled with the
	 * specified label, nothing happens.
	 * </p>
	 * 
	 * @param label
	 *            The revision label. The same label can used for more than one
	 *            revision.
	 * @return this
	 */
	public VersionedMetadata<T> label(final HGHandle tolabel, final String label)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				HGHandle labelHandle = hg.assertAtom(graph, label);
				HGHandle labelLink = graph.findOne(hg.and(hg.type(LabelLink.class),
						hg.orderedLink(labelHandle, tolabel, versioned.getAtomHandle())));
				if (labelLink == null)
					performChange(new VAddLabelChange<T>(labelHandle, tolabel));
				return null;
			}
		});
		return this;
	}

	/**
	 * Remove a label from a revision. If the revision is not labeled with the
	 * specified label, nothing happens.
	 * 
	 * @param label
	 * @return this.
	 */
	public VersionedMetadata<T> unlabel(final HGHandle tolabel, final String label)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				HGHandle labelHandle = graph.findOne(hg.eq(label));
				if (labelHandle == null)
					return this;
				HGHandle labelLink = graph.findOne(hg.and(hg.type(LabelLink.class), hg.incident(labelHandle), hg.incident(tolabel)));
				if (labelLink != null)
					performChange(new VRemoveLabelChange<T>(labelHandle, tolabel));
				return null;
			}
		});
		return this;
	}

	/**
	 * Return the set of all labels with which this revision is labeled
	 * (possibly an empty set).
	 */
	public Set<String> labels(HGHandle labeled)
	{
		HashSet<String> S = new HashSet<String>();
		List<LabelLink> allLabels = graph.getAll(hg.and(hg.type(LabelLink.class), hg.incident(labeled)));
		for (LabelLink ll : allLabels)
			S.add((String) graph.get(ll.label()));
		return S;
	}

	public HGHandle applyChanges(final List<VMetadataChange<T>> changes)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>()
		{
			public HGHandle call()
			{
				for (VMetadataChange<T> change : changes)
					VersionedMetadata.this.performChange(change);
				return lastChange();
			}
		});
	}
}
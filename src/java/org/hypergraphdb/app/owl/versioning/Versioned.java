package org.hypergraphdb.app.owl.versioning;

import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;

/**
 * <p>
 * A versioned artifact is one that undergoes changes that are tracked through
 * revisions. 
 * </p>
 * 
 * @author Borislav Iordanov
 * @param T concrete type
 */
public interface Versioned<T extends Versioned<T>>
{
	/**
	 * Return all version heads. A version head is a revision that is the
	 * last in its branch. One of the revision heads is always going to be
	 * the current {@link #revision()}.
	 */
	Set<HGHandle> heads();
	
	/**
	 * Return the currently active revision.
	 */
	Revision revision();
	
	/**
	 * <p>
	 * Create a new revision based on the current revision. 
	 * All accumulated working changes over the 
	 * currently active revision will be first committed.
	 * </p>
	 * 
	 * @return The newly created revision.
	 */
	Revision commit(final String user, final String comment);
	
	/**
	 * Drop any un-flushed working set changes and return <code>this</code>.
	 */
	T undo();
	
	/**
	 * <p>
	 * Merge a number of different revisions. The following assumptions are
	 * made about the set R of revisions being merged:
	 * <ul>
	 * <li>no revision in the argument set R is an ancestor (direct or indirect) of another
	 * revision</li>
	 * <li>all revisions in the argument set R are head revisions, i.e. they are
	 * "child-less" and no have descendants.</li>
	 * <li>
	 * Revision are either pairwise conflict-free or the user is ok with the conflicts
	 * between automatically resolved as documented by the concrete
	 * <code>Versioned</code> implementation.
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @param user The user performing the merge.
	 * @param comment A comment associated with the new revision to be created.
	 * @param revisions The list of revisions to merge. If there are < 2 revisions, 
	 * nothing is done.
	 * @return The newly created {@link Revision} as the result of the merge or
	 * <code>null</code> if there are less than two revision arguments provided. 
	 */
	Revision merge(final String user, final String comment, Revision...revisions);
	
	/**
	 * <p>
	 * Flush (commit) all working changes to create a change mark point.
	 * This basically resets the working changes to nothing and creates a
	 * point to which the versioned object can be rolled back. 
	 * </p>
	 * 
	 * <p>
	 * This operation is usually invoked by the framework when a new revision is
	 * being created.
	 * </p>
	 */
	ChangeRecord flushChanges();
	
	/**
	 * Return the latest working changes. Those are the changes that will
	 * be committed if the {@link flushChanges} method is called.
	 */
	ChangeSet<T>  changes();

	/**
	 * Return the changes that produced the specified {@link Revision}.
	 * 
	 * @param revision The revision.
	 * @return
	 */
	List<ChangeSet<T>> changes(Revision revision);
}
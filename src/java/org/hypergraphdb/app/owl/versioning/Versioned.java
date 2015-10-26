package org.hypergraphdb.app.owl.versioning;

import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;

/**
 * <p>
 * A versioned artifact is one that undergoes changes that are tracked through
 * revisions. 
 * </p>
 * 
 * @author Borislav Iordanov
 * @param T concrete type
 */
public interface Versioned<T extends Versioned<T>> extends HGHandleHolder
{
	VersionedMetadata<T> metadata();
	
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
	 * Create a new revision for this ontology. The revision is created
	 * regardless of whether there are any pending changes or not. If there are no
	 * pending changes, the latest {@link ChangeRecord} is used and no flush is done.
	 * If there are pending (i.e. working) changes, the {@link #flushChanges()} method
	 * is invoked first to create a new <code>ChangeRecord</code>.
	 * </p>
	 * <p>
	 * The branch of last revision (if any) will automatically be applied to the newly
	 * created revision.
	 * </p>
	 * @param user The name of the user creating the revision.
	 * @param comment Arbitrary comment string.
	 * @return The newly created revision object.
	 */
	Revision commit(final String user, final String comment);

	/**
	 * <p>
	 * Create a new revision with a new branch. The behavior is the same as 
	 * the {@link #commit(String, String)} method except instead of carrying
	 * over the branch of the current revision, a new branch with the specified
	 * name will be created. 
	 * </p>
	 * 
	 * @param user The user creating the new revision.
	 * @param comment An arbitrary comment.
	 * @param branch The new branch or <code>null</code> to switch to 
	 * no branching mode (i.e. anonymous, unnamed branch).
	 * @return The newly created revision object.
	 */
	Revision commit(final String user, final String comment, String branch);
	
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
	 * @param branch The branch for the merged revision. This branch must either be
	 * a new branch name, or the name of the branch of one of the revisions being merged.
	 * @param revisions The list of revisions to merge. If there are < 2 revisions, 
	 * nothing is done.
	 * @return The newly created {@link Revision} as the result of the merge or
	 * <code>null</code> if there are less than two revision arguments provided. 
	 */
	Revision merge(final String user, final String comment, final String branch, Revision...revisions);
		
	/**
	 * Return the latest working changes. Those are the changes that will
	 * be committed if the {@link flushChanges} method is called.
	 */
	ChangeSet<T>  changes();
}
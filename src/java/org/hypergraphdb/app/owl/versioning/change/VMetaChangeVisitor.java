package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.app.owl.versioning.Versioned;

/**
 * 
 * <p>
 * A visitor for all the types of metadata changes.
 * </p>
 *
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public interface VMetaChangeVisitor<T extends Versioned<T>>
{
	void visit(VAddBranchChange<T> change);
	void visit(VAddLabelChange<T> change);
	void visit(VBranchRenameChange<T> change);
	void visit(VRemoveBranchChange<T> change);
	void visit(VRemoveLabelChange<T> change);
}
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Versioned;

/**
 * <p>
 * Implementation of a change that renames an existing branch of
 * a {@link org.hypergraphdb.app.owl.versioning.Versioned} object.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public class VBranchRenameChange<T extends Versioned<T>> extends VBranchChange<T>
{	
	private HGHandle branchHandle;
	private String newname;
	
	@Override
	public void apply(T versioned)
	{
		Branch branch = graph.get(branchHandle);
		assert branch.getVersioned().equals(graph.getHandle(versioned));
		assert graph.findOne(hg.and(hg.type(Branch.class), 
									hg.eq("name", newname), 
									hg.eq("versioned", branch.getVersioned()))) == null;
		branch.setName(newname);
		graph.update(branch);
	}

	@Override
	public VChange<T> inverse()
	{
		VBranchRenameChange<T> inverse = new VBranchRenameChange<T>(); 
		Branch branch = graph.get(branchHandle);
		inverse.setBranchHandle(branchHandle);
		inverse.setNewname(branch.getName());
		return inverse;
	}

	@Override
	public boolean conflictsWith(VChange<T> other)
	{
		return other instanceof VBranchRenameChange &&
			((VBranchRenameChange<T>)other).getBranchHandle().equals(branchHandle);	
	}

	@Override
	public boolean isEffective(T versioned)
	{
		Branch branch = graph.get(branchHandle);
		assert branch.getVersioned().equals(graph.getHandle(versioned));		
		return !newname.equals(branch.getName());
	}

	@Override
	public boolean isIdempotent()
	{
		return true;
	}

	public HGHandle getBranchHandle()
	{
		return branchHandle;
	}

	public void setBranchHandle(HGHandle branchHandle)
	{
		this.branchHandle = branchHandle;
	}

	public String getNewname()
	{
		return newname;
	}

	public void setNewname(String newname)
	{
		this.newname = newname;
	}	
}
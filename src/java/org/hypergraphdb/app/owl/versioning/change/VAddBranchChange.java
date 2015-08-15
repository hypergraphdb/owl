package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Versioned;

public class VAddBranchChange<T extends Versioned<T>> extends VBranchChange<T>
{
	private String name;
	private String createdBy;
	private long createdOn;
	
	public VAddBranchChange()
	{		
	}
	
	public VAddBranchChange(String name, String createdBy, long createdOn)
	{
		this.name = name;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
	}
	
	@Override
	public void apply(T versioned)
	{
		Branch branch = new Branch();
		branch.setName(name);
		branch.setCreatedBy(createdBy);
		branch.setCreatedOn(createdOn);
	}

	@Override
	public boolean conflictsWith(VChange<T> other)
	{
		return other instanceof VAddBranchChange && 
			  ((VAddBranchChange<T>)other).name.equals(name) ||
			  other instanceof VBranchRenameChange && 
			  ((VBranchRenameChange<T>)other).getNewname().equals(name);
	}
	
	@Override
	public VChange<T> inverse()
	{
		return new VRemoveBranchChange<T>(name);
	}
	
	@Override
	public boolean isEffective(T versioned)
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isIdempotent()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Versioned;

public class VAddBranchChange<T extends Versioned<T>> extends VMetadataChange<T>
{
	private String name;
	private String createdBy;
	private long createdOn;
	
	public VAddBranchChange()
	{		
	}
	
	public VAddBranchChange(String name, String createdBy)
	{
		this(name, createdBy, System.currentTimeMillis());
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
		if (isEffective(versioned))
		{
			Branch branch = new Branch();
			branch.setName(name);
			branch.setCreatedBy(createdBy);
			branch.setCreatedOn(createdOn);
			branch.setVersioned(versioned.getAtomHandle());
			graph.add(branch);
		}
	}

	@Override
	public boolean conflictsWith(VChange<T> other)
	{
		return other instanceof VRemoveBranchChange && 
			  ((VRemoveBranchChange<T>)other).getName().equals(name) ||
			  other instanceof VBranchRenameChange && 
			  ((VBranchRenameChange<T>)other).getNewname().equals(name);
	}
	
	@Override
	public VChange<T> inverse()
	{
		return new VRemoveBranchChange<T>(name, createdBy);
	}
	
	@Override
	public boolean isEffective(T versioned)
	{
		return graph.findOne(hg.and(hg.type(Branch.class),
				hg.eq("name", name),
				hg.eq("versioned", versioned.getAtomHandle()))) == null;
	}
	
	@Override
	public boolean isIdempotent()
	{
		return true;
	}
	
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public long getCreatedOn()
	{
		return createdOn;
	}

	public void setCreatedOn(long createdOn)
	{
		this.createdOn = createdOn;
	}
}
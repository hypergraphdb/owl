package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Versioned;

public class VRemoveBranchChange<T extends Versioned<T>> extends VMetadataChange<T>
{
	private String name;
	private String user;
	
	public VRemoveBranchChange()
	{		
	}
	
	public VRemoveBranchChange(String name, String user)
	{
		this.name = name;
		this.user = user;
	}
	
	@Override
	public void apply(T versioned)
	{ 
		HGHandle branchHandle = graph.findOne(hg.and(hg.type(Branch.class),
										hg.eq("name", name),
										hg.eq("versioned", versioned.getAtomHandle())));
		if (branchHandle != null)
			graph.remove(branchHandle);
	}

	@Override
	public VChange<T> inverse()
	{
		return new VAddBranchChange<T>(name, user);
	}

	@Override
	public boolean conflictsWith(VChange<T> other)
	{
		return other instanceof VAddBranchChange && 
				  ((VAddBranchChange<T>)other).getName().equals(name) ||
				  other instanceof VBranchRenameChange && 
				  ((VBranchRenameChange<T>)other).getNewname().equals(name);
		
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

	public String getUser()
	{
		return user;
	}

	public void setUser(String user)
	{
		this.user = user;
	}
}

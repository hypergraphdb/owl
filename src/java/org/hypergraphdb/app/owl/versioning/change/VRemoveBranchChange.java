package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.app.owl.versioning.Versioned;

public class VRemoveBranchChange<T extends Versioned<T>> extends VBranchChange<T>
{
	private String name;
	
	public VRemoveBranchChange(String name)
	{
		this.name = name;
	}
	
	@Override
	public void apply(T versioned)
	{ 
	}

	@Override
	public VChange<T> inverse()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean conflictsWith(VChange<T> other)
	{
		// TODO Auto-generated method stub
		return false;
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

}

package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;

/**
 * <p>
 * Represents a revision branch. A branch is roughly what is thought of as a branch
 * in versioning systems and like versioning systems, this comes with its own rules
 * and assumptions. Here are ours:  
 * </p>
 * 
 * <ul>
 * <li>A branch has a name that must be unique in any repository. However
 * a branch is also a graph object on its and the name is just one of its attributes
 * that can be changed. Its identity remains stable.</li>
 * <li>When a new revision it may be placed on a branch or not. If it is placed on
 * a branch, it remains part of that branch forever.</li>
 * <li>A branch is always linear. Therefore, a more accurate term would be 
 * <code>lineage</code>. However, we use the term <code>branch</code> because it is
 * the most common term of similar concepts. The API will not allow two divergent paths
 * to be qualified with the same branch.</li>
 * <li></li>   
 * </ul>
 * <p>
 * The notion of branch borrows heavily from the Mercurial DVCS, but recognizes that
 * it is highly problematic to allow a branch to have divergent paths. Thus, a branch 
 * cannot have multiple heads as it would be possible in Mercurial. There have been heated
 * debates b/w the Git and Mercurial communities on the merits of named branches. Whatever 
 * one's position, one must recognize that this view of branching looses no information 
 * and therefore subsumes the pointer-based approach that Git implements.
 * </p>
 * @author Borislav Iordanov
 *
 */
public class Branch implements HGHandleHolder
{
	private HGHandle thisHandle;
	private HGHandle versioned;
	private String name;
	private String createdBy;
	private long   createdOn;
	
	public Branch()
	{		
	}
	
	public Branch(String name, HGHandle versioned, String createdBy, long createdOn)
	{
		this.name = name;
		this.versioned = versioned;
		this.createdBy = createdBy;
		this.createdOn = createdOn;
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

	public HGHandle getVersioned()
	{
		return versioned;
	}

	public void setVersioned(HGHandle versioned)
	{
		this.versioned = versioned;
	}

	@Override
	public HGHandle getAtomHandle()
	{
		return thisHandle;
	}

	@Override
	public void setAtomHandle(HGHandle handle)
	{
		this.thisHandle = handle;
	}
	
	public String toString()
	{
		return "Branch[" + name + ", " + thisHandle + "]";
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + (int) (createdOn ^ (createdOn >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((thisHandle == null) ? 0 : thisHandle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Branch other = (Branch) obj;
		if (createdBy == null)
		{
			if (other.createdBy != null)
				return false;
		}
		else if (!createdBy.equals(other.createdBy))
			return false;
		if (createdOn != other.createdOn)
			return false;
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		if (thisHandle == null)
		{
			if (other.thisHandle != null)
				return false;
		}
		else if (!thisHandle.equals(other.thisHandle))
			return false;
		return true;
	}	
}
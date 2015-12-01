package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.Versioned;

/**
 * Adding a branch is always in the context of a new revision with which it is
 * associated, it is always created by someone, it always has a name and the change
 * occurs at a specific time point - hence the 4 attributes here.
 * 
 * @author Borislav Iordanov
 *
 * @param <T>
 */
public class VAddBranchChange<T extends Versioned<T>> extends VMetadataChange<T>
{
	private HGHandle handle;
	private String name;
	private String createdBy;
	private long createdOn;
	private HGHandle revision;
	
	public VAddBranchChange()
	{		
	}
	
	public VAddBranchChange(HGHandle revision, String name, String createdBy)
	{
		this(revision, name, createdBy, System.currentTimeMillis());
	}
	
	public VAddBranchChange(HGHandle revision, String name, String createdBy, long createdOn)
	{
		this.revision = revision;
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
			if (handle == null)
				handle = graph.add(branch);
			else
				graph.define(handle, branch);
			Revision rev = graph.get(revision);
			rev.branchHandle(handle);
			graph.update(rev);
		}
	}

	@Override
	public Change<T> reduce(Change<T> previous)
	{
		return null;
	}
	
	@Override
	public boolean conflictsWith(Change<T> other)
	{
		return other instanceof VAddBranchChange &&
				((VAddBranchChange<T>)other).getName().equals(name) &&
				!((VAddBranchChange<T>)other).getRevision().equals(revision) ||
			  other instanceof VRemoveBranchChange && 
			  ((VRemoveBranchChange<T>)other).getName().equals(name) ||
			  other instanceof VBranchRenameChange && 
			  ((VBranchRenameChange<T>)other).getNewname().equals(name);
	}
	
	@Override
	public Change<T> inverse()
	{
		return new VRemoveBranchChange<T>(revision, name, createdBy);
	}
	
	@Override
	public boolean isEffective(T versioned)
	{
		return graph.findOne(hg.and(hg.type(Branch.class),
				hg.eq("name", name),
				hg.eq("versioned", versioned.getAtomHandle()))) == null &&
				((Revision)graph.get(revision)).branchHandle() == null;
	}
	
	public String toString()
	{
		return "addBranch[" + name + ", " + revision + ", " + createdBy + ", " + createdOn + "]";
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

	public HGHandle getRevision()
	{
		return revision;
	}

	public void setRevision(HGHandle revision)
	{
		this.revision = revision;
	}

	public HGHandle getHandle()
	{
		return handle;
	}

	public void setHandle(HGHandle handle)
	{
		this.handle = handle;
	}	
}
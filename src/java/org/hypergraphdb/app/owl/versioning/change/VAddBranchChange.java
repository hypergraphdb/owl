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
	private HGHandle branchHandle;
	private String name;
	private String createdBy;
	private long createdOn;
	private HGHandle revision;
	
	public VAddBranchChange()
	{		
	}
	
	public void visit(VMetaChangeVisitor<T> visitor)
	{
		visitor.visit(this);
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
			if (branchHandle == null)
				branchHandle = graph.add(branch);
			else
				graph.define(branchHandle, branch);
			Revision rev = graph.get(revision);
			rev.branchHandle(branchHandle);
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
		// If branch already exists for this versioned, the change is not effective.
		if (graph.findOne(hg.and(hg.type(Branch.class),
				hg.eq("name", name),
				hg.eq("versioned", versioned.getAtomHandle()))) != null)
			return false;
		// If the revision we are attaching the branch already has another branch, the
		// change is not effective either. However, if the revision has already the 
		// same branch handle stored in it, the change *is* effective because it simly means
		// we are missing the branch object in the graph.
		HGHandle bhandle =  ((Revision)graph.get(revision)).branchHandle();
		if (bhandle == null || bhandle.equals(this.branchHandle))
			return true;
		else
			return false;
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

	public HGHandle getBranchHandle()
	{
		return branchHandle;
	}

	public void setBranchHandle(HGHandle handle)
	{
		this.branchHandle = handle;
	}	
}
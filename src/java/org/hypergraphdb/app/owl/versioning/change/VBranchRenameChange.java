package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Change;
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
public class VBranchRenameChange<T extends Versioned<T>> extends VMetadataChange<T>
{	
	private String currentName;
	private String newname;
	
	public VBranchRenameChange()
	{		
	}
	
	public VBranchRenameChange(String currentName, String newname)
	{		
		this.currentName = currentName;
		this.newname = newname;
	}
	
	@Override
	public void apply(T versioned)
	{
		Branch branch = graph.getOne(hg.and(hg.type(Branch.class), 
							hg.eq("name", currentName), 
							hg.eq("versioned", versioned.getAtomHandle()))); 
		if (branch == null)
			throw new NullPointerException("No branch with name " + currentName + " in versioned " + versioned);
		if (graph.findOne(hg.and(hg.type(Branch.class), 
							 hg.eq("name", newname), 
							 hg.eq("versioned", branch.getVersioned()))) != null)
			throw new IllegalArgumentException("Attempting to rename branch  " + currentName + " to  " + newname + 
					", but " + newname + " already exists.");
		branch.setName(newname);
		graph.update(branch);
	}

	@Override
	public Change<T> reduce(Change<T> previous)
	{
		if (previous instanceof VAddBranchChange)
		{
			VAddBranchChange<T> add = (VAddBranchChange<T>)previous;
			if (add.getName().equals(currentName))
				return new VAddBranchChange<T>(add.getRevision(), 
											   getNewname(), 
											   add.getCreatedBy(), 
											   add.getCreatedOn());
		}
		return null;
	}
	
	@Override
	public Change<T> inverse()
	{
		return new VBranchRenameChange<T>(newname, currentName); 
	}

	@Override
	public boolean conflictsWith(Change<T> other)
	{
		if (other instanceof VBranchRenameChange)
		{
			VBranchRenameChange<T> renaming = (VBranchRenameChange<T>)other;
			return renaming.currentName.equals(currentName) &&
				   !renaming.newname.equals(newname) ||
				   
				   !renaming.currentName.equals(currentName) &&
				   renaming.newname.equals(newname);
		}
		else if (other instanceof VRemoveBranchChange)
			return  ((VRemoveBranchChange<T>)other).getName().equals(currentName);				
		else if (other instanceof VAddBranchChange)
			return  ((VAddBranchChange<T>)other).getName().equals(currentName);
		else
			return false;
	}

	@Override
	public boolean isEffective(T versioned)
	{
		return graph.findOne(hg.and(hg.type(Branch.class),
					hg.eq("name", currentName),
					hg.eq("versioned", versioned.getAtomHandle()))) != null &&
				
			   graph.findOne(hg.and(hg.type(Branch.class),
					hg.eq("name", newname),
					hg.eq("versioned", versioned.getAtomHandle()))) == null;
	}
	
	public String toString()
	{
		return "renameBranch[" + currentName + ", " + newname + "]";		
	}

	@Override
	public boolean isIdempotent()
	{
		return true;
	}

	public String getCurrentName()
	{
		return currentName;
	}

	public void setCurrentName(String currentName)
	{
		this.currentName = currentName;
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
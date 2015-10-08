package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.util.HGUtils;

/**
 * <p>
 * Connects two revisions (states in the versioned graph)
 *  in parent-child relationship via the {@link ChangeSet}
 *  that would move the state of the object from the parent revision to the child revision.
 * </p>
 * 
 * <p>
 * <b>Format: ChangeLink[ParentHandle, ChangeSetHandle, ChildHandle]</b>
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ChangeLink implements HGLink, HGHandleHolder
{
	private HGHandle thisHandle, child, parent, change;
	
	public ChangeLink()
	{		
	}
	
	/**
	 * Passe in 3 arguments in that order: parent/ancestor, change and child/successor
	 * @param handles
	 */
	public ChangeLink(HGHandle...handles)
	{
		parent = handles[0];
		change = handles[1];
		child  = handles[2];
	}

	public ChangeLink child(HGHandle child)
	{
		this.child = child;
		return this;
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

	public ChangeLink parent(HGHandle parent)
	{
		this.parent = parent;
		return this;
	}
	
	public HGHandle parent()
	{
		return parent;
	}
	
	public HGHandle child()
	{
		return child;
	}
	
	public HGHandle change()
	{
		return change;
	}
	
	public ChangeLink change(HGHandle change)
	{
		this.change = change;
		return this;
	}
	
	@Override
	public int getArity()
	{
		return 3;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		switch (i)
		{
			case 0: return parent;
			case 1: return change;
			case 2: return child;
			default: throw new IllegalArgumentException("target index " + i);				
		}
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		switch (i)
		{
			case 0: { parent = handle; break; }
			case 1: { change = handle; break; }
			case 2: { child = handle; break; }
			default: throw new IllegalArgumentException("target index " + i);				
		}
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}

	@Override
	public int hashCode()
	{
		return HGUtils.hashThem(HGUtils.hashThem(parent, change), child);
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( ! (obj instanceof ChangeLink))
			return false;
		ChangeLink mp = (ChangeLink)obj;
		return HGUtils.eq(parent, mp.parent) && HGUtils.eq(child, mp.child) && HGUtils.eq(change, mp.change);
	}		
	
	public String toString()
	{
		return "ChangeLink[" + parent + ", " + change + "," + child + "]"; 
	}
}
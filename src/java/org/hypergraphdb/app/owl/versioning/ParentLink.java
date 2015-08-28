package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.util.HGUtils;

/**
 * <p>
 * Connects two atoms in parent-child relationship. This is used
 * to link {@link ChangeSet}s and {@link Revision}s.
 * </p>
 * 
 * <p>
 * <b>Format: ParentLink[Child, Parent]</b>
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ParentLink implements HGLink, HGHandleHolder
{
	private HGHandle thisHandle, child, parent;
	
	public ParentLink()
	{		
	}
	
	public ParentLink(HGHandle...handles)
	{
		child = handles[0];
		parent = handles[1];
	}

	public ParentLink child(HGHandle child)
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

	public ParentLink parent(HGHandle parent)
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
	
	@Override
	public int getArity()
	{
		return 2;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i == 0)
			return child;
		else if (i == 1)
			return parent;
		else
			throw new IllegalArgumentException("target index " + i);
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			child = handle;
		else if (i == 1)
			parent = handle;
		else
			throw new IllegalArgumentException("target index " + i);
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}

	@Override
	public int hashCode()
	{
		return HGUtils.hashThem(parent, child);
	}

	@Override
	public boolean equals(Object obj)
	{
		if ( ! (obj instanceof ParentLink))
			return false;
		ParentLink mp = (ParentLink)obj;
		return HGUtils.eq(parent, mp.parent) && HGUtils.eq(child, mp.child);
	}		
	
	public String toString()
	{
		return "ParentLink[" + child + ", " + parent + "]"; 
	}
}
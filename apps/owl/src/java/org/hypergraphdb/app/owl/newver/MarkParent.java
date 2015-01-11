package org.hypergraphdb.app.owl.newver;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class MarkParent implements HGLink
{
	private HGHandle child, parent;
	
	public MarkParent(HGHandle...handles)
	{
		child = handles[0];
		parent = handles[1];
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
}
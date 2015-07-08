package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class LabelLink implements HGLink
{
	private HGHandle label, atom;
	
	public LabelLink(HGHandle...handles)
	{
		if (handles.length != 2)
			throw new IllegalArgumentException("Expecting exactly 2 targets: label and atom labeled");
		label = handles[0];
		atom = handles[1];
	}
	
	public HGHandle label()
	{
		return label;
	}
	
	public HGHandle atom()
	{
		return atom;
	}
	
	@Override
	public int getArity()
	{
		return 2;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i > 1)
			throw new IndexOutOfBoundsException(" Target index " + i + " must 0 or 1");
		return i == 0 ? label : atom;		
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			label = handle;
		else
			atom = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}
}
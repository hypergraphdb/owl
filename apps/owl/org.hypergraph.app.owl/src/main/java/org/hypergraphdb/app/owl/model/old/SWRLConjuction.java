package org.hypergraphdb.app.owl.model.old;

import java.util.ArrayList;
import java.util.Collection;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class SWRLConjuction implements HGLink
{
	private ArrayList<HGHandle> L = new ArrayList<HGHandle>();

	public SWRLConjuction()
	{		
	}
	
	public SWRLConjuction(Collection<HGHandle> args)
	{
		this(args.toArray(new HGHandle[0]));
	}
	
	public SWRLConjuction(HGHandle...args)
	{
		for (HGHandle x:args)
			L.add(x);
	}
	
	public Collection<HGHandle> asCollection() { return L; }
	
	public int getArity()
	{
		return L.size();
	}

	public HGHandle getTargetAt(int i)
	{
		return L.get(i);
	}

	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		L.set(i, handle);
	}

	public void notifyTargetRemoved(int i)
	{
		L.remove(i);
	}
}
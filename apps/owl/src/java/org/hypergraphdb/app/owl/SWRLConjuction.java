package org.hypergraphdb.app.owl;

import java.util.ArrayList;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class SWRLConjuction implements HGLink
{
	private ArrayList<HGHandle> L = new ArrayList<HGHandle>();

	public SWRLConjuction()
	{		
	}
	
	public SWRLConjuction(HGHandle...args)
	{
		for (HGHandle x:args)
			L.add(x);
	}
	
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
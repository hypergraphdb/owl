package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class StashLink implements HGLink
{
	private HGHandle revision;
	private HGHandle changeSet;
	
	public StashLink(HGHandle...targets)
	{
		assert targets.length == 2;
		changeSet = targets[0];
		revision = targets[1];
	}
	@Override
	public int getArity()
	{
		return 2;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		assert i == 0 || i == 1;
		return i == 0 ? changeSet : revision;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			changeSet = handle;
		else
			revision = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}

	public HGHandle changeSet()
	{
		return changeSet;
	}
	
	public HGHandle revision()
	{
		return revision;
	}
}
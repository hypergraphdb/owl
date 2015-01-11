package org.hypergraphdb.app.owl.newver;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

public class TagRevision implements HGLink
{
	private String tag;
	private HGHandle revision;

	public TagRevision(HGHandle revision, String tag)
	{
		this.revision = revision;
		this.tag = tag;
	}
	
	public TagRevision(HGHandle...handles)
	{
		if (handles.length != 1)
			throw new IllegalArgumentException("Expecting exactly 1 targets: the atom being tagged.");
		revision = handles[0];		
	}
	
	public HGHandle revision()
	{
		return revision;
	}
	
	public String getTag()
	{
		return tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	@Override
	public int getArity()
	{
		return 1;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i == 0)
			return revision;
		else
			throw new IllegalArgumentException("Target index must be 0 for a 1-arity link.");
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			revision = handle;
		else
			throw new IllegalArgumentException("Target index must be 0 for a 1-arity link.");		
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}
}
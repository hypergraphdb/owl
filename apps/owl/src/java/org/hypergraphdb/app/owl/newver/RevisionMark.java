package org.hypergraphdb.app.owl.newver;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

/**
 * A <code>RevisionMark</code> connects the {@link ChangeSet} that 
 * led to the creation of a given Revision.
 * 
 * @author Borislav Iordanov
 *
 */
public class RevisionMark implements HGLink
{
	private HGHandle hRevision;
	private HGHandle hMark;
	
	public RevisionMark(HGHandle...handles)
	{
		assert handles.length == 2;		
		hRevision = handles[0];
		hMark = handles[1];
	}
	
	@Override
	public int getArity()
	{
		return 2;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		return (i == 0) ? hRevision : hMark;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			hRevision = handle;
		else if (i == 1)
			hMark = handle;
		else
			throw new IllegalArgumentException("target index " + i);
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
	}
}
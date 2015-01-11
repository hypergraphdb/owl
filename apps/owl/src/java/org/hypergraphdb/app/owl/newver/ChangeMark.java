package org.hypergraphdb.app.owl.newver;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;

/**
 * <p>
 * Represents the application of a set of changes to a versioned object.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class ChangeMark implements HGLink
{
	private HGHandle target, changeset;
	private long timestamp;
	
	public ChangeMark(HGHandle...targets)
	{
		if (targets.length != 2)
			throw new IllegalArgumentException("Expecting exactly 2 targets: target object and changeset");
		target = targets[0];
		changeset = targets[1];
	}

	public HGHandle target()
	{
		return getTargetAt(0);
	}
	
	public HGHandle changeset()
	{
		return getTargetAt(1);
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
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
		return i == 0 ? target : changeset;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i == 0)
			target = handle;
		else
			changeset = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		throw new UnsupportedOperationException("Can't remove a commit link target, first delete the commit atom.");
	}
}
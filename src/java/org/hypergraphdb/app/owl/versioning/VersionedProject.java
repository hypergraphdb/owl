package org.hypergraphdb.app.owl.versioning;

import java.util.Set;
import org.hypergraphdb.HGHandle;

public class VersionedProject implements Versioned<VersionedProject>
{
	private HGHandle thisHandle;
	private Revision currentRevision;
	private VersionedMetadata<VersionedProject> metadata;
	
	public VersionedMetadata<VersionedProject> metadata()
	{
		return this.metadata;
	}
	
	public Revision revision()
	{
		return currentRevision;
	}
	
	@Override
	public Revision commit(final String user, final String comment)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Revision commit(final String user, final String comment, String branch)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Revision merge(String user, String comment, String branch, Revision... revisions)
	{
		// TODO Auto-generated method stub
		return null;
	}
		
	@Override
	public ChangeSet<VersionedProject> changes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VersionedProject undo()
	{
		return this;
	}

	@Override
	public Set<HGHandle> heads()
	{
		// TODO Auto-generated method stub
		return null;
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
}
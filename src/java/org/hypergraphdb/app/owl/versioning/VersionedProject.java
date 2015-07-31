package org.hypergraphdb.app.owl.versioning;

import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;

public class VersionedProject implements Versioned<VersionedProject>
{
	Revision currentRevision;
	
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
	public Revision merge(String user, String comment, Revision... revisions)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public ChangeRecord flushChanges()
	{
		return null;
	}
	
	@Override
	public ChangeSet<VersionedProject> changes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ChangeSet<VersionedProject>> changes(Revision revision)
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
}
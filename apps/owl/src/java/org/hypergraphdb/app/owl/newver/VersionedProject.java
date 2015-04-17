package org.hypergraphdb.app.owl.newver;

import java.util.List;

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
	public Revision merge(String user, String comment, Revision... revisions)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public ChangeMark flushChanges()
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
	public List<ChangeSet<?>> changes(Revision revision)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public VersionedProject undo()
	{
		return this;
	}
}
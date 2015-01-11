package org.hypergraphdb.app.owl.newver;

import org.hypergraphdb.app.owl.versioning.ChangeSet;

public class VersionedProject implements Versioned
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
	public ChangeSet changes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ChangeSet changes(Revision revision)
	{
		// TODO Auto-generated method stub
		return null;
	}

}

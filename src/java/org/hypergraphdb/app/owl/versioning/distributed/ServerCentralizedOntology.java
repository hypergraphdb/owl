package org.hypergraphdb.app.owl.versioning.distributed;

import org.hypergraphdb.HGHandle;

/**
 * ServerCentralizedOntology.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 23, 2012
 */
public class ServerCentralizedOntology extends DistributedOntology
{

	// TODO known clients?, client revisions?
	public ServerCentralizedOntology()
	{
	}

	public ServerCentralizedOntology(HGHandle... args)
	{
		super(args);
	}

	public String toString()
	{
		if (getVersionedOntology() != null)
		{
			return getVersionedOntology().toString() + " (Server)";
		}
		else
		{
			return super.toString();
		}
	}
}

package org.hypergraphdb.app.owl.versioning.distributed;

import org.hypergraphdb.HGHandle;

/**
 * PeerDistributedOntology.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 23, 2012
 */
public class PeerDistributedOntology extends DistributedOntology
{

	public PeerDistributedOntology(HGHandle... args)
	{
		super(args);
	}

	public String toString()
	{
		if (getVersionedOntology() != null)
		{
			return getVersionedOntology().toString() + " (Peer)";
		}
		else
		{
			return super.toString();
		}
	}
}

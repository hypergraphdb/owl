package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.util.ArrayList;
import java.util.Collection;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.Branch;

public class VOWLXMLMetadata
{
	Collection<Branch> branches =  new ArrayList<Branch>();
	HGHandle lastChange;
	
	public Collection<Branch> branches()
	{
		return branches;
	}
	
	public VOWLXMLMetadata lastChange(HGHandle lastChange)
	{
		this.lastChange = lastChange;
		return this;
	}
	
	public HGHandle lastChange()
	{
		return lastChange;
	}
}

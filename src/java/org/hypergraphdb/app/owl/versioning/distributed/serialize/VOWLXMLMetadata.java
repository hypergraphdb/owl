package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.util.ArrayList;
import java.util.Collection;

import org.hypergraphdb.app.owl.versioning.Branch;

public class VOWLXMLMetadata
{
	Collection<Branch> branches =  new ArrayList<Branch>();
	
	public Collection<Branch> branches()
	{
		return branches;
	}
}

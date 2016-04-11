package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.util.ArrayList;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VMetadataChange;

public class VOWLXMLMetadata
{
	List<VMetadataChange<VersionedOntology>> changes =  new ArrayList<VMetadataChange<VersionedOntology>>();
	HGHandle lastChange;
	
	public List<VMetadataChange<VersionedOntology>> changes()
	{
		return changes;
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

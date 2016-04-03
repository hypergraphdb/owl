package org.hypergraphdb.app.owl.versioning.distributed;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.app.owl.versioning.versioning;

/**
 * <p>
 * Represents an association of the local copy of a distributed ontology 
 * with a remotely stored version of it. This object
 * maintains the local state about the remote: a user friendly name, the peer identity,
 * the set of revision heads last fetched (representing the last known version state). 
 * </p>
 * 
 * <p>
 * There may be multiple such association for a locally stored ontology, one per each
 * remote peer.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class RemoteOntology
{
	@AtomReference("symbolic")
	private RemoteRepository repository;
	private HGHandle ontologyHandle;
	private Set<HGHandle> revisionHeads = new HashSet<HGHandle>();
	private HGHandle lastMetaChange = null;
	
	public RemoteOntology()
	{		
	}
	
	public RemoteOntology(HGHandle ontologyHandle, RemoteRepository repository)
	{
		this.ontologyHandle = ontologyHandle; 
		this.repository = repository;
	}
	
	public RemoteOntology(HGHandle ontologyHandle, 
						  RemoteRepository repository, 
						  Set<HGHandle> revisionHeads)
	{
		this.ontologyHandle = ontologyHandle;
		this.repository = repository;
		this.revisionHeads = revisionHeads;
	}
	
	public RemoteOntology updateRevisionHeads(HyperGraph graph, Set<HGHandle> deltaHeads)
	{
		Set<HGHandle> newHeads = new HashSet<HGHandle>();
		newHeads.addAll(getRevisionHeads());
		for (HGHandle deltaHead : deltaHeads)
		{
			for (HGHandle currentHead : getRevisionHeads())
				if (versioning.isPrior(graph, currentHead, deltaHead))
					newHeads.remove(currentHead);
			newHeads.add(deltaHead);
		}
		setRevisionHeads(newHeads);
		return this;
	}
	
	public RemoteRepository getRepository()
	{
		return repository;
	}
	public void setRepository(RemoteRepository repository)
	{
		this.repository = repository;
	}
	public HGHandle getOntologyHandle()
	{
		return ontologyHandle;
	}
	public void setOntologyHandle(HGHandle ontologyHandle)
	{
		this.ontologyHandle = ontologyHandle;
	}
	public Set<HGHandle> getRevisionHeads()
	{
		return revisionHeads;
	}
	public void setRevisionHeads(Set<HGHandle> revisionHeads)
	{
		this.revisionHeads = revisionHeads;
	}

	public HGHandle getLastMetaChange()
	{
		return lastMetaChange;
	}

	public void setLastMetaChange(HGHandle lastMetaChange)
	{
		this.lastMetaChange = lastMetaChange;
	}	
}
package org.hypergraphdb.app.owl.versioning.distributed;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.annotation.AtomReference;

/**
 * <p>
 * Represents an association with a remotely stored versioned ontology. This object
 * maintains the local state about the remote: a user friendly name, the peer identity,
 * the set of revision heads last fetched (representing the last known version state). 
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
}
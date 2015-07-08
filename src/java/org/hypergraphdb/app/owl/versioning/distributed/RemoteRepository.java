package org.hypergraphdb.app.owl.versioning.distributed;

import org.hypergraphdb.peer.HGPeerIdentity;

/**
 * <p>
 * Holds information about a remote repository with which we
 * are interacting (pulling and pushing data). 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class RemoteRepository
{
	private String name; 
	private HGPeerIdentity peer;
	
	public RemoteRepository()
	{		
	}
	
	public RemoteRepository(HGPeerIdentity peer)
	{
		this.peer = peer;
	}
	
	public RemoteRepository(String name, HGPeerIdentity peer)
	{
		this.name = name;
		this.peer = peer;
	}
	
	/**
	 * Return the user-friendly name associated with the repository.
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * Set a user-friendly name for the repository.
	 * @param name The name.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * Return the unique peer identifier for the repository.
	 */
	public HGPeerIdentity getPeer()
	{
		return peer;
	}
	/**
	 * Specify the unique peer identifier of the repository.
	 * @param peer The identifier.
	 */
	public void setPeer(HGPeerIdentity peer)
	{
		this.peer = peer;
	}
}

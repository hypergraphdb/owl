package org.hypergraphdb.app.owl.versioning.distributed;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.peer.HGPeerIdentity;

/**
 * ClientCentralizedOntology.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Aug 23, 2012
 */
public class ClientCentralizedOntology extends DistributedOntology {

	private HGPeerIdentity serverPeer;
	
	public ClientCentralizedOntology() {
	}	

	public ClientCentralizedOntology(HGHandle versionedOntologyHandle, HGPeerIdentity serverPeer) {
		super(versionedOntologyHandle);
		setServerPeer(serverPeer);
	}	

	public ClientCentralizedOntology(HGHandle...args) {
		super(args);
    }
	
	public HGPeerIdentity getServerPeer() {
		return serverPeer;
	}

	public void setServerPeer(HGPeerIdentity server) {
		serverPeer = server;
	}

	public String toString() {
		if (getVersionedOntology() != null) {
			return getVersionedOntology().toString() + " (Client of " + getServerPeer() + ")";
		} else {
			return super.toString();
		}
	}
}

package org.hypergraphdb.app.owl.test.versioning.distributed;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Future;

import mjson.Json;

import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.MessageHandler;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.NetworkPeerPresenceListener;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivityFactory;
import org.hypergraphdb.util.CompletedFuture;

/**
 * This is used to emulate peer to peer communication within the same process purely
 * for testing purposes.
 * 
 * @author Borislav Iordanov
 *
 */
public class InProcessPeerInterface implements PeerInterface
{
	private HyperGraphPeer thisPeer;
    private MessageHandler messageHandler;
    private ArrayList<NetworkPeerPresenceListener> presenceListeners = 
            new ArrayList<NetworkPeerPresenceListener>();

    static HashMap<HGPeerIdentity, InProcessPeerInterface> allpeers = 
    		new HashMap<HGPeerIdentity, InProcessPeerInterface>();
    
	@Override
	public void setMessageHandler(MessageHandler messageHandler)
	{
		this.messageHandler = messageHandler;
	}

	@Override
	public void configure(Json configuration)
	{
	}

	@Override
	public void start()
	{
		synchronized (allpeers)
		{
			if (allpeers.get(thisPeer.getIdentity()) != null)
					throw new RuntimeException("Already started peer " + thisPeer.getIdentity());
			allpeers.put(thisPeer.getIdentity(), this);
			for (InProcessPeerInterface peer : allpeers.values())
				if (peer != this) for (NetworkPeerPresenceListener listener : peer.presenceListeners)
					listener.peerJoined(thisPeer.getIdentity());
		}
	}

    public Principal principal()
    {
    	return new Principal()
    	{
			public String getName()
			{
				return thisPeer.getIdentity().toString();
			}    		
    	};
    }

    
	@Override
	public boolean isConnected()
	{
		return true;
	}

	@Override
	public void stop()
	{
		synchronized (allpeers)
		{
			allpeers.remove(thisPeer.getIdentity());
			for (InProcessPeerInterface peer : allpeers.values())
				if (peer != this) for (NetworkPeerPresenceListener listener : peer.presenceListeners)
					listener.peerLeft(thisPeer.getIdentity());			
		}
	}

	@Override
	public HyperGraphPeer getThisPeer()
	{
		return thisPeer;
	}

	@Override
	public void setThisPeer(HyperGraphPeer thisPeer)
	{
		this.thisPeer = thisPeer;
	}

	@Override
	public PeerRelatedActivityFactory newSendActivityFactory()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void broadcast(Json msg)
	{
		for (InProcessPeerInterface peer : allpeers.values())
			if (peer != this)
				peer.messageHandler.handleMessage(msg);
	}

	@Override
	public Future<Boolean> send(Object networkTarget, Json msg)
	{
		InProcessPeerInterface peer = allpeers.get(networkTarget);
		if (peer == null)
			throw new NullPointerException("Peer for " + networkTarget + " could not be found.");
		if (!msg.has(Messages.REPLY_TO))
			msg.set(Messages.REPLY_TO, Json.make(thisPeer.getIdentity()));
		peer.messageHandler.handleMessage(msg);
		return new CompletedFuture<Boolean>(true); 
	}

	@Override
	public void addPeerPresenceListener(NetworkPeerPresenceListener listener)
	{
		this.presenceListeners.add(listener);
	}

	@Override
	public void removePeerPresenceListener(NetworkPeerPresenceListener listener)
	{
		this.presenceListeners.remove(listener);
	}
}
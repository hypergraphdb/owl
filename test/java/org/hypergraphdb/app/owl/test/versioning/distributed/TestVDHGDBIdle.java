package org.hypergraphdb.app.owl.test.versioning.distributed;

import java.io.File;
import java.util.Date;
import java.util.Set;

import mjson.Json;

import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfig;
import org.jivesoftware.smack.XMPPConnection;

/**
 * TestVDHGDBIdle starts idle at an repository at C:\\temp\\hypergraph-" +
 * PEER_USERNAME and waits for push or pull.
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 12, 2012
 */
public class TestVDHGDBIdle
{

	static
	{
		XMPPConnection.DEBUG_ENABLED = false;
	}

	/**
	 * Delete all Ontologies at repository on startup?
	 */
	public static boolean DELETE_ALL_ONTOLOGIES = false;

	/**
	 * 
	 * @param argv
	 *            call with username [0] password [1].
	 */
	public static void main(String[] argv)
	{
		Json config = Json.read(TestVDHGDBIdle.class.getResource("/testpeer.json"));
		for (int i = 0; i < argv.length; i++)
		{
			if (argv[i].equals("-xmppServer"))
				config.at("interfaceConfig").set("serverUrl", argv[++i]);
			else if (argv[i].equals("-xmppUser"))
				config.at("interfaceConfig").set("user", argv[++i]);
			else if (argv[i].equals("-xmppPass"))
				config.at("interfaceConfig").set("password", argv[++i]);
			else if (argv[i].equals("-dblocation"))
				config.at(PeerConfig.LOCAL_DB, argv[++i]);
		}
		File dir = null;
		if (!config.has(PeerConfig.LOCAL_DB))
		{
			dir = new File(new File(System.getProperty("java.io.tmpdir")), "hgdb.owltest");
			config.set(PeerConfig.LOCAL_DB, dir.getAbsolutePath());
		}
		else
			dir = new File(config.at(PeerConfig.LOCAL_DB).asString());
		dir.mkdirs();
		System.out.println("STARTING IDLE AT: " + dir);
		if (!dir.exists())
			dir.mkdir();
		System.out.println("Repository at : " + dir);
		OntologyDatabasePeer dr = new OntologyDatabasePeer(dir.getAbsolutePath(), 
				  ImplUtils.connectionStringFromConfiguration(config));
		System.out.println("INIT LOCAL IDLE PEER REPOSITORY: " + config.at("interfaceConfig").at("user"));
		initializeVDRepository(dr);
		waitForOnePeer(dr);
		try
		{
			while (true)
			{
				System.out.println("Sleeping 10 mins. At: " + new Date());
				Thread.sleep(10 * 60 * 1000);
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			dr.stopNetworking();
		}
	}

	/**
	 * @param dr
	 */
	private static void initializeVDRepository(OntologyDatabasePeer dr)
	{
		if (DELETE_ALL_ONTOLOGIES && dr.getOntologies().size() > 0)
		{
			dr.deleteAllOntologies();
			GarbageCollector gc = new GarbageCollector(dr);
			gc.runGarbageCollection(GarbageCollector.MODE_DELETED_ONTOLOGIES);
		}
		else
		{
			dr.printAllOntologies();
		}
		dr.printStatistics();
		dr.startNetworking();
	}

	/**
	 * @param dr
	 * 
	 */
	private static void waitForOnePeer(OntologyDatabasePeer dr)
	{
		System.out.println("WAIT FOR PEERS: START");
		Set<HGPeerIdentity> connectedPeers;
		do
		{
			connectedPeers = dr.getPeer().getConnectedPeers();
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		while (connectedPeers.isEmpty());
		System.out.println("WAIT FOR PEERS: DONE, I have : " + dr.getPeer().getConnectedPeers().size());
		printConnectedPeers(dr.getPeer());
	}

	public static void printConnectedPeers(HyperGraphPeer peer)
	{
		for (HGPeerIdentity p : peer.getConnectedPeers())
		{
			System.out.println("ID: " + p.getId() + " Host:" + p.getHostname() + " Graph: " + p.getGraphLocation());
		}
	}

}

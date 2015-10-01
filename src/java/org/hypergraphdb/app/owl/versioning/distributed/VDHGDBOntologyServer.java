package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;


import mjson.Json;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerConfig;
import org.jivesoftware.smack.XMPPConnection;

/**
 * <p>
 * A main class to start an ontology versioning server. The arguments are a
 * filename with the JSON P2P configuration for the HyperGraphDB P2P framework
 * and the directory location of the HyperGraphDB instance holding the
 * ontologies.
 * </p>
 * 
 * <p>
 * For example:<br>
 * <br>
 * java -cp <classpath> VDHGDBOntologyServer VDHGDBOntologyServer.p2p
 * /var/hgdb/ontologydb
 * </p>
 */
public class VDHGDBOntologyServer
{
	static
	{
		XMPPConnection.DEBUG_ENABLED = false;
	}

	public static void main(String[] args)
	{
		if (args.length < 1)
			die("No arguments.");

		File file = new File(args[0]);
		if (!file.exists())
			die("File " + args[0] + " could not be found.");
		OntologyDatabasePeer dr = null;
		try
		{
			Json config = Json.read(file.toURI().toURL());
			for (int i = 1; i < args.length; i++)
			{
				String[] A = args[i].split("=");
				if (A.length != 2)
					die("Invalid argument " + args[i]);
				if ("db".equals(A[0]))
					config.set(PeerConfig.LOCAL_DB, A[1]);
				else if ("name".equals(A[0]))
					config.set(PeerConfig.PEER_NAME, A[1]);
				else
					die("Invalid parameter name " + A[0]);
			}
			String databaseLocation = config.at(PeerConfig.LOCAL_DB).asString();
			System.out.println("Starting ontology server peer at " + 
							config.at(PeerConfig.LOCAL_DB) + " with peer name "
							+ config.at(PeerConfig.PEER_NAME));

			dr = new OntologyDatabasePeer(databaseLocation, 
											  ImplUtils.connectionStringFromConfiguration(config));
//			dr.setOntologyServer(true);
			dr.printAllOntologies();
			dr.printStatistics();
			boolean success = dr.startNetworking();
			if (success)
			{
				System.out.println("Networking started as: ");
				dr.printIdentity();
			}
			else
			{
				System.out.println("Networking failed.: " + dr.getPeer());
			}

			while (true)
			{
				System.out.print("beep\t");
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
			if (dr != null)
				dr.stopNetworking();
		}
		System.out.println("BYE BYE ");
	}

	private static void die(String msg)
	{
		System.out.println(msg);
		System.out.println("Syntax: VDHGDBOntologyServer <configfile> [db=<graph location>] [name=<peer name>] [port=<peer port>.");
		System.out.println("where configfile is a JSON formatted configuration file "
				+ "and the optional 'db', 'name' and 'port' parameters overwrite the "
				+ "'localDB', 'peerName' and 'tcp port' confuration parameters.");
		System.exit(-1);
	}

	public static void printConnectedPeers(HyperGraphPeer peer)
	{
		for (HGPeerIdentity p : peer.getConnectedPeers())
		{
			System.out.println("ID: " + p.getId() + " Host:" + p.getHostname() + " Graph: " + p.getGraphLocation());
		}
	}
}
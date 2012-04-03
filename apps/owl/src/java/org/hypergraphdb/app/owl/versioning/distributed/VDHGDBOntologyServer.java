package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Structs;
import org.jivesoftware.smack.XMPPConnection;

public class VDHGDBOntologyServer
{
	static
	{
		XMPPConnection.DEBUG_ENABLED = false;
	}

	/**
	 * 
	 * @param argv
	 *            call with username [0] password [1].
	 */
	public static void main(String[] args)
	{
		if (args.length < 1)
		    die("No arguments.");
		
		String filename = args[0];
		String db = null;
		String name = null;
		String port = null;
		for (int i = 1; i < args.length; i++)
		{
		    String [] A = args[i].split("=");
		    if (A.length != 2)
		        die("Invalid argument " + args[i]);
		    if ("db".equals(A[0]))
		        db = A[1];
		    else if ("name".equals(A[0]))
		        name = A[1];
		    else if ("port".equals(A[0]))
		        port = A[1];
		    else
		        die("Invalid parameter name " + A[0]);
		}
		Map<String, Object> configuration = HyperGraphPeer.loadConfiguration(new File(filename));
		if (db != null)
		    configuration.put("localDB", db);
		if (name != null)
		    configuration.put("peerName", name);
		if (port != null)
		    Structs.getStruct(configuration, "jxta", "tcp").put("port", Integer.parseInt(port));
		
		File dir = new File(configuration.get("localDB").toString());
		System.out.println("Starting ontology server peer at " + dir + 
				" with peer name " + configuration.get("peerName"));
		
		VDHGDBOntologyRepository dr = null;
		
		try
		{

			if (!dir.exists())
				dir.mkdir();
			VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());

			HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
			dr = (VDHGDBOntologyRepository) manager.getOntologyRepository();
			dr.printAllOntologies();
			dr.printStatistics();
			Map<String, Object> xmppConfig = (Map<String,Object>)configuration.get("interfaceConfig");
			dr.startNetworking(xmppConfig.get("user").toString(), 
							   xmppConfig.get("password").toString(), 
							   xmppConfig.get("serverUrl").toString());
			
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
			if (dr != null) dr.stopNetworking();
		}
		System.out.println("BYE BYE ");
	}

    private static void die(String msg)
    {
        System.out.println(msg);
        System.out.println("Syntax: VDHGDBOntologyServer <configfile> [db=<graph location>] [name=<peer name>] [port=<peer port>.");
        System.out.println("where configfile is a JSON formatted configuration file " +
                           "and the optional 'db', 'name' and 'port' parameters overwrite the " +
                           "'localDB', 'peerName' and 'tcp port' confuration parameters.");
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

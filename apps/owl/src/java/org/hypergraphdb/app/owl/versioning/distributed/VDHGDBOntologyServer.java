package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;

import java.util.Map;

import mjson.Json;

import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Structs;
import org.jivesoftware.smack.XMPPConnection;

/**
 * <p>
 * A main class to start an ontology versioning server. The arguments are 
 * a filename with the JSON P2P configuration for the HyperGraphDB P2P framework
 * and the directory location of the HyperGraphDB instance holding the ontologies.
 * </p>
 * 
 * <p>
 * For example:<br><br>
 * java -cp <classpath> VDHGDBOntologyServer VDHGDBOntologyServer.p2p /var/hgdb/ontologydb
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
		Json configuration = HyperGraphPeer.loadConfiguration(new File(filename));
		if (db != null)
		    configuration.set("localDB", db);
		if (name != null)
		    configuration.set("peerName", name);
//		if (port != null)
//		    Structs.getStruct(configuration, "jxta", "tcp").put("port", Integer.parseInt(port));
		
		File dir = new File(configuration.at("localDB").asString());
		System.out.println("Starting ontology server peer at " + dir + 
				" with peer name " + configuration.at("peerName"));
		
		VDHGDBOntologyRepository dr = null;
		
		try
		{

			if (!dir.exists())
				dir.mkdir();
			VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());

			HGDBOntologyManager manager = HGDBOWLManager.createOWLOntologyManager();
			dr = (VDHGDBOntologyRepository) manager.getOntologyRepository();
			//dr.printAllOntologies();
			dr.printStatistics();
			boolean success = dr.startNetworking(configuration);
			if (success) {
				System.out.println("Networking started as: "); 
				dr.printIdentity();
			} else {
				System.out.println("Networking failed.: " + dr.getPeer());
			}
//			Map<String, Object> xmppConfig = (Map<String,Object>)configuration.get("interfaceConfig");
//			dr.startNetworking(xmppConfig.get("user").toString(), 
//							   xmppConfig.get("password").toString(), 
//							   xmppConfig.get("serverUrl").toString());
			System.out.println("Versioned distributed ontologies are:");
			for (DistributedOntology O : dr.getDistributedOntologies()) {
			    System.out.println(O.getVersionedOntology());
			}
			    
			while (true) {
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
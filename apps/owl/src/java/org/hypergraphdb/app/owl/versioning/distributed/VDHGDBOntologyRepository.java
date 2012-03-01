package org.hypergraphdb.app.owl.versioning.distributed;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.usage.ImportOntologies;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.PushVersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser.VersionedOntologyRoot;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVersionedOntologyRenderer;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLRenderConfiguration;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.PeerPresenceListener;
import org.hypergraphdb.peer.workflow.Activity;
import org.jivesoftware.smack.XMPPConnection;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * VDHGDBOntologyRepository.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 16, 2012
 */
public class VDHGDBOntologyRepository extends VHGDBOntologyRepository {

	static {
	    XMPPConnection.DEBUG_ENABLED = true;
	}

	HyperGraphPeer peer;
	
	public static String PEER_USERNAME = "hg1"; 
	public static String PEER_PASSWORD = "hg1"; 
	
	public static VDHGDBOntologyRepository getInstance() {
		if (!hasInstance()) {
			String hypergraphDBLocation = getHypergraphDBLocation();
			System.out.println("HGDB REPOSITORY AT: " + hypergraphDBLocation);
			setInstance(new VDHGDBOntologyRepository(hypergraphDBLocation));
			((VDHGDBOntologyRepository)getInstance()).initializeActivities();
		}
		HGDBOntologyRepository instance = HGDBOntologyRepository.getInstance(); 
		if (!(instance instanceof VDHGDBOntologyRepository)) throw new IllegalStateException("Instance requested not Versioned Repository type.: " + instance);
		return (VDHGDBOntologyRepository)instance;
	}
	
	private VDHGDBOntologyRepository(String location) {
		super(location);
	}
	
	private void initializeActivities() {
		Map<String, Object> peerConfig = getPeerConfig();
		peer = new HyperGraphPeer(peerConfig, getHyperGraph());
		peer.getActivityManager();
		
		//peer.addPeerPresenceListener(this);
		//Activity a;
		//peer.getNetworkTarget(id)
		peer.addPeerPresenceListener(new PeerPresenceListener() {
			
			@Override
			public void peerLeft(HGPeerIdentity peer) {
				System.out.println("Heeeellooo " + peer);
			}
			
			@Override
			public void peerJoined(HGPeerIdentity peer) {
				System.out.println("Bye bye " + peer);
			}
		});
				

	}
	
	private Map<String,Object> getPeerConfig() {
		Map<String,Object> peerConfig = new HashMap<String, Object>();
		List<Map<?,?>> bootstrapConfig = new ArrayList<Map<?,?>>();
		Map<String,Object> bootstrapConfigSub1 = new HashMap<String, Object>();
		Map<String,Object> bootstrapConfigSub2 = new HashMap<String, Object>();
		Map<String,Object> interfaceConfig = new HashMap<String, Object>();
		peerConfig.put("interfaceType", "org.hypergraphdb.peer.xmpp.XMPPPeerInterface");
		peerConfig.put("peerName", "VDHGDBOntologyRepository");
		peerConfig.put("interfaceConfig", interfaceConfig);
		peerConfig.put("bootstrap", bootstrapConfig);
		bootstrapConfig.add(bootstrapConfigSub1);
		bootstrapConfig.add(bootstrapConfigSub2);
		bootstrapConfigSub1.put("class", "org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap");
		bootstrapConfigSub1.put("config", new HashMap<String, Object>());
		bootstrapConfigSub2.put("class", "org.hypergraphdb.peer.bootstrap.CACTBootstrap");
		bootstrapConfigSub2.put("config", new HashMap<String, Object>());
		//
		interfaceConfig.put("user", PEER_USERNAME);
		interfaceConfig.put("password", PEER_PASSWORD);
		interfaceConfig.put("serverUrl", "W203-003.miamidade.gov");
		//peers from roster! interfaceConfig.put("room", "ontologyCM@conference.127.0.0.1");
		interfaceConfig.put("autoRegister", true);
		interfaceConfig.put("ignoreRoster", false);
		interfaceConfig.put("fileTransferThreashold", 100 * 1024); //default.
		return peerConfig;
	}
	
	public void DBG_ClearDates() {
		List<HGHandle> L = getHyperGraph().findAll(hg.type(Date.class));
		for (HGHandle h : L) {
			getHyperGraph().remove(h);
		}
	}
	
	public boolean startNetworking() {
		DBG_ClearDates();
		//this will block
		Future<Boolean> f = peer.start();
		boolean success = false; 
		try {
			success = f.get(1000, TimeUnit.SECONDS);
			if (!success) {
				Exception  e = peer.getStartupFailedException();
				System.out.println("FAILED TO START PEER: ");
				e.printStackTrace();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		} 
		// Bootstrap Push:
		if (PushVersionedOntology.NotOntologyExists == null) {};
		peer.getActivityManager().registerActivityType(PushVersionedOntology.TYPENAME, PushVersionedOntology.class);

		return success;
	}

	public void stopNetworking() {
		peer.stop();
	}
	public HyperGraphPeer getPeer() {
		return peer;
	}
	Activity a = null;
	public void printPeerInfo() {
		System.out.println("peer.getPeerInterface().isConnected() " + peer.getPeerInterface().isConnected());
		//peer.getPeerInterface().newFilterActivity(evaluator)
		System.out.println("peer.peer.getConnectedPeers().size() " + peer.getConnectedPeers().size());
		//if (a == null) {
		//	a = new AffirmIdentity(peer, UUID.randomUUID());
		//	peer.getActivityManager().initiateActivity(a);
		//	peer.getActivityManager().start();
		//}
		//peer.updateNetworkProperties();
		//System.out.println("Done yet?: " + a.getFuture().isDone());
		// Not working, always empty:
		Set<HGPeerIdentity> others = peer.getConnectedPeers();
		for (HGPeerIdentity pi : others) {
			System.out.println(" " + pi.getHostname());
			System.out.println(" " + pi.getIpAddress());
			System.out.println(" " + pi.getGraphLocation());
			System.out.println("NetworkTarget: " + peer.getNetworkTarget(pi));
		}
		System.out.println(peer.getIdentity());
		//peer
	}
	
	public Future<RemoteRepositoryActionResult> fetch(VersionedOntology vo, HGPeerIdentity remote) {
		return null;
	}

	public Future<RemoteRepositoryActionResult> pull(VersionedOntology vo, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: no pull
		// 3) Can pull == local master head revision is older and equal to one revision in remote 
		//	  branches history 
		// 4) 
		// 
		return null;
		
	}
	
	public Future<RemoteRepositoryActionResult> push(VersionedOntology vo, HGPeerIdentity remote) {
		// 1) Target available
		// 2) Target has vo? No: Push everything vo + all changesets using pushNew
		// 3) Can push == target master head revision is older and equal to one revision in my 
		//	  branches history 
		// 4) find last Changeset that is not in target.
		// 5) push each changeset/Revision pair after that. This is the Delta.
		// 6) remote will receive one changeset/Revision, then apply those changes within one transaction
		//  
		//
		if (peer.getIdentity().getGraphLocation().contains("hg1")
				&& ! peer.getConnectedPeers().isEmpty()) {
					HGPeerIdentity target = peer.getConnectedPeers().iterator().next();
//					//HGHandle atom = getHyperGraph().add(new Date());
//					Activity activity = new DefineAtom(peer, atom, target);
//					peer.getActivityManager().initiateActivity(activity);
					VersionedOntology voN = getVersionControlledOntologies().get(0);
					System.out.println("Sending versioned onto: " + voN.getWorkingSetData().getOntologyID());
					//HGHandle atom = getHyperGraph().add(new Date());
					Activity activity = new PushVersionedOntology(peer, voN , target);
					peer.getActivityManager().initiateActivity(activity);
					
			//System.out.println("SENT Dates: " + getHyperGraph().count(hg.type(Date.class)));
		} else { 
			//System.out.println("RECEIVED Dates: " + getHyperGraph().count(hg.type(Date.class)));
		}
		return null;
	}
	
	
	public enum RemoteRepositoryActionResult {SUCCESS, DENIED_LOCAL_OUT_OF_DATE, DENIED_WERE_EQUAL, DENIED_REMOTE_OUT_OF_DATE}; 
	
	public static void main(String[] argv) {
		VDHGDBOntologyRepository.PEER_USERNAME = argv[0];
		VDHGDBOntologyRepository.PEER_PASSWORD = argv[1];
		File dir = new File ("C:\\temp\\hypergraph-" + VDHGDBOntologyRepository.PEER_USERNAME);
		if (!dir.exists()) dir.mkdir();
		VDHGDBOntologyRepository.setHypergraphDBLocation(dir.getAbsolutePath());
		VDHGDBOntologyRepository dr = VDHGDBOntologyRepository.getInstance();
		if (argv[0].contains("1")) {
			ensureOneVersionedOntology(dr);
		}
//		dr.startNetworking();
//		dr.printPeerInfo();
//		try {
//			while (true) {
//				Thread.sleep((long) 10000);
//				dr.printPeerInfo();
//				dr.push(null, null);
//			}
//			
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			dr.stopNetworking();
//		}
	}

	/**
	 * @param dr
	 */
	private static void ensureOneVersionedOntology(VDHGDBOntologyRepository dr) {
		List <File> renderedFiles = new ArrayList<File>();
		HGDBOntologyManagerImpl manager = HGDBOWLManager.createOWLOntologyManager();
		//
		// IMPORT AND RENDER
		//
		try {
			VDHGDBOntologyRepository repo = (VDHGDBOntologyRepository)manager.getOntologyRepository();
			//repo.dropHypergraph();
			repo.deleteAllOntologies();
			//System.out.println("Running GC");
			//CANNOT RUN GC nullHANDLE problem !!! repo.runGarbageCollector();
			File f = new File("C:\\_CiRM\\testontos\\County.owl");
			IRI targetIRI = ImportOntologies.importOntology(f, manager);
			//File f2 = new File("C:\\_CiRM\\testontos\\1 csr.owl");
			//IRI targetIRI = ImportOntologies.importOntology(f2, manager);
			HGDBOntology o = (HGDBOntology)manager.loadOntologyFromOntologyDocument(targetIRI);
			VersionedOntology vo = repo.addVersionControl(o, "distributedTestUser");
			// MANIPULATE REMOVE CHANGED
			Object[] axioms = o.getAxioms().toArray();
			//remove all axioms 10.
			for (int i = 0; i < axioms.length / 10; i ++) {
				int j = i;
				for (;j < i + axioms.length / 100; j++) {
					if (j < axioms.length) {
						manager.applyChange(new RemoveAxiom(o, (OWLAxiom)axioms[j]));
					}
				}
				i = j;
				vo.commit("SameUser", " commit no " + i);
			}
			// RENDER VERSIONED ONTOLOGY, includes data
			for (int i = 0; i < vo.getArity(); i ++) {
				VOWLRenderConfiguration c = new VOWLRenderConfiguration();
				c.setLastRevisionIndex(i);
				VOWLXMLVersionedOntologyRenderer r = new VOWLXMLVersionedOntologyRenderer(manager);
				File fx = new File("C:\\_CiRM\\testontos\\CountyVersioned-Rev-"+ i + ".vowlxml");
				renderedFiles.add(fx);
				//File fx = new File("C:\\_CiRM\\testontos\\1 csr-Rev-"+ i + ".vowlxml");
				FileWriter fwriter = new FileWriter(fx);
				//	Full export
				r.render(vo, fwriter, c);
			}
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLRendererException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//
		// PARSE
		//
		for (File f : renderedFiles) {
			System.out.println("Parsing: " + f + " length: " + (f.length() / 1024) + " kB");
			OWLOntologyDocumentSource source = new FileDocumentSource(f);
			VOWLXMLParser parser = new VOWLXMLParser();
			OWLOntology onto = new OWLOntologyImpl(manager, new OWLOntologyID());
			// must have onto for manager in super class
			VersionedOntologyRoot versionedOntologyRoot = new VersionedOntologyRoot(null, onto);
			try {
				parser.parse(source, versionedOntologyRoot, new OWLOntologyLoaderConfiguration());
			} catch (OWLOntologyChangeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnloadableImportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OWLParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

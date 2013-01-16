package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import mjson.Json;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.distributed.ClientCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.ServerCentralizedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.VDRenderer;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Messages;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * BrowserVersionedOntologyActivity gets a list of ontologyIDs and UUIDs or ontologies from a peer.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 23, 2012
 */
public class BrowseRepositoryActivity extends FSMActivity {

    public static boolean DBG = true;   
    public static final String TYPENAME = "browse-Repository";

    private HGPeerIdentity targetPeerID;
    private VDHGDBOntologyRepository repository;
    private HyperGraph graph;
    private List<BrowseEntry> repositoryBrowseEntries;

	public BrowseRepositoryActivity(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
        if(!thisPeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) thisPeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
        graph = repository.getHyperGraph();
    }

	/**
	 * @param thisPeer
	 */
	public BrowseRepositoryActivity(HyperGraphPeer sourcePeer, HGPeerIdentity targetPeerID) {
		super(sourcePeer);
		this.targetPeerID = targetPeerID;
        if(!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
        graph = repository.getHyperGraph();
	}	

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.Activity#getType()
	 */
	@Override
	public String getType() {
		return TYPENAME;
	}	
	
    /* (non-Javadoc) // SOURCE
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate() {
        Json msg = createMessage(Performative.QueryIf, this);
        send(targetPeerID, msg);
	}
	
	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //TARGET
    @OnMessage(performative="QueryIf")
    public WorkflowStateConstant targetQueryOntologyIds(final Json msg) throws Throwable {
		Json reply = getReply(msg, Performative.Inform);
		List<BrowseEntry> ontologyIDsAndUUIDs = graph.getTransactionManager().ensureTransaction(new Callable<List<BrowseEntry>>() {
			public List<BrowseEntry> call() {
				// TRANSACTION START
				List<BrowseEntry> ontologyIDsAndUUIDs = new ArrayList<BrowseEntry>();
				Set<DistributedOntology> ontologies = repository.getDistributedOntologies();
				for (DistributedOntology o : ontologies) {
					BrowseEntry entry = new BrowseEntry(o);
					ontologyIDsAndUUIDs.add(entry);
				}
				// TRANSACTION END
				return ontologyIDsAndUUIDs;
			}});
		reply.set(CONTENT, ontologyIDsAndUUIDs);
		send(getSender(msg), reply);
		return WorkflowStateConstant.Completed;
	}

	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //TARGET
    @OnMessage(performative="Inform")
    public WorkflowStateConstant sourceReceiveOntologyIds(final Json msg) throws Throwable {
		repositoryBrowseEntries = new ArrayList<BrowseEntry>(); 
		for (Json x : msg.at(CONTENT).asJsonList())
		    repositoryBrowseEntries.add((BrowseEntry)Messages.fromJson(x));
		return WorkflowStateConstant.Completed;
	}

	/**
	 * To be called after action has completed.
	 * @return the repositoryBrowseEntries
	 */
	public List<BrowseEntry> getRepositoryBrowseEntries() {
		return repositoryBrowseEntries;
	}
	
	public static class BrowseEntry implements Serializable {

		private static final long serialVersionUID = 240951418825364623L;
		
		public static final String DIST_CLIENT = "Client";
		public static final String DIST_PEER = "Peer";
		public static final String DIST_SERVER = "Server";
		
		
		private String owlOntologyIRI;
		private String owlOntologyVersionIRI;
		private String owlOntologyDocumentIRI;
		private HGPersistentHandle uuid;
		private String distributionMode;
		private String lastRevision;
		
		public BrowseEntry() {
		}
		
		public BrowseEntry(DistributedOntology dOnto) {
			if (dOnto instanceof ClientCentralizedOntology) {
				distributionMode = DIST_CLIENT;
			} else if (dOnto instanceof ServerCentralizedOntology) {
				distributionMode = DIST_SERVER;
			} else {
				distributionMode = DIST_PEER;
			}
			OWLOntologyID oId = dOnto.getWorkingSetData().getOntologyID();
			this.owlOntologyIRI = "" + oId.getOntologyIRI();
			this.owlOntologyVersionIRI = oId.getVersionIRI() == null ? null : oId.getVersionIRI().toString();
			this.owlOntologyDocumentIRI = dOnto.getWorkingSetData().getDocumentIRI().toString();
			this.uuid = dOnto.getWorkingSetData().getAtomHandle().getPersistent();
			this.lastRevision = VDRenderer.render(dOnto.getVersionedOntology().getHeadRevision());
		}

		/**
		 * @return the owlOntologyIRI
		 */
		public String getOwlOntologyIRI() {
			return owlOntologyIRI;
		}

		/**
		 * @param owlOntologyIRI the owlOntologyIRI to set
		 */
		public void setOwlOntologyIRI(String owlOntologyIRI) {
			this.owlOntologyIRI = owlOntologyIRI;
		}

		/**
		 * @return the owlOntologyVersionIRI
		 */
		public String getOwlOntologyVersionIRI() {
			return owlOntologyVersionIRI;
		}

		/**
		 * @param owlOntologyVersionIRI the owlOntologyVersionIRI to set
		 */
		public void setOwlOntologyVersionIRI(String owlOntologyVersionIRI) {
			this.owlOntologyVersionIRI = owlOntologyVersionIRI;
		}

		/**
		 * @return the owlOntologyDocumentIRI
		 */
		public String getOwlOntologyDocumentIRI() {
			return owlOntologyDocumentIRI;
		}

		/**
		 * @param owlOntologyDocumentIRI the owlOntologyDocumentIRI to set
		 */
		public void setOwlOntologyDocumentIRI(String owlOntologyDocumentIRI) {
			this.owlOntologyDocumentIRI = owlOntologyDocumentIRI;
		}

		/**
		 * @return the uuid
		 */
		public HGPersistentHandle getUuid() {
			return uuid;
		}

		/**
		 * @param uuid the uuid to set
		 */
		public void setUuid(HGPersistentHandle uuid) {
			this.uuid = uuid;
		}

		/**
		 * @return the distributionMode
		 */
		public String getDistributionMode() {
			return distributionMode;
		}

		/**
		 * @param distributionMode the distributionMode to set
		 */
		public void setDistributionMode(String distributionMode) {
			this.distributionMode = distributionMode;
		}

		/**
		 * @return the lastRevision
		 */
		public String getLastRevision() {
			return lastRevision;
		}

		/**
		 * @param lastRevision the lastRevision to set
		 */
		public void setLastRevision(String lastRevision) {
			this.lastRevision = lastRevision;
		}

		public String toString() {
			return "" + getOwlOntologyIRI() + " [" + getUuid() + "] " + getDistributionMode();
		}
	}
}
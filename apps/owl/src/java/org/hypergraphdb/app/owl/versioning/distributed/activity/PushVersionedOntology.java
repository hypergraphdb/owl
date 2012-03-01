package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.getPart;
import static org.hypergraphdb.peer.Structs.struct;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.UUID;

import org.coode.owlapi.owlxml.renderer.OWLXMLRenderer;
import org.coode.owlapi.owlxmlparser.OWLXMLParser;
import org.coode.owlapi.owlxmlparser.OWLXMLParserFactory;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.owl.HGDBApplication;
import org.hypergraphdb.app.owl.HGDBOWLManager;
import org.hypergraphdb.app.owl.HGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVersionedOntologyRenderer;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLRenderConfiguration;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.PossibleOutcome;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * PushVersionedOntology. Pushes all changes to a target repository, which has the same VersionedOntology and 
 * a change history that is shorter (older) than the source. 
 * 
 * Logical outcomes:
 * - FAILED_VERSIONED_ONTOLOGY_DOES_NOT_EXIST
 * - FAILED_No Update Necessary
 * - FAILED_Target_Newer
 * - CONFLICT (Target history is different than ours)
 * -  
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 17, 2012
 */
public class PushVersionedOntology extends FSMActivity {

    public static final String TYPENAME = "push-VersionedOntology";

    public static boolean DBG = true;

    static {
    	new PushVersionedOntology(null, null, null);
    }
    
    public static final WorkflowStateConstant OntologyExists = WorkflowStateConstant.makeStateConstant("OntologyExists");
    public static final WorkflowStateConstant NotOntologyExists = WorkflowStateConstant.makeStateConstant("NotOntologyExists");
    public static final WorkflowStateConstant Validating = WorkflowStateConstant.makeStateConstant("Validating");
    public static final WorkflowStateConstant Validated = WorkflowStateConstant.makeStateConstant("Validated");
    public static final WorkflowStateConstant NotValidated = WorkflowStateConstant.makeStateConstant("NotValidated");
    public static final WorkflowStateConstant Syncing = WorkflowStateConstant.makeStateConstant("Syncing");
    public static final WorkflowStateConstant Synced = WorkflowStateConstant.makeStateConstant("Synced");
    public static final WorkflowStateConstant NotSynced = WorkflowStateConstant.makeStateConstant("NotSynced");
    public static final WorkflowStateConstant SendingInitial = WorkflowStateConstant.makeStateConstant("SendingInitial");
    public static final WorkflowStateConstant SentInitial = WorkflowStateConstant.makeStateConstant("SentInitial");
    public static final WorkflowStateConstant ReceivingInitial = WorkflowStateConstant.makeStateConstant("ReceivingInitial");
    public static final WorkflowStateConstant ReceivedIntial = WorkflowStateConstant.makeStateConstant("ReceivedIntial");
    public static final WorkflowStateConstant NotTransmitted = WorkflowStateConstant.makeStateConstant("NotTransmitted");
    
    private VersionedOntology sourceVersionedOnto;
    private VersionedOntology targetVersionedOnto;
    private HGPeerIdentity targetPeerID;
    
    public PushVersionedOntology(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
    }

	/**
	 * @param thisPeer
	 */
	public PushVersionedOntology(HyperGraphPeer sourcePeer, VersionedOntology sourceVersionedOnto, HGPeerIdentity targetPeerID) {
		super(sourcePeer);
		this.sourceVersionedOnto = sourceVersionedOnto;
		this.targetPeerID = targetPeerID;
	}
	
	
    /* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate() {
        Message msg = createMessage(Performative.QueryIf, this);
        combine(msg, struct(CONTENT, sourceVersionedOnto.getHeadRevision().getOntologyID())); 
        send(targetPeerID, msg);
        if (DBG) {
        	getThisPeer().getGraph().getLogger().trace("Query if target push : " + sourceVersionedOnto.getHeadRevision());
        }
        
	}

	@FromState("Started") //TARGET
    @OnMessage(performative="QueryIf")
    @PossibleOutcome({"Validating", "NotOntologyExists" }) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant existsTargetVersionedOntology(Message msg) throws Throwable {
		HyperGraph graph = this.getThisPeer().getGraph();
		VDHGDBOntologyRepository repo = VDHGDBOntologyRepository.getInstance();
		// msg parsing
		HGPersistentHandle headRevisionOntologyID = getPart(msg, CONTENT);
		// Look up in repository
		OWLOntology o = graph.get(headRevisionOntologyID);
		if (o != null) {
			targetVersionedOnto = repo.getVersionControlledOntology(o); 
			if (targetVersionedOnto != null) {
				// send Confirm
		        Message reply = getReply(msg, Performative.Confirm);
		        send(getSender(msg), reply);
				return OntologyExists;
			}
		}
		// o null or targetVersionedOnto null
		// send Confirm
        Message reply = getReply(msg, Performative.Disconfirm);
        send(getSender(msg), reply);
		return NotOntologyExists;
    }
	
	@FromState("Started") //SOURCE
    @OnMessage(performative="Confirm")
    @PossibleOutcome({"Validating"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant existsConfirmedNowValidate(Message msg) throws Throwable {
		// ignore confirm msg.
		// initiate validation
		msg = createMessage(Performative.QueryIf, this);
		// send full revision Array
        combine(msg, struct(CONTENT, sourceVersionedOnto.getRevisions())); 
        send(targetPeerID, msg);
		return Validating;
	}

	@FromState("Started") //SOURCE
    @OnMessage(performative="Disconfirm")
    @PossibleOutcome({"SendingInitial", "Completed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant existsDisconfirmedNowSendOntology(Message msg) throws Throwable {
		// Non Prefix Format
		//OWLXMLOntologyFormat renderFormat = new OWLXMLOntologyFormat();
		HyperGraph graph = this.getThisPeer().getGraph();
		VDHGDBOntologyRepository repo = VDHGDBOntologyRepository.getInstance();
		//sourceVersionedOnto
		int headIndex = sourceVersionedOnto.getArity();
		VOWLRenderConfiguration conf = new VOWLRenderConfiguration(headIndex);
		conf.setLastRevisionData(true);
		conf.setUncommittedChanges(false);
		StringWriter stringWriter = new StringWriter(5 * 1024 * 1024);
		//Would need OWLOntologyManager for Format, but null ok here.
		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(sourceVersionedOnto.getWorkingSetData().getOWLOntologyManager());
		owlxmlRenderer.render(sourceVersionedOnto, stringWriter, conf);
		// PROPOSE
		msg = createMessage(Performative.Propose, this);
		// send full head revision data, not versioned yet.
		String owlxmlStringOntology = stringWriter.toString();
        combine(msg, struct(CONTENT, owlxmlStringOntology)); 
        send(targetPeerID, msg);
		return WorkflowStateConstant.Completed;
	}
	
	@FromState("NotOntologyExists") //TARGET
    @OnMessage(performative="Propose")
    @PossibleOutcome({"ReceivingInitial", "Completed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant existsDisconfirmedNowReceiveOntology(Message msg) throws Throwable {
		String owlxmlStringOntology = getPart(msg, CONTENT);
		//StringReader stringReader = new StringReader(owlxmlStringOntology);
		OWLOntologyDocumentSource ds = new StringDocumentSource(owlxmlStringOntology);
		VDHGDBOntologyRepository repo = VDHGDBOntologyRepository.getInstance();
		
		OWLXMLParser owlxmlParser = new OWLXMLParser();
		// Using in memory manager for now
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//This would refer to the one and only local repo and datafactory
		//HGDBOWLManager.createOWLOntologyManager();
		OWLOntology parsedOnto = manager.createOntology();
		//The newly created ontology will hold the manager and the parser will use the managers
		//data factory.
		owlxmlParser.parse(ds, parsedOnto);
		
//		// Non Prefix Format
//		HyperGraph graph = this.getThisPeer().getGraph();
//		VDHGDBOntologyRepository repo = VDHGDBOntologyRepository.getInstance();
//		OWLOntology ontoSend = sourceVersionedOnto.getHeadRevisionData();		
//		StringWriter stringWriter = new StringWriter(5 * 1024 * 1024);
//		//Would need OWLOntologyManager for Format, but null ok here.
//		OWLXMLRenderer owlxmlRenderer = new OWLXMLRenderer(null);
//		owlxmlRenderer.render(ontoSend, stringWriter, renderFormat);
//		// PROPOSE
//		msg = createMessage(Performative.Propose, this);
//		// send full head revision data, not versioned yet.
//		String owlxmlStringOntology = stringWriter.toString();
//        combine(msg, struct(CONTENT, owlxmlStringOntology)); 
//        send(targetPeerID, msg);
		return WorkflowStateConstant.Completed;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.Activity#getType()
	 */
	@Override
	public String getType() {
		return TYPENAME;
	}
	
	
	
	
	
	
//	@FromState("Sending") //SOURCE
//    @OnMessage(performative="AcceptProposal")
//    @PossibleOutcome({"Transmitted"}) 
//    public WorkflowStateConstant transmittingDone(Message msg) throws Throwable {
//		//
//		msg = createMessage(Performative.QueryIf, this);
//		// send full revision Array
//        combine(msg, struct(CONTENT, sourceVersionedOnto.getRevisions())); 
//        send(targetPeerID, msg);
//		return NotOntologyExists;
//	}
//	
	

	
//	@FromState("Validating") //SOURCE
//    @OnMessage(performative="Confirm")
//    @PossibleOutcome({"OntologyExists"}) 
//    //@AtActivity(CONTENT);
//    public WorkflowStateConstant validsatenowe() throws Throwable {
//		return OntologyExists;
//	}
//	
//	@FromState({"Validating"})
//    @OnMessage(performative="QueryIf")
//    @PossibleOutcome({"Validated", "NotValidated"}) 
//    //@AtActivity(CONTENT);
//    public WorkflowStateConstant notExistsTargetChangeSetAfterSourceHead(Message msg) throws Throwable {
//		if (true == new Boolean("true")) { 
//			return Validated;
//		} else {
//			return NotValidated;
//		}
//    }

	
}

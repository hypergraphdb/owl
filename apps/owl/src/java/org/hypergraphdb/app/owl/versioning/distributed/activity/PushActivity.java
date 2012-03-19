package org.hypergraphdb.app.owl.versioning.distributed.activity;

import static org.hypergraphdb.peer.Messages.CONTENT;
import static org.hypergraphdb.peer.Messages.getReply;
import static org.hypergraphdb.peer.Messages.getSender;
import static org.hypergraphdb.peer.Structs.combine;
import static org.hypergraphdb.peer.Structs.getPart;
import static org.hypergraphdb.peer.Structs.struct;
import static org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository.OBJECTCONTEXT_REPOSITORY;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVersionedOntologyRenderer;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.hypergraphdb.peer.HGPeerIdentity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.Message;
import org.hypergraphdb.peer.Performative;
import org.hypergraphdb.peer.workflow.FSMActivity;
import org.hypergraphdb.peer.workflow.FromState;
import org.hypergraphdb.peer.workflow.OnMessage;
import org.hypergraphdb.peer.workflow.PossibleOutcome;
import org.hypergraphdb.peer.workflow.WorkflowStateConstant;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * PushActivity. Pushes all changes to a target repository, which has the same VersionedOntology and 
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
public class PushActivity extends FSMActivity {

    public static final String TYPENAME = "push-VersionedOntology";

    //public static final String ERROR_OBJECT_KEY = "ErrorObject";

    public static final String KEY_LAST_MATCHING_REVISION = "LastMatchingRevision";

    public static final int RENDER_BUFFER_INITIAL_SIZE = 5 * 1024 * 1024; //characters

    public static boolean DBG = true;

    public static boolean DBG_RENDER_ONTOLOGIES_TO_FILE = true;
    
    public static final WorkflowStateConstant SendingInitial = WorkflowStateConstant.makeStateConstant("SendingInitial");
    public static final WorkflowStateConstant ReceivingInitial = WorkflowStateConstant.makeStateConstant("ReceivingInitial");
    public static final WorkflowStateConstant SendingDelta = WorkflowStateConstant.makeStateConstant("SendingDelta");
    public static final WorkflowStateConstant ReceivingDelta= WorkflowStateConstant.makeStateConstant("ReceivingDelta");
    
    private VDHGDBOntologyRepository repository;
    private VersionedOntology sourceVersionedOnto;
    //private VersionedOntology targetVersionedOnto;
    private HGPeerIdentity targetPeerID;
    private String completedMessage;
    
	public PushActivity(HyperGraphPeer thisPeer, UUID id)
    {
        super(thisPeer, id);
        if(!thisPeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) thisPeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
    }

	/**
	 * @param thisPeer
	 */
	public PushActivity(HyperGraphPeer sourcePeer, VersionedOntology sourceVersionedOnto, HGPeerIdentity targetPeerID) {
		super(sourcePeer);
		this.sourceVersionedOnto = sourceVersionedOnto;
		this.targetPeerID = targetPeerID;
        if(!sourcePeer.getObjectContext().containsKey(OBJECTCONTEXT_REPOSITORY)) {
        	System.err.println("PROBLEM DETECTED: NO OBJECTCONTEXT REPO");
        	throw new IllegalArgumentException("Peer's object context must contain OBJECTCONTEXT_REPOSITORY.");
        }
        repository = (VDHGDBOntologyRepository) sourcePeer.getObjectContext().get(OBJECTCONTEXT_REPOSITORY);
	}	
	
	/**
	 * @param completedMessage the completedMessage to set
	 */
	protected void setCompletedMessage(String completedMessage) {
		this.completedMessage = completedMessage;
		if (DBG) System.out.println("Push Completed: " + completedMessage);
	}

	/**
	 * @return the completedMessage
	 */
	public String getCompletedMessage() {
		return completedMessage;
	}
	
    /* (non-Javadoc) // SOURCE
	 * @see org.hypergraphdb.peer.workflow.FSMActivity#initiate()
	 */
	@Override
	public void initiate() {
        Message msg = createMessage(Performative.QueryIf, this);
        combine(msg, struct(CONTENT, sourceVersionedOnto.getHeadRevision().getOntologyUUID())); 
        send(targetPeerID, msg);
        if (DBG) {
        	getThisPeer().getGraph().getLogger().trace("Query if target push : " + sourceVersionedOnto.getHeadRevision());
        }
	}

	/**
	 * 
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("Started") //TARGET
    @OnMessage(performative="QueryIf")
    @PossibleOutcome({"ReceivingDelta", "ReceivingInitial"})
    //@AtActivity(CONTENT);
    public WorkflowStateConstant targetExistsVersionedOntology(Message msg) throws Throwable {
		HyperGraph graph = this.getThisPeer().getGraph();
		VDHGDBOntologyRepository repo = VDHGDBOntologyRepository.getInstance();
		// msg parsing
		HGPersistentHandle headRevisionOntologyID = getPart(msg, CONTENT);
		// Look up in repository
		OWLOntology o = graph.get(headRevisionOntologyID);
		if (o != null) {
			VersionedOntology targetVersionedOnto = repo.getVersionControlledOntology(o); 
			if (targetVersionedOnto != null) {
				// send Confirm with existing revisions objects
				// and tell if we have uncommitted changes.
				// TODO send content hash
		        if (targetVersionedOnto.getWorkingSetChanges().isEmpty()) {
		        	Message reply = getReply(msg, Performative.Confirm);
		        	List<Revision> revList = targetVersionedOnto.getRevisions();
			        combine(reply, struct(CONTENT, revList));
			        send(getSender(msg), reply);
					return ReceivingDelta;
		        } else {
		        	//Target has uncommitted - cannot push
		        	throw new VOWLSourceTargetConflictException("Target has uncommitted changes. Cannot push.");
		        }
			}
		}
		// o null or targetVersionedOnto null
		// send Confirm
        Message reply = getReply(msg, Performative.Disconfirm);
        send(getSender(msg), reply);
		return ReceivingInitial;
    }
	
	//------------------------------------------------------------------------------------
	// SENDING / RECEIVING FULL VERSIONED ONTOLOGY
	//

	@FromState("Started") //SOURCE
    @OnMessage(performative="Disconfirm")
    @PossibleOutcome({"SendingInitial"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant sourceExistsDisconfirmedNowSendOntology(Message msg) throws Throwable {
		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration();
		conf.setLastRevisionData(true);
		conf.setUncommittedChanges(false);
		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_INITIAL_SIZE);
		//Would need OWLOntologyManager for Format, but null ok here.
		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(sourceVersionedOnto.getWorkingSetData().getOWLOntologyManager());
		owlxmlRenderer.render(sourceVersionedOnto, stringWriter, conf);
		// PROPOSE
		msg = createMessage(Performative.Propose, this);
		// send full head revision data, not versioned yet.
		String owlxmlStringOntology = stringWriter.toString();
        combine(msg, struct(CONTENT, owlxmlStringOntology)); 
        send(targetPeerID, msg);
        if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
        	repository.renderFullVersionedontologyToVOWLXML(sourceVersionedOnto, new File("C:\\temp\\sent"));
        }
		return SendingInitial;
	}
	
	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("ReceivingInitial") //TARGET
    @OnMessage(performative="Propose")
    //@PossibleOutcome({"Completed", "Failed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant targetExistsDisconfirmedNowReceiveOntology(Message msg) throws Throwable {
//		boolean failed = false;
//		Object failedActivityObject = null;
		String vowlxmlStringOntology = getPart(msg, CONTENT);
		//
		VersionedOntology voParsed = storeVersionedOntology(new StringDocumentSource(vowlxmlStringOntology), repository.getOntologyManager());
		//TODO HANDLE EXCEPTION, GC created objects			
		// Neither Ontology, nor VersionedOntology was stored, 
		// but ontology axioms & entities, revisions and changeset, changes, axioms were.
		// Run a GC, which also collects dangling changesets, changes and revisions?
		if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
			repository.printAllOntologies();
			repository.renderFullVersionedontologyToVOWLXML(voParsed, new File("C:\\temp\\received"));
		}
		//RESPOND
        Message reply = getReply(msg, Performative.AcceptProposal);
        send(getSender(msg), reply);
        setCompletedMessage("Full versioned ontology received. Size: " + (vowlxmlStringOntology.length()/1024) + " kilo characters");
		return WorkflowStateConstant.Completed;
	}

	/**
	 * Parses a complete versioned ontology (revisions, change sets, head revision data) from a VOWLXML string and stores it as new ontology with versioning in the repository.
	 * 
	 * @param vowlXMLString
	 * @param manager
	 * @throws OWLOntologyChangeException
	 * @throws UnloadableImportException
	 * @throws OWLParserException
	 * @throws IOException
	 * @throws HGDBOntologyAlreadyExistsByDocumentIRIException
	 * @throws HGDBOntologyAlreadyExistsByOntologyIDException
	 * @throws HGDBOntologyAlreadyExistsByOntologyUUIDException
	 */
	private VersionedOntology storeVersionedOntology(OWLOntologyDocumentSource vowlDocumentSource, HGDBOntologyManager manager) throws OWLOntologyChangeException, 
																													UnloadableImportException, 
																													OWLParserException, 
																													IOException, 
																													HGDBOntologyAlreadyExistsByDocumentIRIException, 
																													HGDBOntologyAlreadyExistsByOntologyIDException, 
																													HGDBOntologyAlreadyExistsByOntologyUUIDException {
		//OWLOntologyDocumentSource ds = new StringDocumentSource(vowlXMLString);
		VOWLXMLParser vowlxmlParser = new VOWLXMLParser();
		//Create an partial in mem onto with a hgdb manager and hgdb data factory to use.
		OWLOntology partialInMemOnto = new OWLOntologyImpl(manager, new OWLOntologyID());
		VOWLXMLDocument vowlxmlDoc = new VOWLXMLDocument(partialInMemOnto);
		//The newly created ontology will hold the manager and the parser will use the manager's
		//data factory.
		vowlxmlParser.parse(vowlDocumentSource, vowlxmlDoc, new OWLOntologyLoaderConfiguration());
		if (!vowlxmlDoc.isCompleteVersionedOntology()) {
			throw new OWLParserException("The transmitted ontology was not complete.");
		}
		OWLOntologyID ontologyID = vowlxmlDoc.getRevisionData().getOntologyID();
		IRI documentIRI = IRI.create("hgdb://" + ontologyID.getDefaultDocumentIRI().toString().substring(7));
		HGPersistentHandle ontologyUUID = vowlxmlDoc.getVersionedOntologyID();
		System.out.println("Storing ontology data for : " + ontologyUUID + " using docIRI: " + documentIRI);
		HGDBOntology o = manager.getOntologyRepository().createOWLOntology(ontologyID, documentIRI, ontologyUUID);
		o.setOWLOntologyManager(manager);
		storeFromTo(vowlxmlDoc.getRevisionData(), o);
		HyperGraph graph = repository.getHyperGraph();
		//Add version control with full matching history.
		System.out.println("Creating and adding version control information for : " + ontologyUUID);
		VersionedOntology voParsed = new VersionedOntology(vowlxmlDoc.getRevisions(), vowlxmlDoc.getChangesets(), graph);
		//TODO VALIDATE EVERYTHING HERE, even though we have a lot of validation by getting here.
		graph.add(voParsed);
		return voParsed;
	}
	
	/**
	 * Exits activity.
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("SendingInitial") //SOURCE
    @OnMessage(performative="AcceptProposal")
    @PossibleOutcome({"Completed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant sourceFullVersionedOntologyReceived(Message msg) throws Throwable {
        setCompletedMessage("Target reported: accepted full versioned ontology. All changes were applied.");
		return WorkflowStateConstant.Completed;
	}

	
	//------------------------------------------------------------------------------------
	// TARGET HAS ONTOLOGY -> VALIDATING AND PUSHING MISSING CHANGES
	// OR CANCEL -> has 
	//
	
	/**
	 * Receive a list of revisions from target and determine,
	 * A) if push is possible -> send Cancel
	 * B) which changesets to send -> send Rendered Missing Changesets
	 */
	@FromState("Started") //SOURCE
    @OnMessage(performative="Confirm")
    @PossibleOutcome({"SendingDelta"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant sourceExistsConfirmedNowSendDelta(Message msg) throws Throwable {
		Message reply; 
		WorkflowStateConstant nextState;
		List<Revision> targetRevisions = getPart(msg, CONTENT);
		List<Revision> sourceRevisions = sourceVersionedOnto.getRevisions();
		ListIterator<Revision> sourceIt = sourceRevisions.listIterator();
		ListIterator<Revision> targetIt = targetRevisions.listIterator(); 
		int sourceIndex = 0; 
		boolean sharedAreEqual = true;
		while (sharedAreEqual && sourceIt.hasNext() && targetIt.hasNext()){
			Revision targetR = targetIt.next();			
			Revision sourceR = sourceIt.next();
			//TODO we'll need content dependent comparison here in the future (SHA1?)
			if (!targetR.equals(sourceR)) {
				sharedAreEqual = false; 
			} else {
				sourceIndex ++;
			}
		}
		//Move back one to include last matching revision == first necessary source changeset
		sourceIndex = sourceIndex - 1;
		
		//Push iff:
		// A) sourceIndex >= 0 () is base and must be at target already.
		if (sharedAreEqual && sourceIndex >= 0) {
				if (!targetIt.hasNext()   // we have found each target revision is equal to a source revision.  
				    && sourceIt.hasNext() // we have something to push, also need to push previous changeset
				) { // at least base is equal
					// send Revisions and changeset starting sourceIndex, no data, no uncommitted
					// Send, including the LAST MATCHING REVISION at which index the first necessary 
					// delta changeset will be.
					VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration(sourceIndex);
					StringWriter stringWriter = new StringWriter(5 * 1024 * 1024);
					VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(repository.getOntologyManager());
					owlxmlRenderer.render(sourceVersionedOnto, stringWriter, conf);
					// PROPOSE
					//Performative.Inform
					reply = getReply(msg, Performative.Propose);
					String owlxmlStringOntology = stringWriter.toString();
			        combine(reply, struct(CONTENT, owlxmlStringOntology));
			        combine(reply, struct(KEY_LAST_MATCHING_REVISION, sourceRevisions.get(sourceIndex)));
			        send(targetPeerID, reply);
			        setCompletedMessage("Source sent " + (sourceRevisions.size() - sourceIndex + 1) + " revisions and changesets to target." 
			        		+ " size was : " + (stringWriter.getBuffer().length()/1024) + " kilo characters ");
			        nextState = SendingDelta;
			        if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
			        	try {
			        		File sent = new File("C:\\temp\\SentDelta-" +  new Date().getTime() + ".xml");
			        		Writer writer = new OutputStreamWriter(new FileOutputStream(sent), Charset.forName("UTF-8"));
			        		writer.write(stringWriter.toString());
			        		writer.close();
			        	} catch (Exception e) {
			        		System.err.println("Push: Exception during debug output ignored:");
			        		e.printStackTrace();
						}
			        }
			        stringWriter.close();
				} else {
					if (sourceRevisions.size() == targetRevisions.size()) {
						//TARGET IS EQUAL TO SOURCE, source might have uncommitted.
						reply = getReply(msg, Performative.Confirm);
				        combine(reply, struct(CONTENT, "Source and Target are equal."));
				        setCompletedMessage("Source and Target are equal. Nothing to transmit.");
				        nextState = WorkflowStateConstant.Completed;
					} else {
						//Target has more than source, should pull, if no uncommitted.
						reply = getReply(msg, Performative.Confirm);
				        combine(reply, struct(CONTENT, "Target is newer than source."));
				        setCompletedMessage("Target is newer than source. A pull is suggested.");
				        nextState = WorkflowStateConstant.Completed;
					}
				}
		} else {
			reply = getReply(msg, Performative.Failure);
	        nextState = WorkflowStateConstant.Failed;
			if (sharedAreEqual) {
				assert(sourceIndex == 0);
				//System error: nothing compared!!
		        combine(reply, struct(CONTENT, "System failure: Nothing was compared.")); 
			} else {
				// We found one unequal revision, no push possible.
				// Need to revert target to last equal revision, which is:
		        combine(reply, struct(CONTENT, "Cannot push: Target revision at index " + sourceIndex + " does not match source."));
			}
		}
		msg = createMessage(Performative.QueryIf, this);
		// send full revision Array
        combine(msg, struct(CONTENT, sourceVersionedOnto.getRevisions())); 
        send(targetPeerID, reply);
		return nextState;
	}
	
	/**
	 * @param msg
	 * @return
	 * @throws Throwable
	 */
	@FromState("ReceivingDelta") //TARGET
    @OnMessage(performative="Propose")
    //@PossibleOutcome({"Completed", "Failed"}) 
    //@AtActivity(CONTENT);
    public WorkflowStateConstant targetExistsConfirmedNowReceiveDelta(Message msg) throws Throwable {
		//
		// Test if received last revision matches target head and all other prerequisites are still met.
		//
		Revision lastMatchingRevision = getPart(msg, KEY_LAST_MATCHING_REVISION);
		// Validate if lastMatchingRevision still is target HEAD, keep UUID
		//Throws exceptions if not.
		VersionedOntology targetVersionedOntology = getVersionedOntologyForDeltaFrom(lastMatchingRevision);
		System.out.println("RECEIVING delta");
		String vowlxmlStringDelta = getPart(msg, CONTENT);
		OWLOntologyDocumentSource ds = new StringDocumentSource(vowlxmlStringDelta);
		// Parse, apply and append the delta
		appendDeltaTo(ds, targetVersionedOntology);
		//assert targetVersionedOntology contains delta
		if (DBG_RENDER_ONTOLOGIES_TO_FILE) {
			repository.printAllOntologies();
			repository.renderFullVersionedontologyToVOWLXML(targetVersionedOntology, new File("C:\\temp"));
		}
		Message reply = getReply(msg, Performative.AcceptProposal);
		send(getSender(msg), reply);
		setCompletedMessage("Delta received and applied. Size: " + (vowlxmlStringDelta.length()/1024) + " kilo characters");
		return WorkflowStateConstant.Completed;
	}
	
	/**
	 * Appends the given Changeset and Revision delta information to the targetVersionedOntology.
	 * The vowlxmlDeltaSource has to contain at least one Revision and zero Changesets.
	 * The first revision has to match the head of targetVersionedOntology.
	 * 
	 * @param vowlxmlDeltaSource
	 * @param targetVersionedOntology
	 * @throws OWLOntologyChangeException
	 * @throws UnloadableImportException
	 * @throws OWLParserException
	 * @throws IOException
	 */
	void appendDeltaTo(OWLOntologyDocumentSource vowlxmlDeltaSource, VersionedOntology targetVersionedOntology) throws OWLOntologyChangeException, UnloadableImportException, OWLParserException, IOException {
		VOWLXMLParser vowlxmlParser = new VOWLXMLParser();
		HGDBOntologyManager manager = repository.getOntologyManager();
		//Create an dummy in mem onto with a hgdb manager and hgdb data factory to use.
		OWLOntology dummyOnto = new OWLOntologyImpl(manager, new OWLOntologyID());
		VOWLXMLDocument vowlxmlDoc = new VOWLXMLDocument(dummyOnto);
		//The newly created ontology will hold the manager and the parser will use the manager's
		//data factory.
		vowlxmlParser.parse(vowlxmlDeltaSource, vowlxmlDoc, new OWLOntologyLoaderConfiguration());
		VOWLXMLRenderConfiguration renderConf = vowlxmlDoc.getRenderConfig();
		if (renderConf.isLastRevisionData() || renderConf.isUncommittedChanges()) {
			throw new IllegalStateException("Transmitted data contains unexpected content: revision data or uncommitted.");
		}
		List<Revision> deltaRevisions = vowlxmlDoc.getRevisions();
		List<ChangeSet> deltaChangeSets = vowlxmlDoc.getChangesets();
		if (deltaRevisions.size() != deltaChangeSets.size() + 1) {
			throw new IllegalStateException("Expecting exactly one more Revision than changesets." 
					+ "The workingset changeset after head must not be included in the transmission");
		}
		// Apply and store changesets.
//		if (!deltaRevisions.get(0).equals(lastMatchingRevision)) {
//			throw new IllegalStateException("Internal error. The transmissions lastMatchingRevision data did not match the first owlxml revision.");
//		}
		// This might cause 
		targetVersionedOntology.addApplyDelta(deltaRevisions, deltaChangeSets);
	}
	
	/**
	 * Returns the VersionedOntology specified by the revision object and checks, if it is ready to have delta applied to it.
	 * In particular it checks, if a vo with the revision UUID is available, if the given revision matches the head and that all changes are committed.
	 * 
	 * @param lastMatchingRevision
	 * @return a valid versionedontology and never null (will throw exception instead)
	 * @throws IllegalStateException in all problem cases.
	 */
	VersionedOntology getVersionedOntologyForDeltaFrom(Revision lastMatchingRevision) throws IllegalStateException {
		HGPersistentHandle ontoUUID = lastMatchingRevision.getOntologyUUID();
		HGDBOntology onto = (HGDBOntology)repository.getHyperGraph().get(ontoUUID);
		//boolean applyDelta = false;
		VersionedOntology targetVersionedOntology;
		if (onto != null) {
			targetVersionedOntology = repository.getVersionControlledOntology(onto);
			if (targetVersionedOntology != null) {
				if (targetVersionedOntology.getHeadRevision().equals(lastMatchingRevision)) {
					//	we're good.
					if (targetVersionedOntology.getWorkingSetChanges().isEmpty()) {
						//do it.
						//applyDelta = true;
						return targetVersionedOntology;
					} else {
						throw new IllegalStateException("Delta not applicable, because uncommitted changes exist in target.");
					}
				} else {
					throw new IllegalStateException("Delta not applicable to target head revision. Might have changed.");
				}
			} else {
				// somebody removed version control in the meantime
				throw new IllegalStateException("Delta refers to an ontology that is currently not version controlled.");
			}
		} else {
			// somebody removed the onto in the meantime or the source sent wrong revision.
			// 
			throw new IllegalStateException("Delta refers to an ontology that does currently not exist.");
		}
		//return applyDelta;
	}

	@FromState("SendingDelta") //Source
    @OnMessage(performative="AcceptProposal")
    @PossibleOutcome({"Completed"}) 
    public WorkflowStateConstant sourceDeltaSentConfirmed(Message msg) throws Throwable {
		setCompletedMessage("All changes were applied to target.");
		return WorkflowStateConstant.Completed;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.Activity#getType()
	 */
	@Override
	public String getType() {
		return TYPENAME;
	}
	
	//
	// UTILILTY METHODS
	//TODO put somewhere else
	//
	
	private void storeFromTo(OWLOntology from, HGDBOntology to) {
		final Set<OWLAxiom> axioms = from.getAxioms();
		int i = 0;
		for (OWLAxiom axiom : axioms) {
			to.applyChange(new AddAxiom(to, axiom));
			i++;
			if (DBG && i % 5000 == 0) {
				System.out.println("storeFromTo: Axioms: " + i);
			}
		}
		if (DBG) System.out.println("storeFromTo: Axioms: " + i);
		// Add Ontology Annotations
		for (OWLAnnotation a : from.getAnnotations()) {
			to.applyChange(new AddOntologyAnnotation(to, a));
		}
		// Add Import Declarations
		for (OWLImportsDeclaration im : from.getImportsDeclarations()) {
			to.applyChange(new AddImport(to, im));
		}
	}
}
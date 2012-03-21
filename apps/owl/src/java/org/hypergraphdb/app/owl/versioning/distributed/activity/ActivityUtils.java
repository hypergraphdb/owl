package org.hypergraphdb.app.owl.versioning.distributed.activity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVersionedOntologyRenderer;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLRendererException;
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
 * ActivityUtils.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 21, 2012
 */
public class ActivityUtils {
	//------------------------------------------------------------------------------------------------------------
	// UTILILTY METHODS FOR REUSE
	//TODO put somewhere else
	//------------------------------------------------------------------------------------------------------------
	public static boolean DBG = true;
	
    public static final int RENDER_BUFFER_FULL_INITIAL_SIZE = 20 * 1024 * 1024; //characters
    public static final int RENDER_BUFFER_DELTA_INITIAL_SIZE = 1 * 1024 * 1024; //characters

	/**
	 * Renders a full versioned ontology (All Changesets, Revisions and Head Revision data).
	 * Call within transaction.
	 * 
	 * @param versionedOnto with workingsetdata and manager set.
	 * @return
	 * @throws OWLRendererException
	 */
	String renderVersionedOntology(VersionedOntology versionedOnto) throws OWLRendererException {
		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration();
		conf.setLastRevisionData(true);
		conf.setUncommittedChanges(false);
		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_FULL_INITIAL_SIZE);
		//Would need OWLOntologyManager for Format, but null ok here.
		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(versionedOnto.getWorkingSetData().getOWLOntologyManager());
		owlxmlRenderer.render(versionedOnto, stringWriter, conf);
		return stringWriter.toString();
	}
	

	/**
	 * Parses a complete versioned ontology (revisions, change sets, head revision data) from a VOWLXML string and stores it as new ontology with versioning in the repository.
	 * The created ontology will have the parsed ontologyUUID.
	 * 
	 * Maps the defaultDocumentIRI to hgdb://.
	 * 
	 * Call within Transaction.
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
	VersionedOntology storeVersionedOntology(OWLOntologyDocumentSource vowlDocumentSource, HGDBOntologyManager manager) throws OWLOntologyChangeException, 
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
		HyperGraph graph = manager.getOntologyRepository().getHyperGraph();
		//Add version control with full matching history.
		System.out.println("Creating and adding version control information for : " + ontologyUUID);
		VersionedOntology voParsed = new VersionedOntology(vowlxmlDoc.getRevisions(), vowlxmlDoc.getChangesets(), graph);
		//TODO VALIDATE EVERYTHING HERE, even though we have a lot of validation by getting here.
		graph.add(voParsed);
		return voParsed;
	}

	/**
	 * Appends the given Changeset and Revision delta information to the targetVersionedOntology.
	 * The vowlxmlDeltaSource has to contain at least one Revision and zero Changesets.
	 * The first revision has to match the head of targetVersionedOntology.
	 * 
	 * Call within transaction.
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
		HGDBOntologyManager manager = (HGDBOntologyManager)targetVersionedOntology.getWorkingSetData().getOWLOntologyManager();
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
 	 * Call within transaction.
 	 * 
	 * @param lastMatchingRevision
	 * @return a valid versionedontology and never null (will throw exception instead)
	 * @throws IllegalStateException in all problem cases.
	 */
	VersionedOntology getVersionedOntologyForDeltaFrom(Revision lastMatchingRevision, VHGDBOntologyRepository repository) throws IllegalStateException {
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

	/**
	 * Renders the revisions and changesets starting with the given index.
	 * 
 	 * Call within transaction.
 	 * 
	 * @param versionedOntology with workingsetdata and manager set.
	 * @param startRevisionIndex
	 * @return
	 * @throws OWLRendererException
	 */
	String renderVersionedOntologyDelta(VersionedOntology versionedOntology, int startRevisionIndex) throws OWLRendererException {
		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration(startRevisionIndex);
		HGDBOntologyManager manager = (HGDBOntologyManager)versionedOntology.getWorkingSetData().getOWLOntologyManager();
		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_DELTA_INITIAL_SIZE);
		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(manager);
		//owlxmlRenderer.render(sourceVersionedOnto, stringWriter, conf);
		owlxmlRenderer.render(versionedOntology, stringWriter, conf);
		return stringWriter.toString();
	}
	
	/**
	 * Finds the index of the last 2 revisions that are common (equal) to both histories.
	 * <pre>
	 * 0..source.size()-1 the index of the last matching revision
	 * -1 no common history 
	 * </pre>
	 * 
	 * @param branchA a history of revisions, oldest first.
	 * @param branchB a history of revisions, oldest first.
	 * @return -1 if no common history, or index value [0..Math.Min(branchA.size(), branchB.size()-1)] 
	 */
	int findLastCommonRevisionIndex(List<Revision> branchA, List<Revision> branchB) {
		ListIterator<Revision> aIt = branchA.listIterator();
		ListIterator<Revision> bIt = branchB.listIterator(); 
		int commonIndex = -1; 
		boolean commonAreEqual = true;
		while (commonAreEqual && aIt.hasNext() && bIt.hasNext()){
			commonIndex ++;
			Revision revisionA = aIt.next();			
			Revision revisionB = bIt.next();
			//TODO we'll need content dependent comparison here in the future (SHA1?)
			commonAreEqual = revisionA.equals(revisionB);
		}
		return commonIndex;
	}

	/**
	 * Shallow copies all axioms, ontology annotations and importdeclarations from any ontology and 
	 * adds them to an HGDBOntology by applying the changes to the to ontology directly.
	 * (Without an ontologymanager)
	 * 
	 * The initial use case for this was to load an in memory ontology with axioms, et.c. created from a
	 * DB-Backed HGDBDataFactory before saving all in a DB-backed ontology.
	 * 
	 * The OntologyID is NOT copied.
	 * 
	 * Call within transaction.
	 * 
	 * @param from
	 * @param to
	 */
	void storeFromTo(OWLOntology from, HGDBOntology to) {
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

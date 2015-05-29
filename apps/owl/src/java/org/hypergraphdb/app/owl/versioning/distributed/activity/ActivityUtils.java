package org.hypergraphdb.app.owl.versioning.distributed.activity;

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

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.core.OWLTempOntologyImpl;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.Revision;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.DistributedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
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
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * ActivityUtils.
 * 
 * INTENTIONALLY NOT THREAD SAFE. Synchronize externally.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 21, 2012
 */
public class ActivityUtils
{
	// ------------------------------------------------------------------------------------------------------------
	// UTILILTY METHODS FOR REUSE
	// TODO put somewhere else
	// ------------------------------------------------------------------------------------------------------------
	public static boolean DBG = true;

	public static final int RENDER_BUFFER_FULL_INITIAL_SIZE = 20 * 1024 * 1024; // characters
	public static final int RENDER_BUFFER_DELTA_INITIAL_SIZE = 1 * 1024 * 1024; // characters

	public static String RENDER_DIR = System.getProperty("java.io.tmpdir");
	public static int RENDER_COUNTER = 0;

	static
	{
		System.out.println("ACTIVITY RENDER DIRECTORY: " + RENDER_DIR);
	}

	/**
	 * Renders a full versioned ontology (All Changesets, Revisions and Head
	 * Revision data). Call within transaction.
	 * 
	 * @param versionedOnto
	 *            with workingsetdata and manager set.
	 * @return
	 * @throws OWLRendererException
	 */
	public static String renderVersionedOntology(VersionedOntology versionedOntology)
	{
		try
		{
			VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration();
			conf.firstRevision(versionedOntology.getRootRevision());
			conf.revisionSnapshot(versionedOntology.getCurrentRevision());
			VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(
					HGOntologyManagerFactory.getOntologyManager(versionedOntology.graph().getLocation()));
			StringWriter stringWriter = new StringWriter(RENDER_BUFFER_DELTA_INITIAL_SIZE);
			owlxmlRenderer.render(versionedOntology, null, stringWriter, conf);
			return stringWriter.toString();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static String renderVersionedOntology(VersionedOntology versionedOnto, int lastRevisionToRenderIndex) throws OWLRendererException
	{
		throw new UnsupportedOperationException();
//		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration();
//		conf.setLastRevisionData(true);
//		conf.uncommittedChanges(false);
//		conf.setLastRevisionIndex(lastRevisionToRenderIndex);
//		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_FULL_INITIAL_SIZE);
//		// Would need OWLOntologyManager for Format, but null ok here.
//		VOWLXMLVersionedOntologyRenderer owlxmlRenderer =
//				new VOWLXMLVersionedOntologyRenderer(versionedOnto.getWorkingSetData().getOWLOntologyManager());
//		owlxmlRenderer.render(versionedOnto, stringWriter, conf);
//		return stringWriter.toString();
	}

	/**
	 * Parses a complete versioned ontology (revisions, change sets, head
	 * revision data) from a VOWLXML string and stores it as new ontology with
	 * versioning in the repository. The created ontology will have the parsed
	 * ontologyUUID.
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
	public static VersionedOntology storeVersionedOntology(OWLOntologyDocumentSource vowlDocumentSource, HGDBOntologyManager manager)
	{
		HyperGraph graph = manager.getOntologyRepository().getHyperGraph();		
		try
		{
			VOWLXMLParser vowlxmlParser = new VOWLXMLParser();
			// Create an partial in mem onto with a hgdb manager and hgdb data
			// factory to use.
			OWLOntologyEx partialInMemOnto = new OWLTempOntologyImpl(manager, new OWLOntologyID());
			VOWLXMLDocument vowlxmlDoc = new VOWLXMLDocument(partialInMemOnto);
			// The newly created ontology will hold the manager and the parser will
			// use the manager's
			// data factory.
			vowlxmlParser.parse(graph, vowlDocumentSource, vowlxmlDoc, new OWLOntologyLoaderConfiguration());
			OWLOntologyID ontologyID = vowlxmlDoc.getRevisionData().getOntologyID();
			IRI documentIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(ontologyID.getDefaultDocumentIRI()); 
			HGPersistentHandle ontologyUUID = graph.getHandleFactory().makeHandle(vowlxmlDoc.getOntologyID());
			System.out.println("Storing ontology data for : " + ontologyUUID + " using docIRI: " + documentIRI);
			HGDBOntology o = manager.getOntologyRepository().createOWLOntology(ontologyID, documentIRI, ontologyUUID);
			o.setOWLOntologyManager(manager);
			storeFromTo(vowlxmlDoc.getRevisionData(), o);
			// Add version control with full matching history.
			System.out.println("Creating and adding version control information for : " + ontologyUUID);
			ChangeSet<VersionedOntology> workingChangeSet = new ChangeSet<VersionedOntology>();			
			VersionedOntology voParsed = new VersionedOntology(graph, 
															   o.getAtomHandle(),  
															   vowlxmlDoc.getRenderConfig().revisionSnapshot(), 
															   graph.add(workingChangeSet));
			voParsed.setRootRevision(vowlxmlDoc.getRenderConfig().firstRevision());
			voParsed.setCurrentRevision(vowlxmlDoc.getRenderConfig().revisionSnapshot());
			HGPersistentHandle versionedHandle = graph.getHandleFactory().makeHandle(vowlxmlDoc.getVersionedID());			
			graph.define(versionedHandle, voParsed);
			return voParsed;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}		
	}

	/**
	 * Appends the given Changeset and Revision delta information to the
	 * targetVersionedOntology. The vowlxmlDeltaSource has to contain at least
	 * one Revision and zero Changesets. The first revision has to match the
	 * head of targetVersionedOntology.
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
	public VOWLXMLDocument appendDeltaTo(OWLOntologyDocumentSource vowlxmlDeltaSource, VersionedOntology targetVersionedOntology,
			boolean mergeWithUncommitted) throws OWLOntologyChangeException, UnloadableImportException, OWLParserException,
			IOException
	{
		throw new UnsupportedOperationException();
//		if (!mergeWithUncommitted && !targetVersionedOntology.getWorkingSetChanges().isEmpty())
//		{
//			throw new IllegalStateException("There must not be uncommitted changes on appending delta without merge.");
//		}
//		VOWLXMLParser vowlxmlParser = new VOWLXMLParser();
//		HGDBOntologyManager manager = (HGDBOntologyManager) targetVersionedOntology.getWorkingSetData().getOWLOntologyManager();
//		// Create an dummy in mem onto with a hgdb manager and hgdb data factory
//		// to use.
//		OWLOntologyEx dummyOnto = new OWLTempOntologyImpl(manager, new OWLOntologyID());
//		VOWLXMLDocument vowlxmlDoc = new VOWLXMLDocument(dummyOnto);
//		// The newly created ontology will hold the manager and the parser will
//		// use the manager's
//		// data factory.
//		vowlxmlParser.parse(vowlxmlDeltaSource, vowlxmlDoc, new OWLOntologyLoaderConfiguration());
//		VOWLXMLRenderConfiguration renderConf = vowlxmlDoc.getRenderConfig();
//		if (renderConf.isLastRevisionData() || renderConf.isUncommittedChanges())
//		{
//			throw new IllegalStateException("Transmitted data contains unexpected content: revision data or uncommitted.");
//		}
//		List<Revision> deltaRevisions = vowlxmlDoc.getRevisions();
//		List<ChangeSet> deltaChangeSets = vowlxmlDoc.getChangesets();
//		if (deltaRevisions.size() != deltaChangeSets.size() + 1)
//		{
//			throw new IllegalStateException("Expecting exactly one more Revision than changesets."
//					+ "The workingset changeset after head must not be included in the transmission");
//		}
//		if (mergeWithUncommitted)
//		{
//			System.out.println("MERGING: " + targetVersionedOntology.getWorkingSetChanges().size()
//					+ " uncommitted changes by reapplying them after applying " + deltaChangeSets.size() + " delta changesets ");
//		}
//		targetVersionedOntology.addApplyDelta(deltaRevisions, deltaChangeSets, mergeWithUncommitted);
//		return vowlxmlDoc;
	}

	/**
	 * Returns the VersionedOntology specified by the revision object and
	 * checks, if it is ready to have delta applied to it. In particular it
	 * checks, if a vo with the revision UUID is available, if the given
	 * revision matches the head and that all changes are committed.
	 * 
	 * Call within transaction.
	 * 
	 * @param lastMatchingRevision
	 * @return a valid versionedontology and never null (will throw exception
	 *         instead)
	 * @throws IllegalStateException
	 *             in all problem cases.
	 */
	public DistributedOntology getDistributedOntologyForDeltaFrom(Revision lastMatchingRevision, VDHGDBOntologyRepository repository,
			boolean mergeWithUncommittedMode) throws IllegalStateException
	{
		throw new UnsupportedOperationException();
//		HGPersistentHandle ontoUUID = lastMatchingRevision.getOntologyUUID();
//		HGDBOntology onto = (HGDBOntology) repository.getHyperGraph().get(ontoUUID);
//		if (onto == null)
//		{
//			// somebody removed the onto in the meantime or the source sent
//			// wrong revision.
//			throw new IllegalStateException("Delta refers to an ontology that does currently not exist.");
//		}
//		onto.setOWLOntologyManager(repository.getOntologyManager());
//		DistributedOntology targetDistributedOntology = repository.getDistributedOntology(onto);
//		VersionedOntology targetVersionedOntology = targetDistributedOntology.getVersionedOntology();
//		if (targetVersionedOntology != null)
//		{
//			if (targetVersionedOntology.getHeadRevision().equals(lastMatchingRevision))
//			{
//				if (targetVersionedOntology.getWorkingSetChanges().isEmpty() || mergeWithUncommittedMode)
//				{
//					return targetDistributedOntology;
//				}
//				else
//				{
//					throw new IllegalStateException(
//							"Delta not applicable, because uncommitted changes exist in target and no merge was allowed.");
//				}
//			}
//			else
//			{
//				throw new IllegalStateException("Delta not applicable to head revision. Might have changed.");
//			}
//		}
//		else
//		{
//			// somebody removed version control in the meantime
//			throw new IllegalStateException("Delta refers to an ontology that is currently not version controlled.");
//		}
	}

	public String renderVersionedOntologyDelta(VersionedOntology versionedOntology, Set<Revision> delta) throws OWLRendererException
	{
		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration();
		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(
				HGOntologyManagerFactory.getOntologyManager(versionedOntology.graph().getLocation()));
		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_DELTA_INITIAL_SIZE);
		owlxmlRenderer.render(versionedOntology, delta, stringWriter, conf);
		return stringWriter.toString();
	}
	
	/**
	 * Renders the revisions and changesets starting with the given index.
	 * 
	 * Call within transaction.
	 * 
	 * @param versionedOntology
	 *            with workingsetdata and manager set.
	 * @param startRevisionIndex
	 * @return
	 * @throws OWLRendererException
	 */
	public String renderVersionedOntologyDelta(VersionedOntology versionedOntology, int startRevisionIndex) throws OWLRendererException
	{
		return renderVersionedOntologyDelta(versionedOntology, startRevisionIndex, Integer.MAX_VALUE);
	}

	public String renderVersionedOntologyDelta(VersionedOntology versionedOntology, int startRevisionIndex, int lastRevisionIndex)
			throws OWLRendererException
	{
		throw new UnsupportedOperationException();
//		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration(startRevisionIndex);
//		conf.setLastRevisionIndex(lastRevisionIndex);
//		HGDBOntologyManager manager = (HGDBOntologyManager) versionedOntology.getWorkingSetData().getOWLOntologyManager();
//		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_DELTA_INITIAL_SIZE);
//		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(manager);
//		// owlxmlRenderer.render(sourceVersionedOnto, stringWriter, conf);
//		owlxmlRenderer.render(versionedOntology, stringWriter, conf);
//		return stringWriter.toString();
	}

	/**
	 * Shallow copies all axioms, ontology annotations and importdeclarations
	 * from any ontology and adds them to an HGDBOntology by applying the
	 * changes to the to ontology directly. (Without an ontologymanager)
	 * 
	 * The initial use case for this was to load an in memory ontology with
	 * axioms, et.c. created from a DB-Backed HGDBDataFactory before saving all
	 * in a DB-backed ontology.
	 * 
	 * The OntologyID is NOT copied.
	 * 
	 * Call within transaction.
	 * 
	 * @param from
	 * @param to
	 *            and ontology already in the graph.
	 */
	public static void storeFromTo(OWLOntologyEx from, HGDBOntology to)
	{
		final Set<OWLAxiom> axioms = from.getAxioms();
		int i = 0;
		for (OWLAxiom axiom : axioms)
		{
			to.applyChange(new AddAxiom(to, axiom));
			i++;
			if (DBG && i % 5000 == 0)
			{
				System.out.println("storeFromTo: Axioms: " + i);
			}
		}
		if (DBG)
			System.out.println("storeFromTo: Axioms: " + i);
		// Add Ontology Annotations
		for (OWLAnnotation a : from.getAnnotations())
		{
			to.applyChange(new AddOntologyAnnotation(to, a));
		}
		// Add Import Declarations
		for (OWLImportsDeclaration im : from.getImportsDeclarations())
		{
			to.applyChange(new AddImport(to, im));
		}
		to.setPrefixesFrom(from.getPrefixes());
		if (DBG)
			System.out.println("Prefixes stored: nr: " + to.getPrefixes().size());
	}

	//
	// DBG OUTPUT UTILS
	//

	File getTargetXMLFile(String tag)
	{
		RENDER_COUNTER++;
		return new File(RENDER_DIR + File.separator + (new Date().getTime()) + "-" + RENDER_COUNTER + "-" + tag + ".xml");
	}

	/**
	 * Saves a full vo as xml file to RENDER_DIR with a timestamped unique
	 * sequential name ends with the given tag. eg. 1-8271368127-mytag.xml
	 * Suggested tags: ONTO-SENT-PEERNAME, ONTO-RECEIVED-PEERNAME
	 * 
	 * @param vo
	 * @param tag
	 * @throws OWLRendererException
	 * @throws IOException
	 */
	void saveVersionedOntologyXML(VersionedOntology vo, String tag) throws OWLRendererException, IOException
	{
		throw new UnsupportedOperationException();
//		VOWLXMLVersionedOntologyRenderer r = new VOWLXMLVersionedOntologyRenderer(vo.getWorkingSetData().getOWLOntologyManager());
//		Writer fwriter = new OutputStreamWriter(new FileOutputStream(getTargetXMLFile(tag)), Charset.forName("UTF-8"));
//		r.render(vo, fwriter, new VOWLXMLRenderConfiguration());
//		fwriter.close();
	}

	/**
	 * Saves a given string as xml file to RENDER_DIR with a timestamped unique
	 * sequential name ends with the given tag. Use this for saving DELTA.
	 * Suggested tags: DELTA-SENT-PEERNAME, DELTA-RECEIVED-PEERNAME eg.
	 * 1-8271368127-mytag.xml
	 */
	void saveStringXML(String content, String tag) throws IOException
	{
		Writer fwriter = new OutputStreamWriter(new FileOutputStream(getTargetXMLFile(tag)), Charset.forName("UTF-8"));
		fwriter.write(content);
		fwriter.close();
	}
}
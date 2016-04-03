package org.hypergraphdb.app.owl.versioning.distributed.activity;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.hypergraphdb.app.owl.core.OWLTempOntologyImpl;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.Versioned;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VMetadataChange;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLMetadata;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParser;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVersionedOntologyRenderer;
import org.hypergraphdb.event.HGAtomAddedEvent;
import org.hypergraphdb.query.HGAtomPredicate;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;

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

	private static Collection<HGHandle> collectRevisionsFrom(final HGHandle root, 
															 final Set<HGHandle> sofar, 
															 final VersionedOntology vo,
															 final Collection<HGHandle> heads)
	{
		final HyperGraph graph = vo.ontology().getHyperGraph();
		final HGHandle revisionType = graph.getTypeSystem().getTypeHandle(Revision.class);
		HGAtomPredicate revisionOk = new HGAtomPredicate() {
			public boolean satisfies(HyperGraph graph, HGHandle revision)
			{
				boolean isok = graph.getType(revision).equals(revisionType) && 
					   !sofar.contains(revision) &&
					   !heads.contains(revision);				
				return isok;
			}
		};
		return graph.findAll(hg.bfs(root, hg.type(ChangeLink.class), revisionOk));
	}
	
	public static Set<HGHandle> collectRevisions(VersionedOntology vo, Collection<HGHandle> roots, Collection<HGHandle> heads)
	{
		Set<HGHandle> S = new HashSet<HGHandle>();
		for (HGHandle root : roots)
		{
			if (S.contains(root))
				continue;
			S.addAll(collectRevisionsFrom(root, S, vo, heads));
			S.add(root);
		}
		return S;
	}
	
	public static <T extends Versioned<T>> List<VMetadataChange<T>> collectMetaChanges(HyperGraph graph, T versioned, HGHandle lastKnown)
	{
		ArrayList<VMetadataChange<T>> metaChanges = new ArrayList<VMetadataChange<T>>();
		HGHandle latest = versioned.metadata().lastChange();
		if (latest != null)
		{
			List<VMetadataChange<T>> L = graph.getAll(hg.dfs(
					latest, 
					hg.type(ChangeLink.class), 
					lastKnown == null ? null : hg.not(hg.is(lastKnown)), 
					false, 
					true));
			VMetadataChange<T> last = graph.get(latest);
			if (!latest.equals(lastKnown))
				L.add(last);
			metaChanges.addAll(L);
		}
		return metaChanges;
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
			conf.bottomRevision(versionedOntology.getBottomRevision());
			conf.revisionSnapshot(versionedOntology.getCurrentRevision());
			conf.roots().add(conf.firstRevision());
			conf.writeMetadata(true);
			VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(
					HGOntologyManagerFactory.getOntologyManager(versionedOntology.graph().getLocation()));
			StringWriter stringWriter = new StringWriter(RENDER_BUFFER_DELTA_INITIAL_SIZE);
			owlxmlRenderer.render(versionedOntology, 
								  collectRevisions(versionedOntology, 
										  		   conf.roots(), 
										  		   conf.heads()), 
										  		   stringWriter, 
										  		   conf);
			return stringWriter.toString();
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static void storeChangeSet(HyperGraph graph, ChangeSet<VersionedOntology> changeSet, List<Change<VersionedOntology>> changes)
	{
		changeSet.add(changes);
	}
	
	/**
	 * Parses a complete versioned ontology (revisions, change sets, head
	 * revision data) from a VOWLXML string and returns the corresponding
	 * VOWLXMLDocument representation.
	 * 
	 */
	public static VOWLXMLDocument parseVersionedDoc(HGDBOntologyManager manager, OWLOntologyDocumentSource vowlDocumentSource)
	{
		VOWLXMLParser vowlxmlParser = new VOWLXMLParser();
		// Create an partial in mem onto with a hgdb manager and hgdb data
		// factory to use.
		OWLOntologyEx partialInMemOnto = new OWLTempOntologyImpl(manager, new OWLOntologyID());
		VOWLXMLDocument vowlxmlDoc = new VOWLXMLDocument(partialInMemOnto);
		// The newly created ontology will hold the manager and the parser will
		// use the manager's
		// data factory.
		try
		{
			vowlxmlParser.parse(manager.getOntologyRepository().getHyperGraph(), 
								vowlDocumentSource, 
								vowlxmlDoc, 
								new OWLOntologyLoaderConfiguration());
			return vowlxmlDoc;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static VersionedOntology storeClonedOntology(HGDBOntologyManager manager, VOWLXMLDocument doc)
	{
		try
		{
			HyperGraph graph = manager.getOntologyRepository().getHyperGraph();
			OWLOntologyID ontologyID = doc.getRevisionData().getOntologyID();
			IRI documentIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(ontologyID.getDefaultDocumentIRI()); 
			HGPersistentHandle ontologyUUID = graph.getHandleFactory().makeHandle(doc.getOntologyID());
			HGDBOntology o = manager.getOntologyRepository().createOWLOntology(ontologyID, documentIRI, ontologyUUID);
			o.setOWLOntologyManager(manager);
			storeFromTo(doc.getRevisionData(), o);		
			ChangeSet<VersionedOntology> workingChangeSet = new ChangeSet<VersionedOntology>();			
			VersionedOntology vo = new VersionedOntology(graph, 
														 o.getAtomHandle(),  
														 doc.getRenderConfig().revisionSnapshot(), 
														 graph.add(workingChangeSet));
			vo.setRootRevision(doc.getRenderConfig().firstRevision());
			vo.setBottomRevision(doc.getRenderConfig().bottomRevision());
			HGPersistentHandle versionedHandle = graph.getHandleFactory().makeHandle(doc.getVersionedID());
			graph.define(versionedHandle, vo);			
			manager.getVersionManager().manualVersioned(vo.getOntology());
			updateVersionedOntology(manager, vo, doc);
			storeMetadata(graph, doc.getMetadata());
//			Revision root = graph.get(vo.getRootRevision());
//			RevisionMark mark = graph.get(root.revisionMarks().iterator().next());
//			ChangeRecord record = graph.get(mark.changeRecord());
//			if (record == null) // limit case, during cloning this record is not serialzed
//			{
//				record = new ChangeRecord();
//				record.versioned(o.getAtomHandle());
//				record.changeSet(manager.getVersionManager().emptyChangeSetHandle());
//				graph.define(mark.changeRecord(), record);
//			}
			return vo;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	public static void storeMetadata(HyperGraph graph, VOWLXMLMetadata metadata)
	{
		for (Branch branch : metadata.branches())
		{
			graph.define(branch.getAtomHandle(), branch);
		}
	}
	
	/**
	 * Update the given version ontology from the VOWLXMLDocument obtain from a peer. 
	 * It is assumed that there is already a local copy of the ontology so only the new
	 * revisions and all change objects are stored. The the current head is set to 
	 * the head specified as "revisionSnapshot" in the VOWLXMLDocument.
	 * 
	 * @param manager 
	 * @param vo
	 * @param doc
	 */
	@SuppressWarnings("unchecked")
	public static void updateVersionedOntology(HGDBOntologyManager manager, VersionedOntology vo, VOWLXMLDocument doc)
	{
		final HyperGraph graph = manager.getOntologyRepository().getHyperGraph();		
		// We need to first store the nodes and then the links so that graph
		// indexing/tracking triggered by events works properly.
		for (final HGHandleHolder object : doc.revisionObjects())
		{
			if (graph.get(object.getAtomHandle()) != null) continue;
			if (object instanceof ChangeLink) continue;
			if (DBG)
				System.out.println("Storing object " + object + " with handle " + object.getAtomHandle());
			graph.getTransactionManager().ensureTransaction(new Callable<Object>(){
				public Object call()
				{
					graph.define(object.getAtomHandle(), object);
					// TEMP, until HyperGraph.define starts firing this event (see issue #109 in github)
					graph.getEventManager().dispatch(graph, new HGAtomAddedEvent(object.getAtomHandle(), "HyperGraph.define"));				
					return null;
				}
			});
		}
		
		for (final HGHandleHolder object : doc.revisionObjects())
		{
			if (graph.get(object.getAtomHandle()) != null) continue;
			if (DBG)
				System.out.println("Storing object " + object + " with handle " + object.getAtomHandle());
			graph.getTransactionManager().ensureTransaction(new Callable<Object>(){
				public Object call()
				{
					graph.define(object.getAtomHandle(), object);
					// TEMP, until HyperGraph.define starts firing this event (see issue #109 in github)			
					graph.getEventManager().dispatch(graph, new HGAtomAddedEvent(object.getAtomHandle(), "HyperGraph.define"));				
					return null;
				}
			});		
		}
		for (ChangeSet<VersionedOntology> changeSet : doc.changeSetMap().keySet())
		{
//			if (graph.get(changeSet.getAtomHandle()) == null)
				storeChangeSet(graph, 
							   changeSet, 
							   (List<Change<VersionedOntology>>)(List<?>)doc.changeSetMap().get(changeSet));
		}
		if (doc.getRenderConfig().revisionSnapshot() != null)
			vo.goTo((Revision)graph.get(doc.getRenderConfig().revisionSnapshot()));
	}

	public static String renderVersionedOntologyDelta(VersionedOntology versionedOntology, 
			   								   		  Set<HGHandle> delta) throws OWLRendererException
	{
		return renderVersionedOntologyDelta(versionedOntology, delta, null);
	}
	
	public static String renderVersionedOntologyDelta(VersionedOntology versionedOntology, 
													  Set<HGHandle> delta,
													  HGHandle revisionSnapshot) throws OWLRendererException
	{
		VOWLXMLRenderConfiguration conf = new VOWLXMLRenderConfiguration();
		VOWLXMLVersionedOntologyRenderer owlxmlRenderer = new VOWLXMLVersionedOntologyRenderer(
				HGOntologyManagerFactory.getOntologyManager(versionedOntology.graph().getLocation()));
		conf.heads().addAll(delta);
		if (revisionSnapshot != null)
			conf.revisionSnapshot(revisionSnapshot);
		StringWriter stringWriter = new StringWriter(RENDER_BUFFER_DELTA_INITIAL_SIZE);
		owlxmlRenderer.render(versionedOntology, delta, stringWriter, conf);
		return stringWriter.toString();
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
}
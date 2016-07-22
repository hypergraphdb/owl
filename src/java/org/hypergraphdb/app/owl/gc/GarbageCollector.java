package org.hypergraphdb.app.owl.gc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.algorithms.HGDepthFirstTraversal;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.core.HGDBTask;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLConjuction;
import org.hypergraphdb.app.owl.query.AnySubgraphMemberCondition;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.hypergraphdb.app.owl.util.TargetSetALGenerator;
import org.hypergraphdb.atom.HGSubgraph;
import org.hypergraphdb.indexing.HGIndexer;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * GarbageCollector collects unused OWL related atoms in the graph. Such as
 * <ol>
 * <li>Ontologies marked for deletion with all dependent atoms (axioms, et.c)</li>
 * <li>Disconnected axioms, that do not belong to any ontology.</li>
 * <li>Disconnected entities and its IRIs</li>
 * <li>Disconnected other OWLObjects (everything not part of an onto)</li>
 * </ol>
 * <p>
 * As a general rule: An OWLObject is removable, if
 * <ol>
 * <li>A) Its incidence set can be considered empty.
 * <li>B) Considered means: actual incidence set minus all removable items
 * during processing.
 * <li>C) It's an IRI, has an empty incidence set and is not used in any
 * NamedObject (determined by querying indices).
 * </ol>
 * </p>
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 *          <p>
 *          history: <br>
 *          2012.04.02 hilpold <br>
 *          Bugfix: added SWRLConjunction to known collectable types. <br>
 *          2012.01.05 hilpold <br>
 *          Entity IRIs cannot currently be deleted because of the
 *          implementation of OWLNamedObjectType. <br>
 *          2012.01.08 hilpold <br>
 *          IRIs will now be deleted. We query the IRI indices to determine
 *          removability based on usage in OWLNamedObjectType.
 *          </p>
 */
public class GarbageCollector implements HGDBTask
{
	// Debug switches
	private static final boolean DBG = false;
	private static final boolean DBGX = false;
	private StopWatch stopWatch = new StopWatch();
	private int dbgCounter = 0;

	// Task progress reporting and canceling
	private volatile int taskSize = 0;
	private volatile int taskProgess = 0;
	private volatile boolean cancelTask = false;

	/**
	 * A full GC run entails running: 1. MODE_DELETED_ONTOLOGIES 2.
	 * MODE_DISCONNECTED_AXIOMS 3. MODE_DISCONNECTED_ENTITIES 4.
	 * MODE_DISCONNECTED_OTHER Those 4 modes are exclusive to each other in the
	 * following way: The objects deleted by one mode are not deleted by any
	 * other mode.
	 */
	public static final int MODE_FULL = 0;

	/**
	 * Begins collection at all ontologies marked for deletion and garbage
	 * collects all referenced objects. Will not collect axioms that are not
	 * part of any ontology. Will not collect disconnected entities that are
	 * unreachable by traversing the ontologies.
	 */
	public static final int MODE_DELETED_ONTOLOGIES = 1;

	/**
	 * Begins collection at all axioms that are not member in any ontology and
	 * are disconnected. Each axiom, all reachable dependent objects, and
	 * entities with an otherwise empty incidence set will be removed. A) they
	 * were removed from the last ontology in which they were member. (The
	 * general case is that axioms are exclusive to an ontology; the API user
	 * however can add axioms that exist in Onto A to Onto B, thereby reusing
	 * the axiom and it's dependent objects.
	 */
	public static final int MODE_DISCONNECTED_AXIOMS = 2;

	/**
	 * Begins collection at all disconnected OWLObjectHGDB and IRI atoms
	 * (OWLAnnotationValue), except those implementing OLWEntity or subclasses
	 * of OWLAxiomHGDB. These objects are never member in any ontology. Each
	 * object, all reachable dependent objects, and entities with an otherwise
	 * empty incidence set will be removed. Those include: -
	 * OWLClassExpressionHGDB (not CN, named Class) - (I) OWLDataRange (not R,
	 * named data prop) - OwlFacetRestrictionHGDB - OWLLiteralHGBD -
	 * OWLObjectPropertyExpression (not PN, OWLObjectPropery) - SWRLAtomHGDB -
	 * SWRLIndividualArgument - SWRLLiteralArgument - SWRLVariable
	 */
	public static final int MODE_DISCONNECTED_OTHER = 3;

	/**
	 * Begins collection at entities that are not member in any ontology and are
	 * not target of any other object. No BUILTIN entities will be removed. The
	 * IRIs will be removed if possible.
	 */
	public static final int MODE_DISCONNECTED_ENTITIES = 4;

	private HyperGraph graph;
	private OntologyDatabase repository;

	public GarbageCollector(OntologyDatabase repository)
	{
		this.repository = repository;
		this.graph = repository.getHyperGraph();
	}

	/**
	 * Run full garbage collection
	 * 
	 * @return
	 */
	public synchronized GarbageCollectorStatistics runGarbageCollection()
	{
		return runGarbageCollection(MODE_FULL);
	}

	public synchronized GarbageCollectorStatistics runGarbageCollection(int mode)
	{
		GarbageCollectorStatistics stats = runGCInternal(mode, false);
		return stats;
	}

	/**
	 * Analyze what will be removed on a full garbage collection run.
	 * 
	 * @return
	 */
	public synchronized GarbageCollectorStatistics runGarbageAnalysis()
	{
		return runGCInternal(MODE_FULL, true);
	}

	public synchronized GarbageCollectorStatistics runGarbageAnalysis(int mode)
	{
		GarbageCollectorStatistics stats = runGCInternal(mode, true);
		return stats;
	}

	private GarbageCollectorStatistics runGCInternal(int mode, boolean analyzeMode)
	{
		dbgCounter = 0;
		resetTask();
		Set<HGHandle> analyzeRemovedSet = null;
		if (analyzeMode)
		{
			analyzeRemovedSet = new HashSet<HGHandle>(estimateCollectableAtoms());
		}
		GarbageCollectorStatistics stats = new GarbageCollectorStatistics();
		try
		{
			switchmode: switch (mode)
			{
			case MODE_FULL:
			{
				collectRemovedOntologies(stats, analyzeMode, analyzeRemovedSet);
				if (isCancelTask())
					break switchmode;
				collectAxioms(stats, analyzeMode, analyzeRemovedSet);
				if (isCancelTask())
					break switchmode;
				collectOtherObjects(stats, analyzeMode, analyzeRemovedSet);
				if (isCancelTask())
					break switchmode;
				collectEntities(stats, analyzeMode, analyzeRemovedSet);
			}
				;
				break;
			case MODE_DELETED_ONTOLOGIES:
			{
				collectRemovedOntologies(stats, analyzeMode, analyzeRemovedSet);
			}
				;
				break;
			case MODE_DISCONNECTED_AXIOMS:
			{
				collectAxioms(stats, analyzeMode, analyzeRemovedSet);
			}
				;
				break;
			case MODE_DISCONNECTED_OTHER:
			{
				collectOtherObjects(stats, analyzeMode, analyzeRemovedSet);
			}
				;
				break;
			case MODE_DISCONNECTED_ENTITIES:
			{
				collectEntities(stats, analyzeMode, analyzeRemovedSet);
			}
				;
				break;
			default:
			{
				throw new IllegalArgumentException("runGC with unknown mode called: " + mode);
			}
			}
		}
		catch (RuntimeException e)
		{
			if (e.getCause() instanceof InterruptedException)
			{
				// we cancel and ignore the exception
			}
			else
			{
				throw e;
			}
		}
		return stats;
		// removeableAtomsSet released here
	}

	/**
	 * Roughly estimated based on total atoms and ontologies vs. deleted
	 * ontologies.
	 * 
	 * @return always > 100 and < 1E5
	 */
	private int estimateCollectableAtoms()
	{
		long atoms = repository.getNrOfAtoms();
		int ontologies = repository.getOntologies().size();
		int deletedOntologies = repository.getDeletedOntologies().size();
		int allOntologies = ontologies + deletedOntologies;
		int estimated = (int) (atoms * (deletedOntologies + 1) / (allOntologies + 1));
		if (estimated < 100)
			estimated = 100;
		if (estimated > 1E5)
			estimated = (int) 1E5;
		System.out.print("GC: roughly estimated: " + estimated + " collectible atoms for hashsetsize");
		return estimated;
	}

	private void collectRemovedOntologies(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet)
	{
		List<HGDBOntology> delOntos = repository.getDeletedOntologies();
		int i = 0;
		for (HGDBOntology delOnto : delOntos)
		{
			if (isCancelTask())
				break;
			i++;
			stopWatch.start();
			collectRemovedOntology(delOnto, stats, analyzeMode, analyzeRemovedSet);
			stopWatch.stop("Ontology collection finished (" + i + " of " + delOntos.size() + "): ");
			System.out.println("Stats now: " + stats.toString());
		}
	}

	// private void collectRemovedOntologyTransact(final HGDBOntology onto,
	// final GarbageCollectorStatistics stats, final boolean analyzeMode, final
	// Set<HGHandle> analyzeRemovedSet) {
	// HGTransactionConfig transactionConfig = analyzeMode?
	// HGTransactionConfig.READONLY : HGTransactionConfig.DEFAULT;
	// graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
	// public Object call() {
	// collectRemovedOntology(onto, stats, analyzeMode, analyzeRemovedSet);
	// return null;
	// }}, transactionConfig);
	// }

	private void collectRemovedOntology(final HGDBOntology onto, final GarbageCollectorStatistics stats, final boolean analyzeMode,
			final Set<HGHandle> analyzeRemovedSet)
	{
		// OntologyAnnotations
		// internals.remove does remove anno from onto, NOT graph
		// Ontology Annotations are just added to the ontology, no link.
		HGTransactionConfig transactionConfig = analyzeMode ? HGTransactionConfig.READONLY : HGTransactionConfig.DEFAULT;
		Set<OWLAnnotation> annos = onto.getAnnotations();
		Set<OWLImportsDeclaration> importsDeclarations = onto.getImportsDeclarations();
		Set<OWLAxiom> axioms = onto.getAxioms();
		Set<OWLEntity> entities = onto.getSignature();
		taskProgess = 0;
		taskSize = annos.size() + importsDeclarations.size() + axioms.size() * 2 // remove
																					// from
																					// onto
																					// +
																					// remove
																					// axiom.
				+ entities.size() + onto.getPrefixes().size();

		// we do essential things in the first transaction, but for performance
		// reasons,
		// we do not include the complex removal of axioms in the long term
		// transaction.
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			@SuppressWarnings("unused")
			public Object call()
			{
				Set<OWLAnnotation> annos = onto.getAnnotations();
				Set<OWLImportsDeclaration> importsDeclarations = onto.getImportsDeclarations();
				Set<OWLAxiom> axioms = onto.getAxioms();
				// Set<OWLEntity> entities = onto.getSignature();
				for (OWLAnnotation anno : annos)
				{
					if (isCancelTask())
						return null;
					progressTask();
					HGHandle annoHandle = graph.getHandle(anno);
					if (!analyzeMode)
					{
						onto.remove(annoHandle);
						collectOWLObjectsByDFSTransact(annoHandle, stats, analyzeMode, analyzeRemovedSet);
					}
				}
				// TODO wrap import declaration removal inside a transaction.
				// Import declarations
				// internals.remove does remove from onto&graph:
				// ImportDeclarationLink, ImportDeclaration
				for (OWLImportsDeclaration importsDeclaration : importsDeclarations)
				{
					if (isCancelTask())
						return null;
					progressTask();
					HGHandle importsDeclarationHandle = graph.getHandle(importsDeclaration);
					IncidenceSet is = graph.getIncidenceSet(importsDeclarationHandle);
					if (!is.isEmpty())
					{
						System.err.println("GC: Cannot remove Importsdeclaration with non empty incidence set:"
								+ importsDeclaration);
						continue;
					}
					// ImportDeclarationLink importDeclLink =
					// graph.get(importDeclLinkHandle);
					if (!analyzeMode)
					{
						// onto.remove(importDeclLinkHandle);
						onto.remove(importsDeclarationHandle);
						// graphRemove(importDeclLinkHandle);
						graphRemove(importsDeclarationHandle);
					}
					else
					{
						// analyzeRemovedSet.add(importDeclLinkHandle);
						analyzeRemovedSet.add(importsDeclarationHandle);
					}
					// stats.increaseOtherObjects();
					stats.increaseOtherObjects();
					// stats.increaseTotalAtoms();
					stats.increaseTotalAtoms();
				}
				// Retain Axioms and Entities relevant data:

				// Cancel Membership of entities from ontology as they are onto
				// members,
				// but don't delete or count as this will be done later during
				// axiom removal.
				Set<OWLEntity> entitiesAgain = onto.getSignature();
				if (!analyzeMode)
				{
					for (OWLEntity entity : entitiesAgain)
					{
						if (isCancelTask())
							return null;
						progressTask();
						HGHandle entityHandle = graph.getHandle(entity);
						if (entityHandle != null)
						{
							onto.remove(entityHandle);
						}
						else
						{
							// we might have an entity that was removed when we
							// removed ontologyAnnotations
							if (DBG)
								System.err.println("GC: collectOnto: GRAPH returned null handle for Entity: " + entity + " Class: "
										+ entity.getClass());
						}
					}
				}

				// Now that tasks can be canceled we need to make sure removing
				// axioms is cancelable.
				// A) So we remove each axiom from the onto before removing the
				// ontology.
				for (OWLAxiom axiom : axioms)
				{
					if (isCancelTask())
						return null;
					progressTask();
					HGHandle axiomHandle = graph.getHandle(axiom);
					// 1. remove axiom from Subgraph, index must be zero now for
					// removal,
					// unless axiom is also member in other
					// subgraphs/ontologies, which is possible dependent on how
					// our API is used.
					if (!analyzeMode)
					{
						onto.remove(axiomHandle);
					}
				}
				// B) Cause Prefixes to be removed
				for (Map.Entry<String, String> prefix : onto.getPrefixes().entrySet())
				{
					if (isCancelTask())
						return null;
					// fake progress
					progressTask();
					stats.increaseOtherObjects();
					stats.increaseTotalAtoms();
				}
				if (!analyzeMode)
				{
					onto.setPrefixes(Collections.<String, String> emptyMap());
				}

				// C) Collect Ontology
				HGHandle ontoHandle = graph.getHandle(onto);
				if (analyzeMode)
				{
					// Mark for removal before analysing axioms or entities
					analyzeRemovedSet.add(ontoHandle);
				}
				else
				{
					// TODO how do we make sure subgraph is empty.
					graphRemove(ontoHandle);
				}
				stats.increaseOntologies();
				stats.increaseTotalAtoms();

				return null;
			}
		}, transactionConfig);
		// TRANSACTION END

		// If the task is canceled after removing the onto and during axiom
		// removal,
		// axioms will be found in a collectAxioms run, because they are not
		// members of an ontology anymore.

		// C) Remove axioms, that can be reached through the removed ontology,
		// but are now outside any ontology
		for (OWLAxiom axiom : axioms)
		{
			if (isCancelTask())
				break;
			progressTask();
			HGHandle axiomHandle = graph.getHandle(axiom);
			if (axiomHandle != null)
			{
				// NO, following would remove axiom from graph as of 2011.12.23:
				// onto.applyChange(new RemoveAxiom(onto, axiom));
				// 2. collect enfore zero ontology membership set
				collectAxiomTransact(axiomHandle, stats, analyzeMode, analyzeRemovedSet);
			}
			else
			{
				if (DBG)
					System.out.println(" Axiom was removed already. Class " + axiom.getClass());
			}
		}
	}

	/**
	 * Collects and removes all axioms that do not belong to any ontology. ie.
	 * are not members in any subgraph.
	 */
	private void collectAxioms(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet)
	{
		stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph,
				hg.and(hg.typePlus(OWLAxiomHGDB.class), hg.disconnected(), hg.not(new AnySubgraphMemberCondition(graph))));
		taskProgess = 0;
		taskSize = handlesToRemove.size();
		stopWatch.stop("Disconnected Axiom query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h : handlesToRemove)
		{
			if (isCancelTask())
				break;
			progressTask();
			collectAxiomTransact(h, stats, analyzeMode, analyzeRemovedSet);
		}
		stopWatch.stop("Disconnected Axiom collection time: ");
		System.out.println("Stats now: " + stats.toString());
	}

	/**
	 * Calls collectAxiomInternal within a readonly (analysis) or default (gc
	 * mode) transaction.
	 * 
	 * @param axiomHandle
	 * @param stats
	 * @param analyzeMode
	 */
	private void collectAxiomTransact(final HGHandle axiomHandle, final GarbageCollectorStatistics stats,
			final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet)
	{
		HGTransactionConfig transactionConfig = analyzeMode ? HGTransactionConfig.READONLY : HGTransactionConfig.DEFAULT;
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				collectAxiomInternal(axiomHandle, stats, analyzeMode, analyzeRemovedSet);
				return null;
			}
		}, transactionConfig);
	}

	/**
	 * Removes one axiom and all reachable objects if possible. If you are
	 * deleting an ontology, make sure you remove the axiom from the ontology
	 * before calling this method, as this method expects the axiom not to be a
	 * member in any subgraph.
	 * 
	 * @param axiomHandle
	 * @param stats
	 * @param enforceDisconnected
	 *            causes an exception, if axiom is not disconnected.
	 * @param analyzeMode
	 */
	private void collectAxiomInternal(HGHandle axiomHandle, GarbageCollectorStatistics stats, boolean analyzeMode,
			Set<HGHandle> analyzeRemovedSet)
	{
		int subgraphCount = countSubgraphsWhereAtomIsMember(axiomHandle, analyzeRemovedSet);
		// int maxAllowedSubgraphCount = analyzeMode? 1 : 0;
		if (subgraphCount > 0)
		{
			// the axiom is now a member in an ontology that we are not
			// deleting.
			// do nothing.
			stats.increaseAxiomNotRemovableCases();
		}
		else
		{
			// Remove axiom annotation links and deep remove Annotations!
			List<HGHandle> annoLinkHandles = hg.findAll(graph, hg.and(hg.type(AxiomAnnotatedBy.class), hg.incident(axiomHandle)));
			for (HGHandle annoLinkHandle : annoLinkHandles)
			{
				AxiomAnnotatedBy axAb = graph.get(annoLinkHandle);
				HGHandle annotationHandle = axAb.getTargetAt(1);
				if (!analyzeMode)
				{
					// remove axiom to annotation link
					graphRemove(annoLinkHandle);
				}
				// for the axiom to annotationlink:
				stats.increaseOtherObjects();
				// Deep remove annotation (tree)
				collectOWLObjectsByDFSInternal(annotationHandle, stats, analyzeMode, analyzeRemovedSet);
			}
			// Deep remove axiom (tree)
			collectOWLObjectsByDFSInternal(axiomHandle, stats, analyzeMode, analyzeRemovedSet);
			// stats updated by DFS
		}
	}

	private void collectOWLObjectsByDFSTransact(final HGHandle linkHandle, final GarbageCollectorStatistics stats,
			final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				collectOWLObjectsByDFSInternal(linkHandle, stats, analyzeMode, analyzeRemovedSet);
				return null;
			}
		});
	}

	/**
	 * Everything with an otherwise empty incidence set will be removed. Should
	 * be called within hg transaction; use collectOWLObjectsByDFSTransact.
	 * 
	 * @param linkHandle
	 * @param stats
	 * @param analyzeMode
	 */
	private void collectOWLObjectsByDFSInternal(HGHandle linkHandle, GarbageCollectorStatistics stats, boolean analyzeMode,
			Set<HGHandle> analyzeRemovedSet)
	{
		List<HGHandle> collectibleAtoms = new LinkedList<HGHandle>();
		Set<HGHandle> collectibleAtomsSet = new HashSet<HGHandle>();
		TargetSetALGenerator tsAlg = new TargetSetALGenerator(graph);
		HGDepthFirstTraversal dfs = new HGDepthFirstTraversal(linkHandle, tsAlg);
		int i = 0;
		while (dfs.hasNext())
		{
			Pair<HGHandle, HGHandle> p = dfs.next();
			HGHandle targetHandle = p.getSecond();
			if (DBG)
				printHandle(targetHandle, "" + i, analyzeMode);
			if (maybeCollectAtom(targetHandle, p.getFirst(), collectibleAtoms, collectibleAtomsSet, stats, analyzeMode,
					analyzeRemovedSet))
			{
				// We need to visit the IRI of an entity that DFS would miss,
				// because it is not linked.
				Object target = graph.get(targetHandle);
				if (target instanceof OWLNamedObject)
				{
					OWLNamedObject targetNO = (OWLNamedObject) target;
					HGHandle iriHandle = graph.getHandle(targetNO.getIRI());
					// HGHandle[] layout =
					// graph.getStore().getLink(iriHandle.getPersistent());
					// IRI iri = graph.get(layout[0]);
					// HGHandle iriHandle = layout[0];
					if (DBG)
						printHandle(iriHandle, "" + i, analyzeMode);
					if (iriHandle != null)
					{
						maybeCollectAtom(iriHandle, targetHandle, collectibleAtoms, collectibleAtomsSet, stats, analyzeMode,
								analyzeRemovedSet);
					} // else already deleted.
				}
				// stats were already updated on canRemoveAnalyze
			}
			// if (DBG) System.out.println();
			// if
			// (targetHandle.getPersistent().equals(linkHandle.getPersistent()))
			// linkHandleReturned = true;
			i++;
		}
		// DFS does not return linkHandle, handle it here
		if (DBG)
			printHandle(linkHandle, "top", analyzeMode);
		if (maybeCollectAtom(linkHandle, null, collectibleAtoms, collectibleAtomsSet, stats, analyzeMode, analyzeRemovedSet))
		{
			// We need to visit the IRI of an entity that DFS would miss,
			// because it is not linked.
			Object atom = graph.get(linkHandle);
			if (atom instanceof OWLNamedObject)
			{
				OWLNamedObject atomNO = (OWLNamedObject) atom;
				HGHandle iriHandle = graph.getHandle(atomNO.getIRI());
				// HGHandle[] layout =
				// graph.getStore().getLink(linkHandle.getPersistent());
				// //IRI iri = graph.get(layout[0]);
				// HGHandle iriHandle = layout[0];
				if (DBG)
					printHandle(iriHandle, "IRI" + i, analyzeMode);
				if (iriHandle != null)
				{
					maybeCollectAtom(iriHandle, linkHandle, collectibleAtoms, collectibleAtomsSet, stats, analyzeMode,
							analyzeRemovedSet);
				} // else already deleted.
			}
		}
		// if (DBG) System.out.println();
		if (!analyzeMode && !collectibleAtoms.isEmpty())
		{
			// collect the atoms starting with the top level link.
			collectAtomsReverseOrder(collectibleAtoms);
		}
		dbgCounter++;
		if (dbgCounter % 1000 == 0)
		{
			System.out.println("\n GC: DFS Call Count: " + dbgCounter + " Collectable: " + stats.getTotalAtoms());
			System.out.println(stats.toString());
			System.out.println("Graph atoms: " + repository.getNrOfAtoms());
			stopWatch.stop("Time: ");
		}
	}

	/**
	 * Removes atoms from graph in reverse order, keeping incident atoms.
	 * 
	 * Should be called within a hg transaction.
	 * 
	 * @param collectibleAtoms
	 */
	private void collectAtomsReverseOrder(List<HGHandle> collectibleAtoms)
	{
		ListIterator<HGHandle> it = collectibleAtoms.listIterator(collectibleAtoms.size());
		try
		{
			while (it.hasPrevious())
			{
				HGHandle curAtomHandle = it.previous();
				graphRemove(curAtomHandle);
			}
		}
		catch (RuntimeException e)
		{
			System.err.println("collectAtomsReverseOrder: ");
			ListIterator<HGHandle> it2 = collectibleAtoms.listIterator(collectibleAtoms.size());
			while (it2.hasPrevious())
			{
				HGHandle curAtomHandle = it2.previous();
				System.err.print(curAtomHandle);
				try
				{
					System.err.print((Object)graph.get(curAtomHandle));
				}
				catch (Exception ex)
				{
				}
				try
				{
					System.err.print(graph.get(curAtomHandle).getClass());
				}
				catch (Exception ex)
				{
				}
				System.err.println();
			}
			throw e;
		}
	}

	/**
	 * Determines based on corrected incidence set sizes, whether an atom is
	 * removable or not. It is removable, if the corrected incidence set is
	 * empty.
	 * 
	 * Statistics will be updated based on the atom type accordingly. The atom
	 * will be added to either collectibleAtoms or analyzeRemovedSet
	 * 
	 * @param atomHandle
	 * @param parent
	 *            may be null (e.g. for axioms)
	 * @param collectibleAtoms
	 *            relevant for incidence set size correction during gc mode.
	 * @param collectibleAtomsSet
	 *            a set containing the list handles for performance reasons
	 *            (quick contains)
	 * @param stats
	 * @param analyzeMode
	 *            if true, global analyzeRemovedSet will be relevant for
	 *            incidence set calculation.
	 * @param analyzeRemovedSet
	 *            all atoms that we determined to be removable as we go during
	 *            analyze.
	 * @return
	 */
	private boolean maybeCollectAtom(HGHandle atomHandle, HGHandle parent, List<HGHandle> collectibleAtoms,
			Set<HGHandle> collectibleAtomsSet, GarbageCollectorStatistics stats, boolean analyzeMode,
			Set<HGHandle> analyzeRemovedSet)
	{
		Object atom = graph.get(atomHandle);
		// empty, if we deleted parent already, or only parent => safe to delete
		boolean canRemove = true;
		int incidenceSetSize;
		// Optimize for builtin entities
		if (atom == null)
		{
			if (DBG)
				System.out.println("\n  GC: Atom null for handle: " + atomHandle);// +
																					// " ISSize: "
																					// +
																					// is.size());
			canRemove = false;
		}
		if (canRemove && atom instanceof OWLEntity)
		{
			OWLEntity atomEntity = (OWLEntity) atom;
			canRemove = !atomEntity.isBuiltIn();
			if (DBG)
			{
				if (atomEntity.isBuiltIn())
				{
					System.out.println("GC: Encountered builtin entity during DFS: " + atomEntity + " Class: "
							+ atomEntity.getClass());
				}
			}
		}
		//
		// Determine removability based on incidence set only
		//
		if (canRemove)
		{
			IncidenceSet is = graph.getIncidenceSet(atomHandle);
			if (analyzeMode)
			{
				// Optimize for large incidence sets, if we cannot remove enough
				// objects from is size yet,
				// there is no check necessary
				// -1 for parent object; e.g. is size 2, one analyzed -> need
				// check.; is size 3 -> no check
				if (is.size() - 1 - analyzeRemovedSet.size() > 0)
				{
					incidenceSetSize = is.size();
				}
				else
				{
					// expensive correction
					// we remove those from the incidence set, that we already
					// found plus the current parent.
					incidenceSetSize = calcAnalyzeISSize(is, parent, analyzeRemovedSet);
				}
			}
			else
			{
				// canRemove = (is.isEmpty() || (is.size() == 1 &&
				// (is.first().equals(parent)) || parent == null));
				if (is.size() - 1 - collectibleAtoms.size() > 0)
				{
					incidenceSetSize = is.size();
				}
				else
				{
					// expensive correction
					incidenceSetSize = calcCollectISSize(is, parent, collectibleAtomsSet);
				}
			}
			canRemove = (incidenceSetSize == 0);
			if (DBGX)
			{
				if (!canRemove)
				{
					System.out.println();
					printIncidenceSet(is, parent);
				}
				;
			}
		}
		if (canRemove)
		{
			// incidence set says, we can remove it from graph and we have a
			// loaded atom
			// Analyze the Object and see if we need to revert our decision to
			// remove
			if (atom instanceof OWLOntology)
			{
				stats.increaseTotalAtoms();
				stats.increaseOntologies();
			}
			else if (atom instanceof OWLAxiomHGDB)
			{
				stats.increaseTotalAtoms();
				stats.increaseAxioms();
			}
			else if (atom instanceof OWLEntity)
			{
				OWLEntity entity = (OWLEntity) atom;
				if (entity.isBuiltIn())
				{
					// Don't remove built in entities.
					canRemove = false;
				}
				else
				{
					stats.increaseEntities();
					stats.increaseTotalAtoms();
				}
			}
			else if (atom instanceof IRI)
			{
				IRI iri = (IRI) atom;
				// we'll encounter those here as linked to by Annotations and
				// AnnotationAxioms as
				// an OWLAnnotationValue can be an IRI.
				// Here we know, that the incidence set is empty
				// Check, if other NamedObjects exist that use it. (lookup
				// index)
				if (analyzeMode)
				{
					canRemove = !isUsedByAnyNamedObject(iri, analyzeRemovedSet);
				}
				else
				{
					canRemove = !isUsedByAnyNamedObject(iri, collectibleAtomsSet);
				}
				if (canRemove)
				{
					stats.increaseTotalAtoms();
					stats.increaseIris();
				}
			}
			else if (atom instanceof OWLAnnotationHGDB)
			{
				// Ontology, Axiom, Entity, Sub-Annotations (Links)
				stats.increaseTotalAtoms();
				stats.increaseAnnotations();
			}
			else if (atom instanceof OWLObjectHGDB)
			{
				stats.increaseTotalAtoms();
				stats.increaseOtherObjects();
			}
			else if (atom instanceof SWRLConjuction)
			{
				// SWRLBody or Head
				stats.increaseTotalAtoms();
				stats.increaseOtherObjects();
			}
			else
			{
				System.err.println("GC: Encountered unknown atom during DFS GC: " + atom.getClass() + " Object: " + atom);
				throw new IllegalStateException("GC: Encountered unknown atom during DFS GC: " + atom.getClass());
			}
		}
		// canRemove might have changed.
		if (canRemove)
		{
			if (DBG)
				System.out.print(" > REMOVABLE");
			if (analyzeMode)
			{
				analyzeRemovedSet.add(atomHandle);
			}
			else
			{
				// we maintain two structures, list for correctness, set for
				// performanec
				collectibleAtoms.add(atomHandle);
				collectibleAtomsSet.add(atomHandle);
				// graphRemove(targetHandle);
				if (DBG)
					System.out.print(" > REMOVED ");
			}
			if (DBG)
				System.out.println();
		}
		else
		{
			if (DBG)
				System.out.println(" > KEEP");
		}
		return canRemove;
	}

	/**
	 * Determines, if the IRI is used by any other NamedObject. Uses the
	 * IRIindexers defined by HGDBApplication and ignores users that are about
	 * to be removed.
	 * 
	 * @param iri
	 * @param analyzeRemovedSet
	 * @return true, if the IRI is not used by any other NamedObject.
	 */
	@SuppressWarnings("rawtypes")
	private boolean isUsedByAnyNamedObject(IRI iri, Collection<HGHandle> atomsAboutToBeRemoved)
	{
		for (HGIndexer I : ImplUtils.getIRIIndexers(graph))
		{
			HGRandomAccessResult<Object> iriUsage = graph.getIndexManager().getIndex(I).find(iri);
			while (iriUsage.hasNext())
			{
				HGHandle iriUser = (HGHandle) iriUsage.next();
				if (!(atomsAboutToBeRemoved.contains(iriUser)))
				{
					// we found a namedObject that uses our IRI and is not about
					// to be removed
					iriUsage.close();
					return true;
				} // else we found a namedObject, but it's about to be removed,
					// so we can ignore it
			}
			iriUsage.close();
		}
		return false;
	}

	/**
	 * Calculates incidence set size, by removing those from the actual
	 * incidence set, which were already analyzed and found to be removable. In
	 * addition, a given parent will not be counted. All removable atom handles
	 * are remembered in a HashSet during analysis.
	 * 
	 * @param is
	 *            the current incidence set
	 * @param parent
	 *            may be null
	 * @return
	 */
	private int calcAnalyzeISSize(IncidenceSet is, HGHandle parent, Set<HGHandle> analyzeRemovedSet)
	{
		if (analyzeRemovedSet == null)
			throw new IllegalArgumentException("analyzeRemovedSet == null");
		int i = 0;
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		while (rs.hasNext())
		{
			HGHandle cur = rs.next();
			if (!(analyzeRemovedSet.contains(cur) || cur.equals(parent)))
			{
				i++;
			}
		}
		rs.close();
		return i;
	}

	/**
	 * Calculates incidence set size, by removing those from the actual
	 * incidence set, which were already marked for collection. Removable atom
	 * handles marked during one DFS call are remembered in a LinkedList and
	 * removed instantly after DFS.
	 * 
	 * @param is
	 *            the current incidence set
	 * @return
	 */
	private int calcCollectISSize(IncidenceSet is, HGHandle parent, Set<HGHandle> collectibleAtoms)
	{
		if (collectibleAtoms == null)
			throw new IllegalArgumentException("collectibleAtoms == null");
		int i = 0;
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		while (rs.hasNext())
		{
			HGHandle cur = rs.next();
			if (!(collectibleAtoms.contains(cur) || cur.equals(parent)))
			{
				i++;
			}
		}
		rs.close();
		return i;
	}

	/**
	 * Counts the number of subgraphs a given atom is a member in. Uses
	 * Subgraph.reverseIndex.
	 * 
	 * Should be called within HG Transaction.
	 * 
	 * @param atomHandle
	 * @param analyzeRemovedSet
	 *            may be null
	 * @return >=0
	 */
	private int countSubgraphsWhereAtomIsMember(HGHandle atomHandle, Set<HGHandle> analyzeRemovedSet)
	{
		HGPersistentHandle axiomPersHandle = graph.getPersistentHandle(atomHandle);
		if (axiomPersHandle == null)
			throw new IllegalStateException("Null persistent handle");
		HGIndex<HGPersistentHandle, HGPersistentHandle> indexAxiomToOntologies = HGSubgraph.getReverseIndex(graph);
		HGRandomAccessResult<HGPersistentHandle> rs = indexAxiomToOntologies.find(axiomPersHandle);
		int i = 0;
		try
		{
			while (rs.hasNext())
			{
				HGHandle subgraphHandle = rs.next();
				if (analyzeRemovedSet != null)
				{
					// exclude potentially removed ontologies if we are in
					// analyse mode.
					if (!(analyzeRemovedSet.contains(subgraphHandle)))
					{
						i++;
					}
				}
				else
				{
					i++;
				}
			}
		}
		finally
		{
			rs.close();
		}
		return i;
	}

	// TODO SHOW BORIS more efficient query?
	/**
	 * Collects other OWL objects that are disconnected (subclasses of
	 * OWLObjectHGDB, not Entities, not AxiomsHGDB) and reachable objects.
	 * 
	 * @param stats
	 */
	private void collectOtherObjects(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet)
	{
		stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph,
				hg.and(hg.disconnected(), 
					   hg.typePlus(OWLObjectHGDB.class), 
					   hg.not(hg.typePlus(OWLEntity.class)),
					   hg.not(hg.typePlus(OWLAxiomHGDB.class))));
		taskProgess = 0;
		taskSize = handlesToRemove.size();
		stopWatch.stop("Disconnected Others query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h : handlesToRemove)
		{
//			System.out.println("Removing " + graph.get(h));
			if (isCancelTask())
				break;
			progressTask();
			collectOWLObjectsByDFSTransact(h, stats, analyzeMode, analyzeRemovedSet);
		}
		stopWatch.stop("Disconnected Others collection time: ");
		if (DBG)
			System.out.println("Stats now: " + stats.toString());
	}

	/**
	 * Collects and removes disconnected entities. (Annotations are managed as
	 * AnnotationAssertionAxioms, latter are Ontology members)
	 * 
	 * @param stats
	 */
	private void collectEntities(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet)
	{
		stopWatch.start();
		System.out.println("All " + hg.count(graph, hg.typePlus(OWLEntity.class)));
		System.out.println("All disconnected " + hg.count(graph, hg.and(hg.typePlus(OWLEntity.class), hg.disconnected())));
		System.out.println("All disconnected not builtin "
				+ hg.count(graph, hg.and(hg.typePlus(OWLEntity.class), hg.disconnected(), hg.not(new OWLEntityIsBuiltIn()))));
		System.out.println("All to be collected "
				+ hg.count(
						graph,
						hg.and(hg.typePlus(OWLEntity.class), hg.disconnected(), hg.not(new OWLEntityIsBuiltIn()),
								hg.not(new AnySubgraphMemberCondition(graph)))));

		List<HGHandle> handlesToRemove = hg.findAll(
				graph,
				hg.and(hg.typePlus(OWLEntity.class), hg.disconnected(), hg.not(new OWLEntityIsBuiltIn()),
						hg.not(new AnySubgraphMemberCondition(graph))));
		taskProgess = 0;
		taskSize = handlesToRemove.size();
		stopWatch.stop("Disconnected Entities query time: Found: " + handlesToRemove.size() + " Duration:");
		removeEntitiesTransact(handlesToRemove, stats, analyzeMode, analyzeRemovedSet, 0, handlesToRemove.size());
		// if (successRemoveCounter != handlesToRemove.size()) throw new
		// IllegalStateException("successRemoveCounter != handles.size()");
		// stats.setEntities(stats.getEntities() + successRemoveCounter);
		// stats.setTotalAtoms(stats.getTotalAtoms() + successRemoveCounter);
		stopWatch.stop("Disconnected Entities collection time: ");
		// } else {
		// //stats.setEntities(stats.getEntities() + handlesToRemove.size());
		// //stats.setTotalAtoms(stats.getTotalAtoms() +
		// handlesToRemove.size());
		// }
	}

	private void removeEntitiesTransact(final List<HGHandle> entityHandles, final GarbageCollectorStatistics stats,
			final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet, final int fromIndex, final int toIndex)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<Object>()
		{
			public Object call()
			{
				removeEntitiesInternal(entityHandles, stats, analyzeMode, analyzeRemovedSet, fromIndex, toIndex);
				return null;
			}
		}, HGTransactionConfig.DEFAULT);
	}

	/**
	 * Batch removes entities.
	 * 
	 * Call this only within a hg transaction.
	 * 
	 * @param entityHandle
	 * @return
	 */
	private void removeEntitiesInternal(List<HGHandle> entityHandles, final GarbageCollectorStatistics stats,
			final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet, int fromIndex, int toIndex)
	{
		if (toIndex < fromIndex)
			throw new IllegalArgumentException("to: " + toIndex + " < from: " + fromIndex);
		if (fromIndex < 0)
			throw new IllegalArgumentException("from: " + fromIndex + "< 0");
		if (toIndex > entityHandles.size())
			throw new IllegalArgumentException();
		if (fromIndex > entityHandles.size())
			throw new IllegalArgumentException();

		int i = fromIndex;
		ListIterator<HGHandle> it = entityHandles.listIterator(fromIndex);
		while (i < toIndex)
		{
			if (isCancelTask())
				break;
			progressTask();
			i++;
			HGHandle h = it.next();
			collectOWLObjectsByDFSInternal(h, stats, analyzeMode, analyzeRemovedSet);
		}
	}

	/**
	 * @return the graph
	 */
	public HyperGraph getGraph()
	{
		return graph;
	}

	/**
	 * @return the repository
	 */
	public OntologyDatabase getRepository()
	{
		return repository;
	}

	/**
	 * Removes link, keeping incident links. We currently have a problem with
	 * the implementation of arity after a removenotified in Links.
	 * 
	 * @param atom
	 */
	private boolean graphRemove(HGHandle atom)
	{
		boolean returnValue = false;
		try
		{
			if (DBGX)
			{
				System.out.println("g.remove: " + atom);
			}
			returnValue = graph.remove(atom, true);
		}
		catch (RuntimeException e)
		{
			System.err.println("During remove of: " + atom);
			System.err.println("Remove Exception: " + e);
			System.err.print("Trying to load it: ");
			try
			{
				System.err.println((Object)graph.get(atom));
				System.err.println(graph.get(atom).getClass().toString());
			}
			catch (Exception ex)
			{
				System.out.println("failed ");
			}
			throw e;
		}
		return returnValue;
	}

	//
	// DBG OUTPUT
	//

	/**
	 * @param is
	 * @param parent
	 */
	private void printIncidenceSet(IncidenceSet is, HGHandle parent)
	{
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		int i = 0;
		while (rs.hasNext())
		{
			HGHandle cur = rs.next();
			System.out.print("IS: ");
			if (parent == cur)
				System.out.print("PARENT: ");
			printHandle(cur, "" + i, false);
			System.out.println();
			i++;
		}
		rs.close();
	}

	/**
	 * If not in analyze mode it prints class and handle instead of causing a
	 * toString(), because dependent atoms might be missing at that point.
	 * 
	 * @param h
	 * @param counter
	 * @param analyzeMode
	 */
	private void printHandle(HGHandle h, String counter, boolean analyzeMode)
	{
		Object o = graph.get(h);
		String oclazz = o == null ? "N/A" : o.getClass().getSimpleName();
		// System.out.print("GC: " + counter + " " + o + " C: " + oclazz +
		// " H: " + h);
		String out;
		if (analyzeMode)
		{
			out = "GC: " + counter + " " + o + " C:" + oclazz;
		}
		else
		{
			out = "GC: " + counter + " " + oclazz;
		}
		if (DBGX)
			out += (" H: " + h);
		System.out.print(out);
	}

	private void progressTask()
	{
		if (taskProgess % 100 == 0)
		{
			try
			{
				Thread.sleep(0, 100);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		taskProgess++;
	}

	public void resetTask()
	{
		cancelTask = false;
		taskProgess = 0;
		taskSize = 0;
	}

	public void cancelTask()
	{
		cancelTask = true;
	}

	public boolean isCancelTask()
	{
		return cancelTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.core.HGDBTask#getTaskTotal()
	 */
	@Override
	public int getTaskSize()
	{
		return taskSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.core.HGDBTask#getTaskCurrent()
	 */
	@Override
	public int getTaskProgess()
	{
		return taskProgess;
	}

	// /**
	// * This method determines if an IRI can be removed and
	// * @param iriHandle
	// */
	// private boolean canRemoveIRI(HGHandle iriHandle, boolean analyzeMode, )
	// {
	// for (HGIndexer I : HGDBApplication.getInstance().getIRIIndexers(graph))
	// if (graph.getIndexManager().getIndex(I).count(iriHandle) > 0)
	// return;
	// // IRIs are also used as OWLAnnotationValues, or OWLAnnotationSubjects
	// // In such cases the incidense set of the IRI will contain either a
	// // OWLAnnotationHGDB, OWLAnnotationAssertionAxiomHGDB,
	// OWLAnnotationPropertyDomainAxiomHGDB,
	// // or OWLAnnotationPropertyRangeAxiomHGDB,
	// if (graph.getIncidenceSet(iriHandle).isEmpty) {
	// graph.remove(iriHandle);
	// }
	// }
	//
	// private void scanCleanIRIs()
	// {
	// HGUtils.queryBatchProcess(
	// HGQuery.<HGHandle>make(graph, hg.type(IRI.class)),
	// new Mapping<HGHandle, Boolean>() {
	// public Boolean eval(HGHandle h)
	// {
	// maybeRemoveIRI(h);
	// return true;
	// }
	// },
	// 100,
	// null,
	// 1);
	// }

	// private void removeEntity(HGHandle entityHandle) {
	// HGPersistentHandle entityPHandle = entityHandle.getPersistent();
	// HGHandle[] layout = graph.getStore().getLink(entityPHandle);
	// HGHandle iriHandle = layout[0];
	//
	// }

}
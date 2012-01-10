package org.hypergraphdb.app.owl.gc;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.algorithms.HGDepthFirstTraversal;
import org.hypergraphdb.app.owl.HGDBApplication;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.query.AnySubgraphMemberCondition;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
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
 * <li> Ontologies marked for deletion with all dependent atoms (axioms, et.c)</li>
 * <li> Disconnected axioms, that do not belong to any ontology. </li>
 * <li> Disconnected entities and its IRIs </li>
 * <li> Disconnected other OWLObjects (everything not part of an onto) </li>
 * </ol>
 * <p>
 * As a general rule: 
 * An OWLObject is removable, if
 * <ol>
 * <li> A) Its incidence set can be considered empty.
 * <li> B) Considered means: actual incidence set minus all removable items during processing.
 * <li> C) It's an IRI, has an empty incidence set and is not used in any NamedObject (determined by querying indices). 
 * </ol>
 * </p>
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 * <p>
 * history: 
 * <br> 2012.01.05 hilpold
 * <br>Entity IRIs cannot currently be deleted because of the implementation of OWLNamedObjectType.
 * <br> 2012.01.08 hilpold
 * <br> IRIs will now be deleted. We query the IRI indices to determine removability based on usage in OWLNamedObjectType.
 * </p>
 */
public class GarbageCollector {

	private static final boolean DBG = false;
	private static final boolean DBGX = false;
	
	private StopWatch stopWatch = new StopWatch();
	private int dbgCounter = 0;

	/**
	 * A full GC run entails running:
	 *   1. MODE_DELETED_ONTOLOGIES
	 *   2. MODE_DISCONNECTED_AXIOMS
	 *   3. MODE_DISCONNECTED_ENTITIES
	 *   4. MODE_DISCONNECTED_OTHER
	 *   Those 4 modes are exclusive to each other in the following way:
	 *   The objects deleted by one mode are not deleted by any other mode.
	 */
	public static final int MODE_FULL = 0;

	/**
	 * Begins collection at all ontologies marked for deletion and garbage collects all referenced objects.
	 * Will not collect axioms that are not part of any ontology.
	 * Will not collect disconnected entities that are unreachable by traversing the ontologies.
	 */
	public static final int MODE_DELETED_ONTOLOGIES = 1;

	/**
	 *  Begins collection at all axioms that are not member in any ontology and are disconnected.
	 *  Each axiom, all reachable dependent objects, and entities with an otherwise empty incidence set will be removed.
	 *  A) they were removed from the last ontology in which they were member.
	 *  (The general case is that axioms are exclusive to an ontology; the API user however can add axioms
	 *  that exist in Onto A to Onto B, thereby reusing the axiom and it's dependent objects.
	 */
	public static final int MODE_DISCONNECTED_AXIOMS = 2;
	
	
	/**
	 * Begins collection at all disconnected OWLObjectHGDB and IRI atoms (OWLAnnotationValue), except those implementing OLWEntity or subclasses of OWLAxiomHGDB.
	 * These objects are never member in any ontology.
	 * Each object, all reachable dependent objects, and entities with an otherwise empty incidence set will be removed.
	 * Those include:
	 * - OWLClassExpressionHGDB (not CN, named Class)
	 * - (I) OWLDataRange (not R, named data prop)
	 * - OwlFacetRestrictionHGDB
	 * - OWLLiteralHGBD
	 * - OWLObjectPropertyExpression (not PN, OWLObjectPropery)
	 * - SWRLAtomHGDB
	 * - SWRLIndividualArgument
	 * - SWRLLiteralArgument
	 * - SWRLVariable
	 */
	public static final int MODE_DISCONNECTED_OTHER = 3;
	
	/**
	 *  Begins collection at entities that are not member in any ontology and are not target of any other object.
	 *  No BUILTIN entities will be removed. The IRIs will be removed if possible.
	 */
	public static final int MODE_DISCONNECTED_ENTITIES = 4;

	private HyperGraph graph;
	private HGDBOntologyRepository repository;
	
		
	public GarbageCollector(HGDBOntologyRepository repository) {
		this.repository = repository;
		this.graph = repository.getHyperGraph();
	}


	/**
	 * Run full garbage collection
	 * @return
	 */
	public GarbageCollectorStatistics runGarbageCollection() {
		return runGarbageCollection(MODE_FULL);
	}

	public GarbageCollectorStatistics runGarbageCollection(int mode) {
		GarbageCollectorStatistics stats = runGCInternal(mode, false);
		return stats;
	}

	/**
	 * Analyze what will be removed on a full garbage collection run.
	 * 
	 * @return
	 */
	public GarbageCollectorStatistics runGarbageAnalysis() {
		return runGCInternal(MODE_FULL, true);
	}

	public GarbageCollectorStatistics runGarbageAnalysis(int mode) {
		GarbageCollectorStatistics stats = runGCInternal(mode, true);
		return stats;
	}

	private GarbageCollectorStatistics runGCInternal(int mode, boolean analyzeMode) {
		dbgCounter = 0;
		Set<HGHandle> analyzeRemovedSet = null;
		if (analyzeMode) {		
			analyzeRemovedSet = new HashSet<HGHandle>(estimateCollectableAtoms());
		}
		GarbageCollectorStatistics stats = new GarbageCollectorStatistics();
		switch (mode) {
			case MODE_FULL: {
				collectRemovedOntologies(stats, analyzeMode, analyzeRemovedSet);
				collectAxioms(stats, analyzeMode, analyzeRemovedSet);
				collectOtherObjects(stats, analyzeMode, analyzeRemovedSet);
				collectEntities(stats, analyzeMode, analyzeRemovedSet);
			};break;
			case MODE_DELETED_ONTOLOGIES: {
				collectRemovedOntologies(stats, analyzeMode, analyzeRemovedSet);
			};break;
			case MODE_DISCONNECTED_AXIOMS: {
				collectAxioms(stats, analyzeMode, analyzeRemovedSet);
			};break;
			case MODE_DISCONNECTED_OTHER: {
				collectOtherObjects(stats, analyzeMode, analyzeRemovedSet);
			};break;
			case MODE_DISCONNECTED_ENTITIES: {
				collectEntities(stats, analyzeMode, analyzeRemovedSet);
			};break;
			default: {
				throw new IllegalArgumentException("runGC with unknown mode called: " + mode);
			}
		}
		return stats;
		//removeableAtomsSet released here
	}
	
	/**
	 * Roughly estimated based on total atoms and ontologies vs. deleted ontologies.
	 * 
	 * @return always > 100 and < 1E5
	 */
	private int estimateCollectableAtoms() {
		long atoms = repository.getNrOfAtoms();
		int ontologies = repository.getOntologies().size();
		int deletedOntologies = repository.getDeletedOntologies().size();
		int allOntologies = ontologies + deletedOntologies;
		int estimated = (int)(atoms * (deletedOntologies + 1) / (allOntologies + 1));
		if (estimated < 100) estimated = 100;
		if (estimated > 1E5) estimated = (int)1E5;
		System.out.print("GC: roughly estimated: " + estimated + " collectible atoms for hashsetsize");
		return estimated;
	}

	private void collectRemovedOntologies(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		List<HGDBOntology> delOntos =  repository.getDeletedOntologies();
		int i = 0;
		for (HGDBOntology delOnto : delOntos) {
			i++;
			stopWatch.start();
			collectRemovedOntology(delOnto, stats, analyzeMode, analyzeRemovedSet);
			stopWatch.stop("Ontology collection finished ("+ i + " of " + delOntos.size() + "): ");
			System.out.println("Stats now: " +  stats.toString());
		}
	}

	private void collectRemovedOntology(HGDBOntology onto, GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		//OntologyAnnotations
		//internals.remove does remove anno from onto, NOT graph
		// Ontology Annotations are just added to the ontology, no link.
		Set<OWLAnnotation> annos = onto.getAnnotations();
		for (OWLAnnotation anno : annos) {
			HGHandle annoHandle = graph.getHandle(anno);
			if (!analyzeMode) {
				onto.remove(annoHandle);
				collectOWLObjectsByDFSTransact(annoHandle, stats, analyzeMode, analyzeRemovedSet);
			}
		}
		//TODO wrap import declaration removal inside a transaction.
		//Import declarations
		//internals.remove does remove from onto&graph: ImportDeclarationLink, ImportDeclaration
		Set<OWLImportsDeclaration> importsDeclarations = onto.getImportsDeclarations();
		for (OWLImportsDeclaration importsDeclaration : importsDeclarations) {
			HGHandle importsDeclarationHandle = graph.getHandle(importsDeclaration);
			IncidenceSet is = graph.getIncidenceSet(importsDeclarationHandle);
			if (is.size() != 1) throw new IllegalStateException();
			//remove ImportDeclarationLink
			HGHandle importDeclLinkHandle = is.first();
			//ImportDeclarationLink importDeclLink = graph.get(importDeclLinkHandle);
			if (!analyzeMode) {
				onto.remove(importDeclLinkHandle);
				onto.remove(importsDeclarationHandle);
				graphRemove(importDeclLinkHandle);
				graphRemove(importsDeclarationHandle);
			}
			if (analyzeMode) {
				analyzeRemovedSet.add(importDeclLinkHandle);
				analyzeRemovedSet.add(importsDeclarationHandle);
			}
			stats.increaseOtherObjects();
			stats.increaseOtherObjects();
			stats.increaseTotalAtoms();		
			stats.increaseTotalAtoms();					
		}
		// Retain Axioms and Entities relevant data:
		Set<OWLAxiom> axioms = onto.getAxioms();
		Set<OWLEntity> entities = onto.getSignature();

		// Cancel Membership of entities from ontology as they are onto members, 
		// but don't delete or count as this will be done later during axiom removal.
		if (!analyzeMode) {
			for (OWLEntity entity : entities) {
				HGHandle entityHandle = graph.getHandle(entity);
				onto.remove(entityHandle);
			}
		}
		
		// Collect Ontology
		HGHandle ontoHandle = graph.getHandle(onto);
		if (analyzeMode) {
			//Mark for removal before analysing axioms or entities
			analyzeRemovedSet.add(ontoHandle);
		} else {
			//TODO how do we make sure subgraph is empty.			
			graphRemove(ontoHandle);
		}
		stats.increaseOntologies();
		stats.increaseTotalAtoms();				
		
		for (OWLAxiom axiom : axioms) {
			HGHandle axiomHandle = graph.getHandle(axiom);
			//1. remove axiom from Subgraph, index must be zero now for removal, 
			//unless axiom is also member in other subgraphs/ontologies, which is possible dependent on how our API is used.
			if (!analyzeMode) {
				onto.remove(axiomHandle);
			}
			//NO, following would remove axiom from graph as of 2011.12.23: onto.applyChange(new RemoveAxiom(onto, axiom));
			//2. collect enfore zero ontology membership set 
			collectAxiomTransact(axiomHandle, stats, analyzeMode, analyzeRemovedSet);
		}
	}
	
	/**
	 * Collects and removes all axioms that do not belong to any ontology.
	 * ie. are not members in any subgraph.
	 */
	private void collectAxioms(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		stopWatch.start();		
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLAxiomHGDB.class),
					hg.disconnected(),
					hg.not(new AnySubgraphMemberCondition(graph)))
				);
		stopWatch.stop("Disconnected Axiom query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h: handlesToRemove) {
			collectAxiomTransact(h, stats, analyzeMode, analyzeRemovedSet);
		}
		stopWatch.stop("Disconnected Axiom collection time: ");
		System.out.println("Stats now: " + stats.toString());
	}
	
	/**
	 * Calls collectAxiomInternal within a readonly (analysis) or default (gc mode) transaction.
	 * @param axiomHandle
	 * @param stats
	 * @param analyzeMode
	 */
	private void collectAxiomTransact(final HGHandle axiomHandle, final GarbageCollectorStatistics stats, final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet) {
		HGTransactionConfig transactionConfig = analyzeMode? HGTransactionConfig.READONLY : HGTransactionConfig.DEFAULT;
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				collectAxiomInternal(axiomHandle, stats, analyzeMode, analyzeRemovedSet);
				return null;
			}}, transactionConfig);
	}

	/**
	 * Removes one axiom and all reachable objects if possible.
	 * If you are deleting an ontology, make sure you remove the axiom from the ontology before calling this method,
	 * as this method expects the axiom not to be a member in any subgraph.
	 * 
	 * @param axiomHandle
	 * @param stats
	 * @param enforceDisconnected causes an exception, if axiom is not disconnected.
	 * @param analyzeMode
	 */
	private void collectAxiomInternal(HGHandle axiomHandle, GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		int subgraphCount = countSubgraphsWhereAtomIsMember(axiomHandle, analyzeRemovedSet);
		//int maxAllowedSubgraphCount = analyzeMode? 1 : 0;
		if (subgraphCount > 0) {
			// the axiom is now a member in an ontology that we are not deleting.
			// do nothing.
			stats.increaseAxiomNotRemovableCases();
		} else {
			// Remove axiom annotation links and deep remove Annotations!
			List<HGHandle> annoLinkHandles = hg.findAll(graph,
					hg.and(hg.type(AxiomAnnotatedBy.class), hg.incident(axiomHandle)));
			for (HGHandle annoLinkHandle : annoLinkHandles) {
				AxiomAnnotatedBy axAb = graph.get(annoLinkHandle);
				HGHandle annotationHandle = axAb.getTargetAt(1);						
				if (!analyzeMode) {
					//remove axiom to annotation link		
					graphRemove(annoLinkHandle);
				}
				//for the axiom to annotationlink:
				stats.increaseOtherObjects();
				//Deep remove annotation (tree)
				collectOWLObjectsByDFSInternal(annotationHandle, stats, analyzeMode, analyzeRemovedSet);
			}
			//Deep remove axiom (tree)
			collectOWLObjectsByDFSInternal(axiomHandle, stats, analyzeMode, analyzeRemovedSet);
			// stats updated by DFS
		}
	}

	private void collectOWLObjectsByDFSTransact(final HGHandle linkHandle, final GarbageCollectorStatistics stats, final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet) {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				collectOWLObjectsByDFSInternal(linkHandle, stats, analyzeMode, analyzeRemovedSet);
				return null;
			}});
	}
	
	/**
	 * Everything with an otherwise empty incidence set will be removed.
	 * Should be called within hg transaction; use collectOWLObjectsByDFSTransact.
	 * @param linkHandle
	 * @param stats
	 * @param analyzeMode
	 */
	private void collectOWLObjectsByDFSInternal(HGHandle linkHandle, GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		List<HGHandle> collectibleAtoms = new LinkedList<HGHandle>();
		TargetSetALGenerator tsAlg = new TargetSetALGenerator(graph);
		HGDepthFirstTraversal dfs = new HGDepthFirstTraversal(linkHandle, tsAlg);
		int i = 0;
		while (dfs.hasNext()) {
			Pair<HGHandle, HGHandle> p = dfs.next();
			HGHandle targetHandle = p.getSecond();
			if (DBG) printHandle(targetHandle, "" + i, analyzeMode);
			if (maybeCollectAtom(targetHandle, p.getFirst(), collectibleAtoms, stats, analyzeMode, analyzeRemovedSet)) {
				// We need to visit the IRI of an entity that DFS would miss, because it is not linked.
				Object target = graph.get(targetHandle); 
				if (target instanceof OWLNamedObject) {
					OWLNamedObject targetNO = (OWLNamedObject) target;
					HGHandle iriHandle = graph.getHandle(targetNO.getIRI());
					//HGHandle[] layout = graph.getStore().getLink(iriHandle.getPersistent());
					//IRI iri = graph.get(layout[0]);
					//HGHandle iriHandle = layout[0];
					if (DBG) printHandle(iriHandle, "" + i, analyzeMode);
					if (iriHandle != null) {
						maybeCollectAtom(iriHandle, targetHandle, collectibleAtoms, stats, analyzeMode, analyzeRemovedSet);
					} //else already deleted.
				}
				//stats were already updated on canRemoveAnalyze
			}
			//if (DBG) System.out.println();			
			//if (targetHandle.getPersistent().equals(linkHandle.getPersistent())) linkHandleReturned = true;
			i ++;
		}
		//DFS does not return linkHandle, handle it here
		if (DBG) printHandle(linkHandle, "top", analyzeMode);
		if (maybeCollectAtom(linkHandle, null, collectibleAtoms, stats, analyzeMode, analyzeRemovedSet)) {
			// We need to visit the IRI of an entity that DFS would miss, because it is not linked.
			Object atom = graph.get(linkHandle);
			if (atom instanceof OWLNamedObject) {
				OWLNamedObject atomNO = (OWLNamedObject) atom;
				HGHandle iriHandle = graph.getHandle(atomNO.getIRI());
//				HGHandle[] layout = graph.getStore().getLink(linkHandle.getPersistent());
//				//IRI iri = graph.get(layout[0]);
//				HGHandle iriHandle = layout[0];
				if (DBG) printHandle(iriHandle, "IRI" + i, analyzeMode);
				if (iriHandle != null) {
					maybeCollectAtom(iriHandle, linkHandle, collectibleAtoms, stats, analyzeMode, analyzeRemovedSet);
				} //else already deleted.
			}
		}
		//if (DBG) System.out.println();
		if(!analyzeMode && !collectibleAtoms.isEmpty()) {
			//collect the atoms starting with the top level link.
			collectAtomsReverseOrder(collectibleAtoms);
		}
		dbgCounter ++;
		if (dbgCounter % 500 == 0) {
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
	 * @param collectibleAtoms
	 */
	private void collectAtomsReverseOrder(List<HGHandle> collectibleAtoms) {
		ListIterator<HGHandle> it = collectibleAtoms.listIterator(collectibleAtoms.size());
		while (it.hasPrevious()) {
			HGHandle curAtomHandle = it.previous();
			graphRemove(curAtomHandle);
		}
	}
	
	/**
	 * Determines based on corrected incidence set sizes, whether an atom is removable or not.
	 * It is removable, if the corrected incidence set is empty.
	 * 
	 * Statistics will be updated based on the atom type accordingly. 
	 * The atom will be added to either collectibleAtoms or analyzeRemovedSet
	 * @param atomHandle
	 * @param parent may be null (e.g. for axioms)
	 * @param collectibleAtoms relevant for incidence set size correction during gc mode.
	 * @param stats 
	 * @param analyzeMode if true, global analyzeRemovedSet will be relevant for incidence set calculation. 
	 * @param analyzeRemovedSet all atoms that we determined to be removable as we go during analyze. 
	 * @return
	 */
	private boolean maybeCollectAtom(HGHandle atomHandle, 
			HGHandle parent, 
			List<HGHandle> collectibleAtoms,
			GarbageCollectorStatistics stats, 
			boolean analyzeMode,
			Set<HGHandle> analyzeRemovedSet) {
		Object atom = graph.get(atomHandle);
		//empty, if we deleted parent already, or only parent => safe to delete		
		boolean canRemove = true;
		int incidenceSetSize;
		//Optimize for builtin entities
		if (atom == null) {
			if (DBG) System.out.println("\n  GC: Atom null for handle: " + atomHandle);// + " ISSize: " + is.size());
			canRemove = false;
		}
		if (canRemove && atom instanceof OWLEntity) {
			OWLEntity atomEntity = (OWLEntity)atom;
			canRemove = !atomEntity.isBuiltIn();
			if (DBG && atomEntity.isBuiltIn()) { 
				System.out.println("GC: Encountered builtin entity during DFS: " + atomEntity + " Class: " + atomEntity.getClass());
			}
		}
		//
		// Determine removability based on incidence set only
		//
		if (canRemove) {
			IncidenceSet is = graph.getIncidenceSet(atomHandle);
			if (analyzeMode) {
				// Optimize for large incidence sets, if we cannot remove enough objects from is size yet,
				// there is no check necessary
				// -1 for parent object; e.g. is size 2, one analyzed -> need check.; is size 3 -> no check
				if (is.size() - 1 - analyzeRemovedSet.size() > 0) {
					incidenceSetSize = is.size();
				} else {
					// expensive correction
					// we remove those from the incidence set, that we already found plus the current parent.
					incidenceSetSize = calcAnalyzeISSize(is, parent, analyzeRemovedSet);
				}
			} else {
				//canRemove = (is.isEmpty() || (is.size() == 1 && (is.first().equals(parent)) || parent == null));
				if (is.size() - 1 - collectibleAtoms.size() > 0) {
					incidenceSetSize = is.size();
				} else {
					//expensive correction
					incidenceSetSize = calcCollectISSize(is, parent, collectibleAtoms);
				}
			}
			canRemove = (incidenceSetSize == 0);
			if (DBGX) {
				if (!canRemove) { System.out.println(); printIncidenceSet(is, parent); };
			}
		}
		if (canRemove) {
			// incidence set says, we can remove it from graph and we have a loaded atom
			// Analyze the Object and see if we need to revert our decision to remove
			if (atom instanceof OWLOntology) {
				stats.increaseTotalAtoms();
				stats.increaseOntologies();
			} else if (atom instanceof OWLAxiomHGDB) {	
				stats.increaseTotalAtoms();
				stats.increaseAxioms();
			} else if (atom instanceof OWLEntity) {
				OWLEntity entity = (OWLEntity) atom;
				if (entity.isBuiltIn()) {
					// Don't remove built in entities.
					canRemove = false;
				} else {
					stats.increaseEntities();
					stats.increaseTotalAtoms();
				}
			} else if (atom instanceof IRI) {
				IRI iri = (IRI) atom;
				// we'll encounter those here as linked to by Annotations and AnnotationAxioms as
				// an OWLAnnotationValue can be an IRI.
				// Here we know, that the incidence set is empty
				// Check, if other NamedObjects exist that use it. (lookup index)
				if (analyzeMode) {
					canRemove = !isUsedByAnyNamedObject(iri, analyzeRemovedSet);
				} else {
					canRemove = !isUsedByAnyNamedObject(iri, collectibleAtoms); 
				}
				if (canRemove) {
					stats.increaseTotalAtoms();
					stats.increaseIris();
				}
			} else if (atom instanceof OWLAnnotationHGDB) {
				// Ontology, Axiom, Entity, Sub-Annotations (Links)
				stats.increaseTotalAtoms();
				stats.increaseAnnotations();
			} else if (atom instanceof OWLObjectHGDB) {
				stats.increaseTotalAtoms();
				stats.increaseOtherObjects();
			} else {
				System.err.println("GC: Encountered unknown atom during DFS GC: " +  atom.getClass() + " Object: " + atom);
				throw new IllegalStateException("GC: Encountered unknown atom during DFS GC: " +  atom.getClass());
			}
		}
		// canRemove might have changed.
		if (canRemove) {
			if (DBG) System.out.print(" > REMOVABLE");
			if (analyzeMode) {
				analyzeRemovedSet.add(atomHandle);
			} else {
				collectibleAtoms.add(atomHandle);
				// graphRemove(targetHandle);				
				if (DBG) System.out.print(" > REMOVED ");
			}
			if (DBG) System.out.println();
		} else {
			if (DBG) System.out.println(" > KEEP");
		}
		return canRemove;	
	}
	
	/**
	 * Determines, if the IRI is used by any other NamedObject.
	 * Uses the IRIindexers defined by HGDBApplication and ignores users that are about to be removed. 
	 * 
	 * @param iri
	 * @param analyzeRemovedSet
	 * @return true, if the IRI is not used by any other NamedObject.
	 */
	private boolean isUsedByAnyNamedObject(IRI iri, Collection<HGHandle> atomsAboutToBeRemoved) {
		for (HGIndexer I : HGDBApplication.getInstance().getIRIIndexers(graph)) {
			 HGRandomAccessResult<Object>  iriUsage = graph.getIndexManager().getIndex(I).find(iri);
			 while(iriUsage.hasNext()) {
				 HGHandle iriUser = (HGHandle)iriUsage.next();
				 if (!(atomsAboutToBeRemoved.contains(iriUser))) {
					 // we found a namedObject that uses our IRI and is not about to be removed 
					 iriUsage.close();
					 return true;
				 }  // else we found a namedObject, but it's about to be removed, so we can ignore it 
			 }
			 iriUsage.close();
		}
		return false;
	}

	/**
	 * Calculates incidence set size, by removing those from the actual incidence set, which were already analyzed and found to be removable.
	 * In addition, a given parent will not be counted.
	 * All removable atom handles are remembered in a HashSet during analysis.
	 * 
	 * @param is the current incidence set
	 * @param parent may be null
	 * @return
	 */
	private int calcAnalyzeISSize(IncidenceSet is, HGHandle parent, Set<HGHandle> analyzeRemovedSet) {
		if (analyzeRemovedSet == null) throw new IllegalArgumentException("analyzeRemovedSet == null");
		int i = 0;
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		while (rs.hasNext()) {
			HGHandle cur = rs.next();
			if (!(analyzeRemovedSet.contains(cur) || cur.equals(parent))) {
				i ++;
			} 
		}
		rs.close();
		return i;
	}

	/**
	 * Calculates incidence set size, by removing those from the actual incidence set, which were already marked for collection.
	 * Removable atom handles marked during one DFS call are remembered in a LinkedList and removed instantly after DFS.
	 * 
	 * @param is the current incidence set
	 * @return
	 */
	private int calcCollectISSize(IncidenceSet is, HGHandle parent, List<HGHandle> collectibleAtoms) {
		if (collectibleAtoms == null) throw new IllegalArgumentException("collectibleAtoms == null");
		int i = 0;
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		while (rs.hasNext()) {
			HGHandle cur = rs.next();
			if (!(collectibleAtoms.contains(cur) || cur.equals(parent))) {
				i ++;
			} 
		}
		rs.close();
		return i;
	}


	/**
	 * Counts the number of subgraphs a given atom is a member in. 
	 * Uses Subgraph.reverseIndex.
	 * 
	 * Should be called within HG Transaction.
	 * 
	 * @param atomHandle
	 * @param analyzeRemovedSet may be null
	 * @return >=0
	 */
	private int countSubgraphsWhereAtomIsMember(HGHandle atomHandle, Set<HGHandle> analyzeRemovedSet) {
		HGPersistentHandle axiomPersHandle = graph.getPersistentHandle(atomHandle);
		if (axiomPersHandle == null) throw new IllegalStateException("Null persistent handle");
		HGIndex<HGPersistentHandle,HGPersistentHandle> indexAxiomToOntologies = HGSubgraph.getReverseIndex(graph);
		HGRandomAccessResult<HGPersistentHandle> rs = indexAxiomToOntologies.find(axiomPersHandle);
		int i = 0;
		try {
			while (rs.hasNext()) {
				HGHandle subgraphHandle = rs.next();
				if (analyzeRemovedSet !=null) {
					// exclude potentially removed ontologies if we are in analyse mode.
					if (!(analyzeRemovedSet.contains(subgraphHandle))) {
						i++;
					}
				} else { 
					i++;
				}
			}
		} finally {
			rs.close();			
		}
		return i;
	}
	
	//TODO SHOW BORIS more efficient query?
	/**
	 * Collects other OWL objects that are disconnected (subclasses of OWLObjectHGDB, not Entities, not AxiomsHGDB) and reachable objects.
	 * 
	 * @param stats
	 */
	private void collectOtherObjects(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		if (DBG) stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
				hg.disconnected(),
				hg.typePlus(OWLObjectHGDB.class),
				hg.not(hg.typePlus(OWLEntity.class)),
				hg.not(hg.typePlus(OWLAxiomHGDB.class)))
			);
		if (DBG) stopWatch.stop("Disconnected Others query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h: handlesToRemove) {
			collectOWLObjectsByDFSTransact(h, stats, analyzeMode, analyzeRemovedSet);
		}
		if (DBG) stopWatch.stop("Disconnected Others collection time: ");
		if (DBG) System.out.println("Stats now: " + stats.toString());		
	}
	
	/**
	 * Collects and removes disconnected entities.
	 * (Annotations are managed as AnnotationAssertionAxioms, latter are Ontology members)
	 * @param stats
	 */
	private void collectEntities(GarbageCollectorStatistics stats, boolean analyzeMode, Set<HGHandle> analyzeRemovedSet) {
		if (DBG) stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLEntity.class),
					hg.disconnected(),
					hg.not(new OWLEntityIsBuiltIn()),
					hg.not(new AnySubgraphMemberCondition(graph)))
					);
		if (DBG) stopWatch.stop("Disconnected Entities query time: Found: " + handlesToRemove.size() + " Duration:");
		removeEntitiesTransact(handlesToRemove, stats, analyzeMode, analyzeRemovedSet, 0, handlesToRemove.size());
			//if (successRemoveCounter != handlesToRemove.size()) throw new IllegalStateException("successRemoveCounter != handles.size()");
			//stats.setEntities(stats.getEntities() + successRemoveCounter);
			//stats.setTotalAtoms(stats.getTotalAtoms() + successRemoveCounter);
		if (DBG) stopWatch.stop("Disconnected Entities collection time: ");
//		} else {
//			//stats.setEntities(stats.getEntities() + handlesToRemove.size());
//			//stats.setTotalAtoms(stats.getTotalAtoms() + handlesToRemove.size());
//		}
	}	

	private void removeEntitiesTransact(final List<HGHandle> entityHandles, final GarbageCollectorStatistics stats, final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet, final int fromIndex, final int toIndex) {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				removeEntitiesInternal(entityHandles, stats, analyzeMode, analyzeRemovedSet, fromIndex, toIndex);
				return null;
			}}, HGTransactionConfig.DEFAULT);
	}
	
	/**
	 * Batch removes entities.
	 * 
	 * Call this only within a hg transaction.
	 * 
	 * @param entityHandle
	 * @return
	 */
	private void removeEntitiesInternal(List<HGHandle> entityHandles, final GarbageCollectorStatistics stats, final boolean analyzeMode, final Set<HGHandle> analyzeRemovedSet, int fromIndex, int toIndex) {
		if (toIndex < fromIndex) throw new IllegalArgumentException("to: " + toIndex + " < from: " + fromIndex); 
		if (fromIndex < 0) throw new IllegalArgumentException("from: " + fromIndex + "< 0");
		if (toIndex  > entityHandles.size()) throw new IllegalArgumentException();
		if (fromIndex > entityHandles.size()) throw new IllegalArgumentException();
		
		int i = fromIndex;
		ListIterator<HGHandle> it = entityHandles.listIterator(fromIndex);
		while (i < toIndex) {
			i++;
			HGHandle h = it.next();
			collectOWLObjectsByDFSInternal(h, stats, analyzeMode, analyzeRemovedSet);
		}
	}
		
	/**
	 * @return the graph
	 */
	public HyperGraph getGraph() {
		return graph;
	}

	/**
	 * @return the repository
	 */
	public HGDBOntologyRepository getRepository() {
		return repository;
	}
	
	/**
	 * Removes link, keeping incident links.
	 * We currently have a problem with the implementation of arity after a removenotified in Links.
	 * @param atom
	 */
	private boolean graphRemove(HGHandle atom) {
		boolean returnValue = false;
		try {
		if (DBGX) {
			System.out.println("g.remove: " + atom);
		}
			returnValue = graph.remove(atom, true);
		} catch (RuntimeException e) {
			System.out.println("During remove of: " + atom);
			System.out.println("Remove Exception: " + e);
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
	private void printIncidenceSet(IncidenceSet is, HGHandle parent) {
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		int i = 0;
		while(rs.hasNext()) {
			HGHandle cur = rs.next();
			System.out.print("IS: ");
			if (parent == cur) System.out.print("PARENT: ");
			printHandle(cur, "" + i, false); System.out.println();
			i++;
		}
		rs.close();		
	}

	/**
	 * If not in analyze mode it prints class and handle instead of causing a toString(), 
	 * because dependent atoms might be missing at that point.  
	 * @param h
	 * @param counter
	 * @param analyzeMode
	 */
	private void printHandle(HGHandle h, String counter, boolean analyzeMode) {
		Object o = graph.get(h);
		String oclazz = o==null? "N/A" : o.getClass().getSimpleName();		
		//System.out.print("GC: " + counter + " " + o + " C: " + oclazz + " H: " + h);
		String out;
		if (analyzeMode) {
			out = "GC: " + counter + " " + o + " C:" + oclazz;			
		} else {
			out = "GC: " + counter + " " + oclazz;						
		}
		if (DBGX) out += (" H: " + h);
		System.out.print(out);
	}

//	/**
//	 * This method determines if an IRI can be removed and 
//	 * @param iriHandle
//	 */
//	private boolean canRemoveIRI(HGHandle iriHandle, boolean analyzeMode, )
//	{			
//		for (HGIndexer I : HGDBApplication.getInstance().getIRIIndexers(graph))
//			if (graph.getIndexManager().getIndex(I).count(iriHandle) > 0)
//				return;
//		// IRIs are also used as OWLAnnotationValues, or OWLAnnotationSubjects
//		// In such cases the incidense set of the IRI will contain either a
//		// OWLAnnotationHGDB, OWLAnnotationAssertionAxiomHGDB, OWLAnnotationPropertyDomainAxiomHGDB,
//		// or OWLAnnotationPropertyRangeAxiomHGDB,
//		if (graph.getIncidenceSet(iriHandle).size() == 0) { 
//			graph.remove(iriHandle);
//		}
//	}
//	
//	private void scanCleanIRIs()
//	{ 
//		HGUtils.queryBatchProcess(
//				HGQuery.<HGHandle>make(graph, hg.type(IRI.class)), 
//				new Mapping<HGHandle, Boolean>() {
//					public Boolean eval(HGHandle h)
//					{
//						maybeRemoveIRI(h);
//						return true;
//					}
//				}, 
//				100, 
//				null, 
//				1);		
//	}
	
//	private void removeEntity(HGHandle entityHandle) {
//		HGPersistentHandle entityPHandle = entityHandle.getPersistent();
//		HGHandle[] layout = graph.getStore().getLink(entityPHandle);
//		HGHandle iriHandle = layout[0];
//		
//	}
	
}
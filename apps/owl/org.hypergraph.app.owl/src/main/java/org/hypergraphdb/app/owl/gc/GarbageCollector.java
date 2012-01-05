package org.hypergraphdb.app.owl.gc;

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
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.query.AnySubgraphMemberCondition;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.hypergraphdb.app.owl.type.link.ImportDeclarationLink;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.hypergraphdb.app.owl.util.TargetSetALGenerator;
import org.hypergraphdb.atom.HGSubgraph;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * GarbageCollector collects unused OWL related atoms in the graph. Such as 
 * <ol>
 * <li> Ontologies marked for deletion with all dependent atoms (axioms, et.c)</li>
 * <li> Disconnected axioms, that do not belong to any ontology. </li>
 * <li> Disconnected entities </li>
 * <li> Disconnected other OWLObjects (everything not part of an onto) </li>
 * </ol>
 * <p>
 * As a general rule: 
 * An OWLObject is removable, if
 * <ol>
 * <li> A) Its incidence set can be considered empty.
 * <li> B) Considered means: actual incidence set minus all removable items during processing. 
 * </ol>
 * </p>
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 * <p>
 * history: 
 * <br> 2012.01.05 hilpold
 * <br>Entity IRIs cannot currently be deleted because of the implementation of OWLNamedObjectType.
 * </p>
 */
public class GarbageCollector {
	
	public StopWatch stopWatch = new StopWatch();

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
	 *  A) they were created by DF and never added to an onto
	 *  B) they were removed from the last ontology in which they were member.
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
	 *  No BUILTIN entities will be removed.
	 */
	public static final int MODE_DISCONNECTED_ENTITIES = 4;

	private static final boolean DBG = true;

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
	public GarbageCollectorStatistics runGC() {
		return runGC(MODE_FULL);
	}

	public GarbageCollectorStatistics runGC(int mode) {
		GarbageCollectorStatistics stats = runGCInternal(mode, false);
		return stats;
	}

	/**
	 * Analyze what will be removed on a full garbage collection run.
	 * 
	 * @return
	 */
	public GarbageCollectorStatistics analyze() {
		return runGCInternal(MODE_FULL, true);
	}

	public GarbageCollectorStatistics analyze(int mode) {
		GarbageCollectorStatistics stats = runGCInternal(mode, true);
		return stats;
	}

	protected GarbageCollectorStatistics runGCInternal(int mode, boolean analyzeOnly) {
		Set<HGHandle> removableAtomsSet = null;
		if (analyzeOnly) {		
			removableAtomsSet = new HashSet<HGHandle>(estimateCollectableAtoms());
		}
		GarbageCollectorStatistics stats = new GarbageCollectorStatistics();
		switch (mode) {
			case MODE_FULL: {
				collectRemovedOntologies(stats, analyzeOnly, removableAtomsSet);
				collectAxioms(stats, analyzeOnly, removableAtomsSet);
				collectOtherObjects(stats, analyzeOnly, removableAtomsSet);
				collectEntities(stats, analyzeOnly);
			};break;
			case MODE_DELETED_ONTOLOGIES: {
				collectRemovedOntologies(stats, analyzeOnly, removableAtomsSet);
			};break;
			case MODE_DISCONNECTED_AXIOMS: {
				collectAxioms(stats, analyzeOnly, removableAtomsSet);
			};break;
			case MODE_DISCONNECTED_OTHER: {
				collectOtherObjects(stats, analyzeOnly, removableAtomsSet);
			};break;
			case MODE_DISCONNECTED_ENTITIES: {
				collectEntities(stats, analyzeOnly);
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
	protected int estimateCollectableAtoms() {
		long atoms = repository.getNrOfAtoms();
		int ontologies = repository.getOntologies().size();
		int deletedOntologies = repository.getDeletedOntologies().size();
		int allOntologies = ontologies + deletedOntologies;
		int estimated = (int)(atoms * (deletedOntologies + 1) / (allOntologies + 1));
		if (estimated < 100) estimated = 100;
		if (estimated > 1E5) estimated = (int)1E5;
		return estimated;
	}

	protected void collectRemovedOntologies(GarbageCollectorStatistics stats, boolean analyzeOnly, Set<HGHandle> removableAtomsSet) {
		List<HGDBOntology> delOntos =  repository.getDeletedOntologies();
		int i = 0;
		for (HGDBOntology delOnto : delOntos) {
			i++;
			if (DBG) stopWatch.start();
			collectRemovedOntology(delOnto, stats, analyzeOnly, removableAtomsSet);
			if (DBG) {
				stopWatch.stop("Ontology collection ("+ i + " of " + delOntos.size() + "): ");
				System.out.println("Stats now: " +  stats.toString());
			}
		}
		// 
	}

	protected void collectRemovedOntology(HGDBOntology onto, GarbageCollectorStatistics stats, boolean analyzeOnly, Set<HGHandle> removableAtomsSet) {
		//OntologyAnnotations
		//internals.remove does remove anno from onto, NOT graph
		// Ontology Annotations are just added to the ontology, no link.
		Set<OWLAnnotation> annos = onto.getAnnotations();
		for (OWLAnnotation anno : annos) {
			HGHandle annoHandle = graph.getHandle(anno);
			if (!analyzeOnly) {
				onto.remove(annoHandle);
				collectOWLObjectsByDFSTransact(annoHandle, stats, analyzeOnly, removableAtomsSet);
			}
			//removableAtomsSet.add(annoHandle);
			//stats.increaseOtherObjects();
			//stats.increaseTotalAtoms();					
		}
		//Import declarations
		//internals.remove does remove from onto&graph: ImportDeclarationLink, ImportDeclaration
		Set<OWLImportsDeclaration> importsDeclarations = onto.getImportsDeclarations();
		for (OWLImportsDeclaration importsDeclaration : importsDeclarations) {
			HGHandle importsDeclarationHandle = graph.getHandle(importsDeclaration);
			IncidenceSet is = graph.getIncidenceSet(importsDeclarationHandle);
			if (is.size() != 1) throw new IllegalStateException();
			//remove ImportDeclarationLink
			HGHandle importDeclLinkHandle = is.first();
			ImportDeclarationLink importDeclLink = graph.get(importDeclLinkHandle);
			if (!analyzeOnly) {
				onto.remove(importDeclLinkHandle);
				onto.remove(importsDeclarationHandle);
				graphRemove(importDeclLinkHandle);
				graphRemove(importsDeclarationHandle);
			}
			removableAtomsSet.add(importDeclLinkHandle);
			removableAtomsSet.add(importsDeclarationHandle);
			stats.increaseOtherObjects();
			stats.increaseOtherObjects();
			stats.increaseTotalAtoms();		
			stats.increaseTotalAtoms();					
		}
		
		//collect Axioms
		Set<OWLAxiom> axioms = onto.getAxioms();
		for (OWLAxiom axiom : axioms) {
			HGHandle axiomHandle = graph.getHandle(axiom);
			//1. remove axiom from Subgraph, index must be zero now for removal, 
			//unless axiom is also member in other subgraphs/ontologies, which is possible dependent on how our API is used.
			if (!analyzeOnly) {
				onto.remove(axiomHandle);
			}
			//NO, following would remove axiom from graph as of 2011.12.23: onto.applyChange(new RemoveAxiom(onto, axiom));
			//2. collect enfore zero incidence set, if not analyze, because we removed our onto from axiom incidence set in step 1. 
			collectAxiomInternal(axiomHandle, stats, analyzeOnly, removableAtomsSet);
		}
//		//collect Ontology
		HGHandle ontoHandle = graph.getHandle(onto);
		IncidenceSet ontoIS = graph.getIncidenceSet(ontoHandle);
		if (!ontoIS.isEmpty()) throw new IllegalStateException("GC: Ontology incidence set must be empty now, had size: " + ontoIS.size());
//		System.out.println("Onto incidence set after removing content: (expected empty)");
//		int i = 0;
//		for (HGHandle h : ontoIS) {
//			i++;
//			System.out.println(" " + i + " " + graph.get(h).toString());
//		}
//		System.out.println("Onto incidence set END");
		if (!analyzeOnly) {
			//TODO What happens to the indices in onto.
			//how do we make sure subgraph is empty.			
			graphRemove(ontoHandle);
		}
		removableAtomsSet.add(ontoHandle);
		stats.increaseOntologies();
		stats.increaseTotalAtoms();				
	}
	
	/**
	 * Collects and removes all axioms that do not belong to any ontology.
	 * ie. are not members in any subgraph.
	 */
	protected void collectAxioms(GarbageCollectorStatistics stats, boolean analyzeOnly, Set<HGHandle> removableAtomsSet) {
		stopWatch.start();		
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLAxiomHGDB.class),
					hg.disconnected(),
					hg.not(new AnySubgraphMemberCondition(graph)))
				);
		stopWatch.stop("Disconnected Axiom query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h: handlesToRemove) {
			collectAxiomTransact(h, stats, analyzeOnly, removableAtomsSet);
		}
		stopWatch.stop("Disconnected Axiom collection time: ");
		System.out.println("Stats now: " + stats.toString());
	}
	
	/**
	 * Removes one axiom and all reachable objects if possible.
	 * If you are deleting an ontology, make sure you remove the axiom from the ontology before calling this method,
	 * as this method expects the axiom not to be a member in any subgraph.
	 * 
	 * @param axiomHandle
	 * @param stats
	 * @param enforceDisconnected causes an exception, if axiom is not disconnected.
	 * @param analyzeOnly
	 */
	protected void collectAxiomInternal(HGHandle axiomHandle, GarbageCollectorStatistics stats, boolean analyzeOnly, Set<HGHandle> removableAtomsSet) {
		int subgraphCount = countSubgraphsWhereAtomIsMember(axiomHandle, removableAtomsSet);
		int maxAllowedSubgraphCount = analyzeOnly? 1 : 0;
		if (subgraphCount > maxAllowedSubgraphCount) {
			//
			stats.increaseAxiomNotRemovableCases();
		} else {
			// Remove axiom annotation links and deep remove Annotations!
			List<HGHandle> annoLinkHandles = hg.findAll(graph,
					hg.and(hg.type(AxiomAnnotatedBy.class), hg.incident(axiomHandle)));
			for (HGHandle annoLinkHandle : annoLinkHandles) {
				AxiomAnnotatedBy axAb = graph.get(annoLinkHandle);
				HGHandle annotationHandle = axAb.getTargetAt(1);						
				if (!analyzeOnly) {
					//remove axiom to annotation link		
					graphRemove(annoLinkHandle);
				}
				//for the axiom to annotationlink:
				stats.increaseOtherObjects();
				
				//Deep remove annotation (tree)
				collectOWLObjectsByDFSInternal(annotationHandle, stats, analyzeOnly, removableAtomsSet);
			}
			
			//Deep remove axiom (tree)
			collectOWLObjectsByDFSInternal(axiomHandle, stats, analyzeOnly, removableAtomsSet);
			// stats updated by DFS
		}
	}

	/**
	 * Collects 
	 * @param axiomHandle
	 * @param stats
	 * @param analyzeOnly
	 */
	protected void collectAxiomTransact(final HGHandle axiomHandle, final GarbageCollectorStatistics stats, final boolean analyzeOnly, final Set<HGHandle> removableAtomsSet) {
		HGTransactionConfig transactionConfig = analyzeOnly? HGTransactionConfig.READONLY : HGTransactionConfig.DEFAULT;
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				collectAxiomInternal(axiomHandle, stats, analyzeOnly, removableAtomsSet);
				return null;
			}}, transactionConfig);
	}

	
	protected void collectOWLObjectsByDFSTransact(final HGHandle linkHandle, final GarbageCollectorStatistics stats, final boolean analyzeOnly, final Set<HGHandle> removableAtomsSet) {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				collectOWLObjectsByDFSInternal(linkHandle, stats, analyzeOnly, removableAtomsSet);
				return null;
			}});
	}
	
	/**
	 * Everything with an otherwise empty incidence set will be removed.
	 * Should be called within hg transaction; use collectOWLObjectsByDFSTransact.
	 * @param linkHandle
	 * @param stats
	 * @param analyzeOnly
	 */
	protected void collectOWLObjectsByDFSInternal(HGHandle linkHandle, GarbageCollectorStatistics stats, boolean analyzeOnly, Set<HGHandle> removableAtomsSet) {
		List<HGHandle> collectableAtoms = new LinkedList<HGHandle>();
		TargetSetALGenerator tsAlg = new TargetSetALGenerator(graph);
		HGDepthFirstTraversal dfs = new HGDepthFirstTraversal(linkHandle, tsAlg);
		boolean linkHandleReturned = false;
		int i = 0;
		while (dfs.hasNext()) {
			Pair<HGHandle, HGHandle> p = dfs.next();
			HGHandle targetHandle = p.getSecond();
			if (DBG) {
				printHandle(targetHandle, i, analyzeOnly);
			}
			if (canRemoveAnalyze(targetHandle, p.getFirst(), collectableAtoms, stats, analyzeOnly, removableAtomsSet)) {
				if (analyzeOnly) removableAtomsSet.add(linkHandle);							
				if (DBG) System.out.print(" > REMOVABLE");
				if (!analyzeOnly) {
					collectableAtoms.add(targetHandle);
					//graphRemove(targetHandle);				
					if (DBG) System.out.print(" > REMOVED ");
				}
				//stats were already updated on canRemoveAnalyze
			}
			if (DBG) System.out.println();			
			if (targetHandle.getPersistent().equals(linkHandle.getPersistent())) linkHandleReturned = true;
			i ++;
		}
		if (linkHandleReturned) System.out.println("I GOT THE LINK HANDLE FROM DFS");// throw new IllegalStateException("Error during traversal");
		//DFS does not return linkHandle!
		if (DBG) {
			printHandle(linkHandle, -1, analyzeOnly);
		}
		if (canRemoveAnalyze(linkHandle, null, collectableAtoms, stats, analyzeOnly, removableAtomsSet)) {
			if (analyzeOnly) removableAtomsSet.add(linkHandle);			
			if (DBG) {
				System.out.print(" > REMOVABLE ");
			}
			if (!analyzeOnly) {
				collectableAtoms.add(linkHandle);
				//graphRemove(linkHandle);
				if (DBG) {
					System.out.println(" > REMOVED ");
				}
			}
		}
		if (DBG) {
			System.out.println();
		}
		if(!analyzeOnly && !collectableAtoms.isEmpty()) {
			//collect the atoms starting with the top level link.
			collectAtomsReverseOrder(collectableAtoms);
		}
	}

	/**
	 * Removes atoms from graph in reverse order, keeping incident atoms.
	 *  
	 * Should be called within a hg transaction.
	 * @param collectableAtoms
	 */
	private void collectAtomsReverseOrder(List<HGHandle> collectableAtoms) {
		ListIterator<HGHandle> it = collectableAtoms.listIterator(collectableAtoms.size());
		while (it.hasPrevious()) {
			HGHandle curAtomHandle = it.previous();
			graphRemove(curAtomHandle);
		}
	}


	/**
	 * If not in analyze mode it prints class and handle instead of causing a toString(), 
	 * because dependent atoms might be missing at that point.  
	 * @param h
	 * @param counter
	 * @param analyzeOnly
	 */
	protected void printHandle(HGHandle h, int counter, boolean analyzeOnly) {
		Object o = graph.get(h);
		String oclazz = o==null? "N/A" : o.getClass().getSimpleName();		
		//System.out.print("GC: " + counter + " " + o + " C: " + oclazz + " H: " + h);
		if (analyzeOnly) {
			System.out.print("GC: " + counter + " " + o + " H: " + h);			
		} else {
			System.out.print("GC: " + counter + " " + oclazz + " H: " + h);						
		}
	}
	
	/**
	 * Determines based on corrected incidence set sizes, whether an atom is removable or not.
	 * It is removable, if the corrected incidence set is empty.
	 * 
	 * Statistics will be updated based on the atom type accordingly. 
	 * @param atomHandle
	 * @param parent may be null (e.g. for axioms)
	 * @param collectibleAtoms relevant for incidence set size correction during gc mode.
	 * @param stats 
	 * @param analyzeOnly if true, global removableAtomsSet will be relevant for incidence set calculation. 
	 * @param removableAtomsSet all atoms that we determined to be removable as we go during analyze. 
	 * @return
	 */
	protected boolean canRemoveAnalyze(HGHandle atomHandle, 
			HGHandle parent, 
			final List<HGHandle> collectibleAtoms,
			GarbageCollectorStatistics stats, 
			boolean analyzeOnly,
			Set<HGHandle> removableAtomsSet) {
		Object atom = graph.get(atomHandle);
		IncidenceSet is = graph.getIncidenceSet(atomHandle);
		//empty, if we deleted parent already, or only parent => safe to delete		
		boolean canRemove;
		int incidenceSetSize;
		if (analyzeOnly) {
			// we remove those form the incidence set, that we already found plus the current parent.
			incidenceSetSize = calcAnalyzeISSize(is, parent, removableAtomsSet);
		} else {
			//canRemove = (is.isEmpty() || (is.size() == 1 && (is.first().equals(parent)) || parent == null));
			incidenceSetSize = calcCollectISSize(is, parent, collectibleAtoms);
		}
		canRemove = (incidenceSetSize == 0);
		if (DBG) {
			if (!canRemove) { System.out.println(); printIncidenceSet(is, parent); };
		}
		if (atom == null) {
			System.out.println("\n  GC: Atom null for handle: " + atomHandle + " ISSize: " + is.size());
			canRemove = false;
		}
		if (canRemove) {
			//
			// Stats
			//
			if (atom instanceof OWLOntology) {
				stats.increaseTotalAtoms();
				stats.increaseOntologies();
			} else if (atom instanceof OWLAxiomHGDB) {	
				stats.increaseTotalAtoms();
				stats.increaseAxioms();
			} else if (atom instanceof OWLEntity) {
				OWLEntity entity = (OWLEntity) atom;
				if (entity.isBuiltIn()) {
					//Don't remove built in entities.
					canRemove = false;
					System.out.println("GC: Encountered builtin entity during DFS: " + entity + " Class: " + entity.getClass());
				} else {
					stats.increaseEntities();
					stats.increaseTotalAtoms();
				}
			} else if (atom instanceof IRI) {
				System.out.println("GC: Encountered IRI during DFS: " + atom);
				//we'll encounter those as linked to by Annotations and AnnotationAxioms as
				//an OWLAnnotationValue can be an IRI.
				stats.increaseTotalAtoms();
				stats.increaseIris();
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
		return canRemove;		
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
	protected int calcAnalyzeISSize(IncidenceSet is, HGHandle parent, Set<HGHandle> removableAtomsSet) {
		if (removableAtomsSet == null) throw new IllegalArgumentException("removableAtomsSet == null");
		int i = 0;
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		while (rs.hasNext()) {
			HGHandle cur = rs.next();
			if (!(removableAtomsSet.contains(cur) || cur.equals(parent))) {
				i ++;
			} 
		}
		return i;
	}

	/**
	 * Calculates incidence set size, by removing those from the actual incidence set, which were already marked for collection.
	 * Removable atom handles marked during one DFS call are remembered in a LinkedList and removed instantly after DFS.
	 * 
	 * @param is the current incidence set
	 * @return
	 */
	protected int calcCollectISSize(IncidenceSet is, HGHandle parent, List<HGHandle> collectibleAtoms) {
		if (collectibleAtoms == null) throw new IllegalArgumentException("collectibleAtoms == null");
		int i = 0;
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		while (rs.hasNext()) {
			HGHandle cur = rs.next();
			if (!(collectibleAtoms.contains(cur) || cur.equals(parent))) {
				i ++;
			} 
		}
		return i;
	}

	/**
	 * @param is
	 * @param parent
	 */
	protected void printIncidenceSet(IncidenceSet is, HGHandle parent) {
		HGRandomAccessResult<HGHandle> rs = is.getSearchResult();
		int i = 0;
		while(rs.hasNext()) {
			HGHandle cur = rs.next();
			System.out.print("IS: ");
			if (parent == cur) System.out.print("PARENT: ");
			printHandle(cur, i, false); System.out.println();
			i++;
		}
		rs.close();		
	}


	/**
	 * Counts the number of subgraphs a given atom is a member in. 
	 * Uses Subgraph.reverseIndex.
	 * 
	 * Should be called within HG Transaction.
	 * 
	 * @param atomHandle
	 * @param removableAtomsSet may be null
	 * @return >=0
	 */
	protected int countSubgraphsWhereAtomIsMember(HGHandle atomHandle, Set<HGHandle> removableAtomsSet) {
		HGPersistentHandle axiomPersHandle = graph.getPersistentHandle(atomHandle);
		if (axiomPersHandle == null) throw new IllegalStateException("Null persistent handle");
		HGIndex<HGPersistentHandle,HGPersistentHandle> indexAxiomToOntologies = HGSubgraph.getReverseIndex(graph);
		HGRandomAccessResult<HGPersistentHandle> rs = indexAxiomToOntologies.find(axiomPersHandle);
		int i = 0;
		try {
			while (rs.hasNext()) {
				HGHandle subgraphHandle = rs.next();
				if (removableAtomsSet !=null) {
					// exclude potentially removed ontologies if we are in analyse mode.
					if (!(removableAtomsSet.contains(subgraphHandle))) {
						i++;
				} else 
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
	protected void collectOtherObjects(GarbageCollectorStatistics stats, boolean analyzeOnly, Set<HGHandle> removableAtomsSet) {
		stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
				hg.disconnected(),
				hg.typePlus(OWLObjectHGDB.class),
				hg.not(hg.typePlus(OWLEntity.class)),
				hg.not(hg.typePlus(OWLAxiomHGDB.class)))
			);
		stopWatch.stop("Disconnected Others query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h: handlesToRemove) {
			Object o = graph.get(h); 
			if (o instanceof IRI) {
				System.out.println("Should not have found IRI (check query types): " + o);
			} 
			collectOWLObjectsByDFSTransact(h, stats, analyzeOnly, removableAtomsSet);
		}
		stopWatch.stop("Disconnected Others collection time: ");
		System.out.println("Stats now: " + stats.toString());		
	}
	
	/**
	 * Collects and removes disconnected entities.
	 * (Annotations are managed as AnnotationAssertionAxioms, latter are Ontology members)
	 * @param stats
	 */
	protected void collectEntities(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLEntity.class),
					hg.disconnected(),
					hg.not(new OWLEntityIsBuiltIn()),
					hg.not(new AnySubgraphMemberCondition(graph)))
					);
		stopWatch.stop("Disconnected Entities query time: Found: " + handlesToRemove.size() + " Duration:");
		if (!analyzeOnly) {
			int successRemoveCounter;			
			//TODO add batch processing here
			successRemoveCounter = removeEntitiesTransact(handlesToRemove, 0, handlesToRemove.size());
			//if (successRemoveCounter != handlesToRemove.size()) throw new IllegalStateException("successRemoveCounter != handles.size()");
			stats.setEntities(stats.getEntities() + successRemoveCounter);
			stats.setTotalAtoms(stats.getTotalAtoms() + successRemoveCounter);
			stopWatch.stop("Disconnected Entities collection time: ");
		} else {
			stats.setEntities(stats.getEntities() + handlesToRemove.size());
			stats.setTotalAtoms(stats.getTotalAtoms() + handlesToRemove.size());
		}
	}	

	protected int removeEntitiesTransact(final List<HGHandle> entityHandles, final int fromIndex, final int toIndex) {
		return graph.getTransactionManager().ensureTransaction(new Callable<Integer>() {
			public Integer call() {
				return (Integer)removeEntitiesInternal(entityHandles, fromIndex, toIndex);
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
	protected Integer removeEntitiesInternal(List<HGHandle> entityHandles, int fromIndex, int toIndex) {
		if (toIndex < fromIndex) throw new IllegalArgumentException("to: " + toIndex + " < from: " + fromIndex); 
		if (fromIndex < 0) throw new IllegalArgumentException("from: " + fromIndex + "< 0");
		if (toIndex  >= entityHandles.size()) throw new IllegalArgumentException();
		if (fromIndex >= entityHandles.size()) throw new IllegalArgumentException();
		
		int successRemoveCounter = 0;
		int i = fromIndex;
		ListIterator<HGHandle> it = entityHandles.listIterator(fromIndex);
		while (i < toIndex) {
			i++;
			HGHandle h = it.next();
			///check if still empty incidence set and remove.
			if (graph.getIncidenceSet(h).isEmpty()
					&& graphRemove(h)) {
				successRemoveCounter ++;
			}
		}
		return successRemoveCounter;
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
		//try {
			returnValue = graph.remove(atom, true);
		//} catch (RuntimeException e) {
//			System.out.println("Caught expected: " + e);
		//}
		return returnValue;
	}
	
}

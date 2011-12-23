package org.hypergraphdb.app.owl.gc;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.algorithms.DefaultALGenerator;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.algorithms.HGDepthFirstTraversal;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.type.TypeUtils;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * GarbageCollector.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 */
public class GarbageCollector {
	
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
	 * Finds all ontologies marked for deletion and garbage collects all referenced objects.
	 * Will not collect axioms that are not part of any ontology.
	 * Will not collect disconnected entities that are unreachable by traversing the ontologies.
	 */
	public static final int MODE_DELETED_ONTOLOGIES = 1;

	/**
	 *  Finds all axioms that are not member in any ontology.
	 *  Each axiom, all reachable dependent objects, and entities with an otherwise empty incidence set will be removed.
	 *  A) they were created by DF and never added to an onto
	 *  B) they were removed from the last ontology in which they were member.
	 *  (The general case is that axioms are exclusive to an ontology; the API user however can add axioms
	 *  that exist in Onto A to Onto B, thereby reusing the axiom and it's dependent objects.
	 */
	public static final int MODE_DISCONNECTED_AXIOMS = 2;
	
	/**
	 *  Finds all entities that are not member in any ontology and are not target of any other object.
	 *  No BUILTIN entities will be removed.
	 */
	public static final int MODE_DISCONNECTED_ENTITIES = 3;
	
	/**
	 * Finds all disconnected OWLObjectHGDB, except those implementing OLWEntity or subclasses of OWLAxiomHGDB.
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
	public static final int MODE_DISCONNECTED_OTHER = 4;
	
	private HyperGraph graph;
	private HGDBOntologyRepository repository;
	
		
	public GarbageCollector(HGDBOntologyRepository repository) {
		this.repository = repository;
		this.graph = repository.getHyperGraph();
	}

	public GarbageCollectorStatistics analyze() {
		return analyze(MODE_FULL);
	}
	
	public GarbageCollectorStatistics analyze(int mode) {
		return new GarbageCollectorStatistics();
	}
	
	/**
	 * Run full garbage collection
	 * @return
	 */
	public GarbageCollectorStatistics runGC() {
		return runGC(MODE_FULL);
	}

	public GarbageCollectorStatistics runGC(int mode) {
		GarbageCollectorStatistics stats = new GarbageCollectorStatistics();
		return stats;
	}

	public void collectRemovedOntologies(GarbageCollectorStatistics stats) {
		collectRemovedOntologies(stats, false);
	}

	public void collectRemovedOntologies(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		List<HGDBOntology> delOntos =  repository.getDeletedOntologies();
		stats.setOntologies(stats.getOntologies() + delOntos.size());
		for (HGDBOntology delOnto : delOntos) {
			collectRemovedOntology(delOnto, stats, analyzeOnly);
		}
		// 
	}

	public void collectRemovedOntology(HGDBOntology onto, GarbageCollectorStatistics stats, boolean analyzeOnly) {
		//collect Axioms
		
		//collect Ontology Annotations		
		
		onto.getA
	}
	
	/**
	 * Collects and removes all axioms that do not belong to any ontology.
	 * ie. are not members in any subgraph.
	 */
	public void collectAxioms(GarbageCollectorStatistics stats) {
		HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(OWLAxiomHGDB.class);
		//TypeUtils.printAllSubtypes(graph, graph.getTypeSystem().getType(typeHandle));
		
		int successRemoveCounter = 0;
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLAxiomHGDB.class),
					hg.disconnected())
				);
		for (HGHandle h: handlesToRemove) {
			if (graph.remove(h)) {
				successRemoveCounter ++;
			}
		}
		if (successRemoveCounter != handlesToRemove.size()) throw new IllegalStateException("successRemoveCounter != handles.size()");		
	}
	
	public void collectAxiomRecursive(HGHandle axiomHandle, GarbageCollectorStatistics stats, boolean enforceDisconnected, boolean analyzeOnly) {
		if (!assertAxiomIncidenceSetAppropriate(axiomHandle, enforceDisconnected)) {
			stats.setAxiomNotRemovableCases(stats.getAxiomNotRemovableCases() + 1);
			return;
		} 
		collectOWLObjectsRecursive(linkHandle, stats)LinkRecursive(null, axiomHandle, stats);
		//Remove the axiom object
	}
	
	/**
	 * Everything except with an otherwise empty incidence set will be removed.
	 * A 
	 * @param linkHandle
	 * @param stats
	 */
	public void collectOWLObjectsbyDFS(HGHandle linkHandle, GarbageCollectorStatistics stats, boolean analyzeOnly) {
		TargetSetALGenerator tsAlg = new TargetSetALGenerator(graph);
		HGDepthFirstTraversal dfs = new HGDepthFirstTraversal(linkHandle, tsAlg);
		while (dfs.hasNext()) {
			Pair<HGHandle, HGHandle> p = dfs.next();
			HGHandle targetHandle = p.getSecond();
			if (canRemoveAnalyze(targetHandle, p.getFirst(), stats)) {
				if (!analyzeOnly) {
					graph.remove(targetHandle);				
				}
			}
		}
		
	}
	
	public boolean canRemoveAnalyze(HGHandle atomHandle, HGHandle parent, GarbageCollectorStatistics stats) {
		Object atom = graph.get(atomHandle);
		IncidenceSet is = graph.getIncidenceSet(atomHandle);
		//empty, if we deleted parent already, or only parent => safe to delete		
		boolean canRemove = (is.isEmpty() || (is.size() == 1 && is.first().equals(parent)));
		if (canRemove) {
			stats.increaseTotalAtoms();
			if (atom instanceof OWLOntology) {
				stats.increaseOntologies();
			} else if (atom instanceof OWLAxiomHGDB) {	
				stats.increaseAxioms();
			} else if (atom instanceof OWLEntity) {
				stats.increaseEntities();
			} else if (atom instanceof OWLObjectHGDB) {
				stats.increaseOtherObjects();
			} else {
				System.err.println("Encountered unknown atom during GC: " + atom);
			}
		}
		return canRemove;		
	}
	
	
	/**
	 * Asserts that axiom may be removed.
	 * @param axiomHandle
	 * @param enforceDisconnected if true and incidenseset not empty; if false 
	 * @return true if axiom has appropriate incidence set for being collected.
	 */
	public boolean assertAxiomIncidenceSetAppropriate(HGHandle axiomHandle, boolean enforceDisconnected) {
		//ensure member in max one ontology
		IncidenceSet is = graph.getIncidenceSet(axiomHandle);
		if (enforceDisconnected) {
			if (!is.isEmpty()) {
				throw new IllegalStateException("An axiom that was supposed to be disconnected had an incidence set entry: \n"
			+ "Axiom: " +  graph.get(axiomHandle) + "\n" 
			+ "First incidenceset entry: " + is.iterator().next() + "\n");
			}
			return true;
		} else {
		    //Must be member in only the ontology that we are about to remove.
			return (is.size() <= 1);
		}
	}
	
	
	/**
	 * Collects and removes disconnected entities.
	 * 
	 * @param stats
	 */
	public void collectEntities(GarbageCollectorStatistics stats) {
		HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(OWLEntity.class);
		TypeUtils.printAllSubtypes(graph, graph.getTypeSystem().getType(typeHandle));
		
		int successRemoveCounter = 0;
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLEntity.class),
					hg.disconnected(),
					hg.not(new OWLEntityIsBuiltIn()))
				);
		for (HGHandle h: handlesToRemove) {
			if (graph.remove(h)) {
				successRemoveCounter ++;
			}
		}
		if (successRemoveCounter != handlesToRemove.size()) throw new IllegalStateException("successRemoveCounter != handles.size()");
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
}

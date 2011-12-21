package org.hypergraphdb.app.owl.gc;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;

/**
 * GarbageCollector.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 */
public class GarbageCollector {
	
	private HyperGraph graph;
	private HGDBOntologyRepository repository;
	

	public GarbageCollector(HGDBOntologyRepository repository) {
		this.repository = repository;
		this.graph = repository.getHyperGraph();
	}

	public GarbageCollectorStatistics analyze() {
		return new GarbageCollectorStatistics();
	}
	
	public void collectRemovedOntologies(GarbageCollectorStatistics stats) {
		
	}

	public void collectRemovedOntologies(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		
	}

	/**
	 * Collects and removes all axioms that do not belong to any ontology.
	 * ie. are not members in any subgraph.
	 */
	public void collectAxioms(GarbageCollectorStatistics stats) {
		
	}
	
	public void collectAxiomRecursive(GarbageCollectorStatistics stats, OWLObjectHGDB axiom) {
		
	}
	
	
	/**
	 * Collects and removes disconnected entities.
	 * 
	 * @param stats
	 */
	public void collectEntities(GarbageCollectorStatistics stats) {
		
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

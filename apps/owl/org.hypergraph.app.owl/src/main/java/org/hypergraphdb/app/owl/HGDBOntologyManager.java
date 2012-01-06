package org.hypergraphdb.app.owl;

import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Manages multiple Ontologies. Based on OWL-API OWLOntologyManagerImpl and ProtegeOWLOntologyManager.
 * 
 * HGDBOntologyManager.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyManager extends ProtegeOWLOntologyManager
{
	HGDBOntologyRepository ontologyRepository;
		

	public HGDBOntologyManager(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		ontologyRepository = HGDBOntologyRepository.getInstance(); 
		dataFactory.setHyperGraph(ontologyRepository.getHyperGraph());
	}
	
	/**
	 * @return the dbRepository
	 */
	public HGDBOntologyRepository getOntologyRepository() {
		return ontologyRepository;
	}
	
	/**
	 * Determines if at least one In Memory ontology is managed.
	 * @return
	 */
	public boolean hasInMemoryOntology() {
		for (OWLOntology onto : getOntologies()) {
			if (!(onto instanceof HGDBOntology)) {
				return true;
			}
		}
		return false;
	}

}

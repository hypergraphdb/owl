package org.hypergraphdb.app.owl;

import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Manages multiple Ontologies. Based on OWL-API OWLOntologyManagerImpl and ProtegeOWLOntologyManager.
 * For use with Protege 4.1.
 * 
 * PHGDBOntologyManagerImpl for protege integration.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class PHGDBOntologyManagerImpl extends ProtegeOWLOntologyManager implements HGDBOntologyManager
{
	HGDBOntologyRepository ontologyRepository;
		

	public PHGDBOntologyManagerImpl(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		ontologyRepository = HGDBOntologyRepository.getInstance(); 
		dataFactory.setHyperGraph(ontologyRepository.getHyperGraph());
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getOntologyRepository()
	 */
	@Override
	public HGDBOntologyRepository getOntologyRepository() {
		return ontologyRepository;
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#hasInMemoryOntology()
	 */
	@Override
	public boolean hasInMemoryOntology() {
		for (OWLOntology onto : getOntologies()) {
			if (!(onto instanceof HGDBOntology)) {
				return true;
			}
		}
		return false;
	}

}

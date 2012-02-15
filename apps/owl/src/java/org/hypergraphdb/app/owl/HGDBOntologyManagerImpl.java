package org.hypergraphdb.app.owl;

import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

/**
 * HGDBOntologyManagerImpl.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 3, 2012
 */
public class HGDBOntologyManagerImpl extends OWLOntologyManagerImpl implements HGDBOntologyManager {

	HGDBOntologyRepository ontologyRepository;
	
	public HGDBOntologyManagerImpl(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		if (HGDBApplication.VERSIONING) {
			ontologyRepository = VHGDBOntologyRepository.getInstance();
			this.addOntologyChangeListener(((VHGDBOntologyRepository)ontologyRepository));
		} else {
			ontologyRepository = HGDBOntologyRepository.getInstance();
		}
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

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getCurrentTaskSize()
	 */
	@Override
	public int getCurrentTaskSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyManager#getCurrentTaskProgress()
	 */
	@Override
	public int getCurrentTaskProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

}

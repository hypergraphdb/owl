package org.hypergraphdb.app.owl;

import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

/**
 * HGDBOntologyManagerImpl.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 3, 2012
 */
public class HGDBOntologyManagerImpl extends OWLOntologyManagerImpl implements HGDBOntologyManager {

	public static boolean DBG = true;

	/**
	 * Set this to have all removed ontologies also deleted from the repository.
	 * This is intended for unit testing only.
	 */
	private static boolean deleteOntologiesOnRemove = false;

	HGDBOntologyRepository ontologyRepository;	

	/**
	 * @return the deleteOntologiesOnRemove
	 */
	public static boolean isDeleteOntologiesOnRemove() {
		return deleteOntologiesOnRemove;
	}

	/**
	 * @param deleteOntologiesOnRemove the deleteOntologiesOnRemove to set
	 */
	public static void setDeleteOntologiesOnRemove(boolean deleteOntologiesOnRemove) {
		HGDBOntologyManagerImpl.deleteOntologiesOnRemove = deleteOntologiesOnRemove;
	}

	public HGDBOntologyManagerImpl(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		if (HGDBApplication.DISTRIBUTED) {
			ontologyRepository = VDHGDBOntologyRepository.getInstance();
			((VDHGDBOntologyRepository)ontologyRepository).setOntologyManager(this);
			this.addOntologyChangeListener(((VDHGDBOntologyRepository)ontologyRepository));
		} else if (HGDBApplication.VERSIONING) {
			ontologyRepository = VHGDBOntologyRepository.getInstance();
			this.addOntologyChangeListener(((VHGDBOntologyRepository)ontologyRepository));
		} else {
			ontologyRepository = HGDBOntologyRepository.getInstance();
		}
		addIRIMapper(new HGDBIRIMapper(ontologyRepository));
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
	 * @see uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl#removeOntology(org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public void removeOntology(OWLOntology ontology) {
		super.removeOntology(ontology);
		if (isDeleteOntologiesOnRemove()) {
			OWLOntologyID oid = ontology.getOntologyID();
			if (ontologyRepository.existsOntology(oid)) {
				boolean deleted = ontologyRepository.deleteOntology(ontology.getOntologyID());
				if (DBG) {
					if (deleted) {
						System.out.println("Deleted on remove: " + ontology.getOntologyID());
					} else {
						System.out.println("Deleted on remove FAILED NOT FOUND: " + ontology.getOntologyID());
					}
				}
			} else {
				System.out.println("OID of to remove onto not found in repo: " + ontology.getOntologyID());
			}
		}
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
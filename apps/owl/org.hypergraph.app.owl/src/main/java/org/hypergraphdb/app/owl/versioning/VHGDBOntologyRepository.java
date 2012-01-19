package org.hypergraphdb.app.owl.versioning;

import java.util.List;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.modularity.OntologySegmenter;

/**
 * VHGDBOntologyRepository.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class VHGDBOntologyRepository extends HGDBOntologyRepository implements OWLOntologyChangeListener {

	private static VHGDBOntologyRepository instance = null;
	
	public static HGDBOntologyRepository getInstance() {
		if (instance == null) {
			String hypergraphDBLocation = getHypergraphDBLocation();
			System.out.println("HGDB REPOSITORY AT: " + hypergraphDBLocation);
			instance = new VHGDBOntologyRepository(hypergraphDBLocation);
		}
		return instance;
	}
	
	private VHGDBOntologyRepository(String location) {
		super(location);
	}

		

	public List<VersionedOntology> getVersionControlledOntologies() {
		// TODO Auto-generated method stub
		return null;
	}


	public VersionedOntology addVersionControl(OWLOntology o) {
		// TODO Auto-generated method stub
		return null;
	}


	public void removeVersionControl(VersionedOntology o) {
		// TODO Auto-generated method stub

	}


	public void isVersionControlled(OWLOntology o) {
		// TODO Auto-generated method stub

	}


	public boolean existsRevision(RevisionID rId) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<RevisionID> getRevisionIDs(OWLOntology o) {
		// TODO Auto-generated method stub
		return null;
	}

	public void commitAll() {
		// TODO Auto-generated method stub

	}


	public List<ChangeSet> getChangeSetsTo(OWLOntology o, RevisionID rId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeListener#ontologiesChanged(java.util.List)
	 */
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
		
	}

}

package org.hypergraphdb.app.owl.versioning;

import java.util.List;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChangeFactory;
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

	public static VHGDBOntologyRepository getInstance() {
		if (!hasInstance()) {
			String hypergraphDBLocation = getHypergraphDBLocation();
			System.out.println("HGDB REPOSITORY AT: " + hypergraphDBLocation);
			setInstance(new VHGDBOntologyRepository(hypergraphDBLocation));
		}
		HGDBOntologyRepository instance = HGDBOntologyRepository.getInstance(); 
		if (!(instance instanceof VHGDBOntologyRepository)) throw new IllegalStateException("Instance requested not Versioned Repository type.");
		return (VHGDBOntologyRepository)instance;
	}
	
	private VHGDBOntologyRepository(String location) {
		super(location);
	}		

	public List<VersionedOntology> getVersionControlledOntologies() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the Version controlled Ontology or null.
	 * @param onto
	 * @return
	 */
	public VersionedOntology getVersionControlledOntology(OWLOntology onto) {
		return null;
	}


	public VersionedOntology addVersionControl(OWLOntology o) {
		if (isVersionControlled(o)) throw new IllegalStateException("Ontology already version controlled" + o.getOntologyID());
		return null;
	}


	public void removeVersionControl(VersionedOntology o) {
		// TODO Auto-generated method stub

	}


	public boolean isVersionControlled(OWLOntology o) {
		// TODO Auto-generated method stub
		return true;

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
		for (OWLOntologyChange c : changes) {
			//Cache this in a map
			if (isVersionControlled(c.getOntology())) {
				VersionedOntology vo = getVersionedOntology(c.getOntology());
				VOWLChange vc = VOWLChangeFactory.create(c, getHyperGraph());
				vo.addChange(vc);
			}
		}
	}

}

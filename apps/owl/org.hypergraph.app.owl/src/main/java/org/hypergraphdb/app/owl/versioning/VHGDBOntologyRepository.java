package org.hypergraphdb.app.owl.versioning;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
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
		return getHyperGraph().getAll(hg.type(VersionedOntology.class));
	}

	/**
	 * Returns the Version controlled Ontology or null.
	 * @param onto
	 * @return the versioned ontology or null, if not found.
	 */
	public VersionedOntology getVersionControlledOntology(OWLOntology onto) {
		HGPersistentHandle ontoHandle = getHyperGraph().getHandle(onto).getPersistent();
		for (VersionedOntology vo : getVersionControlledOntologies()) {
			if (vo.getHeadRevision().getOntologyID().equals(ontoHandle)) {
				return vo;
			}
		}
		return null;
	}

	public VersionedOntology addVersionControl(OWLOntology o, String user) {
		HyperGraph graph = getHyperGraph();
		if (isVersionControlled(o)) throw new IllegalStateException("Ontology already version controlled" + o.getOntologyID());
		VersionedOntology newVO = new VersionedOntology(o, user, graph);
		graph.add(newVO);
		return newVO;
	}


	public void removeVersionControl(VersionedOntology o) {
		o.clear();
		
	}


	public boolean isVersionControlled(OWLOntology o) {
		//TODO optimize this
		return getVersionControlledOntology(o) != null;
	}

	public boolean existsRevision(RevisionID rId) {
		// TODO Auto-generated method stub
		return false;
	}

	public void commitAll() {
		
	}


	public List<ChangeSet> getChangeSetsTo(OWLOntology o, RevisionID rId) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeListener#ontologiesChanged(java.util.List)
	 */
	public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
		VersionedOntology lastVo = null;
		OWLOntology lastOnto = null;
		for (OWLOntologyChange c : changes) {
			//Caching last
			if (c.getOntology().equals(lastOnto)) {
				//use cached
				VOWLChange vc = VOWLChangeFactory.create(c, getHyperGraph());
				lastVo.addChange(vc);
			} else {
				// get versionedonto
				if (isVersionControlled(c.getOntology())) {
					lastOnto = c.getOntology();
					lastVo = getVersionControlledOntology(lastOnto);
					VOWLChange vc = VOWLChangeFactory.create(c, getHyperGraph());
					lastVo.addChange(vc);
				}
			}
		}
	}

}

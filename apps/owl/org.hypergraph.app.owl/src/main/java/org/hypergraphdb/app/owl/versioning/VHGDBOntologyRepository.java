package org.hypergraphdb.app.owl.versioning;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * VHGDBOntologyRepository.
 * 
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VHGDBOntologyRepository {

	public List<VersionedOntology> getVersionControlledOntologies();

	public VersionedOntology addVersionControl(OWLOntology o);
	
	/**
	 * Removed the given ontology from version control.
	 * This will remove the full history of the ontology.
	 * @param o
	 */
	public void removeVersionControl(VersionedOntology o);
	
	public void isVersionControlled(OWLOntology o);

	public boolean existsRevision(RevisionID rId);
	
	public List<RevisionID> getRevisionIDs(OWLOntology o);


	/** 
	 * Commmits all loaded ontologies with a non empty changeset.
	 */
	public void commitAll();
	

	//
	// Internal
	//
	/**
	 * Gets a list of Changesets that, if applied in the returned order, 
	 * change the ontology from the current revision to the given RevisionID.
	 *  
	 * @param o
	 * @param rId
	 * @return
	 */
	List<ChangeSet> getChangeSetsTo(OWLOntology o, RevisionID rId);

}

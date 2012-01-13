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

	public List<OWLOntology> getVersionControlledOntologies();

	public void addVersionControl(OWLOntology o);
	
	/**
	 * Removed the given ontology from version control.
	 * This will remove the full history of the ontology.
	 * @param o
	 */
	public void removeVersionControl(OWLOntology o);
	
	public void isVersionControlled(OWLOntology o);

	public boolean existsRevision(RevisionID rId);
	
	public List<RevisionID> getRevisionIDs(OWLOntology o);


	public VersionedOntologyID getOntologyRevisionID(OWLOntology o, RevisionID rId);

	public VersionedOntologyID getOntologyHeadRevisionID(OWLOntology o);

	//
	// This should be in a session later:
	//
	
	/**
	 * Gets the OWLOntology with the given Revision.
	 * @param o
	 * @param rId
	 * @return
	 */
	public OWLOntology getOntologyRevision(RevisionID rId);

	/**
	 * 
	 * @param oID
	 * @param rId
	 * @return
	 */
	public OWLOntology getOntologyRevision(OWLOntologyID oID, RevisionID rId);

	
	public OWLOntology getOntologyRevision(VersionedOntologyID voID);
	
	/**
	 * Returns
	 * @param o
	 * @return
	 */
	public RevisionID getRevisionID(OWLOntology o);
	
	/**
	 * Gets the Head Revision Id. 
	 * After this revision Id, no committed changeset exists.
	 * 
	 * @param o
	 * @return
	 */
	public RevisionID getHeadRevisionID(OWLOntology o);

	public RevisionID isAtHead(OWLOntology o);


	public void commitAll();

	/**
	 * Closes the changeset, creating a new implicit ontology revision
	 * and opens a new changeset. 
	 * 
	 * @param o
	 * @return the RevisionID after the closed changeset and before further changes.
	 */
	public RevisionID commit(OWLOntology o);
	
	/**
	 * Undos uncommitted changes applied to the ontology.
	 * 
	 * @param o
	 */
	public void rollback(OWLOntology o);
	
	

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

package org.hypergraphdb.app.owl.versioning;

import java.util.Date;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.versioning.change.VHGDBOntologyChange;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * ChangeSet contains changes affecting one ontology only.
 * A changeset is closed after a commit.
 * A commit causes a new revision of the ontology.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface ChangeSet extends HGLink {
	
	Date getCreatedDate();
	
//	Date getCommittedDate();
//	
//	boolean isCommitted();
	
	void addChange(VHGDBOntologyChange change);

	//void addChange(OWLOntologyChange change);

//	/**
//	 * Gets 
//	 * @return 
//	 */
//	List<VHGDBOntologyChange> getChanges();
//
//	List<HGHandle> getChangesHandles();
//
//	List<OWLOntologyChange> getChangesAsOWLChanges();
	
	/**
	 * Finds and eliminates changes that became obsolete due to later changes.
	 */
	void pack();

	//to VersionedOntology HGHandle getPreviousChangeSetHandle();
	

	/**
	 * Applies the changes of this changeset.
	 * @param o
	 */
	void applyTo(OWLOntology o);
	
	/**
	 * Reverse applies (undoes) the changes of this changeset. 
	 * @param o
	 */
	void reverseApplyTo(OWLOntology o);
}

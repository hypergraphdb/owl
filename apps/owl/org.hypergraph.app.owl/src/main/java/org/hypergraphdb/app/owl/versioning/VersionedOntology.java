package org.hypergraphdb.app.owl.versioning;

import java.util.List;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VersionedOntology.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VersionedOntology  implements HGLink, HGGraphHolder {
	
	List<ChangeSet> changeSets;
	List<RevisionID> changeSetss;
	
	
	public OWLOntology getWorkingRevisionData();

	public int getWorkingRevision();

	public RevisionID getWorkingRevisionID();
	
	public boolean isWorkingRevisionAtHead();
	
	public List<Integer> getRevisions();
	
	public OWLOntology getHeadRevision();

	/**
	 * Floating end point of MASTER BRANCH
	 * @return
	 */
	public RevisionID getHeadRevisionID();

	public RevisionID getBaseRevisionID();
	
	public boolean existsRevision(Comparable<Object> revisionDescriptor);
	
	public OWLOntology getRevision(int revision);
		
	public void commit();
	
	public void rollback();

}
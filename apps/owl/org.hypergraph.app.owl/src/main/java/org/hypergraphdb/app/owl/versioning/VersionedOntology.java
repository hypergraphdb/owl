package org.hypergraphdb.app.owl.versioning;

import java.util.List;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VersionedOntology.
 * 
 * Usage:
 * By the time we add Version control, we have a revision. The first revision.
 * It's data is the OWLOntology at that time. 
 * 
 * On the first incoming change, we create a changeSet, the workingChangeset.
 * The working Changeset is never empty. 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VersionedOntology  implements HGLink, HGGraphHolder {
//	/**
//	 * The index to the pair that represents the current ontology data state 
//	 * in the changeSetAndRevisionPairs list.
//	 */
//	private int currentRevisionAndChangeSetPairIndex;
	
	/**
	 * The list of all changeSet and Revision Pairs.
	 * Each pair represents the changeset that leads/led to the pair's revision.
	 */
	List<HGHandle> revisionAndChangeSetPairs;
	//List<Pair<Revision, ChangeSet>> revisionAndChangeSetPairs;		
	
	
	HyperGraph graph;
	
	public VersionedOntology(HGHandle... targets) {
		
	}
	
	public VersionedOntology(OWLOntology onto) {
		//link to onto as head and base copy
		
	}
	
	public OWLOntology getWorkingRevisionData(){ };

	public int getWorkingRevision(){ };

	public RevisionID getWorkingRevisionID(){ };
	
	public boolean isWorkingRevisionAtHead(){ };
	
	public List<Integer> getRevisions(){
		
	};
	
	public OWLOntology getHeadRevision(){ };

	/**
	 * Floating end point of MASTER BRANCH
	 * @return
	 */
	public RevisionID getHeadRevisionID(){ };

	public RevisionID getBaseRevisionID(){ };
	
	public boolean existsRevision(int revision){ };
	
	/**
	 * Gets the given revision. 
	 * This is an expensive operation, as all necessary changesets 
	 * will be applied to the current data state.
	 *  
	 *  CURRENTY NOT SUPPORTED (2012.01.19)
	 *  We will later have in-mem copies for this or a limited # of ontos in graph.
	 *  
	 * @param revision
	 * @return
	 */
	public OWLOntology getRevisionData(int revision){ 
		
		
	};

	public Revision getRevision(int revision){		
		
	};

	/**
	 * Gets the changeset after the given revision of this versioned Ontology
	 * @param revision
	 * @return a changeset after the given revision.
	 */
	public ChangeSet getRevisionChangeSetAfter(int revision){		
			int i = getRevisionIndex(revision);
			HGHandle pairHandle =  
	};


	
	private int getRevisionIndex(int revision) {
		// go through pair list, load revisions, return index
	}
	
	/**
	 * 
	 * @param revision
	 * @return
	 */
	public OWLOntology setHeadRevision(int revision){
		// roll back current to 
		getRevision(revision);
		
	};
	
	/**
	 * Anonymous commit of current changeset resulting in a new revision.
	 */
	public void commit(){ 
		//if working changeset not empty, within one transaction
		//	create and persist new Pair P'
		//  	newRevision = Revision + 1
		//  	timestamps new revision
		//		setUser("Anonymous")
		//		newChangeSet = EmptyChangeSet
		// add P' to end of list.
		//  
		
	};

	public void commit(String user){ 

		
	};

	public void commit(int revisionIncrement){ 
		
	};
	
	/**
	 * Undoes all changes in the current changeset, if any and re-intializes the changeset.
	 * Currently the current changeset must be the head changeset.
	 */
	public void rollback(){ 
		//if changeset not empty
		//	
		//else do nothing
	};
	
	void addChange(VOWLChange vc){ 
		if (workingRevisionAndChangeSetPairIndex != revisionAndChangeSetPairs.size() - 1) {
			throw new IllegalStateException("Add changes only allowed, if current revision is head revision.");
		}
		
	}

	//
	// HELPERS
	//
	private Pair<Revision, ChangeSet> getCurrent() {
		HGHandle curPairHandle = revisionAndChangeSetPairs.get(currentRevisionAndChangeSetPairIndex);
		return graph.get(curPairHandle);
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGGraphHolder#setHyperGraph(org.hypergraphdb.HyperGraph)
	 */
	@Override
	public void setHyperGraph(HyperGraph graph) {
		this.graph = graph;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return revisionAndChangeSetPairs.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		return revisionAndChangeSetPairs.get(i);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		revisionAndChangeSetPairs.set(i, handle);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		revisionAndChangeSetPairs.remove(i);
	};
		
}
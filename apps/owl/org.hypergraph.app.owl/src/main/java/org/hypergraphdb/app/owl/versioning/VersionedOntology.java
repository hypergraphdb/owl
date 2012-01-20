package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.event.HGEvent;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.OWLMutableOntology;
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
	private List<HGHandle> revisionAndChangeSetPairs;
	//List<Pair<Revision, ChangeSet>> revisionAndChangeSetPairs;		
		
	protected HyperGraph graph;
	
	public VersionedOntology(HGHandle... targets) {
		revisionAndChangeSetPairs = new ArrayList<HGHandle>(Arrays.asList(targets));
		//assert at least one Pair.
	}
	
	/**
	 * Creates an 
	 * @param onto an ontology already stored in the graph
	 * @param user
	 */
	public VersionedOntology(OWLOntology onto, String user, HyperGraph graph) {
		this.graph = graph;
		//link to onto as head and base copy	
		initialize(onto, user);
	}
	
	private void initialize(OWLOntology onto, String user) {
		revisionAndChangeSetPairs = new ArrayList<HGHandle>();
		commitInternal(graph.getHandle(onto).getPersistent(), user, Revision.REVISION_FIRST);
	}

	/**
	 * Creates a new Pair object, adds it to graph and it's handle to our pairlist,
	 * and updates or adds this versioned Ontology.
	 *
	 * Structure created:
	 * <code>
	 * pairList.add(pairHandle --First--> Revision (Persistenthandle, int revision)
	 *                         --Second-> changeSetHandle --> ChangeSet(empty));
	 * </code>
	 * @param ontoHandle
	 * @param user
	 * @param revision
	 */
	private void commitInternal(HGPersistentHandle ontoHandle, String user, int revision) {
		// assert revision > Pairs.getLast().GetFirst.GetRevision)
		// assert user != null
		// assert ontoHandle != null; pointin to onto.
		// asssert head change set not empty
		Revision newRevision = new Revision();
		newRevision.setOntologyID(ontoHandle);
		newRevision.setRevision(revision);
		newRevision.setUser(user);
		// ChangeSet
		ChangeSet emptyCs = new ChangeSet();
		newRevision.setTimeStamp(emptyCs.getCreatedDate());
		HGHandle csHandle = graph.add(emptyCs);
		//HGPersistentHandle csHandleP = csHandle.getPersistent();
		// Pair
		Pair<Revision, HGHandle> pair = new Pair<Revision, HGHandle>(newRevision, csHandle);
		HGHandle pairHandle = graph.add(pair);
		revisionAndChangeSetPairs.add(pairHandle);
		//this link needs to be graph.updated now.
		if (graph.getHandle(this) != null){
			graph.update(this);
		} else {
			graph.add(this);
		}
	}

	public Revision getHeadRevision() {
		return getRevision(revisionAndChangeSetPairs.size() - 1);
	}

	public ChangeSet getHeadChangeSet() {
		return getChangeSet(revisionAndChangeSetPairs.size() - 1);
	}

	public OWLOntology getHeadRevisionData(){
		return graph.get(getHeadRevision().getOntologyID());
	}
	
	private Revision getRevision(int index) {
		HGHandle pairHandle = revisionAndChangeSetPairs.get(index);		
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		return pair.getFirst();
	}

	private ChangeSet getChangeSet(int index) {
		HGHandle pairHandle = revisionAndChangeSetPairs.get(index);		
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		HGHandle csHandle = pair.getSecond(); 
		return graph.get(csHandle);
	}
	
	/** 
	 * Structure deleted:
	 * 
	 * Only pair deleted. References to Revision, Changesets remain valid and need to be GCd.
	 * <code>
	 * pairList.add(pairHandle --First--> Revision (Persistenthandle, int revision)
	 * 
	 *                         --Second-> changeSetHandle --> ChangeSet(empty));
	 * </code>
	 */
	private void delete(int index) {
		HGHandle pairHandle = revisionAndChangeSetPairs.get(index);		
		graph.remove(pairHandle, true);
	}
	
	public Revision getBaseRevision(){ 
		return pair.getFirst();
	}
	
	public ChangeSet getChangeSet(RevisionID rId){ 
		int i = indexOf(rId);
		if (i == -1) return null;
		HGHandle pairHandle = revisionAndChangeSetPairs.get(i);
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		return graph.get(pair.getSecond());
	}
	
	private int indexOf(RevisionID rId) {
		for (int i = revisionAndChangeSetPairs.size() - 1; i >= 0; i--) {
			HGHandle pairHandle = revisionAndChangeSetPairs.get(i);		
			Pair<Revision, HGHandle> pair = graph.get(pairHandle);
			if (pair.getFirst().equals(rId)) {
				return i;
			}
		}
		return -1;
	}
	

	/**
	 * Deletes the last pair after applying an undo of all changes.
	 * @return new head revision
	 */
	private Revision rollbackHeadOneRevision() {
		
	}
	
//	public OWLOntology getWorkingRevisionData(){ };
//
//	public int getWorkingRevision(){ };
//
//	public RevisionID getWorkingRevisionID(){ };
//	
//	public boolean isWorkingRevisionAtHead(){ };

	/**
	 * 
	 * @return unmodifiable list of all revisions starting with the oldest at index 0.
	 */
	public List<Revision> getRevisions(){
		List<Revision> returnedList = new ArrayList<Revision>(revisionAndChangeSetPairs.size());
		for (HGHandle pairHandle: revisionAndChangeSetPairs) {
			Pair<Revision, HGHandle> pair = graph.get(pairHandle);
			returnedList.add(pair.getFirst());
		}
		return Collections.unmodifiableList(returnedList);
	};
	
	public int size(){
		return revisionAndChangeSetPairs.size();
	};
	
//	/**
//	 * Gets the given revision. 
//	 * This is an expensive operation, as all necessary changesets 
//	 * will be applied to the current data state.
//	 *  
//	 *  CURRENTY NOT SUPPORTED (2012.01.19)
//	 *  We will later have in-mem copies for this or a limited # of ontos in graph.
//	 *  
//	 * @param revision
//	 * @return
//	 */
//	public OWLOntology getRevisionData(int revision){ 
//		
//		
//	};

	/**
	 * Gets the changeset after the given revision of this versioned Ontology
	 * @param revision
	 * @return a changeset after the given revision.
	 */
	public ChangeSet getRevisionChangeSetAfter(RevisionID revisionID){		
			int i = getRevisionIndex(revision);
			HGHandle pairHandle =  
	};


	
//	private int getRevisionIndex(int revision) {
//		// go through pair list, load revisions, return index
//	}
	
//	/**
//	 * 
//	 * @param revision
//	 * @return
//	 */
//	public OWLOntology setHeadRevision(int revision){
//		// roll back current to 
//		getRevision(revision);
//		
//	};
	
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
//		if (workingRevisionAndChangeSetPairIndex != revisionAndChangeSetPairs.size() - 1) {
//			throw new IllegalStateException("Add changes only allowed, if current revision is head revision.");
//		}
		
	}

	//
	// HELPERS
	//
			
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
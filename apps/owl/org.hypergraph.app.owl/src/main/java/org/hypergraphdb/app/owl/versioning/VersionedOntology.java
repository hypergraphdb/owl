package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * A VersionedOntology represents all revisions and changesets of one versioned ontology.
 * Only one concrete owlontology (revision data) is currently maintained. This is the head revision. 
 * All added changes are instantly persisted in changesets and survive downtime.
 * Each commit leads to a new revision and opens a new empty changeset.
 * 
 * Revisions are ordered by RevisionID.
 * The first revision is called base revision, the last head revision.
 * The changeset that accepts changes until the next commit is called head changeset.
 * 
 * Usage:
 * By the time we add Version control, we have a revision. 
 * This is initially both, base and head revision.
 * It's data is the OWLOntology at that time.
 * Subsequent changes will be added to the head changeset and applied to the head revision data.
 * Rollback: will undo all changes in the head changeset.
 * Commit: will create a new head revision and a new empty head changeset, closing the old head changeset. 
 * 
 * 
 * Implementation:
 * Usage of Pair objects. One pair refers to one revision and the changeset that was applied after the revision.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VersionedOntology  implements HGLink, HGGraphHolder {
	
	/**
	 * The list of all changeSet and Revision Pairs.
	 * Each pair represents the changeset that leads/led to the pair's revision.
	 */
	private List<HGHandle> revisionAndChangeSetPairs;
		
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
	 * @param revision sets the revision (of the new head revision)
	 */
	private void commitInternal(HGPersistentHandle ontoHandle, String user, int revision) {
		// assert revision > Pairs.getLast().GetFirst.GetRevision)
		// assert user != null
		// assert ontoHandle != null; pointin to onto.
		// asssert head change set not empty
		// assert head.getOntologyID.equals(ontohandle)
		Revision newRevision = new Revision();
		newRevision.setOntologyID(ontoHandle);
		newRevision.setRevision(revision);
		newRevision.setUser(user);
		// ChangeSet
		ChangeSet emptyCs = new ChangeSet();
		newRevision.setTimeStamp(emptyCs.getCreatedDate());
		HGHandle csHandle = graph.add(emptyCs);
		// HGPersistentHandle csHandleP = csHandle.getPersistent();
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

	/** 
	 * Removes the pair, linked changesets and changes from the graph. 
	 * 
	 * <code>
	 * pairList.add(pairHandle --First--> Revision (Persistenthandle, int revision)
	 * 
	 *                         --Second-> changeSetHandle --> ChangeSet(empty));
	 * </code>
	 */
	private void removePair(HGHandle pairHandle) {		
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		//Revision will be removed with pair removal
		HGHandle changeSetHandle = pair.getSecond();
		ChangeSet changeSet = graph.get(changeSetHandle);
		//Clear changeset
		changeSet.clear();
		//graph.remove(changeSetHandle, true);
		graph.remove(pairHandle, true);
	}
	
	/**
	 * Returns the first revision.
	 * @return
	 */
	public Revision getBaseRevision(){ 
		return getRevision(0);
	}
	
	/**
	 * Returns the changeset that was created after(!) the given revision ID.
	 * (This is NOT the changeset that lead to the given revision.)
	 * 
	 * @param rId
	 * @return the Changeset or null, if it does not exist.
	 */
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
	 * This is only allowed if the head changeset is empty.
	 * <code>
	 * 1. reverse Apply previous change set cs'
	 * 2. clear cs'
	 * 3. Delete current head pair
	 * 
	 * Head is now previous revision, data is before cs', cs' is head changeset and  empty.
	 * </code>
	 * @throws IllegalStateException, if current head changeset is not empty.
	 */
	private void rollbackHeadToPreviousRevision() {
		if (!getHeadChangeSet().isEmpty()) {
			throw new IllegalStateException("Need to rollback head before rolling back one revision");
		}
		if (!(size() > 1)) {
			throw new IllegalStateException("There is no revision to roll back");
		}
		int indexPrevious = revisionAndChangeSetPairs.size() - 2;
		ChangeSet cs = getChangeSet(indexPrevious);
		cs.reverseApplyTo((OWLMutableOntology)getHeadRevisionData());
		cs.clear();
		// delete cur head, making prev cur.
		HGHandle pairHandle = revisionAndChangeSetPairs.remove(revisionAndChangeSetPairs.size() - 1);
		removePair(pairHandle);
	}
	
	/**
	 * Rolls back changes in the current head changeset and clears it.
	 */
	private void rollbackHeadChangeSet() {
		int index = revisionAndChangeSetPairs.size() -1;
		ChangeSet s = getChangeSet(index);
		s.reverseApplyTo((OWLMutableOntology)getHeadRevisionData());
		s.clear();
		// The head changeset is now empty and data represents state before changes.
	}	

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
	}
	/**
	 * The number of revisions, which is equal to the number of changesets.
	 * 
	 * @return the number of revisions and changesets.
	 */
	public int size(){
		return revisionAndChangeSetPairs.size();
	}
	
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
		commit(Revision.USER_ANONYMOUS, Revision.REVISION_INCREMENT);
	}

	/**
	 * Commits all head changes and creates a new head revision with an empty change set.
	 * @param user
	 */
	public void commit(String user){ 
		commit(user, Revision.REVISION_INCREMENT);
	}

	/**
	 * Commits all head changes and creates a new head revision with an empty change set.
	 * 
	 * @param revisionIncrement
	 */
	public void commit(String user, int revisionIncrement){
		int headRevision = getHeadRevision().getRevision();		
		commitInternal(getHeadRevision().getOntologyID(), user, headRevision + revisionIncrement);
	}
	
	/**
	 * Undoes all changes in the current changeset, if any and re-intializes the changeset.
	 * Currently the current changeset must be the head changeset.
	 */
	public void rollback(){ 
		rollbackHeadChangeSet();
	}

	/**
	 * Undoes all changes from head to the given revision, 
	 * deleting all changesets.
	 * The head changeset must be empty when calling this method.
	 * 
	 * @param rId
	 * @throws IllegalStateException if head changeset has changes or rId not found.
	 */
	public void revertHeadTo(RevisionID rId) {
		int revertIndex = indexOf(rId);
		if (revertIndex == -1) throw new IllegalStateException("Revert: No such revision: " + rId);
		if (!getHeadChangeSet().isEmpty()) throw new IllegalStateException("Revert Error: Head changeset not empty, needs rollback.");
		// 1,C; 2,C; H,HC
		//  0    1     2 size: 3  => 2 calls
		for (int curHeadIndex = revisionAndChangeSetPairs.size() - 1; curHeadIndex > revertIndex; curHeadIndex--) {
			rollbackHeadToPreviousRevision();
		}
	}
	
	public void revertHeadOneRevision() {
		rollbackHeadToPreviousRevision();
	}

	/**
	 * Adds one change to the current head changeset.
	 * The change will be instantly persisted.
	 * 
	 * @param vc
	 */
	void addChange(VOWLChange vc){ 
		getHeadChangeSet().addChange(vc);
	}
	
	/**
	 * Removes all revisions and changesets without modifying head revision data.
	 * The versioned ontology may be removed after this operation.
	 */
	void clear() {
		List<HGHandle> revisionAndChangeSetPairsCopy = new ArrayList<HGHandle>(revisionAndChangeSetPairs);
		for (int i = 0; i < revisionAndChangeSetPairsCopy.size(); i++) {
			HGHandle pairHandle = revisionAndChangeSetPairsCopy.get(i);	
			removePair(pairHandle);
		}
		// assert revisionAndChangeSetPairs.isEmpty()
		if(!revisionAndChangeSetPairs.isEmpty()) throw new IllegalStateException("List expected to be empty.");
		//Will be empty revisionAndChangeSetPairs.clear();
		//graph.update(this);
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
	}
		
}
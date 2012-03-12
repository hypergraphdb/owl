package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * A VersionedOntology represents all revisions and changesets of one versioned ontology.
 * Only one concrete owlontology (revision data) is currently maintained. This is the workingset (Head + uncommitted changes). 
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
public class VersionedOntology  implements HGLink, HGGraphHolder, VersioningObject {
	
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
	 * Creates a versionedOntology containing the given list of revisions and changesets.
	 * All revisions, changesets, changes and axioms, importdecls and ontoAnnotations are
	 * expected to be stored in the graph and currently loaded.
	 * The creator shall add this object to the graph.
	 *  
	 * @param revisions
	 * @param changeSets
	 */
	public VersionedOntology(final List<Revision> revisions, final List<ChangeSet> changeSets, HyperGraph graph) {
		this.graph = graph;
		if (revisions.size() - changeSets.size() > 1) throw new IllegalArgumentException("Sizes must match by 1;" 
				+ " only last changeset may be omitted. \n Revisions: " + revisions.size()
				+ " Changesets: " + changeSets.size());
		if (revisions.size() - changeSets.size() < 0) throw new IllegalArgumentException("Must not have less revisions than changesets."); 
		revisionAndChangeSetPairs = new ArrayList<HGHandle>(revisions.size());
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				for (int i = 0; i < revisions.size(); i++) {
					HGHandle curCsHandle;
					if (i < changeSets.size()) {
						curCsHandle = VersionedOntology.this.graph.getHandle(changeSets.get(i));
					} else {
						curCsHandle = VersionedOntology.this.graph.add(new ChangeSet());
					}
					if (curCsHandle == null) throw new NullPointerException("Changeset must be in graph and loaded:" + changeSets.get(i));
					Pair<Revision, HGHandle> pair = new Pair<Revision, HGHandle>(revisions.get(i), curCsHandle);
					HGHandle pairHandle = VersionedOntology.this.graph.add(pair);
					revisionAndChangeSetPairs.add(pairHandle);
				}
				return null;
			}});
		//assert at least one Pair.
	}

	/**
	 * Creates an 
	 * 
 	 * Should be called within HGTransaction.
 	 * 
	 * @param onto an ontology already stored in the graph
	 * @param user
	 */
	public VersionedOntology(HGDBOntology onto, String user, HyperGraph graph) {
		this.graph = graph;
		//link to onto as head and base copy	
		initialize(onto, user);
	}
	
	private void initialize(final HGDBOntology onto, final String user) {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				revisionAndChangeSetPairs = new ArrayList<HGHandle>();		
				commitInternal(graph.getHandle(onto).getPersistent(), user, Revision.REVISION_FIRST, "Initial Revision");
				return null;
			}});
	}


	public Revision getHeadRevision() {
		return graph.getTransactionManager().ensureTransaction(new Callable<Revision>() {
			public Revision call() {
				return getRevision(revisionAndChangeSetPairs.size() - 1);
			}}, HGTransactionConfig.READONLY);
	}

	public ChangeSet getWorkingSetChanges() {
		return graph.getTransactionManager().ensureTransaction(new Callable<ChangeSet>() {
			public ChangeSet call() {
				return getChangeSet(revisionAndChangeSetPairs.size() - 1);
			}}, HGTransactionConfig.READONLY);
	}

	public HGDBOntology getWorkingSetData(){
		return graph.getTransactionManager().ensureTransaction(new Callable<HGDBOntology>() {
			public HGDBOntology call() {
					return graph.get(getHeadRevision().getOntologyUUID());
			}}, HGTransactionConfig.READONLY);
	}
	
	/**
	 * Gets the revision Data representing a particular revision.
	 * This is an expensive operation.
	 * Only Committed changes will be included.
	 * Returns a partial in memory Ontology, that has in memory sets and indices, but
	 * shares graph loaded axioms and Entities.
	 * It is expected to fail during a GC run, if after onto creation:
	 * A) the head ontology gets reverted to a state before the r revision AND GC is run after that
	 * OR 
	 * B) Version Control get's removed from the head ontology, which deletes all changes AND GC is run after that
	 * OR
	 * C) The head ontology gets Removed AND GC is run after that
	 *  
	 * @param r
	 * @return
	 */
	public OWLOntologyImpl getRevisionData(Revision targetRevision) {
		// Assert revision is in VersionedOnto
		HGDBOntology latest = getWorkingSetData();
		// Create an empty copy.
		OWLOntologyImpl memOnto = copyIntoPartialInMemOnto(latest);
		// Revert all Changesets from HEAD to r in mem
		List<ChangeSet> changeSetsToRevert = getChangeSetsFromRevisionToLast(targetRevision);
		// Iterate over list last to first
		ListIterator<ChangeSet> csIt = changeSetsToRevert.listIterator(changeSetsToRevert.size());
		while (csIt.hasPrevious()) {
			ChangeSet cs = csIt.previous();
			cs.reverseApplyTo(memOnto);
		}
		return memOnto;
	}
	
	/**
	 * Returns an OWLOntology representing the given revision by rolling back changes in memory. 
	 * If the given revision is the head and includeUncommited is true, 
	 * the workingsetData will be returned. 
	 * 
	 * @param targetRevisionIndex [0..getArity()[
	 * @param includeUncommitted
	 * @return
	 */
	public OWLOntology getRevisionData(int targetRevisionIndex, boolean includeUncommitted) {
		if (targetRevisionIndex == getArity() - 1 
				&& targetRevisionIndex >= 0
				&& includeUncommitted) {
			//Workingset requested
			return getWorkingSetData();
		} else { 
			Revision target = getRevision(targetRevisionIndex);
			return getRevisionData(target);
		}
	}

	
	/**
	 * Returns a view of all changesets from the one after r including the after head changeset.
	 * @param r
	 * @return a sublist from changeset after r including the changeset after head.
	 */
	public List<ChangeSet> getChangeSetsFromRevisionToLast(Revision r) {
		int i = indexOf(r);
		List<ChangeSet> allCS = getChangeSets();
		return getChangeSets().subList(i, allCS.size() - 1);
	}
	
	/**
	 * Returns a partial in memory Ontology, that has in memory sets and indices, but
	 * relies on persisted axioms and entities.
	 * It is expected to fail during a GC run, if after onto creation:
	 * A) the Head ontology gets reverted to a state before the r revision AND GC is run after that
	 * OR 
	 * B) Version Control get's removed from the head ontology, which deletes all changes AND GC is run after that
	 * OR
	 * C) The head ontology gets Removed AND GC is run after that
	 * 
	 * @param original
	 * @return
	 */
	public OWLOntologyImpl copyIntoPartialInMemOnto(HGDBOntology original) {
		OWLOntologyImpl memOnto = new OWLOntologyImpl(original.getOWLOntologyManager(), original.getOntologyID());
		// Copy A) ImportDeclarations 
		for(OWLImportsDeclaration id : original.getImportsDeclarations()) {
			memOnto.applyChange(new AddImport(memOnto, id));
		}
		// Copy B) ImportDeclarations 
		for(OWLAnnotation an : original.getAnnotations()) {
			memOnto.applyChange(new AddOntologyAnnotation(memOnto, an));
		}
		// Copy C) Axioms 
		for(OWLAxiom ax : original.getAxioms()) {
			memOnto.applyChange(new AddAxiom(memOnto, ax));
		}
		return memOnto;
	}
	
	/**
	 * Should be called within HGTransaction.
	 * 
	 * @param index
	 * @return
	 */
	private Revision getRevision(int index) {
		HGHandle pairHandle = revisionAndChangeSetPairs.get(index);		
		Pair<Revision, HGHandle> pair = graph.get(pairHandle);
		return pair.getFirst();
	}
	
	/**
	 * Should be called within HGTransaction.
	 * 
	 * @param index
	 * @return
	 */
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
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param ontoHandle
	 * @param user
	 * @param revision sets the revision (of the new head revision)
	 */
	private void commitInternal(HGPersistentHandle ontoHandle, String user, int revision, String comment) {
		// assert revision > Pairs.getLast().GetFirst.GetRevision)
		// assert user != null
		// assert ontoHandle != null; pointin to onto.
		// asssert head change set not empty
		// assert head.getOntologyID.equals(ontohandle)
		Revision newRevision = new Revision();
		newRevision.setOntologyUUID(ontoHandle);
		newRevision.setRevision(revision);
		newRevision.setUser(user);
		newRevision.setRevisionComment(comment);
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
		} //2012.01.27 hilpold BIG PROBLEM WAS: 
		// else {
		//	graph.add(this);
		//}
	}

	/** 
	 * Removes the pair, linked changesets and changes from the graph. 
	 * 
	 * <code>
	 * pairList.add(pairHandle --First--> Revision (Persistenthandle, int revision)
	 * 
	 *                         --Second-> changeSetHandle --> ChangeSet(empty));
	 * </code>
	 * 
 	 * Should be called within HGTransaction.
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
		graph.update(this);
	}
	
	/**
	 * Returns the first revision.
	 * @return
	 */
	public Revision getBaseRevision(){ 
		return graph.getTransactionManager().ensureTransaction(new Callable<Revision>() {
			public Revision call() {
				return getRevision(0);
			}}, HGTransactionConfig.READONLY);
	}
	
	/**
	 * Returns the changeset that was created after(!) the given revision ID.
	 * (This is NOT the changeset that lead to the given revision.)
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param rId
	 * @return the Changeset or null, if it does not exist.
	 */
	public ChangeSet getChangeSet(final RevisionID rId){ 
		return graph.getTransactionManager().ensureTransaction(new Callable<ChangeSet>() {
			public ChangeSet call() {
				int i = indexOf(rId);
				if (i == -1) return null;
				HGHandle pairHandle = revisionAndChangeSetPairs.get(i);
				Pair<Revision, HGHandle> pair = graph.get(pairHandle);
				return graph.get(pair.getSecond());
			}}, HGTransactionConfig.READONLY);
	}
	
	/**
	 * Returns a list of changesets in the same order as revisions.
	 * The list, but not its entries may be modified without affecting the versioned ontology.
	 * @return
	 */
	public List<ChangeSet> getChangeSets() {
		return graph.getTransactionManager().ensureTransaction(new Callable<List<ChangeSet>>() {
			public List<ChangeSet> call() {
				List<ChangeSet> returnedList = new ArrayList<ChangeSet>(revisionAndChangeSetPairs.size());
				for (HGHandle pairHandle: revisionAndChangeSetPairs) {
					Pair<Revision, HGHandle> pair = graph.get(pairHandle);
					returnedList.add(graph.<ChangeSet>get(pair.getSecond()));
				}
				return returnedList;
			}}, HGTransactionConfig.READONLY);
	}
	
	/**
 	 * Should be called within HGTransaction.
	 * 
	 * @param rId
	 * @return
	 */
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
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @throws IllegalStateException, if current head changeset is not empty.
	 */
	private void rollbackHeadToPreviousRevision() {
		if (!getWorkingSetChanges().isEmpty()) {
			throw new IllegalStateException("Need to rollback head before rolling back one revision");
		}
		if (!(getNrOfRevisions() > 1)) {
			throw new IllegalStateException("Cannot roll back Head, because Head is Base.");
		}
		int indexPrevious = revisionAndChangeSetPairs.size() - 2;
		ChangeSet cs = getChangeSet(indexPrevious);
		cs.reverseApplyTo((OWLMutableOntology)getWorkingSetData());
		cs.clear();
		// delete cur head, making prev cur.
		HGHandle pairHandle = revisionAndChangeSetPairs.remove(revisionAndChangeSetPairs.size() - 1);
		removePair(pairHandle); //will graph.update this
	}
	
	/**
	 * Rolls back changes in the current head changeset and clears it.
	 * 
 	 * Should be called within HGTransaction.
	 */
	private void rollbackWorkingChangeSet() {
		int index = revisionAndChangeSetPairs.size() -1;
		ChangeSet s = getChangeSet(index);
		s.reverseApplyTo((OWLMutableOntology)getWorkingSetData());
		s.clear(); //will graph.update
		// The head changeset is now empty and data represents state before changes.
	}	

	/**
	 * Returns all Revisions ordered lowest/oldest revision first.
	 * The list, but not its entries may be modified without affecting the versioned ontology.
	 */
	public List<Revision> getRevisions(){
		return graph.getTransactionManager().ensureTransaction(new Callable<List<Revision>>() {
			public List<Revision> call() {
				List<Revision> returnedList = new ArrayList<Revision>(revisionAndChangeSetPairs.size());
				for (HGHandle pairHandle: revisionAndChangeSetPairs) {
					Pair<Revision, HGHandle> pair = graph.get(pairHandle);
					returnedList.add(pair.getFirst());
				}
				return returnedList;
			}}, HGTransactionConfig.READONLY);
	}
	
	/**
	 * The number of revisions, which is equal to the number of changesets.
	 * 
	 * @return the number of revisions and changesets.
	 */
	public int getNrOfRevisions(){
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
		commit(Revision.USER_ANONYMOUS, Revision.REVISION_INCREMENT, null);
	}

	/**
	 * Commits all head changes and creates a new head revision with an empty change set.
	 * @param user
	 */
	public void commit(String user, String comment){ 
		commit(user, Revision.REVISION_INCREMENT, comment);
	}

	/**
	 * Commits all head changes and creates a new head revision with an empty change set.
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param revisionIncrement
	 */
	public void commit(final String user, final int revisionIncrement, final String comment){
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				int headRevision = getHeadRevision().getRevision();		
				commitInternal(getHeadRevision().getOntologyUUID(), user, headRevision + revisionIncrement, comment);
				return null;
			}});
	}
	
	/**
	 * Undoes all changes in the current uncommitted working changeset, if any and re-intializes the changeset.
	 * Currently the current changeset must be the working changeset after head.
	 * 
	 * This method ensures a HGTransaction.
	 */
	public void rollback(){ 
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				rollbackWorkingChangeSet();
				return null;
			}});
	}

	/**
	 * Undoes all changes from head to the given revision, 
	 * deleting all changesets.
	 * The head changeset must be empty when calling this method.
	 * 
	 * This method ensures a HGTransaction.
	 * 
	 * @param rId
	 * @throws IllegalStateException if working changeset has changes or rId not found.
	 */
	public void revertHeadTo(final RevisionID rId) {
		int revertIndex = indexOf(rId);
		if (revertIndex == -1) throw new IllegalStateException("Revert: No such revision: " + rId);
		if (!getWorkingSetChanges().isEmpty()) throw new IllegalStateException("Revert Error: Head changeset not empty, needs rollback.");
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				//2nd check, repeatable! could be -1 on repeat, but that's ok below.
				int revertIndex = indexOf(rId);			
				// 1,C; 2,C; H,HC
				//  0    1     2 size: 3  => 2 calls
				for (int curHeadIndex = revisionAndChangeSetPairs.size() - 1; curHeadIndex > revertIndex; curHeadIndex--) {
					rollbackHeadToPreviousRevision();
				}
				return null;
			}});
	}
	
	/**
	 * Reverts head to the previous revision. 
	 * Changes from previous to head will be applied inversely.
	 * WorkingChangeSet must be empty.
	 * 
 	 * This method ensures a HGTransaction.
	 */
	public void revertHeadOneRevision() {
		graph.getTransactionManager().ensureTransaction(new Callable<Object>() {
			public Object call() {
				rollbackHeadToPreviousRevision();
				return null;
			}});
	}

	/**
	 * Adds one change to the current head changeset.
	 * The change will be instantly persisted.
	 * 
	 * Should be called within HGTransaction.
	 * 
	 * @param vc
	 */
	void addChange(VOWLChange vc){ 
		getWorkingSetChanges().addChange(vc);
	}
	
	/**
	 * Removes all revisions and changesets without modifying head revision data.
	 * The versioned ontology may be removed after this operation.
	 * 
	 * Should be called within HGTransaction.
	 * 
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

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLObjectVisitor visitor) {
		visitor.visit(this);
	}
}
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.VersioningObject;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Change.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VOWLChange implements HGLink, VersioningObject, HGGraphHolder {	
	
	HyperGraph graph;
	
	public static boolean isAddChange(VOWLChange c) {
		return c instanceof VAddAxiomChange || c instanceof VAddImportChange || c instanceof VAddOntologyAnnotationChange;
	}

	public static boolean isRemoveChange(VOWLChange c) {
		return c instanceof VRemoveAxiomChange || c instanceof VRemoveImportChange || c instanceof VRemoveOntologyAnnotationChange;
	}

	public static boolean isModifyChange(VOWLChange c) {
		return c instanceof VModifyOntologyIDChange;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGGraphHolder#setHyperGraph(org.hypergraphdb.HyperGraph)
	 */
	@Override
	public void setHyperGraph(HyperGraph graph) {
		this.graph = graph;
	}
	
	public HyperGraph getHyperGraph() {
		return graph;
	}
	
	/**
	 * Checks, if this change would not cause a modification to the given Ontology.
	 * If not, we consider the change a conflicting change, which might be a
	 * double addition, double removal, set from the same old to the same newvalue.
	 * Even such changes would be noops if applied forwards, they must not be reverted as this 
	 * breaks the logical relationship of the workingset with the history. 
	 * (E.g. [c1(Add A), c2(Add A), revert(c2(Add A)] would remove A from workingset, 
	 * despite c1 implying that A exists) 
	 * 
	 * @param o
	 * @return true
	 */
	public abstract boolean isConflict(OWLOntology o);

//	/**
//	 * Checks, if the effect of this change on an ontology is equal to the given change.
//	 * @param c
//	 * @return
//	 */
//	public abstract boolean isEqualTo(VOWLChange c);
//
//	/**
//	 * Checks, if the effect of this change on an ontology is the inverse of the given change.
//	 * @param c
//	 * @return
//	 */
//	public abstract boolean isInverseOf(VOWLChange c);
	
}

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

package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.AddAxiom;

/**
 * Change.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VOWLChange implements HGLink {	
	
	public static boolean isAddChange(VOWLChange c) {
		return c instanceof VAddAxiomChange || c instanceof VAddImportChange || c instanceof VAddOntologyAnnotationChange;
	}

	public static boolean isRemoveChange(VOWLChange c) {
		return c instanceof VRemoveAxiomChange || c instanceof VRemoveAxiomChange || c instanceof VRemoveOntologyAnnotationChange;
	}

	public static boolean isModifyChange(VOWLChange c) {
		return c instanceof VModifyOntologyIDChange;
	}
	
	//public abstract OWLOntologyChange convertToOWLOntologyChange();
	
	//public abstract OWLOntologyChange convertToInverseOWLOntologyChange();
	
	
}

package org.hypergraphdb.app.owl.versioning.change;

import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLAnnotation;

/**
 * AxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class AxiomChange extends VOWLChange {
	
	private HGHandle axiom; 
	
	public AxiomChange(HGHandle...args) {
    	axiom = args[0];
    }

	public HGHandle getAxiom() {
		return axiom;
	}
	
	//Set<HGHandle> getEntities();
}

package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VAddAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddAxiomChange extends VAxiomChange {
    
	public VAddAxiomChange(HGHandle...args) {
    	super(args[0]);
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.change.VOWLChange#isConflict(org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public boolean isConflict(OWLOntology o) {
		return o.containsAxiom(getAxiom());
	}
	
}

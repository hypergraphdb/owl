package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * VRemoveImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveImportChange extends VImportChange {
		
	public VRemoveImportChange(HGHandle...args) {
    	super(args[0]);
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.change.VOWLChange#isConflict(org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	public boolean isConflict(OWLOntology o) {
		return !o.getImportsDeclarations().contains(getImportDeclaration());
	}
}

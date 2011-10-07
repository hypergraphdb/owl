package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLPropertyAxiom;

/**
 * OWLPropertyAxiomHGDB.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 7, 2011
 */
public abstract class OWLPropertyAxiomHGDB extends OWLLogicalAxiomHGDB implements OWLPropertyAxiom {
	
    protected OWLPropertyAxiomHGDB(Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
    }

}

package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;

/**
 * OWLClassAxiomHGDB.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 5, 2011
 */
public abstract class OWLClassAxiomHGDB extends OWLLogicalAxiomHGDB implements OWLClassAxiom {

    protected OWLClassAxiomHGDB(Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
    }

}

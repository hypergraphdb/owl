package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;

/**
 * OWLIndividualAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public abstract class OWLIndividualAxiomHGDB extends OWLLogicalAxiomHGDB implements OWLIndividualAxiom{

    public OWLIndividualAxiomHGDB(Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
    }
}
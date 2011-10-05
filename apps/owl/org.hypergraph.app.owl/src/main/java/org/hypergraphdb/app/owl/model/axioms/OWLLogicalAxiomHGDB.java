package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Set;

import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLLogicalAxiomHGDB.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 5, 2011
 */
public abstract class OWLLogicalAxiomHGDB extends OWLAxiomHGDB implements OWLLogicalAxiom {

    protected OWLLogicalAxiomHGDB(Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
    }

    public boolean isLogicalAxiom() {
        return true;
    }

    public boolean isAnnotationAxiom() {
        return false;
    }
}

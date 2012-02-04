package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLSameIndividualAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public class OWLSameIndividualAxiomHGDB extends OWLNaryIndividualAxiomHGDB implements OWLSameIndividualAxiom {

    public OWLSameIndividualAxiomHGDB(HGHandle...args) {
    	super(args);
    }

    public OWLSameIndividualAxiomHGDB(Set<? extends HGHandle> individuals, Set<? extends OWLAnnotation> annotations) {
    	//Set<? extends OWLIndividual> individuals, Set<? extends OWLAnnotation> annotations
        super(individuals, annotations);
    }

    public OWLSameIndividualAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLSameIndividualAxiom(getIndividuals());
    }

    public OWLSameIndividualAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLSameIndividualAxiom(getIndividuals(), mergeAnnos(annotations));
    }

    public Set<OWLSameIndividualAxiom> asPairwiseAxioms() {
        List<OWLIndividual> inds = getIndividualsAsList();
        Set<OWLSameIndividualAxiom> result = new HashSet<OWLSameIndividualAxiom>();
        for (int i = 0; i < inds.size() - 1; i++) {
            OWLIndividual indI = inds.get(i);
            OWLIndividual indJ = inds.get(i + 1);
            result.add(getOWLDataFactory().getOWLSameIndividualAxiom(indI, indJ));
        }
        return result;
    }

    /**
     * Determines whether this axiom contains anonymous individuals.  Anonymous individuals are not allowed in
     * same individuals axioms.
     * @return <code>true</code> if this axioms contains anonymous individual axioms
     */
    public boolean containsAnonymousIndividuals() {
        for (OWLIndividual ind : getIndividuals()) {
            if (ind.isAnonymous()) {
                return true;
            }
        }
        return false;
    }

    public Set<OWLSubClassOfAxiom> asOWLSubClassOfAxioms() {
        List<OWLClassExpression> nominalsList = new ArrayList<OWLClassExpression>();
        for (OWLIndividual individual : getIndividuals()) {
            nominalsList.add(getOWLDataFactory().getOWLObjectOneOf(individual));
        }
        Set<OWLSubClassOfAxiom> result = new HashSet<OWLSubClassOfAxiom>();
        for (int i = 0; i < nominalsList.size() - 1; i++) {
            OWLClassExpression ceI = nominalsList.get(i);
            OWLClassExpression ceJ = nominalsList.get(i + 1);
            result.add(getOWLDataFactory().getOWLSubClassOfAxiom(ceI, ceJ));
            result.add(getOWLDataFactory().getOWLSubClassOfAxiom(ceJ, ceI));
        }
        return result;
    }

    public Set<OWLSameIndividualAxiom> asPairwiseSameIndividualAxioms() {
        List<OWLIndividual> individuals = new ArrayList<OWLIndividual>(getIndividuals());
        Set<OWLSameIndividualAxiom> result = new HashSet<OWLSameIndividualAxiom>();
        for (int i = 0; i < individuals.size() - 1; i++) {
            OWLIndividual indI = individuals.get(i);
            OWLIndividual indJ = individuals.get(i + 1);
            result.add(getOWLDataFactory().getOWLSameIndividualAxiom(indI, indJ));
        }
        return result;
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLSameIndividualAxiom;
        }
        return false;
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.SAME_INDIVIDUAL;
    }
}
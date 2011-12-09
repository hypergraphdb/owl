package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLNegativeDataPropertyAssertionAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public class OWLNegativeDataPropertyAssertionAxiomHGDB extends OWLIndividualRelationshipAxiomHGDB<OWLDataPropertyExpression, OWLLiteral> implements OWLNegativeDataPropertyAssertionAxiom {

    public OWLNegativeDataPropertyAssertionAxiomHGDB(HGHandle...args) {
    	super(args);
    }

    public OWLNegativeDataPropertyAssertionAxiomHGDB(HGHandle subject, HGHandle property, HGHandle object, Collection<? extends OWLAnnotation> annotations) {
    	//OWLIndividual subject, OWLDataPropertyExpression property, OWLLiteral value, Set<? extends OWLAnnotation> annotations
        super(subject, property, object, annotations);
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        OWLDataFactory df = getOWLDataFactory();
        return df.getOWLSubClassOfAxiom(df.getOWLObjectOneOf(getSubject()), df.getOWLObjectComplementOf(df.getOWLDataHasValue(getProperty(), getObject())));
    }

    public OWLNegativeDataPropertyAssertionAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLNegativeDataPropertyAssertionAxiom(getProperty(), getSubject(), getObject());
    }

    public OWLNegativeDataPropertyAssertionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLNegativeDataPropertyAssertionAxiom(getProperty(), getSubject(), getObject(), mergeAnnos(annotations));
    }

    /**
     * Determines whether this axiom contains anonymous individuals.  Anonymous individuals are not allowed in
     * negative data property assertion axioms.
     * @return <code>true</code> if this axioms contains anonymous individual axioms
     */
    public boolean containsAnonymousIndividuals() {
        return getSubject().isAnonymous();
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLNegativeDataPropertyAssertionAxiom;
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
        return AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION;
    }
}
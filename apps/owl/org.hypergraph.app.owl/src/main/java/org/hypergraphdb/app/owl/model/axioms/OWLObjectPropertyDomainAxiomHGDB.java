package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLObjectPropertyDomainAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLObjectPropertyDomainAxiomHGDB  extends OWLPropertyDomainAxiomHGDB<OWLObjectPropertyExpression> implements OWLObjectPropertyDomainAxiom {
    
    public OWLObjectPropertyDomainAxiomHGDB(HGHandle...args) {    
        //TODO assert args[0] type OWLObjectPropertyExpression, args[1] type OWLClassExpression
    	super(args[0], args[1], Collections.<OWLAnnotation>emptySet());   
    	if (args.length != 2) throw new IllegalArgumentException("args.length must be 2. Was " + args.length);
    }

	public OWLObjectPropertyDomainAxiomHGDB(HGHandle property, HGHandle domain, Set<? extends OWLAnnotation> annotations) {
		//OWLObjectPropertyExpression property, OWLClassExpression domain, Set<? extends OWLAnnotation> annotations
        super(property, domain, annotations);
    }

    public OWLObjectPropertyDomainAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLObjectPropertyDomainAxiom(getProperty(), getDomain());
    }

    public OWLObjectPropertyDomainAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLObjectPropertyDomainAxiom(getProperty(), getDomain(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectPropertyDomainAxiom;
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
        return AxiomType.OBJECT_PROPERTY_DOMAIN;
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        OWLDataFactory df = getOWLDataFactory();
        OWLClassExpression sub = df.getOWLObjectSomeValuesFrom(getProperty(), df.getOWLThing());
        return df.getOWLSubClassOfAxiom(sub, getDomain());
    }	
}
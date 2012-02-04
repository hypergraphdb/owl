package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLFunctionalObjectPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLFunctionalObjectPropertyAxiomHGDB extends OWLObjectPropertyCharacteristicAxiomHGDB implements OWLFunctionalObjectPropertyAxiom {
	
    public OWLFunctionalObjectPropertyAxiomHGDB(HGHandle...args) {
    	this(args[0], Collections.<OWLAnnotation>emptySet());
    	if (args[0] == null) throw new IllegalArgumentException("args[0] was null");
    }

    public OWLFunctionalObjectPropertyAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations) {
    	//OWLObjectPropertyExpression property, Collection<? extends OWLAnnotation> annotations
        super(property, annotations);
    	if (property == null) throw new IllegalArgumentException("property was null");        
    }

    public OWLFunctionalObjectPropertyAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(getProperty());
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLFunctionalObjectPropertyAxiom;
        }
        return false;
    }

    public OWLFunctionalObjectPropertyAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLFunctionalObjectPropertyAxiom(getProperty(), mergeAnnos(annotations));
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
        return AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        OWLDataFactory df = getOWLDataFactory();
        return df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectMaxCardinality(1, getProperty()));
    }
}
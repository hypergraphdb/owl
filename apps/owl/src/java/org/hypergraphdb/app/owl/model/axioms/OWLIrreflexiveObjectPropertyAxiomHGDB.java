package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLIrreflexiveObjectPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLIrreflexiveObjectPropertyAxiomHGDB extends OWLObjectPropertyCharacteristicAxiomHGDB implements OWLIrreflexiveObjectPropertyAxiom {
	
    public OWLIrreflexiveObjectPropertyAxiomHGDB(HGHandle...args) {
    	this(args[0], Collections.<OWLAnnotation>emptySet());
    	if (args[0] == null) throw new IllegalArgumentException("args[0] was null");
    }

    public OWLIrreflexiveObjectPropertyAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations) {
    	//OWLObjectPropertyExpression property, Collection<? extends OWLAnnotation> annotations
        super(property, annotations);
    	if (property == null) throw new IllegalArgumentException("property was null");        
    }

    public OWLIrreflexiveObjectPropertyAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(getProperty());
    }

    public OWLIrreflexiveObjectPropertyAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLIrreflexiveObjectPropertyAxiom(getProperty(), mergeAnnos(annotations));
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        return getOWLDataFactory().getOWLSubClassOfAxiom(getOWLDataFactory().getOWLThing(), getOWLDataFactory().getOWLObjectComplementOf(getOWLDataFactory().getOWLObjectHasSelf(getProperty())));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLIrreflexiveObjectPropertyAxiom;
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
        return AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
    }
}
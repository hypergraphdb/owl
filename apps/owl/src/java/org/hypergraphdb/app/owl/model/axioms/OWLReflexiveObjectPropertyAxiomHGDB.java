package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLReflexiveObjectPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLReflexiveObjectPropertyAxiomHGDB extends OWLObjectPropertyCharacteristicAxiomHGDB implements OWLReflexiveObjectPropertyAxiom {
   
    public OWLReflexiveObjectPropertyAxiomHGDB(HGHandle...args) {
    	this(args[0], Collections.<OWLAnnotation>emptySet());
    	if (args[0] == null) throw new IllegalArgumentException("args[0] was null");
    }

	public OWLReflexiveObjectPropertyAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations) {
		//OWLObjectPropertyExpression property, Collection<? extends OWLAnnotation> annotations
        super(property, annotations);
    	if (property == null) throw new IllegalArgumentException("property was null");
    }

    public OWLReflexiveObjectPropertyAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(getProperty());
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        return getOWLDataFactory().getOWLSubClassOfAxiom(getOWLDataFactory().getOWLThing(), getOWLDataFactory().getOWLObjectHasSelf(getProperty()));
    }

    public OWLReflexiveObjectPropertyAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLReflexiveObjectPropertyAxiom(getProperty(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLReflexiveObjectPropertyAxiom;
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
        return AxiomType.REFLEXIVE_OBJECT_PROPERTY;
    }
}
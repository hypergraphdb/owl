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
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLObjectPropertyRangeAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLObjectPropertyRangeAxiomHGDB extends OWLPropertyRangeAxiomHGDB<OWLObjectPropertyExpression, OWLClassExpression> implements OWLObjectPropertyRangeAxiom {
	
    public OWLObjectPropertyRangeAxiomHGDB(HGHandle...args) {    
        //TODO assert arg[0] type OWLObjectPropertyExpression, args[1] type OWLClassExpression
    	super(args[0], args[1], Collections.<OWLAnnotation>emptySet());   
    	if (args.length != 2) throw new IllegalArgumentException("args.length must be 2. Was " + args.length);
    }

	public OWLObjectPropertyRangeAxiomHGDB(HGHandle property, HGHandle range, Set<? extends OWLAnnotation> annotations) {
		//OWLObjectPropertyExpression property, OWLClassExpression range, Set<? extends OWLAnnotation> annotations
        super(property, range, annotations);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectPropertyRangeAxiom;
        }
        return false;
    }

    public OWLObjectPropertyRangeAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLObjectPropertyRangeAxiom(getProperty(), getRange());
    }

    public OWLObjectPropertyRangeAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLObjectPropertyRangeAxiom(getProperty(), getRange(), mergeAnnos(annotations));
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
        return AxiomType.OBJECT_PROPERTY_RANGE;
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        OWLDataFactory df = getOWLDataFactory();
        OWLClassExpression sup = df.getOWLObjectAllValuesFrom(getProperty(), getRange());
        return df.getOWLSubClassOfAxiom(df.getOWLThing(), sup);
    }
}
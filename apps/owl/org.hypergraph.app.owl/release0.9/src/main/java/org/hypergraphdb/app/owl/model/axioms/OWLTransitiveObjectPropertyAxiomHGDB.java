package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

/**
 * OWLTransitiveObjectPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLTransitiveObjectPropertyAxiomHGDB extends OWLObjectPropertyCharacteristicAxiomHGDB implements OWLTransitiveObjectPropertyAxiom {
    
    public OWLTransitiveObjectPropertyAxiomHGDB(HGHandle...args) {
    	this(args[0], Collections.<OWLAnnotation>emptySet());
    	if (args[0] == null) throw new IllegalArgumentException("args[0] was null");
    }
	
	public OWLTransitiveObjectPropertyAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations) {
		//OWLObjectPropertyExpression property, Collection<? extends OWLAnnotation> annotations
        super(property, annotations);
    	if (property == null) throw new IllegalArgumentException("property was null");                
    }

    public OWLTransitiveObjectPropertyAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(getProperty());
    }

    public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLTransitiveObjectPropertyAxiom(getProperty(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLTransitiveObjectPropertyAxiom;
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
        return AxiomType.TRANSITIVE_OBJECT_PROPERTY;
    }
}
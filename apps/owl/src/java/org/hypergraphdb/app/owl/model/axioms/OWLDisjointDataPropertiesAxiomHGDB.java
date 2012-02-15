package org.hypergraphdb.app.owl.model.axioms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;


/**
 * OWLDisjointDataPropertiesAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 7, 2011
 */
public class OWLDisjointDataPropertiesAxiomHGDB extends OWLNaryPropertyAxiomHGDB<OWLDataPropertyExpression> implements OWLDisjointDataPropertiesAxiom {

    public OWLDisjointDataPropertiesAxiomHGDB(HGHandle...args){
    	this(new HashSet<HGHandle>(Arrays.asList(args)), Collections.<OWLAnnotation>emptySet());
    	if (new HashSet<HGHandle>(Arrays.asList(args)).size() != args.length) throw new IllegalArgumentException("Duplicates in args not allowed. " + args);
    }
    
	public OWLDisjointDataPropertiesAxiomHGDB(Set<? extends HGHandle> properties, Collection<? extends OWLAnnotation> annotations) {
		//Set<? extends OWLDataPropertyExpression> properties, Collection<? extends OWLAnnotation> annotations
        super(properties, annotations);
    }

    public OWLDisjointDataPropertiesAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(getProperties());
    }

    public OWLDisjointDataPropertiesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLDisjointDataPropertiesAxiom(getProperties(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLDisjointDataPropertiesAxiom;
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
        return AxiomType.DISJOINT_DATA_PROPERTIES;
    }
}
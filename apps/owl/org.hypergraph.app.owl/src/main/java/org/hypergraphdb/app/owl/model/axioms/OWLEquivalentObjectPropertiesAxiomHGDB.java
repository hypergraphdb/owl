package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

/**
 * OWLEquivalentObjectPropertiesAxiomImpl.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLEquivalentObjectPropertiesAxiomHGDB extends OWLNaryPropertyAxiomHGDB<OWLObjectPropertyExpression> implements OWLEquivalentObjectPropertiesAxiom {
    
    public OWLEquivalentObjectPropertiesAxiomHGDB(HGHandle...args){
    	this(new HashSet<HGHandle>(Arrays.asList(args)), Collections.<OWLAnnotation>emptySet());
    	if (new HashSet<HGHandle>(Arrays.asList(args)).size() != args.length) throw new IllegalArgumentException();
    }

	public OWLEquivalentObjectPropertiesAxiomHGDB(Set<HGHandle> properties, Collection<? extends OWLAnnotation> annotations) {
		//Set<? extends OWLObjectPropertyExpression> properties, Collection<? extends OWLAnnotation> annotations
        super(properties, annotations);
    }

    public OWLEquivalentObjectPropertiesAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLEquivalentObjectPropertiesAxiom(getProperties());
    }

    public OWLEquivalentObjectPropertiesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLEquivalentObjectPropertiesAxiom(getProperties(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLEquivalentObjectPropertiesAxiom;
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
        return AxiomType.EQUIVALENT_OBJECT_PROPERTIES;
    }

    public Set<OWLSubObjectPropertyOfAxiom> asSubObjectPropertyOfAxioms() {
        List<OWLObjectPropertyExpression> props = new ArrayList<OWLObjectPropertyExpression>(getProperties());
        Set<OWLSubObjectPropertyOfAxiom> axs = new HashSet<OWLSubObjectPropertyOfAxiom>();
        for (int i = 0; i < props.size() - 1; i++) {
            for (int j = i + 1; j < props.size(); j++) {
                axs.add(getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(props.get(i), props.get(j)));
            }
        }
        return axs;
    }
}

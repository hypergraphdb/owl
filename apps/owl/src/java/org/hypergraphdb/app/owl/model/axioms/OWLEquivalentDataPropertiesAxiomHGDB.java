package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;


/**
 * OWLEquivalentDataPropertiesAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 7, 2011
 */
public class OWLEquivalentDataPropertiesAxiomHGDB extends OWLNaryPropertyAxiomHGDB<OWLDataPropertyExpression> implements OWLEquivalentDataPropertiesAxiom {

    
    public OWLEquivalentDataPropertiesAxiomHGDB(HGHandle...args){
    	this(new HashSet<HGHandle>(Arrays.asList(args)), Collections.<OWLAnnotation>emptySet());
    	if (new HashSet<HGHandle>(Arrays.asList(args)).size() != args.length) throw new IllegalArgumentException();
    }

    public OWLEquivalentDataPropertiesAxiomHGDB(Set<? extends HGHandle> properties, Collection<? extends OWLAnnotation> annotations) {
    	//Set<? extends OWLDataPropertyExpression> properties, Collection<? extends OWLAnnotation> annotations
        super(properties, annotations);
    }

    public OWLEquivalentDataPropertiesAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLEquivalentDataPropertiesAxiom(getProperties());
    }

    public OWLEquivalentDataPropertiesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLEquivalentDataPropertiesAxiom(getProperties(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLEquivalentDataPropertiesAxiom;
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
        return AxiomType.EQUIVALENT_DATA_PROPERTIES;
    }
    
    public Set<OWLSubDataPropertyOfAxiom> asSubDataPropertyOfAxioms() {
        List<OWLDataPropertyExpression> props = new ArrayList<OWLDataPropertyExpression>(getProperties());
        Set<OWLSubDataPropertyOfAxiom> axs = new HashSet<OWLSubDataPropertyOfAxiom>();
        for (int i = 0; i < props.size() - 1; i++) {
            for (int j = i + 1; j < props.size(); j++) {
                axs.add(getOWLDataFactory().getOWLSubDataPropertyOfAxiom(props.get(i), props.get(j)));
            }
        }
        return axs;
    }
}
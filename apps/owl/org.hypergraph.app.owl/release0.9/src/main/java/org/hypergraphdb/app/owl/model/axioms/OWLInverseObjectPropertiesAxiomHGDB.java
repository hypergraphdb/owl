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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

/**
 * OWLInverseObjectPropertiesAxiomHGDB.
 * 
 * 2011.11.07 we cannot use a set for the properties, because OWL-API test testInverseSelf implies that 
 * both properties might be equal and therefore we use a list.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLInverseObjectPropertiesAxiomHGDB extends OWLNaryPropertyAxiomHGDB<OWLObjectPropertyExpression> implements OWLInverseObjectPropertiesAxiom {
    //private OWLObjectPropertyExpression first;
    //private OWLObjectPropertyExpression second;

    public OWLInverseObjectPropertiesAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    	if (args.length != 2) throw new IllegalArgumentException("args.length was not 2 but " + args.length);
    }

    public OWLInverseObjectPropertiesAxiomHGDB(HGHandle first, HGHandle second, Collection<? extends OWLAnnotation> annotations) {
    	//OWLObjectPropertyExpression first, OWLObjectPropertyExpression second, Collection<? extends OWLAnnotation> annotations
    	super(Arrays.asList(first, second), annotations);
        //super(new TreeSet<HGHandle>(Arrays.asList(first, second)), annotations);
        //super(new TreeSet<OWLObjectPropertyExpression>(Arrays.asList(first, second)), annotations);
        //this.first = first;
        //this.second = second;
    }

    public OWLInverseObjectPropertiesAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(getFirstProperty(), getSecondProperty());
    }

    public OWLInverseObjectPropertiesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLInverseObjectPropertiesAxiom(getFirstProperty(), getSecondProperty(), mergeAnnos(annotations));
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public OWLObjectPropertyExpression getFirstProperty() {
        return getHyperGraph().get(getTargetAt(0));
    }

    public OWLObjectPropertyExpression getSecondProperty() {
        return getHyperGraph().get(getTargetAt(1));
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLInverseObjectPropertiesAxiom;
        }
        return false;
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.INVERSE_OBJECT_PROPERTIES;
    }

    public Set<OWLSubObjectPropertyOfAxiom> asSubObjectPropertyOfAxioms() {
        Set<OWLSubObjectPropertyOfAxiom> axs = new HashSet<OWLSubObjectPropertyOfAxiom>();
        OWLDataFactory df = getOWLDataFactory();
        axs.add(df.getOWLSubObjectPropertyOfAxiom(getFirstProperty(), getSecondProperty().getInverseProperty().getSimplified()));
        axs.add(df.getOWLSubObjectPropertyOfAxiom(getSecondProperty(), getFirstProperty().getInverseProperty().getSimplified()));
        return axs;
    }
}
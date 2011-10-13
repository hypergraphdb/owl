package org.hypergraphdb.app.owl.model.axioms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLDisjointClassesAxiomImpl.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 13, 2011
 */
public class OWLDisjointClassesAxiomHGDB extends OWLNaryClassAxiomHGDB implements
		OWLDisjointClassesAxiom {

    public OWLDisjointClassesAxiomHGDB(Set<? extends HGHandle> classExpressions, Set<? extends OWLAnnotation> annotations) {
    	//TODO assert HGHandle atom type extends OWLClassExpression
        super(classExpressions, annotations);
    }

    public OWLDisjointClassesAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLDisjointClassesAxiom(getClassExpressions());
    }

    public OWLDisjointClassesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLDisjointClassesAxiom(getClassExpressions(), mergeAnnos(annotations));
    }

    @Override
	public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof OWLDisjointClassesAxiom;
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
        return AxiomType.DISJOINT_CLASSES;
    }

    public Set<OWLDisjointClassesAxiom> asPairwiseAxioms() {
        Set<OWLDisjointClassesAxiom> result = new HashSet<OWLDisjointClassesAxiom>();
        List<OWLClassExpression> list = getClassExpressionsAsList();
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                result.add(getOWLDataFactory().getOWLDisjointClassesAxiom(list.get(i), list.get(j)));
            }
        }
        return result;
    }

    public Set<OWLSubClassOfAxiom> asOWLSubClassOfAxioms() {
        Set<OWLSubClassOfAxiom> result = new HashSet<OWLSubClassOfAxiom>();
        List<OWLClassExpression> list = getClassExpressionsAsList();
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                result.add(getOWLDataFactory().getOWLSubClassOfAxiom(list.get(i), list.get(j).getObjectComplementOf()));
            }
        }
        return result;
    }


}

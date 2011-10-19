package org.hypergraphdb.app.owl.model.classexpr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectUnionOfHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public class OWLObjectUnionOfHGDB extends OWLNaryBooleanClassExpressionHGDB implements OWLObjectUnionOf {

    public OWLObjectUnionOfHGDB(HGHandle...args) {
    	super(args);
    	// no duplicates allowed
    	assert(new TreeSet<HGHandle>(Arrays.asList(args)).size() == args.length);
    }

    public OWLObjectUnionOfHGDB(Set<? extends HGHandle> operands) {
        super(operands);
    }

    /**
     * Gets the class expression type for this class expression
     * @return The class expression type
     */
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OBJECT_UNION_OF;
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectUnionOf;
        }
        return false;
    }


    @Override
	public Set<OWLClassExpression> asDisjunctSet() {
        Set<OWLClassExpression> disjuncts = new HashSet<OWLClassExpression>();
        for (OWLClassExpression op : getOperands()) {
            disjuncts.addAll(op.asDisjunctSet());
        }
        return disjuncts;
    }


    public void accept(OWLClassExpressionVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

}

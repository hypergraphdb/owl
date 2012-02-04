package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectSomeValuesFromHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLObjectSomeValuesFromHGDB extends OWLQuantifiedObjectRestrictionHGDB implements OWLObjectSomeValuesFrom {
    
	/**
	 * @param args [0]...property, [1]...filler
	 */
    public OWLObjectSomeValuesFromHGDB(HGHandle... args) {
    	super(args[0], args[1]);
    	if (args.length != 2) throw new IllegalArgumentException("Must be exactly 2 handles.");
    }
	
    public OWLObjectSomeValuesFromHGDB(HGHandle property, int cardinality, HGHandle filler) {
    	//TODO check types: OWLObjectPropertyExpression property, OWLClassExpression filler
        super(property, filler);
    }
    
    /**
     * Gets the class expression type for this class expression
     * @return The class expression type
     */
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OBJECT_SOME_VALUES_FROM;
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectSomeValuesFrom;
        }
        return false;
    }

    public boolean isObjectRestriction() {
        return true;
    }

    public boolean isDataRestriction() {
        return false;
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

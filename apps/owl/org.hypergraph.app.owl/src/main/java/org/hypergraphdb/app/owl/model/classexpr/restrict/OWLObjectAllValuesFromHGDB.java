package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectAllValuesFromHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLObjectAllValuesFromHGDB extends OWLQuantifiedObjectRestrictionHGDB implements OWLObjectAllValuesFrom {
    
	/**
	 * @param args [0]...property, [1]...filler
	 */
    public OWLObjectAllValuesFromHGDB(HGHandle... args) {
    	super(args[0], args[1]);
    	if (args.length != 2) throw new IllegalArgumentException("Must be exactly 2 handles.");
    }
	
    public OWLObjectAllValuesFromHGDB(HGHandle property, int cardinality, HGHandle filler) {
    	//TODO check types: OWLObjectPropertyExpression property, OWLClassExpression filler
        super(property, filler);
    }

    /**
     * Gets the class expression type for this class expression
     * @return The class expression type
     */
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OBJECT_ALL_VALUES_FROM;
    }

    public boolean isObjectRestriction() {
        return true;
    }

    public boolean isDataRestriction() {
        return false;
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectAllValuesFrom;
        }
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

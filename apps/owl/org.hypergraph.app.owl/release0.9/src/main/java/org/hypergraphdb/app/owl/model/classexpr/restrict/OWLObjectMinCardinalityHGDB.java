package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectMinCardinalityHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLObjectMinCardinalityHGDB extends OWLObjectCardinalityRestrictionHGDB implements OWLObjectMinCardinality {
    
	/**
	 * @param args [0]...property, [1]...filler
	 */
    public OWLObjectMinCardinalityHGDB(HGHandle... args) {
    	super(args[0], 0, args[1]);
    	//TODO we call with 0 cardinality here, test that HG sets it later.
    	if (args.length != 2) throw new IllegalArgumentException("Must be exactly 2 handles.");
    }
	
    public OWLObjectMinCardinalityHGDB(HGHandle property, int cardinality, HGHandle filler) {
    	//TODO check types: OWLObjectPropertyExpression property, OWLClassExpression filler
        super(property, cardinality, filler);
    }

    /**
     * Gets the class expression type for this class expression
     * @return The class expression type
     */
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OBJECT_MIN_CARDINALITY;
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLObjectMinCardinality;
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

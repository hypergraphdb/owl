package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;

/**
 * OWLCardinalityRestrictionHGDB.
 *
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public abstract class OWLCardinalityRestrictionHGDB<R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, F extends OWLPropertyRange> extends OWLRestrictionHGDB<R, P, F> implements
		OWLCardinalityRestriction<R, P, F> {
  
	//TODO need a type and subsumes for that or a getter and setter.
	private int cardinality;

    private HGHandle fillerHandle;
    //private F filler;


    protected OWLCardinalityRestrictionHGDB(HGHandle property, int cardinality, HGHandle filler) {
        super(property);
        this.cardinality = cardinality;
        fillerHandle = filler;
    }


    public int getCardinality() {
        return cardinality;
    }


    public F getFiller() {    	
        return getHyperGraph().get(fillerHandle);
    }


    @Override
	public boolean equals(Object obj) {
            if(super.equals(obj)) {
                if(!(obj instanceof OWLCardinalityRestriction)) {
                    return false;
                }
                OWLCardinalityRestriction<R, P, F> other = (OWLCardinalityRestriction<R, P, F>) obj;
                return other.getCardinality() == cardinality &&
                        other.getFiller().equals(getFiller());
            }
        return false;
    }


    @Override
	final protected int compareObjectOfSameType(OWLObject object) {
        OWLCardinalityRestriction<R, P, F> other = (OWLCardinalityRestriction<R, P, F>) object;
        int diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        diff = getCardinality() - other.getCardinality();
        if (diff != 0) {
            return diff;
        }
        return getFiller().compareTo(other.getFiller());
    }

}

package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLDataCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;

/**
 * OWLDataCardinalityRestrictionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public abstract class OWLDataCardinalityRestrictionHGDB extends OWLCardinalityRestrictionHGDB<OWLDataRange, OWLDataPropertyExpression, OWLDataRange> implements
		OWLDataCardinalityRestriction {
	
    protected OWLDataCardinalityRestrictionHGDB(HGHandle property, int cardinality, HGHandle filler) {
        super(property, cardinality, filler);
    }


    public boolean isQualified() {
        return !getFiller().equals(getOWLDataFactory().getTopDatatype());
    }

    public boolean isObjectRestriction() {
        return false;
    }

    public boolean isDataRestriction() {
        return true;
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return obj instanceof OWLDataCardinalityRestriction;
        }
        return false;
    }

}

package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * OWLObjectCardinalityRestrictionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public abstract class OWLObjectCardinalityRestrictionHGDB extends OWLCardinalityRestrictionHGDB<OWLClassExpression, OWLObjectPropertyExpression, OWLClassExpression> implements
		OWLObjectCardinalityRestriction {

    protected OWLObjectCardinalityRestrictionHGDB(HGHandle property, int cardinality, HGHandle filler) {
    	//TODO check types: OWLObjectPropertyExpression property, OWLClassExpression filler
        super(property, cardinality, filler);
    }

    public boolean isQualified() {
        return getFiller().isAnonymous() || !getFiller().isOWLThing();
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
            return obj instanceof OWLObjectCardinalityRestriction;
        }
        return false;
    }
}

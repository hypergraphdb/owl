package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.model.classexpr.OWLAnonymousClassExpressionHGDB;
import org.hypergraphdb.type.HGHandleType;
import org.semanticweb.owlapi.model.OWLCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLRestriction;

/**
 * OWLRestrictionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public abstract class OWLRestrictionHGDB<R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, F> extends OWLAnonymousClassExpressionHGDB implements HGLink, OWLRestriction<R, P, F> {

    private HGHandle propertyHandle;
    //private P property;

    public OWLRestrictionHGDB(HGHandle property) {
        propertyHandle = property;
    }


    public boolean isClassExpressionLiteral() {
        return false;
    }


    public P getProperty() {
    	return getHyperGraph().get(propertyHandle);
        //return property;
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLRestriction)) {
                return false;
            }
            return ((OWLRestriction<?,?,?>) obj).getProperty().equals(getProperty());
        }
        return false;
    }
}

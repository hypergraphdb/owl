package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.model.classexpr.OWLAnonymousClassExpressionHGDB;
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
    	if (property == null) throw new IllegalArgumentException("Property was null");
        propertyHandle = property;
    }

    public boolean isClassExpressionLiteral() {
        return false;
    }


    public P getProperty() {
    	return (P)getHyperGraph().get(propertyHandle);
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
    

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 * This will be overridden in subclasses.
	 */
	@Override
	public int getArity() {
		return 1;
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i < 0 || i >= getArity()) throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		return propertyHandle;
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i < 0 || i >= getArity()) throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		propertyHandle = handle;		
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i < 0 || i >= getArity()) throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		propertyHandle = null;
	}
}

package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLUnaryPropertyAxiom;

/**
 * OWLUnaryPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public abstract class OWLUnaryPropertyAxiomHGDB <P extends OWLPropertyExpression<?,?>> extends OWLPropertyAxiomHGDB implements HGLink, OWLUnaryPropertyAxiom<P> {

	private HGHandle propertyHandle;
    //private P property;

    public OWLUnaryPropertyAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations) {
    	//P property, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        propertyHandle = property;
    }

    public P getProperty() {
        return getHyperGraph().<P>get(propertyHandle);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLUnaryPropertyAxiom)) {
                return false;
            }
            return ((OWLUnaryPropertyAxiom<?>) obj).getProperty().equals(getProperty());
        }
        return false;
    }
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
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
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		return propertyHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		propertyHandle = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		propertyHandle= getHyperGraph().getHandleFactory().nullHandle();
	}
}
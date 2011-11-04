package org.hypergraphdb.app.owl.model.axioms;

import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

/**
 * OWLPropertyDomainAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public abstract class OWLPropertyDomainAxiomHGDB<P extends OWLPropertyExpression<?,?>> extends OWLUnaryPropertyAxiomHGDB<P> implements OWLPropertyDomainAxiom<P> {

	private HGHandle domainHandle;
    //private OWLClassExpression domain;


    public OWLPropertyDomainAxiomHGDB(HGHandle property, HGHandle domain, Set<? extends OWLAnnotation> annotations) {
    	//P property, OWLClassExpression domain, Set<? extends OWLAnnotation> annotations
    	super(property, annotations);
        this.domainHandle = domain;
    }


    public OWLClassExpression getDomain() {
        return getHyperGraph().get(domainHandle);
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLPropertyDomainAxiom)) {
                return false;
            }
            return ((OWLPropertyDomainAxiom<?>) obj).getDomain().equals(getDomain());
        }
        return false;
    }


    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLPropertyDomainAxiom<?> other = (OWLPropertyDomainAxiom<?>) object;
        int diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        return getDomain().compareTo(other.getDomain());
    }
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return super.getArity() + 1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (i == 0) {
			return super.getTargetAt(i);
		} else {
			return domainHandle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			super.notifyTargetHandleUpdate(i, handle);
		} else {
			domainHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			super.notifyTargetRemoved(i);
		} else {
			domainHandle = null;
		}
	}
}

package org.hypergraphdb.app.owl.model.axioms;

import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLPropertyRangeAxiom;

/**
 * OWLPropertyRangeAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public abstract class OWLPropertyRangeAxiomHGDB <P extends OWLPropertyExpression<?,?>, R extends OWLPropertyRange> extends OWLUnaryPropertyAxiomHGDB<P> implements OWLPropertyRangeAxiom<P, R> {

	private HGHandle rangeHandle;
    //private R range;

    public OWLPropertyRangeAxiomHGDB(HGHandle property, HGHandle range, Set<? extends OWLAnnotation> annotations) {
    	//P property, R range, Set<? extends OWLAnnotation> annotations
        super(property, annotations);
        rangeHandle = range;
    }

    public R getRange() {
        return getHyperGraph().get(rangeHandle);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLPropertyRangeAxiom)) {
                return false;
            }
            return ((OWLPropertyRangeAxiom<?,?>) obj).getRange().equals(getRange());
        }
        return false;
    }

    @Override
	final protected int compareObjectOfSameType(OWLObject object) {
        int diff = getProperty().compareTo(((OWLPropertyRangeAxiom<?,?>) object).getProperty());
        if (diff != 0) {
            return diff;
        }
        return getRange().compareTo(((OWLPropertyRangeAxiom<?,?>) object).getRange());
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
			return rangeHandle;
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
			rangeHandle = handle;
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
			rangeHandle = null;
		}
	}
}
package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLHasValueRestriction;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;

/**
 * OWLValueRestrictionHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public abstract class OWLValueRestrictionHGDB<R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, V extends OWLObject>
		extends OWLRestrictionHGDB<R, P, P> implements OWLHasValueRestriction<R, P, V> {

	private HGHandle valueHandle;

	protected OWLValueRestrictionHGDB(HGHandle property, HGHandle value) {
		// TODO check type: V value
		super(property);
    	if (value == null) throw new IllegalArgumentException("Value was null");
		valueHandle = value;
	}

	public V getValue() {
		return (V)getHyperGraph().get(valueHandle);
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (!(obj instanceof OWLHasValueRestriction<?, ?, ?>)) {
				return false;
			}
			return ((OWLHasValueRestriction<?, ?, ?>) obj).getValue().equals(getValue());
		}
		return false;
	}

	@Override
	final protected int compareObjectOfSameType(OWLObject object) {
		OWLHasValueRestriction<?, ?, ?> other = (OWLHasValueRestriction<?, ?, ?>) object;
		int diff = getProperty().compareTo(other.getProperty());
		if (diff != 0) {
			return diff;
		}
		return getValue().compareTo(other.getValue());
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 * This will be overridden in subclasses.
	 */
	@Override
	public int getArity() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i < 0 || i >= getArity()) throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		if (i == 0) {
			return super.getTargetAt(i);
		} else { // i == 1
			return valueHandle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i < 0 || i >= getArity()) throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		if (i == 0) {
			super.notifyTargetHandleUpdate(i, handle);
		} else { // i == 1
			valueHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i < 0 || i >= getArity()) throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		if (i == 0) {
			super.notifyTargetRemoved(i);
		} else { // i == 1
			valueHandle = null;
		}
	}
	/**
	 * Sets the valueHandle to a new value.
	 */
	protected void updateValueHandle(HGHandle handle) {
		if (handle == null) throw new IllegalArgumentException("handle was null.");
		valueHandle = handle;
	}
}

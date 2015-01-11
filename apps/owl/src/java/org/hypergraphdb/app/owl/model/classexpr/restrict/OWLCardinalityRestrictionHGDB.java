package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGException;
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
public abstract class OWLCardinalityRestrictionHGDB<R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, F extends OWLPropertyRange>
		extends OWLRestrictionHGDB<R, P, F> implements OWLCardinalityRestriction<R, P, F>
{
	// TODO need a type and subsumes for that or a getter and setter.
	private int cardinality;

	private HGHandle fillerHandle;

	// private F filler;

	protected OWLCardinalityRestrictionHGDB(HGHandle property, int cardinality, HGHandle filler)
	{
		super(property);
		setCardinality(cardinality);
		// TODO check type F filler
		if (filler == null)
			throw new IllegalArgumentException("Filler was null");
		fillerHandle = filler;
	}

	public int getCardinality()
	{
		return cardinality;
	}

	/**
	 * This method should only be called on creation. (e.g. hypergraph after
	 * loading an object from persistent store.)
	 * 
	 * @param c
	 */
	public void setCardinality(int c)
	{
		if (c < 0)
			throw new IllegalArgumentException("c must be a non negative integer. Was " + c);
		cardinality = c;
	}

	public F getFiller()
	{
		return getHyperGraph().<F> get(fillerHandle);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			if (!(obj instanceof OWLCardinalityRestriction))
			{
				return false;
			}
			@SuppressWarnings("unchecked")
			OWLCardinalityRestriction<R, P, F> other = (OWLCardinalityRestriction<R, P, F>) obj;
			return other.getCardinality() == cardinality && other.getFiller().equals(getFiller());
		}
		return false;
	}

	@Override
	final protected int compareObjectOfSameType(OWLObject object)
	{
		@SuppressWarnings("unchecked")
		OWLCardinalityRestriction<R, P, F> other = (OWLCardinalityRestriction<R, P, F>) object;
		int diff = getProperty().compareTo(other.getProperty());
		if (diff != 0)
		{
			return diff;
		}
		diff = getCardinality() - other.getCardinality();
		if (diff != 0)
		{
			return diff;
		}
		return getFiller().compareTo(other.getFiller());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity() This will be overridden in
	 * subclasses.
	 */
	@Override
	public int getArity()
	{
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i < 0 || i >= getArity())
			throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		if (i == 0)
		{
			return super.getTargetAt(i);
		}
		else
		{ // i == 1
			return fillerHandle;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i < 0 || i >= getArity())
			throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		if (i == 0)
		{
			super.notifyTargetHandleUpdate(i, handle);
		}
		else
		{ // i == 1
			fillerHandle = handle;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i < 0 || i >= getArity())
			throw new HGException("Index i must be within [0..getArity()-1]. Was : " + i);
		if (i == 0)
		{
			super.notifyTargetRemoved(i);
		}
		else
		{ // i == 1
			fillerHandle = getHyperGraph().getHandleFactory().nullHandle();
		}
	}
}

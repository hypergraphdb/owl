package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.util.HGUtils;
import org.hypergraphdb.util.HashCodeUtil;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;

/**
 * OWLQuantifiedRestrictionHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public abstract class OWLQuantifiedRestrictionHGDB<R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, F extends OWLPropertyRange>
		extends OWLRestrictionHGDB<R, P, F> implements
		OWLQuantifiedRestriction<R, P, F>
{
	private static final long serialVersionUID = 1L;
	// private F filler;
	private HGHandle fillerHandle;

	public OWLQuantifiedRestrictionHGDB(HGHandle property, HGHandle filler)
	{
		// /TODO check type P property, F filler
		super(property);
		fillerHandle = filler;
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
			if (obj instanceof OWLQuantifiedRestriction)
			{
				return ((OWLQuantifiedRestriction<?, ?, ?>) obj).getFiller()
						.equals(getFiller());
			}
		}
		return false;
	}
	public int hashCode()
	{
		return HashCodeUtil.hash(super.hashCode(), HGUtils.hashIt(this.getFiller()));
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
			throw new HGException(
					"Index i must be within [0..getArity()-1]. Was : " + i);
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
			throw new HGException(
					"Index i must be within [0..getArity()-1]. Was : " + i);
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
			throw new HGException(
					"Index i must be within [0..getArity()-1]. Was : " + i);
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

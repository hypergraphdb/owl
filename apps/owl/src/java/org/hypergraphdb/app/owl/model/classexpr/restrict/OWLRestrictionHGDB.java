package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.model.classexpr.OWLAnonymousClassExpressionHGDB;
import org.hypergraphdb.util.HGUtils;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLPropertyRange;
import org.semanticweb.owlapi.model.OWLRestriction;

/**
 * OWLRestrictionHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public abstract class OWLRestrictionHGDB<R extends OWLPropertyRange, P extends OWLPropertyExpression<R, P>, F>
		extends OWLAnonymousClassExpressionHGDB implements HGLink,
		OWLRestriction<R, P, F>
{
	private static final long serialVersionUID = 1L;
	
	private HGHandle propertyHandle;

	// private P property;

	public OWLRestrictionHGDB(HGHandle property)
	{
		if (property == null)
			throw new IllegalArgumentException("Property was null");
		propertyHandle = property;
	}

	public boolean isClassExpressionLiteral()
	{
		return false;
	}

	public P getProperty()
	{
		return getHyperGraph().<P> get(propertyHandle);
		// return property;
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
		return 1;
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
		return propertyHandle;
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
		propertyHandle = handle;
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
		propertyHandle = getHyperGraph().getHandleFactory().nullHandle();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 0;
		for (int i = 0; i < getArity(); i++)
			result = prime * result + this.getHyperGraph().get(getTargetAt(i)).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		OWLRestrictionHGDB<?, ?, ?> other = (OWLRestrictionHGDB<?, ?, ?>) obj;
		if (getArity() != other.getArity())
			return false;
		for (int i = 0; i < getArity(); i++)
			if (!getTargetAt(i).equals(other.getTargetAt(i)))
			{
				Object x = this.getHyperGraph().get(getTargetAt(i));
				Object y = this.getHyperGraph().get(other.getTargetAt(i));
				if (!HGUtils.eq(x, y))
					return false;
			}
		return true;
	}
}

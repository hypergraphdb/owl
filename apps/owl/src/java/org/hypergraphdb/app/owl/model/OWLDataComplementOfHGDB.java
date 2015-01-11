package org.hypergraphdb.app.owl.model;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.OWLDataComplementOf;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLRuntimeException;

/**
 * OWLDataComplementOfHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 31, 2011
 */
public class OWLDataComplementOfHGDB extends OWLObjectHGDB implements HGLink, OWLDataComplementOf
{
	private HGHandle dataRangeHandle;

	// private OWLDataRange dataRange;

	public OWLDataComplementOfHGDB(HGHandle... args)
	{
		this(args[0]);
	}

	public OWLDataComplementOfHGDB(HGHandle dataRange)
	{
		// OWLDataRange dataRange
		dataRangeHandle = dataRange;
	}

	public DataRangeType getDataRangeType()
	{
		return DataRangeType.DATA_COMPLEMENT_OF;
	}

	public boolean isDatatype()
	{
		return false;
	}

	public boolean isTopDatatype()
	{
		return false;
	}

	public OWLDataRange getDataRange()
	{
		return getHyperGraph().get(dataRangeHandle);
	}

	public OWLDatatype asOWLDatatype()
	{
		throw new OWLRuntimeException("Not a data type!");
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			if (!(obj instanceof OWLDataComplementOf))
			{
				return false;
			}
			return ((OWLDataComplementOf) obj).getDataRange().equals(getDataRange());
		}
		return false;
	}

	public void accept(OWLDataVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLDataVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public void accept(OWLDataRangeVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLDataRangeVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		OWLDataComplementOf other = (OWLDataComplementOf) object;
		return getDataRange().compareTo(other.getDataRange());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return dataRangeHandle == null ? 0 : 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		return dataRangeHandle;
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
		if (i != 0)
			throw new HGException("Index i must be 0");
		dataRangeHandle = handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		dataRangeHandle = null;
	}
}

package org.hypergraphdb.app.owl.model;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * OWLFacetRestrictionHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 1, 2011
 */
public class OWLFacetRestrictionHGDB extends OWLObjectHGDB implements HGLink, OWLFacetRestriction
{
	private static final long serialVersionUID = 1L;

	private OWLFacet facet;
	// private OWLFacet facet;

	private HGHandle facetValueHandle; // index 0

	// private OWLLiteral facetValue;

	public OWLFacetRestrictionHGDB(HGHandle... args)
	{
		// TODO assert args[0] type OWLLiteral
		assert (args.length == 1);
		facetValueHandle = args[0];
	}

	public OWLFacetRestrictionHGDB(OWLFacet facet, HGHandle facetValue)
	{
		// OWLFacet facet, OWLLiteral facetValue
		this.facet = facet;
		facetValueHandle = facetValue;
	}

	/**
	 * Gets the restricting facet for this facet restriction
	 */
	public OWLFacet getFacet()
	{
		return facet;
	}

	/**
	 * For Hypergraph bean inspection only.
	 * 
	 * @param facet
	 *            the facet to set
	 */
	public void setFacet(OWLFacet facet)
	{
		this.facet = facet;
	}

	/**
	 * Gets the corresponding facet value for this facet restriction
	 */
	public OWLLiteral getFacetValue()
	{
		return getHyperGraph().get(facetValueHandle);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLDataVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public void accept(OWLDataVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		OWLFacetRestriction other = (OWLFacetRestriction) object;
		int diff = getFacet().compareTo(other.getFacet());
		if (diff != 0)
		{
			return diff;
		}
		return getFacetValue().compareTo(other.getFacetValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return facetValueHandle == null ? 0 : 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be [0... " + getArity() + "[");
		return facetValueHandle;
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
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be [0... " + getArity() + "[");
		if (handle == null)
			throw new IllegalArgumentException("handle null");
		facetValueHandle = handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be [0... " + getArity() + "[");
		facetValueHandle = null;
	}
}

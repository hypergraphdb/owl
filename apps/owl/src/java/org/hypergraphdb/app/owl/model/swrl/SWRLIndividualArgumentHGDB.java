package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;

import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

/**
 * SWRLIndividualArgumentHGDB.
 * 
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLIndividualArgumentHGDB extends OWLObjectHGDB implements HGLink, SWRLIndividualArgument
{
	// private OWLIndividual individual;
	private HGHandle individualHandle;

	public SWRLIndividualArgumentHGDB(HGHandle... args)
	{
		// OWLIndividual individual
		this(args[0]);
		if (args.length != 1)
			throw new IllegalArgumentException("args.length != 1, but " + args.length);
	}

	public SWRLIndividualArgumentHGDB(HGHandle individual)
	{
		// OWLIndividual individual
		individualHandle = individual;
	}

	public OWLIndividual getIndividual()
	{
		return getHyperGraph().get(individualHandle);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(SWRLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(SWRLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof SWRLIndividualArgument))
		{
			return false;
		}
		SWRLIndividualArgument other = (SWRLIndividualArgument) obj;
		return other.getIndividual().equals(getIndividual());
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		return getIndividual().compareTo(((SWRLIndividualArgument) object).getIndividual());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return individualHandle == null ? 0 : 1;
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
		return individualHandle;
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
		individualHandle = handle;
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
		individualHandle = null;
	}
}
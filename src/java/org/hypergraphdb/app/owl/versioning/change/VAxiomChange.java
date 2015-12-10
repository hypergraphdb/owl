package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;

/**
 * VAxiomChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VAxiomChange extends VOWLChange
{
	private HGHandle axiom;

	// Set<HGHandle> getEntities();

	public VAxiomChange(HGHandle... args)
	{
		if (args[0] == null)
			throw new IllegalArgumentException("Tried to create a VAxiomChange with a null axiom handle.");
		axiom = args[0];
	}

	public HGHandle getAxiomHandle()
	{
		return axiom;
	}

	public OWLAxiomHGDB getAxiom()
	{
		return graph.get(axiom);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return (axiom == null) ? 0 : 1;
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
			throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		return axiom;
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
			throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		axiom = handle;
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
			throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		axiom = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb
	 * .app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLChangeVisitor visitor)
	{
		visitor.visit(this);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axiom == null) ? 0 : axiom.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VAxiomChange other = (VAxiomChange) obj;
		if (axiom == null)
			return other.axiom != null;
		else
			return axiom.equals(other.axiom); // || getAxiom().equals(other.getAxiom());
	}	
}
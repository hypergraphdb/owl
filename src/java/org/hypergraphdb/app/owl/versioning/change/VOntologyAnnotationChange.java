package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;

/**
 * VOntologyAnnotationChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VOntologyAnnotationChange extends VOWLChange
{

	private HGHandle ontologyAnnotationHandle;

	public VOntologyAnnotationChange(HGHandle... args)
	{
		if (args[0] == null)
			throw new IllegalArgumentException(
					"Tried to create a VOntologyAnnotationChange with a null annotation declaration handle.");
		ontologyAnnotationHandle = args[0];
	}

	HGHandle getOntologyAnnotationHandle()
	{
		return ontologyAnnotationHandle;
	}

	public OWLAnnotationHGDB getOntologyAnnotation()
	{
		return graph.get(ontologyAnnotationHandle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return (ontologyAnnotationHandle == null) ? 0 : 1;
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
		return ontologyAnnotationHandle;
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
		ontologyAnnotationHandle = handle;
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
		ontologyAnnotationHandle = null;
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
		result = prime * result + ((ontologyAnnotationHandle == null) ? 0 : ontologyAnnotationHandle.hashCode());
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
		VOntologyAnnotationChange other = (VOntologyAnnotationChange) obj;
		if (ontologyAnnotationHandle == null)
		{
			if (other.ontologyAnnotationHandle != null)
				return false;
		}
		else if (!ontologyAnnotationHandle.equals(other.ontologyAnnotationHandle))
			return false;
		return true;
	}
}

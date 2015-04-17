package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * VModifyOntologyIDChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VModifyOntologyIDChange extends VOWLChange
{
	private HGHandle oldOntologyIDHandle;
	private HGHandle newOntologyIDHandle;
	
	@Override
	public VChange<VersionedOntology> inverse()
	{
		VOWLChange ic = new VModifyOntologyIDChange(newOntologyIDHandle, oldOntologyIDHandle);
		ic.setHyperGraph(graph);
		return ic;		
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return !graph.get(newOntologyIDHandle).equals(versioned.ontology().getOntologyID());		
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new SetOntologyID(versioned.ontology(), 
								 (OWLOntologyID)graph.get(newOntologyIDHandle));		
	}

	/**
	 * old = [0], new = [1].
	 * 
	 * @param args
	 */
	public VModifyOntologyIDChange(HGHandle... args)
	{
		oldOntologyIDHandle = args[0];
		newOntologyIDHandle = args[1];
	}

	HGHandle getOldOntologyIDHandle()
	{
		return oldOntologyIDHandle;
	}

	HGHandle getNewOntologyIDHandle()
	{
		return newOntologyIDHandle;
	}

	public OWLOntologyID getOldOntologyID()
	{
		return graph.get(oldOntologyIDHandle);
	}

	public OWLOntologyID getNewOntologyID()
	{
		return graph.get(newOntologyIDHandle);
	}

	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		int arity = (oldOntologyIDHandle == null) ? 0 : 1;
		return arity + ((newOntologyIDHandle == null) ? 0 : 1);
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
		if (i == 0)
		{
			if (oldOntologyIDHandle != null)
			{
				return oldOntologyIDHandle;
			}
			else
			{
				return newOntologyIDHandle;
			}
		}
		else
		{
			return newOntologyIDHandle;
		}
		// return (i == 0)? oldOntologyIDHandle : newOntologyIDHandle;
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
		if (i == 0)
		{
			if (oldOntologyIDHandle != null)
			{
				oldOntologyIDHandle = handle;
			}
			else
			{
				newOntologyIDHandle = handle;
			}
		}
		else
		{
			newOntologyIDHandle = handle;
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
		if (!(i >= 0 && i < getArity()))
			throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		// two calls with 0 will delete both.
		if (i == 0)
		{
			if (oldOntologyIDHandle != null)
			{
				oldOntologyIDHandle = null;
			}
			else
			{
				newOntologyIDHandle = null;
			}
		}
		else
		{
			newOntologyIDHandle = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb
	 * .app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((newOntologyIDHandle == null) ? 0 : newOntologyIDHandle.hashCode());
		result = prime * result + ((oldOntologyIDHandle == null) ? 0 : oldOntologyIDHandle.hashCode());
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
		VModifyOntologyIDChange other = (VModifyOntologyIDChange) obj;
		if (newOntologyIDHandle == null)
		{
			if (other.newOntologyIDHandle != null)
				return false;
		}
		else if (!newOntologyIDHandle.equals(other.newOntologyIDHandle))
			return false;
		if (oldOntologyIDHandle == null)
		{
			if (other.oldOntologyIDHandle != null)
				return false;
		}
		else if (!oldOntologyIDHandle.equals(other.oldOntologyIDHandle))
			return false;
		return true;
	}
}
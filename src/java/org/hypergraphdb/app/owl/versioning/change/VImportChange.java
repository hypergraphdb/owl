package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;

/**
 * VImportChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VImportChange extends VOWLChange
{
	private HGHandle importDeclarationHandle;

	public VImportChange(HGHandle... args)
	{
		if (args[0] == null)
			throw new IllegalArgumentException("Tried to create a VImportChange with a null import declaration handle.");
		importDeclarationHandle = args[0];
	}

	HGHandle getImportDeclarationHandle()
	{
		return importDeclarationHandle;
	}

	public OWLImportsDeclaration getImportDeclaration()
	{
		return graph.get(importDeclarationHandle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return (importDeclarationHandle == null) ? 0 : 1;
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
		return importDeclarationHandle;
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
		importDeclarationHandle = handle;
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
		importDeclarationHandle = null;
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
		result = prime * result + ((importDeclarationHandle == null) ? 0 : importDeclarationHandle.hashCode());
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
		VImportChange other = (VImportChange) obj;
		if (importDeclarationHandle == null)
		{
			if (other.importDeclarationHandle != null)
				return false;
		}
		else if (!importDeclarationHandle.equals(other.importDeclarationHandle))
			return false;
		return true;
	}
}

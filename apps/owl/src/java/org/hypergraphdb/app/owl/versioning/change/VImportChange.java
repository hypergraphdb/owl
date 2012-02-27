package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;

/**
 * VImportChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VImportChange extends VOWLChange {
	
	private HGHandle importDeclarationHandle;
	
	public VImportChange(HGHandle...args) {
		importDeclarationHandle = args[0];
    }
	
	HGHandle getImportDeclarationHandle() {
		return importDeclarationHandle;
	}

	public OWLImportsDeclaration getImportDeclaration() {
		return graph.get(importDeclarationHandle);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {		
		return (importDeclarationHandle == null)? 0:1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		return importDeclarationHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		importDeclarationHandle = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		importDeclarationHandle = null;
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor)
	 */
	@Override
	public void accept(VersioningObjectVisitor visitor) {
		visitor.visit(this);
	}

}

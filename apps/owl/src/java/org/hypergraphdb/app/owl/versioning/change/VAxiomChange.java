package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor;

/**
 * VAxiomChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public abstract class VAxiomChange extends VOWLChange {
	
	private HGHandle axiom; 
	
	//Set<HGHandle> getEntities();

	public VAxiomChange(HGHandle...args) {
    	axiom = args[0];
    }

	public HGHandle getAxiomHandle() {
		return axiom;
	}

	public OWLAxiomHGDB getAxiom() {
		return graph.get(axiom);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {		
		return (axiom == null)? 0:1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		return axiom;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		axiom = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		axiom = null;
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor)
	 */
	@Override
	public void accept(VersioningObjectVisitor visitor) {
		visitor.visit(this);
	}
	
}

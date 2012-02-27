package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * VModifyOntologyIDChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class VModifyOntologyIDChange extends VOWLChange {
	
	private HGHandle oldOntologyIDHandle;

	private HGHandle newOntologyIDHandle;
	
	public VModifyOntologyIDChange(HGHandle...args) {
    	oldOntologyIDHandle = args[0];
    	newOntologyIDHandle = args[1];    	
    }
	
	HGHandle getOldOntologyIDHandle() {
		return oldOntologyIDHandle;
	}
	
	HGHandle getNewOntologyIDHandle() {
		return newOntologyIDHandle;
	}
	
	public OWLOntologyID getOldOntologyID() {
		return graph.get(oldOntologyIDHandle);
	}

	public OWLOntologyID getNewOntologyID() {
		return graph.get(newOntologyIDHandle);
	}
		
//	}
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		int arity = (oldOntologyIDHandle == null)? 0 : 1;
		return arity + ((newOntologyIDHandle == null)? 0 : 1);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		if (i == 0) {
			if (oldOntologyIDHandle != null) {
				return oldOntologyIDHandle;
			} else {
				return newOntologyIDHandle;
			}
		} else {
			return newOntologyIDHandle;
		}
		//return (i == 0)? oldOntologyIDHandle : newOntologyIDHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		if (i == 0) {
			if (oldOntologyIDHandle != null) {
				oldOntologyIDHandle = handle;
			} else {
				newOntologyIDHandle = handle;
			}
		} else {
			newOntologyIDHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be >= 0 and less than " + getArity());
		// two calls with 0 will delete both.
		if (i == 0) {
			if (oldOntologyIDHandle != null) {
				oldOntologyIDHandle = null;
			} else {
				newOntologyIDHandle = null;
			}
		} else {
			newOntologyIDHandle = null;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VersioningObjectVisitor)
	 */
	@Override
	public void accept(VersioningObjectVisitor visitor) {
		visitor.visit(this);
	}
}
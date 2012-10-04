package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor;
import org.hypergraphdb.util.Pair;

/**
 * VPrefixChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public class VPrefixChange extends VOWLChange {

	HGHandle prefixNameToPrefixPairHandle;
	//Pair<String, String> prefixNameToPrefix;
	
	public VPrefixChange(HGHandle... args) {
		prefixNameToPrefixPairHandle = args[0];
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		return prefixNameToPrefixPairHandle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		prefixNameToPrefixPairHandle = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		prefixNameToPrefixPairHandle = null;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.VersioningObject#accept(org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor)
	 */
	@Override
	public void accept(VOWLObjectVisitor visitor) {
		visitor.visit(this);
	}
	
	public String getPrefixName() {
		Pair<String, String> p = graph.get(prefixNameToPrefixPairHandle);
		return p.getFirst();
	}

	public String getPrefix() {
		Pair<String, String> p = graph.get(prefixNameToPrefixPairHandle);
		return p.getSecond();
	}
	
	public HGHandle getPrefixPairHandle() {
		return prefixNameToPrefixPairHandle;
	}
}
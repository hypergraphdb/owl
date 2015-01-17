package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VOWLObjectVisitor;
import org.hypergraphdb.util.Pair;

/**
 * VPrefixChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public abstract class VPrefixChange extends VOWLChange
{
	HGHandle prefixNameToPrefixPairHandle;

	public VPrefixChange(HGHandle... args)
	{
		prefixNameToPrefixPairHandle = args[0];
	}

	@Override
	public int getArity()
	{
		return 1;
	}

	@Override
	public HGHandle getTargetAt(int i)
	{
		return prefixNameToPrefixPairHandle;
	}

	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		prefixNameToPrefixPairHandle = handle;
	}

	@Override
	public void notifyTargetRemoved(int i)
	{
		prefixNameToPrefixPairHandle = null;
	}

	@Override
	public void accept(VOWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public String getPrefixName()
	{
		Pair<String, String> p = graph.get(prefixNameToPrefixPairHandle);
		return p.getFirst();
	}

	public String getPrefix()
	{
		Pair<String, String> p = graph.get(prefixNameToPrefixPairHandle);
		return p.getSecond();
	}

	public HGHandle getPrefixPairHandle()
	{
		return prefixNameToPrefixPairHandle;
	}
}
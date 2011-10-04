package org.hypergraphdb.app.owl;

import java.util.Collection;

import org.hypergraphdb.HGHandle;

public class SWRLHead extends SWRLConjuction
{
	public SWRLHead() { }
	public SWRLHead(Collection<HGHandle> args) { super(args); }
	public SWRLHead(HGHandle...args) { super(args); }
}

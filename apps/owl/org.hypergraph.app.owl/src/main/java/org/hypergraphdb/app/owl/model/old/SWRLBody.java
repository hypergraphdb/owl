package org.hypergraphdb.app.owl.model.old;

import java.util.Collection;

import org.hypergraphdb.HGHandle;

public class SWRLBody extends SWRLConjuction
{
	public SWRLBody() { }
	public SWRLBody(Collection<HGHandle> args) { super(args); }	
	public SWRLBody(HGHandle...args) { super(args); }
}
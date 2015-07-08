package org.hypergraphdb.app.owl.model.swrl;

import java.util.Collection;

import org.hypergraphdb.HGHandle;

/**
 * SWRLHead.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLHead extends SWRLConjuction
{
	public SWRLHead(Collection<HGHandle> args) 
	{ 
		super(args); 
	}
	
	public SWRLHead(HGHandle...args) 
	{ 
		super(args); 
	}
}

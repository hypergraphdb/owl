package org.hypergraphdb.app.owl.model.swrl;

import java.util.Collection;

import org.hypergraphdb.HGHandle;

/**
 * SWRLBody.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLBody extends SWRLConjuction
{
	public SWRLBody(Collection<HGHandle> args) { 
		super(args); 
	}	
	public SWRLBody(HGHandle...args) { 
		super(args); 
	}
}
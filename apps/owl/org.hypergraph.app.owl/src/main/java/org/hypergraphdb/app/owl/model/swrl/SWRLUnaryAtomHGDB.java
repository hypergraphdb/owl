package org.hypergraphdb.app.owl.model.swrl;

import java.util.Collection;
import java.util.Collections;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLUnaryAtom;

/**
 * SWRLUnaryAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 * @param <A>
 */
public abstract class SWRLUnaryAtomHGDB<A extends SWRLArgument> extends
		SWRLAtomHGDB implements SWRLUnaryAtom<A>
{
	private A arg;

	public SWRLUnaryAtomHGDB()
	{
		
	}
	
	public SWRLUnaryAtomHGDB(SWRLPredicate predicate, A arg)
	{
		this.predicate = predicate;
		this.arg = arg;
	}
	
	public void setArgument(A arg)
	{
		this.arg = arg;
	}

	public A getArgument()
	{
		return arg;
	}

	public Collection<SWRLArgument> getAllArguments()
	{
		return Collections.singleton((SWRLArgument) arg);
	}
}
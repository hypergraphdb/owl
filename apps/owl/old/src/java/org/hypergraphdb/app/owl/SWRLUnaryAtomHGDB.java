package org.hypergraphdb.app.owl;

import java.util.Collection;
import java.util.Collections;

import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLUnaryAtom;

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
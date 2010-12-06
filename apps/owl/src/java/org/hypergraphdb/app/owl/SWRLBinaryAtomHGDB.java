package org.hypergraphdb.app.owl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLBinaryAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;

public abstract class SWRLBinaryAtomHGDB<A extends SWRLArgument, B extends SWRLArgument>
		extends SWRLAtomHGDB implements SWRLBinaryAtom<A, B>
{
	private A arg0;
	private B arg1;

	protected SWRLBinaryAtomHGDB(SWRLPredicate predicate, A arg0, B arg1)
	{
		this.predicate = predicate;
		this.arg0 = arg0;
		this.arg1 = arg1;
	}

	public SWRLBinaryAtomHGDB()
	{

	}

	public Collection<SWRLArgument> getAllArguments()
	{
		List<SWRLArgument> objs = new ArrayList<SWRLArgument>();
		objs.add(arg0);
		objs.add(arg1);
		return objs;
	}

	public void setFirstArgument(A arg0)
	{
		this.arg0 = arg0;
	}
	
	public A getFirstArgument()
	{
		return arg0;
	}

	public void setSecondArgument(B arg1)
	{
		this.arg1 = arg1;
	}
	public B getSecondArgument()
	{
		return arg1;
	}

	@SuppressWarnings("unchecked")
	protected int compareObjectOfSameType(OWLObject object)
	{
		SWRLBinaryAtom other = (SWRLBinaryAtom) object;
		int diff = ((OWLObject) getPredicate()).compareTo((OWLObject) other
				.getPredicate());
		if (diff != 0)
		{
			return diff;
		}
		diff = arg0.compareTo(other.getFirstArgument());
		if (diff != 0)
		{
			return diff;
		}
		return arg1.compareTo(other.getSecondArgument());
	}
}

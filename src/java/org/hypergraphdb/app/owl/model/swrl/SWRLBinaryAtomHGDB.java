package org.hypergraphdb.app.owl.model.swrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLBinaryAtom;

/**
 * SWRLBinaryAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 * @param <A>
 * @param <B>
 */
public abstract class SWRLBinaryAtomHGDB<A extends SWRLArgument, B extends SWRLArgument>
		extends SWRLAtomHGDB implements SWRLBinaryAtom<A, B>
{
	private HGHandle arg0;
	private HGHandle arg1;

	public SWRLBinaryAtomHGDB(HGHandle...args) {
		super(args);
		if(args[1] == null) throw new IllegalArgumentException();
		if(args[2] == null) throw new IllegalArgumentException();
		arg0 = args[1];
		arg1 = args[2];
	}

	//(SWRLPredicate predicate, A arg0, B arg1)
	public SWRLBinaryAtomHGDB(HGHandle predicate, HGHandle arg0, HGHandle arg1)
	{
		super(predicate);
		if(arg0 == null) throw new IllegalArgumentException();
		if(arg1 == null) throw new IllegalArgumentException();
		this.arg0 = arg0;
		this.arg1 = arg1;
	}
	
	public Collection<SWRLArgument> getAllArguments()
	{
		List<SWRLArgument> objs = new ArrayList<SWRLArgument>();
		objs.add(getHyperGraph().<SWRLArgument>get(arg0));
		objs.add(getHyperGraph().<SWRLArgument>get(arg1));
		return objs;
	}

	public A getFirstArgument()
	{
		return getHyperGraph().get(arg0);
	}

	public B getSecondArgument()
	{
		return getHyperGraph().get(arg1);
	}

	protected int compareObjectOfSameType(OWLObject object)
	{
		SWRLBinaryAtom<?, ?> other = (SWRLBinaryAtom<?,?>) object;
		int diff = ((OWLObject) getPredicate()).compareTo((OWLObject) other
				.getPredicate());
		if (diff != 0)
		{
			return diff;
		}
		diff = getFirstArgument().compareTo(other.getFirstArgument());
		if (diff != 0)
		{
			return diff;
		}
		return getSecondArgument().compareTo(other.getSecondArgument());
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#getArity()
	 */
	@Override
	public int getArity() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		//i = correctIndex(i);
		HGHandle ret;
		if (i == 0) {
			ret = super.getTargetAt(i);
		} else if (i == 1){
			ret = arg0;
		} else if (i == 2) {
			ret = arg1;
		} else {
			throw new IllegalArgumentException("i > 2 || < 0: was " + i );
		}
		if (ret == null) System.err.println("Returning null for " + i 
				+ "\r\n" + this.getClass());
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		//i = correctIndex(i);
		if (i == 0) {
			super.notifyTargetHandleUpdate(i, handle);
		} else if (i == 1){
			arg0 = handle;
		} else if (i == 2) {
			arg1 = handle;
		} else {
			throw new IllegalArgumentException("i > 2 || < 0: was " + i );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.model.swrl.SWRLAtomHGDB#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i == 0) {
			super.notifyTargetRemoved(i);
		} else if (i == 1){
			arg0 = getHyperGraph().getHandleFactory().nullHandle();
		} else if (i == 2) {
			arg1 = getHyperGraph().getHandleFactory().nullHandle();
		} else {
			throw new IllegalArgumentException("i > 2 || < 0: was " + i );
		}
	}
}
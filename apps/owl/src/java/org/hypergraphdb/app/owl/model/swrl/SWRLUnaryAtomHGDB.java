package org.hypergraphdb.app.owl.model.swrl;

import java.util.Collection;
import java.util.Collections;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.SWRLArgument;
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
	private HGHandle arg;

	public SWRLUnaryAtomHGDB(HGHandle...args) {
		super(args);
		if (args[1] == null) throw new IllegalArgumentException();
		arg = args[1];
	}

	//public SWRLUnaryAtomHGDB(SWRLPredicate predicate, A arg)
	public SWRLUnaryAtomHGDB(HGHandle predicate, HGHandle arg)
	{
		super(predicate);
		if (arg == null) throw new IllegalArgumentException();
		this.arg = arg;
	}
	
	@SuppressWarnings("unchecked")
	public A getArgument()
	{
		return (A)getHyperGraph().get(arg);
	}

	public Collection<SWRLArgument> getAllArguments()
	{
		return Collections.singleton((SWRLArgument) getArgument());
	}
	
//	private int correctIndex(int i) {
//		int iCorrect = i;
//		if (iCorrect >= 0 && super.getArity() == 0) {
//			iCorrect ++;
//		}
//		if (iCorrect >= 1 && arg == null) {
//			throw new IllegalArgumentException();
//		} 
//		return iCorrect;
//	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		//i = correctIndex(i);
		if (i == 0)
			return super.getTargetAt(i);
		else if (i == 1)
			return arg;
		else 
			throw new IllegalArgumentException("Max index is 1, was: " + i);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		//i = correctIndex(i);
		if (i == 0)
			super.notifyTargetHandleUpdate(i, handle);
		else if (i == 1)
			arg = handle;
		else 
			throw new IllegalArgumentException("Max index is 1, was: " + i);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		//i = correctIndex(i);
		if (i == 0)
			super.notifyTargetRemoved(i);
		else if (i == 1)
			arg = getHyperGraph().getHandleFactory().nullHandle();
		else 
			throw new IllegalArgumentException("Max index is 1, was: " + i);
	}
}
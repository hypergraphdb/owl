package org.hypergraphdb.app.owl.model.swrl;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLPredicate;

/**
 * SWRLAtomHGDB.
 * 
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011 2012.08.07 hilpold added get/setPredicateDirect(...) to
 *          enable bean introspection and avoid naming conflict in subclasses.
 *          2012.09.25 hilpold made HGLink
 */
public abstract class SWRLAtomHGDB extends OWLObjectHGDB implements SWRLAtom, HGLink
{
	private HGHandle predicateHandle;

	public SWRLAtomHGDB(HGHandle... args)
	{
		if (args[0] == null)
			throw new IllegalArgumentException();
		predicateHandle = args[0];
	}

	public SWRLAtomHGDB(HGHandle predicate)
	{
		if (predicate == null)
			throw new IllegalArgumentException();
		predicateHandle = predicate;
	}

	public void setArguments(List<? extends SWRLArgument> L)
	{
		throw new UnsupportedOperationException();
	}	
	
	public void setPredicate(SWRLPredicate predicate)
	{
		throw new UnsupportedOperationException();

		// if (predicate == null) {
		// predicate = null;
		// } else if (predicate instanceof OWLObjectHGDB) {
		// predicateHandle = getHyperGraph().getHandle(predicate);
		// if (predicateHandle == null) {
		// throw new
		// IllegalArgumentException("Set an OWLObject predicate that's not in the graph");
		// }
		// } else if (predicate instanceof IRI) {
		// predicateHandle = hg.assertAtom(getHyperGraph(), predicate);
		// } else {
		// predicateHandle = getHyperGraph().getHandle(predicate);
		// if (predicateHandle == null) {
		// predicateHandle = getHyperGraph().add(predicate);
		// }
		// }
	}

	public SWRLPredicate getPredicate()
	{
		return (SWRLPredicate) ((predicateHandle == null) ? null : getHyperGraph().get(predicateHandle));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		return predicateHandle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		predicateHandle = handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		predicateHandle = getHyperGraph().getHandleFactory().nullHandle();
	}
}
package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;

/**
 * SWRLAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public abstract class SWRLAtomHGDB extends OWLObjectHGDB implements SWRLAtom
{
	SWRLPredicate predicate;

	public void setPredicate(SWRLPredicate predicate)
	{
		this.predicate = predicate;
	}
	
	public SWRLPredicate getPredicate()
	{
		return predicate;
	}
}

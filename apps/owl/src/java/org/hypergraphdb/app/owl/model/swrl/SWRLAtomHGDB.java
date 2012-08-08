package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;

/**
 * SWRLAtomHGDB.
 * 
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011 
 * 2012.08.07 hilpold added get/setPredicateDirect(...) to enable bean introspection and avoid naming conflict in subclasses.
 */
public abstract class SWRLAtomHGDB extends OWLObjectHGDB implements SWRLAtom
{
	SWRLPredicate predicate;

	public void setPredicateDirect(SWRLPredicate predicateDirect)
	{
		this.predicate = predicateDirect;
	}
	
	public SWRLPredicate getPredicateDirect()
	{
		return predicate;
	}

	public void setPredicate(SWRLPredicate predicate)
	{
		this.predicate = predicate;
	}
	
	public SWRLPredicate getPredicate()
	{
		return predicate;
	}
}

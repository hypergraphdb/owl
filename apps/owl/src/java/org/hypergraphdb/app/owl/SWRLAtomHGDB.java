package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;

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

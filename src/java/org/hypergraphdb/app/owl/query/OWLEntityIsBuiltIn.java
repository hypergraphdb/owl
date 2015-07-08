package org.hypergraphdb.app.owl.query;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.HGAtomPredicate;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * IsOWLEntityBuiltIn checks if an atom implements OWLEntity and returns isBuiltin() method's result.
 * It returns false, if isBuiltin() returns false, the atom does not implement OWLEntity or the atom could not be retrieved from the graph.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2011
 */
public class OWLEntityIsBuiltIn implements HGAtomPredicate {

	/* (non-Javadoc)
	 * @see org.hypergraphdb.query.HGAtomPredicate#satisfies(org.hypergraphdb.HyperGraph, org.hypergraphdb.HGHandle)
	 */
	@Override
	public boolean satisfies(HyperGraph graph, HGHandle handle) {
		Object o = graph.get(handle);
		if (o == null) 
			return false;
		if (o instanceof OWLEntity) {
			return ((OWLEntity)o).isBuiltIn();
		} else {
			return false;		
		}
	}
}

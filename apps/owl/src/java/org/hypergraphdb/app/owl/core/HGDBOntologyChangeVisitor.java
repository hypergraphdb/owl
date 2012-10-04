package org.hypergraphdb.app.owl.core;

import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;

/**
 * HGDBOntologyChangeVisitor.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public interface HGDBOntologyChangeVisitor extends OWLOntologyChangeVisitor {
    
	void visit(AddPrefixChange change);

	void visit(RemovePrefixChange change);

}

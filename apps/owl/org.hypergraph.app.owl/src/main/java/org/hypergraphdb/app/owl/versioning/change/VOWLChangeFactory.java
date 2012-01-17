package org.hypergraphdb.app.owl.versioning.change;

import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VOWLChangeFactory.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface VOWLChangeFactory {

	VOWLChange create(OWLOntologyChange ooc);

}

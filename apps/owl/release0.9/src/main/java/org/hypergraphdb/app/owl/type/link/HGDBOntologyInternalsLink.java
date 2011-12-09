package org.hypergraphdb.app.owl.type.link;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

/**
 * HGDBOntologyInternalsLink.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 * @deprecated Internals has no state that's worth storing in the graph and all links are made to refer to the ontology subgraph.
 */
public class HGDBOntologyInternalsLink extends HGPlainLink {
	
	public HGDBOntologyInternalsLink(HGHandle...args) { super(args); }

}

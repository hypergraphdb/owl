package org.hypergraphdb.app.owl.type.link;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

/**
 * AxiomAnnotatedBy connects Axioms to OWLAnnotationHGDB atoms. 
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Nov 18, 2011
 */
public class AxiomAnnotatedBy extends HGPlainLink {

	public AxiomAnnotatedBy(HGHandle...args) { super(args); }

}

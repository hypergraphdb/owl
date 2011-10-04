package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.OWLOntologyFormat;

public class HGDBOntologyFormat extends OWLOntologyFormat {

	public final static String PARAMETER_IRI = "IRI";
	
	@Override
	public String toString() {
		return "Hypergraph Ontology Format class:" + getClass().getCanonicalName();
	}
	
}

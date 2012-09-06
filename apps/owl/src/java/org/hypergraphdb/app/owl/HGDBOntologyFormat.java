package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

public class HGDBOntologyFormat extends PrefixOWLOntologyFormat {

	/**
	 * The schema used for documentIRIs to identify them as stored in the HGDB repository.
	 */
    public final static String HGDB_SCHEME	= "hgdb";	

	@Override
	public String toString() {
		return "Hypergraph Database Backend";
	}
	
    /**
     * Converts an ontology IRI by replacing it's schema with "hgdb://"
     * @param ontologyIRI must not be null, it's schema must not be null
     * @return
     */
    public static IRI convertToHGDBDocumentIRI(IRI ontologyIRI) {
    	String iriNoScheme = ontologyIRI.toString();
    	String scheme = ontologyIRI.getScheme();
    	iriNoScheme = iriNoScheme.substring(scheme.length());
    	return IRI.create(HGDB_SCHEME + iriNoScheme);  
    }

    /**
     * Converts an ontology IRI by replacing it's schema with "hgdb://"
     * @param ontologyIRI must not be null, it's schema must not be null
     * @return
     */
    public static boolean isHGDBDocumentIRI(IRI documentIRI) {
    	String scheme = documentIRI.getScheme();
    	return HGDB_SCHEME.equals(scheme);  
    }
}

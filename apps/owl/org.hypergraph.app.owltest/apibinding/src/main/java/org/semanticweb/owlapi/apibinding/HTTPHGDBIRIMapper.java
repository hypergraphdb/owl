package org.semanticweb.owlapi.apibinding;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;


public class HTTPHGDBIRIMapper implements OWLOntologyIRIMapper {
	
	public static boolean DBG = true;
	
	public HTTPHGDBIRIMapper() {
	}
	
    public IRI getDocumentIRI(IRI ontologyIRI) {
    	String iriNoScheme = ontologyIRI.toString();
    	String scheme = ontologyIRI.getScheme();
    	iriNoScheme = iriNoScheme.substring(scheme.length());
    	IRI docIRI = IRI.create("hgdb" + iriNoScheme);  
    	if (DBG) { 
    		System.out.println("HGDBIRIMapper: " + ontologyIRI + " -> " + docIRI);
    	}
    	if (scheme.startsWith("http")) {
    		return docIRI;
    	} else {
    		return null;
    	}
    }
}
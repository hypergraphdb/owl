package org.hypergraphdb.app.owl;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
/**
 * HGDBIRIMapper will try to find the ontology by IRI in the current HGDB repository 
 * and return a DocumentIRI with a hgdb schema on success; null otherwise.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 06, 2021
 */
public class HGDBIRIMapper implements OWLOntologyIRIMapper {
	
	public static boolean DBG = false;
	
	HGDBOntologyRepository ontologyRepository;
	
	public HGDBIRIMapper(HGDBOntologyRepository ontologyRepository) {
		this.ontologyRepository = ontologyRepository;
	}
	
    public IRI getDocumentIRI(IRI ontologyIRI) {
    	IRI docIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(ontologyIRI);
    	if (DBG) { 
    		System.out.println("HGDBIRIMapper: " + ontologyIRI + " -> " + docIRI);
    	}
    	if (ontologyRepository.existsOntologyByDocumentIRI(docIRI)) {
    		return docIRI;
    	} else {
    		return null;
    	}
    }
    
}
package org.hypergraphdb.app.owl.test;

import java.util.logging.Logger;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * TestData creates some ontologies for a given repository.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 */
public class TestData {
	
	static Logger log = Logger.getLogger(TestData.class.getCanonicalName());
	
	public static String baseOntoURI = "http://www.miamidade.gov/ontologies/generatedTestData/TestA";
	public static String baseOntoPhysURI = "hgdb://generatedTestData/TestA";
	
	public static void ensureTestData(HGDBOntologyRepository r, int howMany) {
		if (howMany < 0) throw new IllegalArgumentException("howMany < 0");
		for (int i =0; i < howMany; i++) {
			OWLOntologyID ontologyID = new OWLOntologyID(IRI.create(baseOntoURI + i));
			IRI documentIRI = IRI.create(baseOntoPhysURI + i);
			if (!r.existsOntology(ontologyID)) {
				r.createOWLOntology(ontologyID, documentIRI);
			} else {
				log.info("Ontology already exists: " + ontologyID);
			}
		}		
	}
}

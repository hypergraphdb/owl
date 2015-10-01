package org.hypergraphdb.app.owl.test;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public interface OntologyContext
{
	HyperGraph graph();
	OWLOntology ontology();
	VersionedOntology vonto();	
	OWLOntologyManager manager();
	OWLDataFactory df();
	OntologyDatabase repo();
	VersionManager vrepo();
}
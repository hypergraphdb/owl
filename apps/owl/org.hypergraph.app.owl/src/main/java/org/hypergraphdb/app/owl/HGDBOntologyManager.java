package org.hypergraphdb.app.owl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGManagement;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.util.HGUtils;
import org.protege.owlapi.model.ProtegeOWLOntologyManager;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.ImpendingOWLOntologyChangeListener;
import org.semanticweb.owlapi.model.MissingImportListener;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeBroadcastStrategy;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyChangeProgressListener;
import org.semanticweb.owlapi.model.OWLOntologyChangeVetoException;
import org.semanticweb.owlapi.model.OWLOntologyChangesVetoedListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
import org.semanticweb.owlapi.model.OWLOntologyRenameException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyStorer;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * Manages multiple Ontologies. Based on OWL-API OWLOntologyManagerImpl and ProtegeOWLOntologyManager.
 * 
 * HGDBOntologyManager.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyManager extends ProtegeOWLOntologyManager
{
	HGDBOntologyRepository ontologyRepository;
		

	public HGDBOntologyManager(OWLDataFactoryHGDB dataFactory) {
		super(dataFactory);						
		//Make sure there is an application, a graph, et.c.
		ontologyRepository = new HGDBOntologyRepository(); 
		dataFactory.setHyperGraph(ontologyRepository.getHyperGraph());
	}
	
	/**
	 * @return the dbRepository
	 */
	public HGDBOntologyRepository getOntologyRepository() {
		return ontologyRepository;
	}

}

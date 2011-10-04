package org.hypergraphdb.app.owl;

import java.util.List;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HyperNode;
import org.hypergraphdb.annotation.HGIgnore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * HGDBMutableOntology declares methods to enable initialization after instantiation.
 * This is for Hypergraph Bean introspection.
 * 
 * Extension was necessary for three situations:
 * A) No default constructor
 * Need a way to set fields after new default.
 * These might be huge objects that shall not be serialized by Hypergraph.
 * Therefore these methods are marked with @HGIgnore 
 * 
 * B) Only getter in superclass 
 * We sometimes want Hypergraph to use Bean Introspection and add a setter here.
 * 
 * C) Document IRI 
 * Physical location in DB.
 * 
 * Only Hypergraph should use those methods. All others should rely on the Change mechanism.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public interface HGDBOntology extends OWLMutableOntology, HyperNode
{	
		
	void setOntologyID(OWLOntologyID id);
	
	@HGIgnore 
	void setOWLOntologyManager(OWLOntologyManager manager);
	
	
	void setDocumentIRI(IRI documentIRI);
	
	IRI getDocumentIRI();
	
}
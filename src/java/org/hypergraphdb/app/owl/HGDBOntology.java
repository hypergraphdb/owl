package org.hypergraphdb.app.owl;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HyperNode;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
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
public interface HGDBOntology extends OWLOntologyEx, HyperNode, HGHandleHolder, HGGraphHolder
{	
		
	void setOntologyID(OWLOntologyID id);
	
	@HGIgnore 
	void setOWLOntologyManager(OWLOntologyManager manager);
	
	
	void setDocumentIRI(IRI documentIRI);
	
	IRI getDocumentIRI();
	/**
	 * Returns the number of atoms who are members in the ontology subgraph.
	 * Use this for testing.
	 * @return
	 */
	long getNrOfAtoms();
	
	/**
	 * Returns the number of atoms who are links and members in the ontology subgraph.
	 * Use this for testing.
	 * @return
	 */	
    long getNrOfLinks();
	
    /**
	 * Returns the number of atoms who are not links and members in the ontology subgraph.
	 * Use this for testing.
	 * @return
	 */	
	long getNrOfNonLinkAtoms();
	
	/**
	 * Return the HyperGraph instance where this ontology is stored.
	 */
	HyperGraph getHyperGraph();
	
	/**
	 * Lookup an axiom as an atom in the HyperGraphDB database. The lookup will only return
	 * an atom if it is a member of this ontology.
	 *   
	 * @param axiom
	 * @return
	 */
	OWLAxiomHGDB findAxiom(OWLAxiom axiom);

}
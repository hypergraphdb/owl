package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.AbstractClassExpressionElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractOWLDataRangeHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationElementHandler;
import org.coode.owlapi.owlxmlparser.OWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * 
 * 
 * Based on:
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 13-Dec-2006<br><br>
 */
public class OWLOntologyHandlerModified extends AbstractVOWLElementHandler<OWLOntology> {

    public OWLOntologyHandlerModified(OWLXMLParserHandler handler) {
        super(handler);
    }

    @Override
	public void attribute(String name, String value) throws OWLParserException {
        if (name.equals("ontologyIRI")) {
            OWLOntologyID newID = new OWLOntologyID(IRI.create(value), getOntology().getOntologyID().getVersionIRI());
            //getOWLOntologyManager().applyChange(new SetOntologyID(getOntology(), newID));
            ((OWLMutableOntology)getOntology()).applyChange(new SetOntologyID(getOntology(), newID));
        } else if(name.equals("versionIRI")) {
            OWLOntologyID newID = new OWLOntologyID(getOntology().getOntologyID().getOntologyIRI(), IRI.create(value));
            //getOWLOntologyManager().applyChange(new SetOntologyID(getOntology(), newID));
            ((OWLMutableOntology)getOntology()).applyChange(new SetOntologyID(getOntology(), newID));
        } else {
        	throw new OWLParserException("Attibute not recognized: " + name);
        }
    }

    @Override
	public void startElement(String name) throws OWLXMLParserException {
    	System.out.println("ONTOLOGY Data parsing: START");
    }

    /**
     * Adds axiom to the ontology directy, bypassing the manager to avoid change registration.
     */
    @Override
	public void handleChild(AbstractOWLAxiomElementHandler handler) throws OWLXMLParserException {
        OWLAxiom axiom = handler.getOWLObject();
        //if(!axiom.isAnnotationAxiom() || getConfiguration().isLoadAnnotationAxioms()) {
        	//((OWLMutableOntology)getOntology()).applyChange(new AddAxiom(getOntology(), axiom));
            //In case of an HGDB ontology, this will store the axiom in the ontology.
        	//In case of a in mem ontology, this will not store the axiom.
        	//getOWLOntologyManager().applyChange(new AddAxiom(getOntology(), axiom));
        	//BYPASS MANAGER to avoid notification of listeners.
            ((OWLMutableOntology)getOntology()).applyChange(new AddAxiom(getOntology(), axiom));
        //}
    }

    @Override
	public void handleChild(AbstractOWLDataRangeHandler handler) throws OWLXMLParserException {
    }

    @Override
	public void handleChild(AbstractClassExpressionElementHandler handler) throws OWLXMLParserException {
    }

    @Override
	public void handleChild(OWLAnnotationElementHandler handler) throws OWLXMLParserException {
    	((OWLMutableOntology)getOntology()).applyChange(new AddOntologyAnnotation(getOntology(), handler.getOWLObject()));
    }

    //2012.03.27 BUGFIX: added full method; no imports inside ontologydata were parsed before!  
    @Override
	public void handleChild(OWLImportsHandlerModified handler) throws OWLXMLParserException {
    	((OWLMutableOntology)getOntology()).applyChange(new AddImport(getOntology(), handler.getOWLObject()));
    }

    public void endElement() throws OWLParserException, UnloadableImportException {
    	System.out.println("ONTOLOGY Data parsing: END: " + getOntology().toString());
    	if (getParentHandler() != null) {
    		getParentHandler().handleChild(this);
    	}
    }

    public OWLOntology getOWLObject() {
        return getOntology();
    }

    @Override
	public void setParentHandler(OWLElementHandler<?> handler) {
    	if (handler instanceof VersionedOntologyElementHandler) {
    		super.setParentHandler(handler);
    	} else {
    		System.out.println("OntologyHandler set parent intentially ignored: " + handler);
    	}
    }
}
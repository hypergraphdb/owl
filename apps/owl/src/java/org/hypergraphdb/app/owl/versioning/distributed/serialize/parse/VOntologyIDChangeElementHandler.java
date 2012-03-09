package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VModifyOntologyIDChangeElementHandler calls parenthandler.
 * Adds OntologyIds, but not the Change object to the graph.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 5, 2012
 */
public class VOntologyIDChangeElementHandler extends VOWLChangeElementHandler {

	private IRI ontologyIRI;	// old and new required
	private IRI versionIRI;		// optional
	private OWLOntologyID newId;
	private OWLOntologyID oldId;
		
	VModifyOntologyIDChange modifyOntologyIDChange;
	
	/**
	 * @param handler
	 */
	public VOntologyIDChangeElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		reset();
	}
	
	public void reset() {
		ontologyIRI = null;	// old and new required
		versionIRI = null;		// optional
		newId = null;
		oldId = null;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#attribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void attribute(String localName, String value) throws OWLParserException {
		if (localName.equals("ontologyIRI")) {
			ontologyIRI = getIRIFromAttribute(localName, value);
		} else if (localName.equals("versionIRI")) {
			versionIRI = getIRIFromAttribute(localName, value);
		} else {
			throw new OWLParserException("attribute not recognized: " + localName);
		}
	}

	public void startElement(String name) throws OWLXMLParserException {
		//reset();
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		if (getElementName().equals(VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_CHANGE)) {
			HGHandle oldIDH = getHyperGraph().add(oldId);
			HGHandle newIDH = getHyperGraph().add(newId);
			modifyOntologyIDChange = new VModifyOntologyIDChange(oldIDH, newIDH);
			//getAbbreviatedIRI(abbreviatedIRI)modifyOntologyIDChange.
			getParentHandler().handleChild(this);
		} else if (getElementName().equals(VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_NEW_ID)) {
			newId = new OWLOntologyID(ontologyIRI, versionIRI);
			ontologyIRI = null; 
			versionIRI = null;
		} else if (getElementName().equals(VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_OLD_ID)) {
			oldId = new OWLOntologyID(ontologyIRI, versionIRI);
			ontologyIRI = null; 
			versionIRI = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLChangeElementHandler#getOWLObject()
	 */
	@Override
	public VOWLChange getOWLObject() throws OWLXMLParserException {
		return modifyOntologyIDChange;
	}
}
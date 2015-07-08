package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLAnnotationElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.versioning.change.VAddOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VOntologyAnnotationChangeElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 5, 2012
 */
public class VOntologyAnnotationChangeElementHandler extends VOWLChangeElementHandler {

	private VOntologyAnnotationChange ontologyAnnotationChange;
	private OWLAnnotationHGDB annotation;
	
	/**
	 * @param handler
	 */
	public VOntologyAnnotationChangeElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}	
	
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public VOWLChange getOWLObject() throws OWLXMLParserException {
		if (ontologyAnnotationChange == null) throw new OWLXMLParserException("Failed to parse Annotation Change", getLineNumber(), getColumnNumber());
		return ontologyAnnotationChange;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		String name = getElementName();
		HGHandle annotationHandle = getHyperGraph().getHandle(annotation);
		if (annotationHandle == null) throw new OWLParserException("Annotation handle was null");
		if (name.equals(VOWLXMLVocabulary.V_ADD_ONTOLOGY_ANNOTATION_CHANGE.getShortName())) {
			ontologyAnnotationChange = new VAddOntologyAnnotationChange(annotationHandle);
			getParentHandler().handleChild(this);
		} else if (name.equals(VOWLXMLVocabulary.V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE.getShortName())) {
			ontologyAnnotationChange = new VRemoveOntologyAnnotationChange(annotationHandle);
			getParentHandler().handleChild(this);
		} else {
			throw new IllegalStateException("element unknown");
		}
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#handleChild(org.coode.owlapi.owlxmlparser.OWLAnnotationElementHandler)
	 */
	@Override
	public void handleChild(OWLAnnotationElementHandler _handler) throws OWLXMLParserException {
		annotation = (OWLAnnotationHGDB)_handler.getOWLObject();
	}
}
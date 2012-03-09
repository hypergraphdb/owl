package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.change.VAddImportChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveImportChange;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * VImportChangeElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 5, 2012
 */
public class VImportChangeElementHandler extends VOWLChangeElementHandler {

	private VImportChange importChange;
	private OWLImportsDeclarationImpl importsDeclaration;
	
	/**
	 * @param handler
	 */
	public VImportChangeElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.AbstractVOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OWLImportsHandlerModified)
	 */
	@Override
	public void handleChild(OWLImportsHandlerModified h) {
		importsDeclaration = (OWLImportsDeclarationImpl)h.getOWLObject();
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		String name = getElementName();		
		if (name.equals(VOWLVocabulary.V_ADD_IMPORT_CHANGE.getShortName())) {
			HGHandle importDeclHandle = getHyperGraph().add(importsDeclaration);
			importChange = new VAddImportChange(importDeclHandle);
			getParentHandler().handleChild(this);
		} else if (name.equals(VOWLVocabulary.V_REMOVE_IMPORT_CHANGE.getShortName())) {
			HGHandle importDeclHandle = getHyperGraph().add(importsDeclaration);
			importChange = new VRemoveImportChange(importDeclHandle);
			getParentHandler().handleChild(this);
		} else {
			throw new IllegalStateException("unknown element");
		}
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public VOWLChange getOWLObject() throws OWLXMLParserException {
		if (importChange == null) throw new OWLXMLParserException("Handler importChange was null on get", getLineNumber(), getColumnNumber());
		return importChange;
	}
}
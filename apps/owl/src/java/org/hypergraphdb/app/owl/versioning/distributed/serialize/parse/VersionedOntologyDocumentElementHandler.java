package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.List;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VersionedOntologyDocumentElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VersionedOntologyDocumentElementHandler extends AbstractVOWLElementHandler<VOWLXMLDocument> {

	private VOWLXMLRenderConfiguration renderConfig;
	//private VersionedOntology versionedOntology;
	private List<Revision> revisions;
	private List<ChangeSet> changesets;	
	private OWLOntology revisionData;
	private int headRevisionIndex = -2; //means not parsed yet
	private HGPersistentHandle versionedOntologyID;
	//private String xmlBase;
	
	private VOWLXMLDocument versionedOntologyDocument;
	
	/**
	 * @param handler
	 */
	public VersionedOntologyDocumentElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		reset();	
	}
	
	public void reset() {
		renderConfig = null;
		//versionedOntology = null;
		revisions = null;
		changesets = null;	
		revisionData = null;
		headRevisionIndex = -2;
		versionedOntologyID = null;
		//xmlBase = null;
		versionedOntologyDocument = null;
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException {
        if (localName.equals("xml:base")) {
        	//xmlBase = value;
        }
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws OWLXMLParserException {
		//reset();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.AbstractVOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RenderConfigurationElementHandler)
	 */
	@Override
	public void handleChild(RenderConfigurationElementHandler h) throws OWLXMLParserException {
		renderConfig = h.getOWLObject();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.AbstractVOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyElementHandler)
	 */
	@Override
	public void handleChild(VersionedOntologyElementHandler h) throws OWLXMLParserException {
		changesets = h.getChangeSets();
		revisions  = h.getRevisions();
		//determine later: versionedOntology
		headRevisionIndex = h.getHeadRevisionIndex();
		revisionData = h.getOntologyHeadData();
		versionedOntologyID = h.getOntologyID();
		//NO! : h.getOWLObject(), we use all the other methods.
	}
	
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		System.out.println("VersionedOntologyDocumentElementHandler");
		//We either have it already, or we create it
		// MATCH WITH RENDERCONFIG and see if we are valid here.
		
		versionedOntologyDocument = getDocumentRoot();
		versionedOntologyDocument.setRenderConfig(renderConfig);
		versionedOntologyDocument.setHeadRevisionIndex(headRevisionIndex);
		versionedOntologyDocument.setChangesets(changesets);
		versionedOntologyDocument.setRevisions(revisions);
		versionedOntologyDocument.setVersionedOntologyID(versionedOntologyID);
		versionedOntologyDocument.setRevisionData(revisionData);
		//
		//versionedOntologyDocument.setVersionedOntology(versionedOntology);
	}
	
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public VOWLXMLDocument getOWLObject() throws OWLXMLParserException {
		return versionedOntologyDocument;
	}
}
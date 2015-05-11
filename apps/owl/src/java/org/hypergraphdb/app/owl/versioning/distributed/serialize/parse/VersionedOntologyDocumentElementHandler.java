package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VersionedOntologyDocumentElementHandler.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VersionedOntologyDocumentElementHandler extends AbstractVOWLElementHandler<VOWLXMLDocument>
{

	private VOWLXMLRenderConfiguration renderConfig;
	// private VersionedOntology versionedOntology;
//	private List<Revision> revisions;
//	private List<ChangeSet> changesets;
//	private OWLOntologyEx revisionData;
//	private int headRevisionIndex = -2; // means not parsed yet
//	private HGPersistentHandle versionedOntologyID;
	// private String xmlBase;

	private VOWLXMLDocument versionedOntologyDocument;
	
	public VersionedOntologyDocumentElementHandler(OWLXMLParserHandler handler)
	{
		super(handler);
		versionedOntologyDocument = getDocumentRoot();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("xml:base"))
		{
			// xmlBase = value;
		}
	}

	@Override
	public void handleChild(RenderConfigurationElementHandler h) throws OWLXMLParserException
	{
		renderConfig = h.getOWLObject();
	}

	@Override
	public void handleChild(VersionedOntologyElementHandler h) throws OWLXMLParserException
	{
//		changesets = h.getChangeSets();
//		revisions = h.getRevisions();
//		// determine later: versionedOntology
//		headRevisionIndex = h.getHeadRevisionIndex();
//		revisionData = h.getOntologyHeadData();
//		versionedOntologyID = h.getOntologyID();
//		// NO! : h.getOWLObject(), we use all the other methods.
	}

	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		System.out.println("VersionedOntologyDocumentElementHandler");
		// We either have it already, or we create it
		// MATCH WITH RENDERCONFIG and see if we are valid here.

//
//		versionedOntologyDocument.setRenderConfig(renderConfig);
//		versionedOntologyDocument.setHeadRevisionIndex(headRevisionIndex);
//		versionedOntologyDocument.setChangesets(changesets);
//		versionedOntologyDocument.setRevisions(revisions);
//		versionedOntologyDocument.setVersionedOntologyID(versionedOntologyID);
//		versionedOntologyDocument.setRevisionData(revisionData);
//		//
//		// versionedOntologyDocument.setVersionedOntology(versionedOntology);
	}
	
	@Override
	public VOWLXMLDocument getOWLObject() throws OWLXMLParserException
	{
		return versionedOntologyDocument;
	}
}
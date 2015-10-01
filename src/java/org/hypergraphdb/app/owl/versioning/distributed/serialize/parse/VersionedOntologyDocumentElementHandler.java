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
	private VOWLXMLDocument versionedOntologyDocument;
	
	public VersionedOntologyDocumentElementHandler(OWLXMLParserHandler handler)
	{
		super(handler);
		versionedOntologyDocument = getDocumentRoot();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
	}

	@Override
	public void handleChild(RenderConfigurationElementHandler h) throws OWLXMLParserException
	{
		versionedOntologyDocument.setRenderConfig(h.getOWLObject());
	}

	@Override
	public void handleChild(MetadataElementHandler h) throws OWLXMLParserException
	{
		versionedOntologyDocument.setMetadata(h.getOWLObject());
	}
	
	@Override
	public void handleChild(VersionedOntologyElementHandler h) throws OWLXMLParserException
	{
		System.out.println("The onot: " + h.getText());
	}

	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
	}
	
	@Override
	public VOWLXMLDocument getOWLObject() throws OWLXMLParserException
	{
		return versionedOntologyDocument;
	}
}
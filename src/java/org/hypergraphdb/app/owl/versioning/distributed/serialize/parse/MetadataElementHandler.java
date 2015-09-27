package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLMetadata;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class MetadataElementHandler extends AbstractVOWLElementHandler<VOWLXMLMetadata>
{
	VOWLXMLMetadata metadata = new VOWLXMLMetadata();
	
	public MetadataElementHandler(OWLXMLParserHandler handler)
	{
		super(handler);
	}

	@Override
	public void handleChild(BranchElementHandler h) throws OWLXMLParserException
	{
		metadata.branches().add(h.getOWLObject());
	}
	
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public VOWLXMLMetadata getOWLObject() throws OWLXMLParserException
	{
		return metadata;
	}
}

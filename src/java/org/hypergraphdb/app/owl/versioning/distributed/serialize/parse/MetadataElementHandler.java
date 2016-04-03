package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLMetadata;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class MetadataElementHandler extends AbstractVOWLElementHandler<VOWLXMLMetadata>
{
	VOWLXMLMetadata metadata = new VOWLXMLMetadata();
	HyperGraph graph;
	
	public MetadataElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("lastMetaChange"))
		{
			metadata.lastChange(graph.getHandleFactory().makeHandle(value.trim()));
		}
		else
		{
			throw new OWLParserException("Attribute: " + localName + " not recognized.", getLineNumber(), getColumnNumber());
		}		
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

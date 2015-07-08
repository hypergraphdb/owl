package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.RevisionMark;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class RevisionMarkElementHandler extends AbstractVOWLElementHandler<RevisionMark>
{
	private HyperGraph graph;
	private RevisionMark revisionMark;

	public RevisionMarkElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		revisionMark = new RevisionMark();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("revision"))
		{
			revisionMark.revision(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("mark"))
		{
			revisionMark.changeRecord(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("handle"))
		{
			revisionMark.setAtomHandle(graph.getHandleFactory().makeHandle(value));
		}		
		else
			throw new IllegalArgumentException("Unrecognized attribute '" + localName + " for RevisionMark");
	}
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public RevisionMark getOWLObject() throws OWLXMLParserException
	{
		return revisionMark;
	}
}
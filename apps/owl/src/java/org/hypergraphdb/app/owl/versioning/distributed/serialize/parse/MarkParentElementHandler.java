package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.newver.MarkParent;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class MarkParentElementHandler extends AbstractVOWLElementHandler<MarkParent>
{
	private HyperGraph graph;
	private MarkParent markParent;

	public MarkParentElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		markParent = new MarkParent();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("parent"))
		{
			markParent.parent(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("child"))
		{
			markParent.child(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("handle"))
		{
			markParent.setAtomHandle(graph.getHandleFactory().makeHandle(value.trim()));
		}		
		else
			throw new IllegalArgumentException("Unrecognized attribute '" + localName + " for MarkParent");
	}
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public MarkParent getOWLObject() throws OWLXMLParserException
	{
		return markParent;
	}
}
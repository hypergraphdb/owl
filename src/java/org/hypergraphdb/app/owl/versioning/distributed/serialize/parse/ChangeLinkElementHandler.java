package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class ChangeLinkElementHandler extends AbstractVOWLElementHandler<ChangeLink>
{
	private HyperGraph graph;
	private ChangeLink link;

	public ChangeLinkElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		link = new ChangeLink();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("parent"))
		{
			link.parent(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("child"))
		{
			link.child(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("change"))
		{
			link.change(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("handle"))
		{
			link.setAtomHandle(graph.getHandleFactory().makeHandle(value.trim()));
		}		
		else
			throw new IllegalArgumentException("Unrecognized attribute '" + localName + " for ParentLink");
	}
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public ChangeLink getOWLObject() throws OWLXMLParserException
	{
		return link;
	}
}
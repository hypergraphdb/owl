package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ParentLink;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class ParentLinkElementHandler extends AbstractVOWLElementHandler<ParentLink>
{
	private HyperGraph graph;
	private ParentLink markParent;

	public ParentLinkElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		markParent = new ParentLink();
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
			throw new IllegalArgumentException("Unrecognized attribute '" + localName + " for ParentLink");
	}
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public ParentLink getOWLObject() throws OWLXMLParserException
	{
		return markParent;
	}
}
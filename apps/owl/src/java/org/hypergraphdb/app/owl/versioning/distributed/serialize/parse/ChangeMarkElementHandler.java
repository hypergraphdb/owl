package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.newver.ChangeMark;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class ChangeMarkElementHandler extends AbstractVOWLElementHandler<ChangeMark>
{
	private HyperGraph graph;
	private ChangeMark changeMark;

	public ChangeMarkElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		changeMark = new ChangeMark();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("target"))
		{
			changeMark.target(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("changeSet"))
		{
			changeMark.changeSet(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("handle"))
		{
			changeMark.setAtomHandle(graph.getHandleFactory().makeHandle(value.trim()));
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
	public ChangeMark getOWLObject() throws OWLXMLParserException
	{
		return changeMark;
	}
}
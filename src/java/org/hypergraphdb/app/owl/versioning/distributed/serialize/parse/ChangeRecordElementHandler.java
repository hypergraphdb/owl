package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeRecord;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class ChangeRecordElementHandler extends AbstractVOWLElementHandler<ChangeRecord>
{
	private HyperGraph graph;
	private ChangeRecord changeRecord;

	public ChangeRecordElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		changeRecord = new ChangeRecord();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("target"))
		{
			changeRecord.versioned(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("changeSet"))
		{
			changeRecord.changeSet(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("handle"))
		{
			changeRecord.setAtomHandle(graph.getHandleFactory().makeHandle(value.trim()));
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
	public ChangeRecord getOWLObject() throws OWLXMLParserException
	{
		return changeRecord;
	}
}
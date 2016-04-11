package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VAddLabelChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class AddLabelElementHandler extends AbstractVOWLElementHandler<VAddLabelChange<VersionedOntology>>
{
	private VAddLabelChange<VersionedOntology> change;
	private String label;
	private HyperGraph graph;
	
	public AddLabelElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		change = new VAddLabelChange<VersionedOntology>();
	}
	
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("handle"))
		{
			change.setAtomHandle(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("label"))
		{
			label = value;
		}
		else if (localName.equals("labelHandle"))
		{
			change.setLabel(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("labeled"))
		{
			change.setLabeled(graph.getHandleFactory().makeHandle(value));
		}		
		else
			throw new IllegalArgumentException("Unrecognized attribute '" + localName + " for RevisionMark");
	}
	
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		if (graph.get(change.getLabel()) == null)
			graph.define(change.getLabel(), label);
		((MetadataElementHandler)getParentHandler()).handleChange(change);			
	}

	@Override
	public VAddLabelChange<VersionedOntology> getOWLObject() throws OWLXMLParserException
	{
		return change;
	}
}
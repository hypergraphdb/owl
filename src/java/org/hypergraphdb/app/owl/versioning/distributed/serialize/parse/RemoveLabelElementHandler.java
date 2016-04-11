package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VRemoveLabelChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class RemoveLabelElementHandler extends AbstractVOWLElementHandler<VRemoveLabelChange<VersionedOntology>>
{
	private VRemoveLabelChange<VersionedOntology> change;
	private HyperGraph graph;
	
	public RemoveLabelElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		change = new VRemoveLabelChange<VersionedOntology>();
	}
	
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("handle"))
		{
			change.setAtomHandle(graph.getHandleFactory().makeHandle(value));
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
		((MetadataElementHandler)getParentHandler()).handleChange(change);			
	}

	@Override
	public VRemoveLabelChange<VersionedOntology> getOWLObject() throws OWLXMLParserException
	{
		return change;
	}
}
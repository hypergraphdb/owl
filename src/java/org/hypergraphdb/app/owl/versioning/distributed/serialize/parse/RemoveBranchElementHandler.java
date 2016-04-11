package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VRemoveBranchChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class RemoveBranchElementHandler extends AbstractVOWLElementHandler<VRemoveBranchChange<VersionedOntology>>
{
	private VRemoveBranchChange<VersionedOntology> change;
	private HyperGraph graph;
	
	public RemoveBranchElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		change = new VRemoveBranchChange<VersionedOntology>();
	}
	
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("handle"))
		{
			change.setAtomHandle(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("name"))
		{
			change.setName(value);
		}
		else if (localName.equals("user"))
		{
			change.setUser(value);
		}		
		else if (localName.equals("revision"))
		{
			change.setRevision(graph.getHandleFactory().makeHandle(value));
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
	public VRemoveBranchChange<VersionedOntology> getOWLObject() throws OWLXMLParserException
	{
		return change;
	}
}
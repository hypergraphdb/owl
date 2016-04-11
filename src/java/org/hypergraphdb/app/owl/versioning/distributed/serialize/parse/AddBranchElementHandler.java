package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VAddBranchChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class AddBranchElementHandler extends AbstractVOWLElementHandler<VAddBranchChange<VersionedOntology>>
{
	private VAddBranchChange<VersionedOntology> change;
	private HyperGraph graph;
	
	public AddBranchElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		change = new VAddBranchChange<VersionedOntology>();
	}
	
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("handle"))
		{
			change.setAtomHandle(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("createdOn"))
		{
			change.setCreatedOn(Long.parseLong(value));
		}
		else if (localName.equals("createdBy"))
		{
			change.setCreatedBy(value);
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
	public VAddBranchChange<VersionedOntology> getOWLObject() throws OWLXMLParserException
	{
		return change;
	}
}
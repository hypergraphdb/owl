package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class BranchElementHandler extends AbstractVOWLElementHandler<Branch>
{
	private Branch branch;
	private HyperGraph graph;
	
	public BranchElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		branch = new Branch();
	}
	
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("name"))
		{
			branch.setName(value);
		}
		else if (localName.equals("createdOn"))
		{
			branch.setCreatedOn(Long.parseLong(value));
		}
		else if (localName.equals("createdBy"))
		{
			branch.setCreatedBy(value);
		}		
		else if (localName.equals("handle"))
		{
			branch.setAtomHandle(graph.getHandleFactory().makeHandle(value));
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
	public Branch getOWLObject() throws OWLXMLParserException
	{
		return branch;
	}
}

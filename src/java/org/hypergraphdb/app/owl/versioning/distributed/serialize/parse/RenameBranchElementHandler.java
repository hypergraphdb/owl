package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VBranchRenameChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

public class RenameBranchElementHandler extends AbstractVOWLElementHandler<VBranchRenameChange<VersionedOntology>>
{
	private VBranchRenameChange<VersionedOntology> change;
	private HyperGraph graph;
	
	public RenameBranchElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		change = new VBranchRenameChange<VersionedOntology>();
	}
	
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("handle"))
		{
			change.setAtomHandle(graph.getHandleFactory().makeHandle(value));
		}
		else if (localName.equals("currentName"))
		{
			change.setCurrentName(value);
		}
		else if (localName.equals("newName"))
		{
			change.setNewname(value);
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
	public VBranchRenameChange<VersionedOntology> getOWLObject() throws OWLXMLParserException
	{
		return change;
	}
}

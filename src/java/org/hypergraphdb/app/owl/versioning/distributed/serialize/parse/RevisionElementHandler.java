package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * RevisionElementHandler.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class RevisionElementHandler extends AbstractVOWLElementHandler<Revision>
{
	private HyperGraph graph;
	private Revision revision;

	public RevisionElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		revision = new Revision();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("ontology"))
		{
			revision.versioned(graph.getHandleFactory().makeHandle(value.trim()));
		}
		else if (localName.equals("handle"))
		{
			revision.setAtomHandle(graph.getHandleFactory().makeHandle(value.trim()));
		}
		else if (localName.equals("user"))
		{
			revision.user(value);
		}
		else if (localName.equals("timestamp"))
		{
			revision.timestamp(Long.parseLong(value));
		}
		else if (localName.equals("comment"))
		{
			revision.comment(value);
		}
		else
		{
			throw new OWLParserException("Attribute: " + localName + " not recognized.", getLineNumber(), getColumnNumber());
		}
	}

	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public Revision getOWLObject() throws OWLXMLParserException
	{
		return revision;
	}
}
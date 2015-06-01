package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * ChangeSetElementHandler.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @author Borislav Iordanov
 * @created Feb 29, 2012
 */
public class ChangeSetElementHandler extends AbstractVOWLElementHandler<ChangeSet<VersionedOntology>>
{
	private HyperGraph graph;
	private ChangeSet<VersionedOntology> changeSet;

	public ChangeSetElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
		changeSet = new ChangeSet<VersionedOntology>();
		changeSet.setHyperGraph(graph);
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("timestamp"))
		{
			try
			{
				changeSet.timestamp(Long.parseLong(value.trim()));
			}
			catch (NumberFormatException e)
			{
				throw new OWLParserException("Could not parse timestamp: " + value);
			}
		}
		else if (localName.equals("handle"))
		{
			changeSet.setAtomHandle(graph.getHandleFactory().makeHandle(value.trim()));
		}		
	}

	@Override
	public void handleChild(VOWLChangeElementHandler h) throws OWLXMLParserException
	{
		// We expect to be called in order here.
		// The first call to handleChild must refer to the oldest change in a changeset.
		VOWLChange c = h.getOWLObject();
		changeSet.add(c);
	}

	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public ChangeSet<VersionedOntology> getOWLObject() throws OWLXMLParserException
	{
		return changeSet;
	}
}
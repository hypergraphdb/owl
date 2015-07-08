package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * RenderConfigurationElementHandler.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class RenderConfigurationElementHandler extends AbstractVOWLElementHandler<VOWLXMLRenderConfiguration>
{
	private HyperGraph graph;
	private VOWLXMLRenderConfiguration configuration = new VOWLXMLRenderConfiguration();

	/**
	 * @param handler
	 */
	public RenderConfigurationElementHandler(HyperGraph graph, OWLXMLParserHandler handler)
	{
		super(handler);
		this.graph = graph;
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{
		if (localName.equals("firstRevision"))
			configuration.firstRevision(graph.getHandleFactory().makeHandle(value));
		else if (localName.equals("revisionSnapshot"))
			configuration.revisionSnapshot(graph.getHandleFactory().makeHandle(value));
		else if (localName.equals("maxDepth"))
			configuration.maxDepth(Integer.parseInt(value));
		else
			throw new IllegalStateException("");
	}
	
	@Override
	public void startElement(String name) throws OWLXMLParserException
	{
		// reset();
	}

	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		getParentHandler().handleChild(this);
	}

	@Override
	public VOWLXMLRenderConfiguration getOWLObject() throws OWLXMLParserException
	{
		if (configuration == null)
			throw new OWLXMLParserException("Could not read configuration", getLineNumber(), getColumnNumber());
		return configuration;
	}
}
package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParserHandler;

/**
 * AbstractVOWLElementHandler.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public abstract class AbstractVOWLElementHandler<O> extends AbstractOWLElementHandler<O> implements VOWLElementHandler<O>
{
	private VOWLXMLParserHandler handler;

	protected AbstractVOWLElementHandler(OWLXMLParserHandler handler)
	{
		super(handler);
		this.handler = (VOWLXMLParserHandler) handler;
	}

	public VOWLXMLDocument getDocumentRoot()
	{
		return handler.getDocumentRoot();
	}

	public VOWLElementHandler<?> getParentHandler()
	{
		return (VOWLElementHandler<?>) super.getParentHandler();
	}

	HyperGraph getHyperGraph()
	{
		return getOWLDataFactory().getHyperGraph();
	}

	public OWLDataFactoryHGDB getOWLDataFactory()
	{
		return ((OWLDataFactoryHGDB) super.getOWLDataFactory());
	}

	@Override
	public void handleChild(ParentLinkElementHandler h)
			throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(ChangeRecordElementHandler h)
			throws OWLXMLParserException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleChild(RevisionMarkElementHandler h)
			throws OWLXMLParserException
	{
		// TODO Auto-generated method stub
		
	}	

	@Override
	public void handleChild(RenderConfigurationElementHandler h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(VersionedOntologyElementHandler h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(RevisionElementHandler h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(ChangeSetElementHandler h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(VOWLChangeElementHandler h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(OWLOntologyHandlerModified h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(OWLImportsHandlerModified h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(VPrefixMapElementHandler h) throws OWLXMLParserException
	{
	}

	@Override
	public void handleChild(VPrefixMapEntryElementHandler h) throws OWLXMLParserException
	{
	}
}
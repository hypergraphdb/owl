package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.Map;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.core.OWLOntologyEx;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VersionedOntologyElementHandler.
 * 
 * This class does not create a VersionedOntology, because it cannot be
 * determined here, if a whole versionedOntology was parsed. The
 * VersionedOntologyRootHandler will interpret the render configuration
 * information and our data and will determine, if a new VersionedOntology shall
 * be created.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VersionedOntologyElementHandler extends AbstractVOWLElementHandler<Object>
{
	private HGPersistentHandle ontologyID;
	private OWLOntologyEx ontologyHeadData;

	/**
	 * @param handler
	 */
	public VersionedOntologyElementHandler(OWLXMLParserHandler handler)
	{
		super(handler);
		reset();
	}

	public void reset()
	{
		ontologyID = null;
		ontologyHeadData = null;
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{		
		if (localName.equals("ontologyID"))
			this.getDocumentRoot().setOntologyID(value);
		else if (localName.equals("versionedID"))
			this.getDocumentRoot().setVersionedID(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.
	 * AbstractVOWLElementHandler
	 * #handleChild(org.hypergraphdb.app.owl.versioning
	 * .distributed.serialize.parse.RevisionElementHandler)
	 */
	@Override
	public void handleChild(RevisionElementHandler h) throws OWLXMLParserException
	{
		this.getDocumentRoot().revisionObjects().add(h.getOWLObject());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.
	 * AbstractVOWLElementHandler
	 * #handleChild(org.hypergraphdb.app.owl.versioning
	 * .distributed.serialize.parse.ChangeSetElementHandler)
	 */
	@Override
	public void handleChild(ChangeSetElementHandler h) throws OWLXMLParserException
	{
		this.getDocumentRoot().revisionObjects().add(h.getOWLObject());		
		this.getDocumentRoot().changeSetMap().put(h.getOWLObject(), h.changes());
	}
	
	@Override
	public void handleChild(ChangeLinkElementHandler h) throws OWLXMLParserException
	{
		this.getDocumentRoot().revisionObjects().add(h.getOWLObject());	
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.
	 * AbstractVOWLElementHandler
	 * #handleChild(org.hypergraphdb.app.owl.versioning
	 * .distributed.serialize.parse.OWLOntologyHandlerModified)
	 */
	@Override
	public void handleChild(OWLOntologyHandlerModified h) throws OWLXMLParserException
	{
		ontologyHeadData = h.getOWLObject();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleChild(VPrefixMapElementHandler h) throws OWLXMLParserException
	{
		if (ontologyHeadData == null)
		{
			throw new OWLXMLParserException("VPrefixMap must not exist without an OwlOntology data entry.", getLineNumber(),
					getColumnNumber());
		}
		ontologyHeadData.setPrefixes((Map<String, String>) h.getOWLObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException
	{
		System.out.println();
		getParentHandler().handleChild(this);
	}

	/**
	 * Unsupported exception thrown here; use other methods to get parsed
	 * objects.
	 */
	@Override
	public Object getOWLObject() throws OWLXMLParserException
	{
		throw new UnsupportedOperationException("Use the other methods to get changesets and revisions");
	}

	/**
	 * @return the ontologyID
	 */
	public HGPersistentHandle getOntologyID() throws OWLXMLParserException
	{
		return ontologyID;
	}

	/**
	 * All ontology axioms, annotations and importDeclarations will be part of
	 * the ontology, applied as changes directly; bypassing the manager to avoid
	 * change tracking. In case of an HGDBOntologyImpl, all a
	 * 
	 * @return the ontologyHeadData
	 */
	public OWLOntologyEx getOntologyHeadData() throws OWLXMLParserException
	{
		return ontologyHeadData;
	}
}
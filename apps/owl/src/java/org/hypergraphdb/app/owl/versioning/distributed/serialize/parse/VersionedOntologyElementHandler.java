package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGHandleHolder;
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
	private int headRevisionIndex; // -1 is undefined

	private Set<HGHandleHolder> revisionObjects;
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
		headRevisionIndex = -1;
		revisionObjects = new HashSet<HGHandleHolder>();
		ontologyHeadData = null;
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException
	{		
		if (localName.equals("ontologyID"))
			this.getDocumentRoot().setOntologyID(value);
		else if (localName.equals("versionedID"))
			this.getDocumentRoot().setVersionedID(value);
//
//		if (localName.equals("ontologyID"))
//		{
//			ontologyID = UUIDPersistentHandle.makeHandle(value.trim());
//		}
//		if (localName.equals("headRevisionIndex"))
//		{
//			headRevisionIndex = Integer.parseInt(value.trim());
//		}
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
//		Revision r = h.getOWLObject();
		// Add Revision to Graph or not??
		// > NO, will be added as bean of the Pair object in VersionedOntology
//		getHyperGraph().define(r.getAtomHandle(), r);
//		System.out.print("R" + revisions.size());
		revisionObjects.add(h.getOWLObject());
	}

	@Override
	public void handleChild(RevisionMarkElementHandler h) throws OWLXMLParserException
	{
		revisionObjects.add(h.getOWLObject());

//		RevisionMark r = h.getOWLObject();
		// Add Revision to Graph or not??
		// > NO, will be added as bean of the Pair object in VersionedOntology
		//getHyperGraph().define(r.getAtomHandle(), r);
//		System.out.print("R" + revisions.size());
//		revisions.add(r);
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
		revisionObjects.add(h.getOWLObject());		
//		ChangeSet<VersionedOntology> c = h.getOWLObject();
//		// Add Changeset link to Graph or not??
//		// > Yes so we can remove changes more easily later if things go wrong.
//		getHyperGraph().add(c);
//		changeSets.add(c);
	}

	@Override
	public void handleChild(ChangeMarkElementHandler h) throws OWLXMLParserException
	{
		revisionObjects.add(h.getOWLObject());		
	}
	
	@Override
	public void handleChild(MarkParentElementHandler h) throws OWLXMLParserException
	{
		revisionObjects.add(h.getOWLObject());		
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
		ontologyHeadData.setPrefixesFrom((Map<String, String>) h.getOWLObject());
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
	 * @return the headRevisionIndex
	 */
	public int getHeadRevisionIndex() throws OWLXMLParserException
	{
		return headRevisionIndex;
	}

	/**
	 * Each revision will also be stored in the graph.
	 * 
	 * @return the revisions
	 */
	public Set<HGHandleHolder> getRevisionObjects() throws OWLXMLParserException
	{
		return revisionObjects;
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
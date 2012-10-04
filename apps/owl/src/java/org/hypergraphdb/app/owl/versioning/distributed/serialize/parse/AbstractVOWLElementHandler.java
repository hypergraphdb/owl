package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLDocument;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLParserHandler;

/**
 * AbstractVOWLElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public abstract class AbstractVOWLElementHandler<O> extends AbstractOWLElementHandler<O> implements VOWLElementHandler<O> {

	private VOWLXMLParserHandler handler;
	
	/**
	 * @param handler
	 */
	protected AbstractVOWLElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		this.handler = (VOWLXMLParserHandler)handler;
	}

	public void setParentHandler(OWLElementHandler<?> handler) {
        super.setParentHandler((VOWLElementHandler<?>)handler);
    }

	public VOWLXMLDocument getDocumentRoot() {
        return handler.getDocumentRoot();
    }	

    public VOWLElementHandler<?> getParentHandler() {
        return (VOWLElementHandler<?>)super.getParentHandler();
    }	
    
    HyperGraph getHyperGraph() {
    	return getOWLDataFactory().getHyperGraph();
    }
    
    public OWLDataFactoryHGDB getOWLDataFactory() {
    	return ((OWLDataFactoryHGDB)super.getOWLDataFactory());
    }

    /* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RenderConfigurationElementHandler)
	 */
	@Override
	public void handleChild(RenderConfigurationElementHandler h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyElementHandler)
	 */
	@Override
	public void handleChild(VersionedOntologyElementHandler h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RevisionElementHandler)
	 */
	@Override
	public void handleChild(RevisionElementHandler h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.ChangeSetElementHandler)
	 */
	@Override
	public void handleChild(ChangeSetElementHandler h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLChangeElementHandler)
	 */
	@Override
	public void handleChild(VOWLChangeElementHandler h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OWLOntologyHandlerModified)
	 */
	@Override
	public void handleChild(OWLOntologyHandlerModified h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OWLImportsHandlerModified)
	 */
	@Override
	public void handleChild(OWLImportsHandlerModified h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VPrefixMapElementHandler)
	 */
	@Override
	public void handleChild(VPrefixMapElementHandler h) throws OWLXMLParserException {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VPrefixMapEntryElementHandler)
	 */
	@Override
	public void handleChild(VPrefixMapEntryElementHandler h) throws OWLXMLParserException {
	}
}
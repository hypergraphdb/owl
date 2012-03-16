package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLRenderConfiguration;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * RenderConfigurationElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class RenderConfigurationElementHandler extends AbstractVOWLElementHandler<VOWLXMLRenderConfiguration> {
	 
	private int firstRevisionIndex;
	private int lastRevisionIndex;
	private boolean lastRevisionData;
	private boolean unCommittedChanges;
	private boolean lastRevisionDataParsed;
	private boolean unCommittedChangesParsed;
	private VOWLXMLRenderConfiguration configuration; 
	
	/**
	 * @param handler
	 */
	public RenderConfigurationElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		reset();
	}

	public void reset() {
		firstRevisionIndex = -1;
		lastRevisionIndex = -1;
		lastRevisionData = false;
		unCommittedChanges = false;
		lastRevisionDataParsed = false;
		unCommittedChangesParsed = false;
		configuration = null;
	}
	
	@Override
	public void attribute(String localName, String value) throws OWLParserException {
        if (localName.equals("firstRevisionIndex")) {
        	firstRevisionIndex = Integer.parseInt(value.trim());
        } else if (localName.equals("lastRevisionIndex")) {
        	lastRevisionIndex = Integer.parseInt(value.trim());
        } else if (localName.equals("lastRevisionData")) {
        	lastRevisionData = Boolean.parseBoolean(value.trim());
        	lastRevisionDataParsed = true;
        } else if (localName.equals("unCommittedChanges")) {
        	unCommittedChanges = Boolean.parseBoolean(value.trim());
        	unCommittedChangesParsed = true;
        } else {
        	throw new IllegalStateException("");
        }
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws OWLXMLParserException {
		//reset();
	}
		
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		if (firstRevisionIndex < 0) throw new OWLParserException("firstRevision index not parsed.", getLineNumber(), getColumnNumber());
		if (lastRevisionIndex < 0) throw new OWLParserException("lastRevisionIndex index not parsed.", getLineNumber(), getColumnNumber());
		if (!lastRevisionDataParsed) throw new OWLParserException("lastRevisionDataParsed index not parsed.", getLineNumber(), getColumnNumber());
		if (!unCommittedChangesParsed) throw new OWLParserException("unCommittedChangesParsed index not parsed.", getLineNumber(), getColumnNumber());
		configuration = new VOWLXMLRenderConfiguration();
		configuration.setFirstRevisionIndex(firstRevisionIndex);
		configuration.setLastRevisionIndex(lastRevisionIndex);
		configuration.setLastRevisionData(lastRevisionData);
		configuration.setUncommittedChanges(unCommittedChanges);
		getParentHandler().handleChild(this);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public VOWLXMLRenderConfiguration getOWLObject() throws OWLXMLParserException {
		if (configuration == null) throw new OWLXMLParserException("Could not read configuration", getLineNumber(), getColumnNumber());
		return configuration;
	}
}
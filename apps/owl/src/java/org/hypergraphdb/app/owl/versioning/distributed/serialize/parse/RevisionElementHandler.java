package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.Date;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.handle.UUIDPersistentHandle;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * RevisionElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class RevisionElementHandler extends AbstractVOWLElementHandler<Revision> {
	
	private HGPersistentHandle ontologyID;
	private int revisionNumber = -1;
	private String user;
	private boolean userParsed;
	private Date timeStamp;
	private String revisionCommment;	
	private Revision revision;
	
	/**
	 * @param handler
	 */
	public RevisionElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

	public void reset() {
		ontologyID = null;
		revisionNumber = -1;
		user = null;
		userParsed = false;
		timeStamp = null;
		revisionCommment = null;	
		revision = null;
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException {
        if (localName.equals("ontologyID")) {
            ontologyID =  UUIDPersistentHandle.makeHandle(value.trim());
        } else if (localName.equals("revision")) {
        	revisionNumber = Integer.parseInt(value.trim());
        } else if (localName.equals("user")) {
        	user = value;
        	if ("".equals(user)) {
        		user = Revision.USER_ANONYMOUS;
        	}
        	userParsed = true;
        } else if (localName.equals("timeStamp") ){
        	//timeStamp="Mon Mar 05 15:40:55 EST 2012"
        	//ignored
        } else if (localName.equals("timeStampLong")) {
        	try {
				timeStamp = new Date(Long.parseLong(value.trim()));
			} catch (NumberFormatException e) {
				throw new OWLParserException("Could not parse timeStampLong " + value, getLineNumber(), getColumnNumber());
			}
        } else if (localName.equals("revisionComment")) {
        	revisionCommment = value;
        } else {
        	throw new OWLParserException("Attribute: " + localName + " not recognized.", getLineNumber(), getColumnNumber());
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
		System.out.println("RevisionElementHandler end Element RevNr: " + revisionNumber);
		if (ontologyID == null) throw new OWLParserException("was null");
		if (revisionNumber < 0) throw new OWLParserException("revision (int) was not parsed");
		if (!userParsed) throw new OWLParserException("user was not parsed");
		if (timeStamp == null) throw new OWLParserException("TimeStamp was null");
		if (revisionCommment == null) throw new OWLParserException("RevisionComment was null");
		revision = new Revision();
		revision.setOntologyUUID(ontologyID);
		revision.setRevision(revisionNumber);
		revision.setUser(user);
		revision.setTimeStamp(timeStamp);
		revision.setRevisionComment(revisionCommment);
		getParentHandler().handleChild(this);
	}


	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public Revision getOWLObject() throws OWLXMLParserException {
		if (revision == null) throw new OWLXMLParserException("Could not parse Revision", getLineNumber(), getColumnNumber());
		return revision;
	}
}
package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * ChangeSetElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class ChangeSetElementHandler extends AbstractVOWLElementHandler<ChangeSet> {

	private Date createdDate;
	private List<HGHandle> changes;
	private ChangeSet changeSet;
	
	/**
	 * @param handler
	 */
	public ChangeSetElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		reset();
	}
	
	public void reset() {
		createdDate = null;
		changes = new LinkedList<HGHandle>();
		changeSet = null;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws OWLXMLParserException {
		//System.out.println("CHANGESET END, Line: " + getLineNumber());
		//reset();
	}

	@Override
	public void attribute(String localName, String value) throws OWLParserException {
		//createdDateLong ="Mon Mar 05 15:40:55 EST 2012"
        if (localName.equals("createdDateLong")) {
        	try {
        		createdDate = new Date(Long.parseLong(value.trim()));
        	} catch (NumberFormatException e) {
        		throw new OWLParserException("Could not parse createdDateLong: " + value);
        	}
        } 
        //createdDate ignored!
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.AbstractVOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLChangeElementHandler)
	 */
	@Override
	public void handleChild(VOWLChangeElementHandler h) throws OWLXMLParserException {
		//We expect to be called in order here. 
		//The first call to handleChild must refer to the oldest change in a changeset.
		VOWLChange c = h.getOWLObject();
		HGHandle change = getHyperGraph().add(c);
		//ORDERED!
		changes.add(change);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		if (createdDate == null) throw new OWLParserException("Missing createdDate", getLineNumber(), getColumnNumber());
		if (changes == null) throw new OWLParserException("Missing changes", getLineNumber(), getColumnNumber());
		changeSet = new ChangeSet(changes);
		changeSet.setCreatedDate(createdDate);
		getParentHandler().handleChild(this);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public ChangeSet getOWLObject() throws OWLXMLParserException {
		if (changeSet == null) throw new OWLXMLParserException("Expected Changeset was null", getLineNumber(), getColumnNumber());
		return changeSet;
	}
}
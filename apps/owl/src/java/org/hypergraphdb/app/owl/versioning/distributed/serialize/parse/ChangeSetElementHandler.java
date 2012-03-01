package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * ChangeSetElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class ChangeSetElementHandler extends AbstractVOWLElementHandler<ChangeSet> {

	/**
	 * @param handler
	 */
	public ChangeSetElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		System.out.println("CHANGESET END Line: " + getLineNumber());
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public ChangeSet getOWLObject() throws OWLXMLParserException {
		// TODO Auto-generated method stub
		return null;
	}

}

package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.coode.owlapi.owlxmlparser.SWRLAtomListElementHandler;

/**
 * OrigSWRLAtomListElementHandler.
 * Constructor in superclass was protected, so we had to create a class here.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 5, 2012
 */
public class OrigSWRLAtomListElementHandler extends SWRLAtomListElementHandler {

	/**
	 * @param handler
	 */
	public OrigSWRLAtomListElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

}

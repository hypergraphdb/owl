package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;

/**
 * VOWLChangeElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public abstract class VOWLChangeElementHandler extends AbstractVOWLElementHandler<VOWLChange> {

	/**
	 * @param handler
	 */
	public VOWLChangeElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

}

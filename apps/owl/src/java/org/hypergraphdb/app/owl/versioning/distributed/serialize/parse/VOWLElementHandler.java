package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLElementHandler;

/**
 * VOWLElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public interface VOWLElementHandler<O> extends OWLElementHandler<O> {
	void handleChild(RevisionElementHandler h);
	void handleChild(RenderConfigurationElementHandler h);
	void handleChild(ChangeSetElementHandler h);
	void handleChild(VersionedOntologyElementHandler h);
	void handleChild(VOWLChangeElementHandler h);
}

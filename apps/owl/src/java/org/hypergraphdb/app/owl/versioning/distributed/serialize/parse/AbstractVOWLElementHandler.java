package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.AbstractClassExpressionElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractIRIElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractOWLDataRangeHandler;
import org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractOWLObjectPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnonymousIndividualElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDatatypeFacetRestrictionElementHandler;
import org.coode.owlapi.owlxmlparser.OWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owlapi.owlxmlparser.OWLLiteralElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSubObjectPropertyChainElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.coode.owlapi.owlxmlparser.SWRLAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLAtomListElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLVariableElementHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * AbstractVOWLElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public abstract class AbstractVOWLElementHandler<O> extends AbstractOWLElementHandler<O> implements VOWLElementHandler<O> {

	/**
	 * @param handler
	 */
	protected AbstractVOWLElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RevisionElementHandler)
	 */
	@Override
	public void handleChild(RevisionElementHandler h) {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RenderConfigurationElementHandler)
	 */
	@Override
	public void handleChild(RenderConfigurationElementHandler h) {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.ChangeSetElementHandler)
	 */
	@Override
	public void handleChild(ChangeSetElementHandler h) {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyElementHandler)
	 */
	@Override
	public void handleChild(VersionedOntologyElementHandler h) {
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLChangeElementHandler)
	 */
	@Override
	public void handleChild(VOWLChangeElementHandler h) {
	}

}

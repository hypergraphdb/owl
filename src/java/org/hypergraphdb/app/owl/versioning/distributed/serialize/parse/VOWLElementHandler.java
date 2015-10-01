package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;

/**
 * VOWLElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public interface VOWLElementHandler<O> extends OWLElementHandler<O> 
{
	void handleChild(ParentLinkElementHandler h) throws OWLXMLParserException;
	void handleChild(ChangeRecordElementHandler h) throws OWLXMLParserException;
	void handleChild(RevisionMarkElementHandler h) throws OWLXMLParserException;
	void handleChild(RevisionElementHandler h) throws OWLXMLParserException;
	void handleChild(BranchElementHandler h) throws OWLXMLParserException;	
	void handleChild(RenderConfigurationElementHandler h) throws OWLXMLParserException;
	void handleChild(MetadataElementHandler h) throws OWLXMLParserException;
	void handleChild(ChangeSetElementHandler h) throws OWLXMLParserException;
	void handleChild(VersionedOntologyElementHandler h) throws OWLXMLParserException;
	void handleChild(VOWLChangeElementHandler h) throws OWLXMLParserException;
	void handleChild(OWLImportsHandlerModified h) throws OWLXMLParserException;
	void handleChild(OWLOntologyHandlerModified h) throws OWLXMLParserException;
	void handleChild(VPrefixMapElementHandler h) throws OWLXMLParserException;
	void handleChild(VPrefixMapEntryElementHandler h) throws OWLXMLParserException;
}
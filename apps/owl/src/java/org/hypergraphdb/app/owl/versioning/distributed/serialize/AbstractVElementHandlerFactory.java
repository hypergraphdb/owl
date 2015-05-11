package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import org.coode.owlapi.owlxmlparser.OWLElementHandlerFactory;

/**
 * AbstractVElementHandlerFactory.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public abstract class AbstractVElementHandlerFactory implements OWLElementHandlerFactory
{

	private String elementName;

	public AbstractVElementHandlerFactory(VOWLXMLVocabulary v)
	{
		this.elementName = v.getShortName();
	}

	protected AbstractVElementHandlerFactory(String elementName)
	{
		this.elementName = elementName;
	}

	public String getElementName()
	{
		return elementName;
	}
}

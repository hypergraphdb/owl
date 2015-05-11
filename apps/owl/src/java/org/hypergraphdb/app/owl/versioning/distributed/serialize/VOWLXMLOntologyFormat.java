package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;

/**
 * VOWLXMLOntologyFormat.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 14, 2012
 */
public class VOWLXMLOntologyFormat extends OWLXMLOntologyFormat
{
	private static final long serialVersionUID = 1L;

	@Override
	public String toString()
	{
		return "Versioned OWL/XML";
	}
}

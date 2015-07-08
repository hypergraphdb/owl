package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;

/**
 * VOWLXMLVersionedOntologyFormat.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VOWLXMLVersionedOntologyFormat extends OWLXMLOntologyFormat
{
	private static final long serialVersionUID = 1L;

	@Override
	public String toString()
	{
		return "Versioned OWL/XML";
	}
}
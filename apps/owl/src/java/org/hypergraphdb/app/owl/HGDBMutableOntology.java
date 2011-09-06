package org.hypergraphdb.app.owl;

import java.util.List;

import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;

public class HGDBMutableOntology extends HGDBOntology implements OWLMutableOntology
{
	public List<OWLOntologyChange> applyChange(OWLOntologyChange change) throws OWLOntologyChangeException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public List<OWLOntologyChange> applyChanges(List<OWLOntologyChange> changes) throws OWLOntologyChangeException
	{
		// TODO Auto-generated method stub
		return null;
	}
}
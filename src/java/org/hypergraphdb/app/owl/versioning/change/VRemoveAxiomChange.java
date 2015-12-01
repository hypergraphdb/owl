package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveAxiom;

/**
 * VRemoveAxiomChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveAxiomChange extends VAxiomChange
{
	public VRemoveAxiomChange(HGHandle... args)
	{
		super(args[0]);
	}

	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new RemoveAxiom(versioned.ontology(), (OWLAxiom) graph.get(getAxiomHandle()));		
	}

	@Override
	public Change<VersionedOntology> inverse()
	{
		VOWLChange ic = new VAddAxiomChange(getAxiomHandle());
		ic.setHyperGraph(graph);
		return ic;
	}
	
	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return versioned.ontology().containsAxiom(getAxiom());
	}	
}
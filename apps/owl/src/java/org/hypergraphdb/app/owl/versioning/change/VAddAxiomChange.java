package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VAddAxiomChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddAxiomChange extends VAxiomChange
{
	public VAddAxiomChange(HGHandle... args)
	{
		super(args[0]);
	}

	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new AddAxiom(versioned.ontology(), (OWLAxiom) graph.get(getAxiomHandle()));		
	}
	
	@Override
	public VChange<VersionedOntology> inverse()
	{
		VOWLChange ic = new VRemoveAxiomChange(getAxiomHandle());
		ic.setHyperGraph(graph);
		return ic;
	}
	
	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return !versioned.ontology().containsAxiom(getAxiom());
	}	
}
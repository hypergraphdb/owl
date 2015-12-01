package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.RemovePrefixChange;
import org.hypergraphdb.app.owl.versioning.VChange;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VRemovePrefixChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public class VRemovePrefixChange extends VPrefixChange
{

	public VRemovePrefixChange(HGHandle... args)
	{
		super(args);
	}
	
	@Override
	public VChange<VersionedOntology> inverse()
	{
		VOWLChange ic = new VAddPrefixChange(prefixNameToPrefixPairHandle);
		ic.setHyperGraph(graph);
		return ic;										
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return getPrefix().equals(versioned.ontology().getPrefixes().get(getPrefixName()));
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new RemovePrefixChange(versioned.ontology(), 
								   getPrefixName(), 
								   getPrefix());
	}		
}
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.AddPrefixChange;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VAddPrefixChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public class VAddPrefixChange extends VPrefixChange
{
	public VAddPrefixChange(HGHandle... args)
	{
		super(args);
	}

	@Override
	public VChange<VersionedOntology> inverse()
	{
		VOWLChange ic = new VRemovePrefixChange(prefixNameToPrefixPairHandle);
		ic.setHyperGraph(graph);
		return ic;								
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return !getPrefix().equals(versioned.ontology().getPrefixes().get(getPrefixName()));
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new AddPrefixChange(versioned.ontology(), 
								   getPrefixName(), 
								   getPrefix());
	}	
}
package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VChange;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VAddOntologyAnnotationChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddOntologyAnnotationChange extends VOntologyAnnotationChange
{
	public VAddOntologyAnnotationChange(HGHandle... args)
	{
		super(args[0]);
	}
	
	@Override
	public VChange<VersionedOntology> inverse()
	{
		VOWLChange ic = new VRemoveOntologyAnnotationChange(getOntologyAnnotationHandle());
		ic.setHyperGraph(graph);
		return ic;				
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return !versioned.ontology().getAnnotations().contains(getOntologyAnnotation());
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new AddOntologyAnnotation(versioned.ontology(), getOntologyAnnotation());
	}	
}
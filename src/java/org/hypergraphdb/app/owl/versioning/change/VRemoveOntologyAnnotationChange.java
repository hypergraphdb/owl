package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;

/**
 * VRemoveOntologyAnnotationChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VRemoveOntologyAnnotationChange extends VOntologyAnnotationChange
{

	public VRemoveOntologyAnnotationChange(HGHandle... args)
	{
		super(args[0]);
	}
	
	@Override
	public Change<VersionedOntology> inverse()
	{
		VOWLChange ic = new VAddOntologyAnnotationChange(getOntologyAnnotationHandle());
		ic.setHyperGraph(graph);
		return ic;						
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return versioned.ontology().getAnnotations().contains(getOntologyAnnotation());
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new RemoveOntologyAnnotation(versioned.ontology(), getOntologyAnnotation());
	}	
	
}
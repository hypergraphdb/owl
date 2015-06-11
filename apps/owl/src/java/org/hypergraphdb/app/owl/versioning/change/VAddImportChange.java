package org.hypergraphdb.app.owl.versioning.change;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.OWLOntologyChange;

/**
 * VAddImportChange.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 17, 2012
 */
public class VAddImportChange extends VImportChange
{
	public VAddImportChange(HGHandle... args)
	{
		super(args[0]);
	}

	@Override
	public VChange<VersionedOntology> inverse()
	{
		VOWLChange ic = new VRemoveImportChange(getImportDeclarationHandle());
		ic.setHyperGraph(graph);
		return ic;
	}

	@Override
	public boolean isEffective(VersionedOntology versioned)
	{
		return !versioned.ontology().getImportsDeclarations().contains(getImportDeclaration());
	}

	@Override
	public OWLOntologyChange toOWLChange(VersionedOntology versioned)
	{
		return new AddImport(versioned.ontology(), getImportDeclaration());
	}
}
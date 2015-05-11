package org.hypergraphdb.app.owl.newver;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;

/**
 * <p>
 * Captures the version state of an ontology.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class OntologyVersionState implements VersionState<VersionedOntology>
{
	private VersionedOntology vo;
	private Set<HGHandle> heads = new HashSet<HGHandle>();
	
	public OntologyVersionState(VersionedOntology vo)
	{
		this.vo = vo;
	}

	@Override
	public Compared compare(VersionState<VersionedOntology> otherState)
	{
		return null;
	}

	@Override
	public Set<Revision> delta(Versioned<VersionedOntology> otherOntology)
	{
		HashSet<Revision> result = new HashSet<Revision>();
		return result;
	}
}

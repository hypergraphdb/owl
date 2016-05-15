package org.hypergraphdb.app.owl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * <p>
 * Some configuration properties to drive the behavior when storing OWL ontologies
 * into a database.
 * </p>
 */
public class HGDBImportConfig
{
	HashSet<IRI> toignore = new HashSet<IRI>();
	boolean storeAllImported;
	
	/**
	 * Do not 
	 * @param iri
	 * @return
	 */
	public HGDBImportConfig ignore(IRI iri)
	{
		toignore.add(iri);
		return this;
	}
	
	public Set<IRI> ignored() 
	{
		return toignore;
	}
	
	public HGDBImportConfig storeAllImported(boolean storeAllImported)
	{
		this.storeAllImported = storeAllImported;
		return this;
	}
	
	public boolean storeAllImported()
	{
		return this.storeAllImported;
	}
}

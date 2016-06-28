package org.hypergraphdb.app.owl;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

/**
 * <p>
 * Some configuration properties to drive the behavior when importing OWL ontologies
 * into a database.
 * </p>
 * 
 * <p>
 * By default, importing is conservative: it will only import the request ontology and
 * skip its imports. However you can change that by setting the {@link #storeAllImported}
 * flag to true in this configuration. This is will result in importing the full
 * imports closure. To selectively skip the import of some ontologies from an
 * import closure, add their ontology <code>IRI</code>s to the ignore set using
 * the {@link #ignore(IRI)} method.
 * </p>
 */
public class HGDBImportConfig
{
	HashSet<IRI> toignore = new HashSet<IRI>();
	boolean storeAllImported = false;
	boolean silentMissingImports = false;
	
	/**
	 * Do not import the ontology with the given IRI.
	 * 
	 * @return <code>this</code>
	 */
	public HGDBImportConfig ignore(IRI iri)
	{
		toignore.add(iri);
		return this;
	}
	
	/**
	 * Return the set of ontologies from the imports closure to ignore.
	 */
	public Set<IRI> ignored() 
	{
		return toignore;
	}
	
	/**
	 * Specify whether to store all ontologies from the imports closure or not (default is
	 * not to).
	 * 
	 * @param storeAllImported
	 * @return
	 */
	public HGDBImportConfig storeAllImported(boolean storeAllImported)
	{
		this.storeAllImported = storeAllImported;
		return this;
	}
	
	/**
	 * Return <code>true</code> if ontologies from the import closure will be stored
	 * and <code>false</code> otherwise.
	 */
	public boolean storeAllImported()
	{
		return this.storeAllImported;
	}
	
	/**
	 * Specify whether to throw an exception when an import cannot be found or just
	 * ignore the problem (default is to throw an exception).
	 * 
	 * @return <code>this</code>
	 */
	public HGDBImportConfig silentMissingImports(boolean silentMissingImports)
	{
		this.silentMissingImports = silentMissingImports;
		return this;
	}
	
	/**
	 * Return <code>true</code> if an exception is thrown when an import cannot be found and
	 * <code>false</code> if the problem is ignored during the import process.
	 */
	public boolean silentMissingImports()
	{
		return this.silentMissingImports;
	}
}

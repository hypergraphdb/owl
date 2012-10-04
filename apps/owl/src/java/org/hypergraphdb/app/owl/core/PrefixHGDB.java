package org.hypergraphdb.app.owl.core;

/**
 * Prefix.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 3, 2012
 */
public class PrefixHGDB {
	
	private String prefixName;
	private String namespace;
	
	public PrefixHGDB ()  {
	}
	
	public PrefixHGDB (String prefixName, String namespace)  {
		setPrefixName(prefixName);
		setNamespace(namespace);
	}
	
	/**
	 * @param prefixName the prefixName to set
	 */
	public void setPrefixName(String prefixName) {
		this.prefixName = prefixName;
	}
	/**
	 * @return the prefixName
	 */
	public String getPrefixName() {
		return prefixName;
	}
	
	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}
	
	public int hashCode() {
		int hash = (prefixName != null) ? prefixName.hashCode() : 0;
		if (namespace != null)
			hash ^= namespace.hashCode();      
		return hash;
	}
	
	public boolean equals(Object other) {
		if (! (other instanceof PrefixHGDB))
			return false;
		
		PrefixHGDB p = (PrefixHGDB)other;
		if (prefixName == null)
			{ if (p.prefixName != null) return false; }
		else if (p.prefixName == null)
			return false;
		else if (!prefixName.equals(p.prefixName)) 
			return false;
		if (namespace == null) 
			return p.namespace == null;
		else if (p.namespace == null)
			return false;
		else
			return namespace.equals(p.namespace);
	}
}

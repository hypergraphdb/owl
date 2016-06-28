package org.hypergraphdb.app.owl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hypergraphdb.app.owl.core.AddPrefixChange;
import org.hypergraphdb.app.owl.core.PrefixChange;
import org.hypergraphdb.app.owl.core.PrefixChangeListener;
import org.hypergraphdb.app.owl.core.RemovePrefixChange;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

/**
 * HGDBOntologyFormat with prefix change listeners.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public class HGDBOntologyFormat extends PrefixOWLOntologyFormat
{
	private static final long serialVersionUID = 1L;

	private List<PrefixChangeListener> prefixChangeListeners = new LinkedList<PrefixChangeListener>();
	// private boolean fireChanges;

	/**
	 * The schema used for documentIRIs to identify them as stored in the HGDB
	 * repository.
	 */
	public final static String HGDB_SCHEME = "hgdb";

	private boolean isFiringChange = false;

	@Override
	public String toString()
	{
		return "Hypergraph Database Backend";
	}

	/**
	 * Converts an ontology IRI by replacing it's schema with "hgdb://"
	 * 
	 * @param ontologyIRI
	 *            must not be null, it's schema must not be null
	 * @return
	 */
	public static IRI convertToHGDBDocumentIRI(IRI ontologyIRI)
	{
		String iriNoScheme = ontologyIRI.toString();
		String scheme = ontologyIRI.getScheme();
		iriNoScheme = iriNoScheme.substring(scheme.length());
		return IRI.create(HGDB_SCHEME + iriNoScheme);
	}

	/**
	 * Converts an ontology IRI by replacing it's schema with "hgdb://"
	 * 
	 * @param ontologyIRI
	 *            must not be null, it's schema must not be null
	 * @return
	 */
	public static boolean isHGDBDocumentIRI(IRI documentIRI)
	{
		String scheme = documentIRI.getScheme();
		return HGDB_SCHEME.equals(scheme);
	}

	// /**
	// * @return the fireChanges
	// */
	// protected boolean isFireChanges() {
	// return fireChanges;
	// }
	//
	// /**
	// * @param fireChanges the fireChanges to set
	// */
	// protected void setFireChanges(boolean fireChanges) {
	// this.fireChanges = fireChanges;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat#setPrefix(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public void setPrefix(String prefixName, String prefix)
	{
		if (!prefixName.endsWith(":"))
		{
			prefixName = prefixName + ":";
		}
		Map<String, String> prefixes = getPrefixName2PrefixMap();
		if (prefixes.containsKey(prefixName))
		{
			String removedPrefix = prefixes.get(prefixName);
			fireChange(new RemovePrefixChange(this, prefixName, removedPrefix));
		}
		fireChange(new AddPrefixChange(this, prefixName, prefix));
		super.setPrefix(prefixName, prefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat#clearPrefixes()
	 */
	@Override
	public void clearPrefixes()
	{
		Map<String, String> prefixes = getPrefixName2PrefixMap();
		for (Map.Entry<String, String> prefixNameToPrefix : prefixes.entrySet())
		{
			fireChange(new RemovePrefixChange(this, prefixNameToPrefix.getKey(),
					prefixNameToPrefix.getValue()));
		}
		super.clearPrefixes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat#setDefaultPrefix(
	 * java.lang.String)
	 */
	@Override
	public void setDefaultPrefix(String namespace)
	{
		Map<String, String> prefixes = getPrefixName2PrefixMap();
		if (prefixes.containsKey(":"))
		{
			String removedPrefix = prefixes.get(":");
			fireChange(new RemovePrefixChange(this, ":", removedPrefix));
		}
		fireChange(new AddPrefixChange(this, ":", namespace));
		super.setDefaultPrefix(namespace);
	}

	/**
	 * Sets the prefixes, no events will be fired.
	 * 
	 * @param prefixMap
	 */
	public void setPrefixesFromMapQuiet(Map<String, String> prefixMap)
	{
		// Avoid firing changes by using superclass
		super.clearPrefixes();
		for (Map.Entry<String, String> entry : prefixMap.entrySet())
		{
			super.setPrefix(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Removes a prefixname quietly, if it exists. No Events fired. Internal use
	 * only.
	 * 
	 * @param prefixName
	 */
	public void removePrefixQuiet(String prefixName)
	{
		if (!prefixName.endsWith(":"))
		{
			prefixName = prefixName + ":";
		}
		if (super.containsPrefixMapping(prefixName))
		{
			Map<String, String> oldPrefixes = new HashMap<String, String>();
			oldPrefixes.putAll(getPrefixName2PrefixMap());
			oldPrefixes.remove(prefixName);
			super.clearPrefixes();
			for (Map.Entry<String, String> prefix : oldPrefixes.entrySet())
			{
				super.setPrefix(prefix.getKey(), prefix.getValue());
			}
		}
	}

	/**
	 * Adds a prefix quietly, if not contained. No Events fired. Internal use
	 * only.
	 * 
	 * @param prefixName
	 * @param prefix
	 */
	public void addPrefixQuiet(String prefixName, String prefix)
	{
		if (!prefixName.endsWith(":"))
		{
			prefixName = prefixName + ":";
		}
		String oldPrefix = super.getPrefix(prefixName);
		if (!prefix.equals(oldPrefix))
		{
			super.setPrefix(prefixName, prefix);
		}
	}

	//
	// LISTENERS
	//

	/**
	 * adds a listener if it is not already contained.
	 */
	public void addPrefixChangeListener(PrefixChangeListener l)
	{
		if (!prefixChangeListeners.contains(l))
		{
			prefixChangeListeners.add(l);
		}
		else
		{
			System.err.println(
					"tried to register already listening prefixlistener: " + l);
		}
	}

	public void removePrefixChangeListener(PrefixChangeListener l)
	{
		prefixChangeListeners.remove(l);
	}

	protected void fireChange(PrefixChange c)
	{
		setFiringChange(true);
		for (PrefixChangeListener pcl : prefixChangeListeners)
		{
			pcl.prefixChanged(c);
		}
		setFiringChange(false);
	}

	/**
	 * @return the isFiringChanges
	 */
	public boolean isFiringChange()
	{
		return isFiringChange;
	}

	/**
	 * @param isFiringChanges
	 *            the isFiringChanges to set
	 */
	protected void setFiringChange(boolean isFiringChange)
	{
		this.isFiringChange = isFiringChange;
	}
}

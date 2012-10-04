package org.hypergraphdb.app.owl.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.OWLOntologyImpl;

/**
 * OWLTempOntology an in memory ontology with prefixes.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 3, 2012
 */
public class OWLTempOntologyImpl extends OWLOntologyImpl implements OWLOntologyEx {

	/**
	 * @param manager
	 * @param ontologyID
	 */
	public OWLTempOntologyImpl(OWLOntologyManager manager, OWLOntologyID ontologyID) {
		super(manager, ontologyID);
	}

	Map<String, String> prefixMap = new HashMap<String, String>();
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.PrefixHolder#getPrefixes()
	 */
	@Override
	public Map<String, String> getPrefixes() {
		return prefixMap;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.PrefixHolder#setPrefixesFrom(java.util.Map)
	 */
	@Override
	public void setPrefixesFrom(Map<String, String> prefixMap) {
		this.prefixMap = prefixMap;
	}
	
	/**
	 * Applies prefixchanges to this and all other changes to the superclass.
	 */
    public List<OWLOntologyChange> applyChange(OWLOntologyChange change) {
        List<OWLOntologyChange> appliedChanges = Collections.emptyList();
        if (change instanceof AddPrefixChange) {
        	AddPrefixChange ac = (AddPrefixChange) change;
        	String existingNamespace = getPrefixes().get(ac.getPrefixName());
        	if (existingNamespace == null || !existingNamespace.equals(ac.getPrefix())) {
        		prefixMap.put(ac.getPrefixName(), ac.getPrefix());
        		appliedChanges = Collections.singletonList(change);
        	} 
        } else if (change instanceof RemovePrefixChange) {
        	RemovePrefixChange rc = (RemovePrefixChange) change;
        	if (getPrefixes().containsKey(rc.getPrefixName())) {
        		prefixMap.remove(rc.getPrefixName());
        		appliedChanges = Collections.singletonList(change);
        	} 
        } else {
        	appliedChanges = super.applyChange(change);
        }
        return appliedChanges;
    }

    public List<OWLOntologyChange> applyChanges(List<OWLOntologyChange> changes) {
        List<OWLOntologyChange> appliedChanges = new ArrayList<OWLOntologyChange>();
        OWLOntologyChangeFilter changeFilter = new OWLOntologyChangeFilter();
        for (OWLOntologyChange change : changes) {
            appliedChanges.addAll(applyChange(change));
        }
        return appliedChanges;
    }
}

package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.net.URI;

import org.semanticweb.owlapi.model.IRI;

/**
 * VOWLXMLVocabulary.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public enum VOWLXMLVocabulary  
{
    VERSIONED_ONTOLOGY_ROOT("VOWLXMLDocument"),
    RENDER_CONFIGURATION("RenderConfiguration"),
    VERSIONED_ONTOLOGY("VersionedOntology"),
    CHANGE_SET("ChangeSet"),
    REVISION("Revision"),
    BRANCH("Branch"),
    MARK_PARENT("ParentLink"),
    REVISION_MARK("RevisionMark"),    
    CHANGE_MARK("ChangeRecord"),
    ROOTS("Roots"),
    HEADS("Heads"),
    HGHANDLE("Handle"),
    V_ADD_AXIOM_CHANGE("VAddAxiomChange"),
    V_ADD_IMPORT_CHANGE("VAddImportChange"),
    V_ADD_ONTOLOGY_ANNOTATION_CHANGE("VAddOntologyAnnotationChange"),
    V_ADD_PREFIX_CHANGE("VAddPrefixChange"),
    V_MODIFY_ONTOLOGY_ID_CHANGE("VModifyOntologyIDChange"),
    V_MODIFY_ONTOLOGY_ID_NEW_ID("NewID"),
    V_MODIFY_ONTOLOGY_ID_OLD_ID("OldID"),
    V_REMOVE_AXIOM_CHANGE("VRemoveAxiomChange"),
    V_REMOVE_IMPORT_CHANGE("VRemoveImportChange"),
    V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE("VRemoveOntologyAnnotationChange"),
    V_REMOVE_PREFIX_CHANGE("VRemovePrefixChange"),
    V_PREFIX_MAP("VPrefixMap"),
    V_PREFIX_MAP_ENTRY("VPrefixMapEntry")
    ;
    
	public final static String NAMESPACE = "http://www.miamidade.gov/ciao/VOWLXMLVocabulary#";
	public final static String NAMESPACE_PREFIX = "vo:";

    private IRI iri;

    private String shortName;

    /**
	 * 
	 */
	private VOWLXMLVocabulary(String name) 
	{
        this.iri = IRI.create(NAMESPACE + name);
        shortName = name;
    }

    public IRI getIRI()
    {
        return iri;
    }

    public URI getURI() 
    {
        return iri.toURI();
    }


    public String getShortName() 
    {
        return shortName;
    }

    @Override
	public String toString() 
    {
        return iri.toString();
    }
}
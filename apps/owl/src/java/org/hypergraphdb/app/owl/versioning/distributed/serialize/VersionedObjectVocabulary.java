package org.hypergraphdb. app.owl.versioning.distributed.serialize;

import java.net.URI;

import org.semanticweb.owlapi.model.IRI;

/**
 * VersionedObjectVocabulary.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public enum VersionedObjectVocabulary {

    VERSIONED_ONTOLOGY("VersionedOntology"),
    CHANGE_SET("ChangeSet"),
    REVISION("Revision"),
    V_ADD_AXIOM_CHANGE("VAddAxiomChange"),
    V_ADD_IMPORT_CHANGE("VAddImportChange"),
    V_ADD_ONTOLOGY_ANNOTATION_CHANGE("VAddOntologyAnnotationChange"),
    V_MODIFY_ONTOLOGY_ID_CHANGE("VModifyOntologyIDChange"),
    V_REMOVE_AXIOM_CHANGE("VRemoveAxiomChange"),
    V_REMOVE_IMPORT_CHANGE("VRemoveImportChange"),
    V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE("VRemoveOntologyAnnotationChange")
    ;
    
	public final static String NAMESPACE = "http://www.w3.org/2002/07/owl#";

    private IRI iri;

    private String shortName;

    /**
	 * 
	 */
	private VersionedObjectVocabulary(String name) {
        this.iri = IRI.create(NAMESPACE + name);
        shortName = name;
    }

    public IRI getIRI() {
        return iri;
    }

    public URI getURI() {
        return iri.toURI();
    }


    public String getShortName() {
        return shortName;
    }

    @Override
	public String toString() {
        return iri.toString();
    }
}
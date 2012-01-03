package org.hypergraphdb.app.owl.exception;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * HGDBOntologyDocumentIRIAlreadyExistsException indicates that an ontology was already stored in the repository with the same document IRI.
 * The documentIRI refers to the physical location of the Ontology.
 * 
 * Different meaning than @see OWLOntologyDocumentAlreadyExistsException, which refers to loaded ontology document IRIs.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 3, 2012
 */
public class HGDBOntologyDocumentIRIAlreadyExistsException extends OWLOntologyCreationException {
    
	private static final long serialVersionUID = -750105131290316893L;

	private IRI ontologyDocumentIRI;

    public HGDBOntologyDocumentIRIAlreadyExistsException(IRI ontologyDocumentIRI) {
        this.ontologyDocumentIRI = ontologyDocumentIRI;
    }

    public IRI getOntologyDocumentIRI() {
        return ontologyDocumentIRI;
    }
}

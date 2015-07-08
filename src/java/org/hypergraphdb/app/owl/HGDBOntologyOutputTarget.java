package org.hypergraphdb.app.owl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.IRI;

/**
 * HGDBOntologyOutputTarget objects have an IRI but nothing else.
 * 
 * @author Thomas Hilpold
 */
public class HGDBOntologyOutputTarget implements OWLOntologyDocumentTarget {
 
	final IRI iri;
	

	public HGDBOntologyOutputTarget(final IRI iri) {
		this.iri = iri;
	}
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentTarget#isWriterAvailable()
	 */
	@Override
	public boolean isWriterAvailable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentTarget#getWriter()
	 */
	@Override
	public Writer getWriter() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentTarget#isOutputStreamAvailable()
	 */
	@Override
	public boolean isOutputStreamAvailable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentTarget#getOutputStream()
	 */
	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentTarget#isDocumentIRIAvailable()
	 */
	@Override
	public boolean isDocumentIRIAvailable() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLOntologyDocumentTarget#getDocumentIRI()
	 */
	@Override
	public IRI getDocumentIRI() {
		return iri;
	}

}

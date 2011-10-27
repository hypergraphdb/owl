package org.hypergraphdb.app.owl;

import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyStorer;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * HGDBStorer.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBStorer implements OWLOntologyStorer {

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyStorer#canStoreOntology(org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public boolean canStoreOntology(OWLOntologyFormat ontologyFormat) {
		return ontologyFormat instanceof HGDBOntologyFormat;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyStorer#storeOntology(org.semanticweb.owlapi.model.OWLOntologyManager, org.semanticweb.owlapi.model.OWLOntology, org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public void storeOntology(OWLOntologyManager manager, OWLOntology ontology,
			IRI documentIRI, OWLOntologyFormat ontologyFormat)
			throws OWLOntologyStorageException, IOException {
		System.out.println("HGDBStorer.storeOntology");
		if (!(ontologyFormat instanceof HGDBOntologyFormat)) {
			throw new OWLOntologyStorageException ("illegal format, need HGDBOntologyFormat, was " + ontologyFormat.getClass());
		}
		try
		{
			final OWLMutableOntology newOnto = (OWLMutableOntology) manager.createOntology (documentIRI);
			//Set ID
			newOnto.applyChange (new SetOntologyID(newOnto, ontology.getOntologyID ()));
			final Set<OWLAxiom> axioms = ontology.getAxioms ();
			manager.addAxioms (newOnto, axioms);
			//no need to store in HG, already done by createOntology.
		}
		catch (final OWLOntologyCreationException e)
		{
			throw new OWLOntologyStorageException (e);
		}
		catch (final OWLOntologyChangeException e)
		{
			throw new OWLOntologyStorageException (e);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyStorer#storeOntology(org.semanticweb.owlapi.model.OWLOntologyManager, org.semanticweb.owlapi.model.OWLOntology, org.semanticweb.owlapi.io.OWLOntologyDocumentTarget, org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public void storeOntology(OWLOntologyManager manager, OWLOntology ontology,
			OWLOntologyDocumentTarget target, OWLOntologyFormat ontologyFormat)
			throws OWLOntologyStorageException, IOException {
		// TODO storeOntology Necessary? maybe export? based on saveAs with a selection of formats.
		System.out.println("HGDBStorer.storeOntology");
		if (!(ontologyFormat instanceof HGDBOntologyFormat)) {
			throw new OWLOntologyStorageException ("illegal format, need HGDBOntologyFormat, was " + ontologyFormat.getClass());
		}
		storeOntology(manager, ontology, target.getDocumentIRI(), ontologyFormat);
	}
}

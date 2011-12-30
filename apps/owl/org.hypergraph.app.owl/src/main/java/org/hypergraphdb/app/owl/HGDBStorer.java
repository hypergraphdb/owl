package org.hypergraphdb.app.owl;

import java.io.IOException;
import java.util.Set;

import org.hypergraphdb.app.owl.util.StopWatch;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyStorer;

/**
 * HGDBStorer.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBStorer implements OWLOntologyStorer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.semanticweb.owlapi.model.OWLOntologyStorer#canStoreOntology(org.
	 * semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public boolean canStoreOntology(OWLOntologyFormat ontologyFormat) {
		return ontologyFormat instanceof HGDBOntologyFormat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLOntologyStorer#storeOntology(org.semanticweb
	 * .owlapi.model.OWLOntologyManager,
	 * org.semanticweb.owlapi.model.OWLOntology,
	 * org.semanticweb.owlapi.model.IRI,
	 * org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public void storeOntology(OWLOntologyManager manager, OWLOntology ontology, IRI documentIRI,
			OWLOntologyFormat ontologyFormat) throws OWLOntologyStorageException, IOException {
		//Store ontology using low level API but do not make known to OntologyManager.
		System.out.println("HGDBStorer.storeOntology ");
		HGDBOntologyManager man = (HGDBOntologyManager) manager;
		HGDBOntologyRepository repo =  man.getOntologyRepository();
		StopWatch stopWatch = new StopWatch(true);
		if (!(ontologyFormat instanceof HGDBOntologyFormat)) {
			throw new OWLOntologyStorageException("illegal format, need HGDBOntologyFormat, was "
					+ ontologyFormat.getClass());
		}	
		try {
			// documentIRI shall start with hgdb://
			//2011.12.08 Do not use the manager to create the ontology.
			// as we do not load it here and don't want it to know about the new onto yet.
			//
			// final OWLMutableOntology newOnto = (OWLMutableOntology) manager.createOntology(documentIRI);
			final HGDBOntology newOnto = repo.createOWLOntology(ontology.getOntologyID(), documentIRI); 
//			if (!(newOnto instanceof HGDBOntologyImpl)) {
//				throw new IllegalStateException("We did not get a HGDBOntologyImpl, but : " + newOnto);
//			}
			// Set ID
			//Done on creation ! newOnto.applyChange(new SetOntologyID(newOnto, ontology.getOntologyID()));
			final Set<OWLAxiom> axioms = ontology.getAxioms();
			for (OWLAxiom axiom : axioms) {
				newOnto.applyChange(new AddAxiom(newOnto, axiom));
			}
			//manager.addAxioms(newOnto, axioms);
			// Add Ontology Annotations
			for (OWLAnnotation a : ontology.getAnnotations()) {
				newOnto.applyChange(new AddOntologyAnnotation(newOnto, a));
			}
			// Add Import Declarations
			for (OWLImportsDeclaration i : ontology.getImportsDeclarations()) {
				newOnto.applyChange(new AddImport(newOnto, i));
			}
			// no need to store in HG, already done by createOntology.
			// TODO after storage, we need to use it.
		} catch (final OWLOntologyChangeException e) {
			throw new OWLOntologyStorageException(e);
		}
		stopWatch.stop("Done: HGDBStorer.storeOntology ");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.semanticweb.owlapi.model.OWLOntologyStorer#storeOntology(org.semanticweb
	 * .owlapi.model.OWLOntologyManager,
	 * org.semanticweb.owlapi.model.OWLOntology,
	 * org.semanticweb.owlapi.io.OWLOntologyDocumentTarget,
	 * org.semanticweb.owlapi.model.OWLOntologyFormat)
	 */
	@Override
	public void storeOntology(OWLOntologyManager manager, OWLOntology ontology, OWLOntologyDocumentTarget target,
			OWLOntologyFormat ontologyFormat) throws OWLOntologyStorageException, IOException {
		// TODO storeOntology Necessary? maybe export? based on saveAs with a
		// selection of formats.
		System.out.println("HGDBStorer.storeOntology");
		if (!(ontologyFormat instanceof HGDBOntologyFormat)) {
			throw new OWLOntologyStorageException("illegal format, need HGDBOntologyFormat, was "
					+ ontologyFormat.getClass());
		}
		storeOntology(manager, ontology, target.getDocumentIRI(), ontologyFormat);
	}
}

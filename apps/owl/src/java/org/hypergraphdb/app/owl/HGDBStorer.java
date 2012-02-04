package org.hypergraphdb.app.owl;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
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

	private volatile int taskTotalAxioms;
	private volatile int taskCurrentAxioms;
	
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
		HGDBOntology newOnto = null;
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
			newOnto = repo.createOWLOntology(ontology.getOntologyID(), documentIRI); 
//			if (!(newOnto instanceof HGDBOntologyImpl)) {
//				throw new IllegalStateException("We did not get a HGDBOntologyImpl, but : " + newOnto);
//			}
			// Set ID
			//Done on creation ! newOnto.applyChange(new VModifyOntologyIDChange(newOnto, ontology.getOntologyID()))			
			final Set<OWLAxiom> axioms = ontology.getAxioms();
			taskTotalAxioms = axioms.size();
			taskCurrentAxioms = 0;
			for (OWLAxiom axiom : axioms) {
				taskCurrentAxioms++;
				if (taskCurrentAxioms % 2000 == 0) {
					System.out.println("Saved axioms: " + taskCurrentAxioms + " of " + taskTotalAxioms + " at " + new Date());
					repo.printStatistics();
					System.out.println("By Signature test onto member: " + HGDBOntologyInternalsImpl.PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS);
					System.out.println("By Signature test slow equals: " + HGDBOntologyInternalsImpl.PERFCOUNTER_FIND_BY_SIGNATURE_EQUALS);
					
				}
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
		} catch (OWLOntologyChangeException e) {
			System.out.println("Storage Exception during ontology axiom adding. Removing newly created ontology: " + newOnto.getOntologyID());
			repo.deleteOntology(newOnto.getOntologyID());
			throw new OWLOntologyStorageException(e);
		} catch (HGDBOntologyAlreadyExistsByDocumentIRIException e) {
			throw new OWLOntologyStorageException(e);
		} catch (HGDBOntologyAlreadyExistsByOntologyIDException e) {
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

	//
	// 
	//
	
	/**
	 * @return the taskTotalAxioms (volatile)
	 */
	protected int getTaskTotalAxioms() {
		return taskTotalAxioms;
	}

	/**
	 * @return the taskCurrentAxioms (volatile)
	 */
	protected int getTaskCurrentAxioms() {
		return taskCurrentAxioms;
	}

}

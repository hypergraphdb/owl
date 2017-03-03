package org.hypergraphdb.app.owl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.app.owl.core.HGDBTask;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
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
 * HGDBStorer used to import ontologies into the repository by creating a new DB
 * backed ontology and copying all content.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBStorer implements OWLOntologyStorer, HGDBTask
{
	private static final long serialVersionUID = -1964255557180959519L;
	
	public static boolean DBG = false;
	private volatile int taskSize;
	private volatile int taskProgess;

	@Override
	public boolean canStoreOntology(OWLOntologyFormat ontologyFormat)
	{
		return ontologyFormat instanceof HGDBOntologyFormat;
	}

	@Override
	public void storeOntology(OWLOntology ontology, OWLOntologyDocumentTarget target, OWLOntologyFormat format)
			throws OWLOntologyStorageException, IOException
	{
		this.storeOntology(ontology.getOWLOntologyManager(), ontology, target, format);
	}

	@Override
	public void storeOntology(OWLOntology ontology, IRI documentIRI, OWLOntologyFormat format) throws OWLOntologyStorageException,
			IOException
	{
		this.storeOntology(ontology.getOWLOntologyManager(), ontology, documentIRI, format);
	}

	@Override
	public void storeOntology(OWLOntologyManager manager, 
							  OWLOntology ontology, 
							  IRI documentIRI, 
							  OWLOntologyFormat ontologyFormat)
		throws OWLOntologyStorageException, IOException
	{
		// Store ontology using low level API but do not make known to
		// OntologyManager.
		if (DBG)
			System.out.println("HGDBStorer.storeOntology ");
		HGDBOntologyManager man = (HGDBOntologyManager) manager;
		OntologyDatabase repo = man.getOntologyRepository();
		HGDBOntologyFormat format = (HGDBOntologyFormat) ontologyFormat;
		StopWatch stopWatch = new StopWatch(true);
		HGDBOntology newOnto = null;
		if (!(ontologyFormat instanceof HGDBOntologyFormat))
		{
			throw new OWLOntologyStorageException("illegal format, need HGDBOntologyFormat, was " + ontologyFormat.getClass());
		}
		try
		{
			if (!HGDBOntologyFormat.isHGDBDocumentIRI(documentIRI))
			{
				if (DBG)
					System.out.println("HGDBStorer: storing onto and ignoring passed documentIRI: " + documentIRI
							+ " using default instead.");
				IRI defaultDocumentIRI = ontology.getOntologyID().getDefaultDocumentIRI();
				// TODO deal with anonymous, causes NPE now.
				documentIRI = HGDBOntologyFormat.convertToHGDBDocumentIRI(defaultDocumentIRI);
			}
			// documentIRI shall start with hgdb://
			// 2011.12.08 Do not use the manager to create the ontology.
			// as we do not load it here and don't want it to know about the new
			// onto yet.
			//
			// final OWLMutableOntology newOnto = (OWLMutableOntology)
			// manager.createOntology(documentIRI);
			newOnto = repo.createOWLOntology(ontology.getOntologyID(), 
										     documentIRI, 
										     format.atomHandle());
			copyAxioms(man, ontology, newOnto);
		}
		catch (OWLOntologyChangeException e)
		{
			System.out.println("Storage Exception during ontology axiom adding. Removing newly created ontology: "
					+ newOnto.getOntologyID());
			repo.deleteOntology(newOnto.getOntologyID());
			throw new OWLOntologyStorageException(e);
		}
		catch (HGDBOntologyAlreadyExistsByDocumentIRIException e)
		{
			throw new OWLOntologyStorageException(e);
		}
		catch (HGDBOntologyAlreadyExistsByOntologyIDException e)
		{
			throw new OWLOntologyStorageException(e);
		}
		catch (HGDBOntologyAlreadyExistsByOntologyUUIDException e)
		{
			throw new OWLOntologyStorageException(e);
		}
		stopWatch.stop("Done: HGDBStorer.storeOntology ");
	}

	public void copyAxioms(HGDBOntologyManager man, OWLOntology from, HGDBOntology to)
	{
		final Set<OWLAxiom> axioms = from.getAxioms();
		taskSize = axioms.size();
		taskProgess = 0;
		for (OWLAxiom axiom : axioms)
		{
			taskProgess++;
			if (taskProgess % 5000 == 0)
			{
				printProgress(man.getOntologyRepository());
			}
			to.applyChange(new AddAxiom(to, axiom));
		}
		// manager.addAxioms(newOnto, axioms);
		// Add Ontology Annotations
		for (OWLAnnotation a : from.getAnnotations())
		{
			to.applyChange(new AddOntologyAnnotation(to, a));
		}
		// Add Import Declarations
		for (OWLImportsDeclaration i : from.getImportsDeclarations())
		{
			man.applyChange(new AddImport(to, i));
		}
		// Save prefixes in HGDBOntology
		storePrefixes(new HGDBOntologyFormat(), to);
		// no need to store in HG, already done by createOntology.
		printProgress(man.getOntologyRepository());		
	}
	
	private void storePrefixes(HGDBOntologyFormat format, HGDBOntology onto)
	{
		Map<String, String> prefixMap = new HashMap<String, String>();
		prefixMap.putAll(format.getPrefixName2PrefixMap());
		onto.setPrefixes(prefixMap);
	}

	@Override
	public void storeOntology(OWLOntologyManager manager, 
							  OWLOntology ontology, 
							  OWLOntologyDocumentTarget target,
							  OWLOntologyFormat ontologyFormat) throws OWLOntologyStorageException, IOException
	{
		// TODO storeOntology Necessary? maybe export? based on saveAs with a
		// selection of formats.
		//System.out.println("HGDBStorer.storeOntology");
		if (!(ontologyFormat instanceof HGDBOntologyFormat))
		{
			throw new OWLOntologyStorageException("illegal format, need HGDBOntologyFormat, was " + ontologyFormat.getClass());
		}
		storeOntology(manager, ontology, target.getDocumentIRI(), ontologyFormat);
	}

	private void printProgress(OntologyDatabase repo)
	{
		//System.out.println("Saved axioms: " + taskProgess + " of " + taskSize + " at " + new Date());
		repo.printStatistics();
		System.out.println("By Signature test onto member: "
				+ HGDBOntologyInternalsImpl.PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS);
		System.out.println("By Signature test slow equals: " + HGDBOntologyInternalsImpl.PERFCOUNTER_FIND_BY_SIGNATURE_EQUALS);
		System.out.println("By HashCode test equals: " + HGDBOntologyInternalsImpl.PERFCOUNTER_FIND_BY_HASHCODE_EQUALS);
	}

	@Override
	public int getTaskSize()
	{
		return taskSize;
	}

	@Override
	public int getTaskProgess()
	{
		return taskProgess;
	}

	@Override
	public void cancelTask()
	{
	}
}
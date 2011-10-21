package org.hypergraphdb.app.owl;

import java.util.List;
import java.util.logging.Logger;

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.coode.owlapi.latex.LatexOntologyStorer;
import org.coode.owlapi.obo.renderer.OBOFlatFileOntologyStorer;
import org.coode.owlapi.owlxml.renderer.OWLXMLOntologyStorer;
import org.coode.owlapi.rdf.rdfxml.RDFXMLOntologyStorer;
import org.coode.owlapi.turtle.TurtleOntologyStorer;
import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGManagement;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.test.TestData;
import org.hypergraphdb.app.owl.type.TypeUtils;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.util.HGUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.util.NonMappingOntologyIRIMapper;

import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOntologyStorer;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxOntologyStorer;

/**
 * HGDBOntologyRepository.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyRepository {	
	
	public static boolean DBG = true; //trigger log string creation.
	
	private Logger log = Logger.getLogger(HGDBOntologyRepository.class.getName());

	public static final boolean DROP_HYPERGRAPH_ON_START = true;

	/**
	 * Set this to >0 to create Test Ontologies Data on startup.
	 */
	public static final int ENSURE_TEST_ONTOLOGY_COUNT = 3; 
	
	/**
	 * Preliminary fixed location of a hypergraph instance. 
	 */
	public static final String HYPERGRAPH_DB_LOCATION = "c:/temp/protegedb";
	

	private HyperGraph graph; 
		
    /**
	 * @param graph
	 */
	public HGDBOntologyRepository() {
		initialize();
		if (graph.isOpen()) {
			printAllOntologies();
		} else {
			//TODO force open?
		}
			
	}
	
	public void initialize() {
		if (DROP_HYPERGRAPH_ON_START) {
			dropHypergraph();
		}
		ensureHypergraph();
		//we have a graph here.
		HGManagement.ensureInstalled(graph, HGDBApplication.getInstance());	
		if (ENSURE_TEST_ONTOLOGY_COUNT > 0) {
			TestData.ensureTestData(this, ENSURE_TEST_ONTOLOGY_COUNT);			
		}		
	}

	/** 
	 * Ensures a HypergraphDB at the HYPERGRAPH_DB_LOCATION.
	 */
	public void ensureHypergraph() {
		graph = HGEnvironment.get(HYPERGRAPH_DB_LOCATION);
		long nrOfAtoms = hg.count(graph, hg.all());
		log.info("Hypergraph contains " + nrOfAtoms + " Atoms");
	}

	public void dropHypergraph() {
		HGUtils.dropHyperGraphInstance(HYPERGRAPH_DB_LOCATION);	
	}
	

	
	/**
	 * Creates an Ontology and adds it to the graph, if an Ontology with the same ontologyID does not yet exist.
	 * The graph will create an Internals object.
	 * @param ontologyID not null
	 * @param documentIRI not null
	 * @return created ontology or null, if exists.
	 */
	public HGDBOntology createOWLOntology(OWLOntologyID ontologyID,
			IRI documentIRI) {
		if (ontologyID == null || documentIRI == null) throw new IllegalArgumentException();
		HGDBOntology o;
		if (!existsOntology(ontologyID)) {
			o = new HGDBOntologyImpl(ontologyID, documentIRI, graph);
			addOntology(o);			
		} else {
			o = null;
		}
		return o;
	}
	
	public List<HGDBOntology> getOntologies() {
		return hg.getAll(graph, hg.type(HGDBOntologyImpl.class));
	}
	
	/**
	 * Gets one Ontology by OWLOntologyID.
	 * @param ontologyIRI
	 * @return ontology or null if not found.
	 * @throws IllegalStateException, if more than one Ontology found.
	 */
	public HGDBOntology getOntologyByID(OWLOntologyID ontologyId) {
		HGQueryCondition queryCondition = hg.and(hg.type(HGDBOntologyImpl.class), hg.eq("ontologyID", ontologyId));
		List<HGDBOntologyImpl> l = hg.getAll(graph, queryCondition);
		if (l.size() > 1) throw new IllegalStateException("Found more than one ontology by Id");
		return (l.size() == 1? l.get(0): null);
	}

	/**
	 * Gets one Ontology by Document IRI.
	 * @param ontologyIRI
	 * @return ontology or null if not found.
	 * @throws IllegalStateException, if more than one Ontology found.
	 */
	public HGDBOntology getOntologyByDocumentIRI(IRI documentIRI) {
		HGQueryCondition queryCondition = hg.and(hg.type(HGDBOntologyImpl.class), hg.eq("documentIRI", documentIRI));
		List<HGDBOntologyImpl> l = hg.getAll(graph, queryCondition);
		if (l.size() > 1) throw new IllegalStateException("Found more than one ontology by Id");
		return (l.size() == 1? l.get(0): null);
	}
	
	public boolean existsOntologyByDocumentIRI(IRI documentIRI) {		
		return getOntologyByDocumentIRI(documentIRI) != null;
	}

	/**
	 * Gets one Ontology HAndle by OWLOntology IRI.
	 * @param ontologyIRI
	 * @return ontology or null if not found.
	 * @throws IllegalStateException, if more than one Ontology found.
	 */
	public HGHandle getOntologyHandleByID(OWLOntologyID ontologyId) {
		HGQueryCondition queryCondition = hg.and(hg.type(HGDBOntologyImpl.class), hg.eq("ontologyID", ontologyId));
		List<HGHandle> l = hg.findAll(graph, queryCondition);
		if (l.size() > 1) throw new IllegalStateException("Found more than one ontology by Id");
		return (l.size() == 1? l.get(0): null);
	}
	
	public boolean existsOntology(OWLOntologyID ontologyId) {
		return getOntologyByID(ontologyId) != null;
	}
	
	public boolean deleteOntology(OWLOntologyID ontologyId) {
		//printAllOntologies();
		HGHandle ontologyHandle = getOntologyHandleByID(ontologyId);
		//Currently Ontology AND NOT Incidence List
		return graph.remove(ontologyHandle, true);
	}
		
	public HGHandle addOntology(HGDBOntology ontology) {
		printAllOntologies();
		return graph.add(ontology);
	}
	
	/**
	 * Deletes all OWLEntities that are not referenced by any axioms (disconnected), 
	 * and not built-in entities.
	 * 
	 * @return
	 */
	public int cleanUpOwlEntities() {
		//TODO remove this expensive debug output
		HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(OWLEntity.class);
		TypeUtils.printAllSubtypes(graph, graph.getTypeSystem().getType(typeHandle));
		
		int successRemoveCounter = 0;
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLEntity.class),
					hg.disconnected(),
					hg.not(new OWLEntityIsBuiltIn()))
				);
		for (HGHandle h: handlesToRemove) {
			if (DBG) {
				Object o = graph.get(h);
				log.info("Removing: " + o + " : " + o.getClass().getSimpleName());
			}
			if (graph.remove(h)) {
				successRemoveCounter ++;
			}
		}
		if (successRemoveCounter != handlesToRemove.size()) throw new IllegalStateException("successRemoveCounter != handles.size()");
		return successRemoveCounter;
	}

//Boris idea:	    HGHandle th = graph.getTypeSystem().getTypeHandle(OWLEntity.class);
//  List<HGAtomType> l = hg.getAll(graph, hg.apply(hg.targetAt(graph, 1), hg.and(hg.type(HGSubsumes.class), hg.orderedLink(th, hg.anyHandle()))));
//	log.info("Removing " + l.size() + " disconnected non builtin OWLEntities from graph.");
//	return l.size();

	public void printAllOntologies() {
		List<HGDBOntology> l = getOntologies();
		for (HGDBOntology hgdbMutableOntology : l) {
			printOntology(hgdbMutableOntology);
		}			
	}
	
	
	public void printOntology(HGDBOntology hgdbMutableOntology ) {
		System.out.println("------");		
		System.out.println("DD IRI" + hgdbMutableOntology.getOntologyID().getDefaultDocumentIRI());
		System.out.println("ON IRI" + hgdbMutableOntology.getOntologyID().getOntologyIRI());
		System.out.println("V  IRI" + hgdbMutableOntology.getOntologyID().getVersionIRI());		
		System.out.println("DOCIRI" + hgdbMutableOntology.getDocumentIRI());		
	}
	
	/**
	 * Disposes of the repository and closes the hypergraph database.
	 */
	public void dispose() {
		graph.close();
	}
	
	public HyperGraph getHyperGraph() {
		return graph;
	}
	
	
	//
	//TODO MOVE CREATION OF HGDBONTOLOGYMANAGER SOMEWHERE ELSE ??
	//
	public static HGDBOntologyManager createOWLOntologyManager() {
        return createOWLOntologyManager(OWLDataFactoryHGDB.getInstance());
    }
    
	/**
	 * Create the ontology manager and add ontology factories, mappers and
	 * storers.
	 * 
	 * @param dataFactory The data factory to use
	 * @return <code>OWLDBOntologyManager</code>
	 */
	public static HGDBOntologyManager createOWLOntologyManager (final OWLDataFactoryHGDB dataFactory)
	{
		final HGDBOntologyManager ontologyManager = new HGDBOntologyManager(dataFactory);
		ontologyManager.addOntologyStorer (new RDFXMLOntologyStorer());
		ontologyManager.addOntologyStorer (new OWLXMLOntologyStorer());
		ontologyManager.addOntologyStorer (new OWLFunctionalSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer (new ManchesterOWLSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer (new OBOFlatFileOntologyStorer());
		ontologyManager.addOntologyStorer (new KRSS2OWLSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer (new TurtleOntologyStorer());
		ontologyManager.addOntologyStorer (new LatexOntologyStorer());
		ontologyManager.addOntologyStorer (new HGDBStorer ());

		ontologyManager.addIRIMapper (new NonMappingOntologyIRIMapper());

		ontologyManager.addOntologyFactory (new EmptyInMemOWLOntologyFactory());
		ontologyManager.addOntologyFactory (new ParsableOWLOntologyFactory());
		ontologyManager.addOntologyFactory (new HGDBOntologyFactory ());

		return ontologyManager;
	}	
	

}

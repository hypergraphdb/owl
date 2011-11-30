package org.hypergraphdb.app.owl;

import java.util.List;
import java.util.logging.Logger;

import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxParserFactory;
import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.coode.owlapi.latex.LatexOntologyStorer;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxParserFactory;
import org.coode.owlapi.obo.parser.OBOParserFactory;
import org.coode.owlapi.obo.renderer.OBOFlatFileOntologyStorer;
import org.coode.owlapi.owlxml.renderer.OWLXMLOntologyStorer;
import org.coode.owlapi.owlxmlparser.OWLXMLParserFactory;
import org.coode.owlapi.rdf.rdfxml.RDFXMLOntologyStorer;
import org.coode.owlapi.rdfxml.parser.RDFXMLParserFactory;
import org.coode.owlapi.turtle.TurtleOntologyStorer;
import org.hypergraphdb.HGConfiguration;
import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGManagement;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.test.TestData;
import org.hypergraphdb.app.owl.type.TypeUtils;
import org.hypergraphdb.app.owl.util.Path;
import org.hypergraphdb.handle.SequentialUUIDHandleFactory;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.storage.BDBConfig;
import org.hypergraphdb.util.HGUtils;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.util.NonMappingOntologyIRIMapper;

import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOntologyStorer;
import uk.ac.manchester.cs.owl.owlapi.turtle.parser.TurtleOntologyParserFactory;
import de.uulm.ecs.ai.owlapi.krssparser.KRSS2OWLParserFactory;
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
	public static final int ENSURE_TEST_ONTOLOGY_COUNT = 0; 
	
	/**
	 * Preliminary fixed location of a hypergraph instance. 
	 */
	public static final String HYPERGRAPH_DB_LOCATION = "c:/temp/protegedb";
	
	private static HGDBOntologyRepository instance = null;

	private HyperGraph graph; 
		
	public static HGDBOntologyRepository getInstance() {
		if (instance == null) {
			instance = new HGDBOntologyRepository();
		}
		return instance;
	}
	
    /**
	 * @param graph
	 */
	private HGDBOntologyRepository() {
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
		HGConfiguration config = new HGConfiguration();
		config.setUseSystemAtomAttributes(false);
		BDBConfig bdbConfig = (BDBConfig)config.getStoreImplementation().getConfiguration();
		// Change the storage cache from the 20MB default to 500MB
		bdbConfig.getEnvironmentConfig().setCacheSize(500*1024*1024);
		SequentialUUIDHandleFactory handleFactory =
            new SequentialUUIDHandleFactory(System.currentTimeMillis(), 0);
		config.setHandleFactory(handleFactory);		
		graph = HGEnvironment.get(HYPERGRAPH_DB_LOCATION, config);
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
		return graph.remove(ontologyHandle);
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
	
	/**
	 * Gets the Number of Atoms (including Links) in the graph. 
	 * @return
	 */
	public long getNrOfAtoms() {
		return graph.count(hg.all());
	}

	/**
	 * Gets the Number of Atoms in the graph that are of a given type.  
	 * @return
	 */
	public long getNrOfAtomsByType(Class<?> clazz) {
		return graph.count(hg.type(clazz));
	}

	/**
	 * Gets the Number of Atoms in the graph that are of a given type.  
	 * @return
	 */
	public long getNrOfAtomsByTypePlus(Class<?> clazz) {
		return graph.count(hg.typePlus(clazz));
	}

	/**
	 * Gets the Number of Links in the graph. 
	 * @return
	 */
	public long getNrOfLinks() {
		return graph.count(hg.typePlus(HGLink.class));
	}

	/**
	 * Gets the Number of Atoms (excluding Links) in the graph. 
	 * @return
	 */
	public long getNrOfNonLinkAtoms() {
		return getNrOfAtoms() - getNrOfLinks();
	}
	
	private int recLevel;
	
	/**
	 * Tests, if for a given Entity, ClassExpression, ObjectPropExopression or
	 * Datarange the given axiom can be reached by traversing incidence sets and returns the path to it.
	 * 
	 * !! Does not return if cycle in graph starting atomHandle. !!
	 * 
	 * @param atomHandle non null.
	 * @param path a path that will be filled with all objects on the path including the axiom or unchanged if not found.
	 * @param axiom an axiom that is equal to the axiom to be found. May be outside of any ontology and outside the hypergraph. 
	 * @return true if the axiom is found.
	 */
	private boolean pathToAxiomRecursive(HGHandle atomHandle, Path path, OWLAxiom axiom) {
		//TODO make cycle safe.
		if (DBG) System.out.print("*" + recLevel);
		Object atom = graph.get(atomHandle);
		path.addAtom(atom);
		// Terminal condition 1: ax found
		if (atom instanceof OWLAxiom && axiom.equals(atom)) {
			if (DBG)
				System.out.println("\r\nFound axiom match: " + atom);
				return true;
		}		
		IncidenceSet iSet = graph.getIncidenceSet(atomHandle);
		// Terminal condition 2: empty incident set.
		for (HGHandle incidentAtomHandle : iSet) {
			Object o = graph.get(incidentAtomHandle);
			if (o != null) {
				// we have no cycles up incidence sets starting
				// on an entity.
				if (!(o instanceof OWLAxiom
						|| o instanceof OWLClassExpression 
						|| o instanceof OWLObjectPropertyExpression 
						|| o instanceof OWLDataRange 
						|| o instanceof OWLLiteral 
						|| o instanceof OWLFacetRestriction)) {
					throw new IllegalStateException("We encountered an unexpected object in an incidenceset:" + o);
				}
				recLevel++;
				// Recursive descent
				if (pathToAxiomRecursive(incidentAtomHandle, path , axiom)) {
					recLevel--;
					return true;
				}
				recLevel--;
			} // else o == null do nothing
		} // for
		path.removeLast();
		return false;
	}
	
	/**
	 * Tries to find a simple path from an entity to an axiom traversing incidence sets.
	 * Neither has to be member of any ontology.
	 * Might not return if a cycle can be reached from owlObject.
	 * 
	 * @param owlObject an OWLObject that is in the graph. 
	 * @param axiom an axiom (May not be in the graph, compares by equals.)
	 * @return the path including owlObject and axiom, null if axiom not found.
	 */
	public Path getPathFromOWLObjectToAxiom(OWLObject owlObject, OWLAxiom axiom) {
		Path p = new Path();
		pathToAxiomRecursive(graph.getHandle(owlObject), p, axiom);
		return p;
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
	
    static {
		//2011.11.29 Parsers to load from files:		
        // Register useful parsers
        OWLParserFactoryRegistry registry = OWLParserFactoryRegistry.getInstance();
        registry.registerParserFactory(new ManchesterOWLSyntaxParserFactory());
        registry.registerParserFactory(new KRSS2OWLParserFactory());
        registry.registerParserFactory(new OBOParserFactory());
        registry.registerParserFactory(new TurtleOntologyParserFactory());
        registry.registerParserFactory(new OWLFunctionalSyntaxParserFactory());
        registry.registerParserFactory(new OWLXMLParserFactory());
        registry.registerParserFactory(new RDFXMLParserFactory());
    }


}

package org.hypergraphdb.app.owl;

import java.io.PrintWriter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLDataFactoryInternalsHGDB;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyUUIDException;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.util.Path;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.transaction.HGTransactionConfig;
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

/**
 * <p>
 * Represents a HyperGraph instance storing OWL ontologies.
 * </p>
 * 
 * <p>
 * This is a lightweight object, tied to the underlying graph database. 
 * </p>
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County), Borislav Iordanov
 */
public class OntologyDatabase
{
	private static boolean DBG = false;
	private HyperGraph graph;
	
	private HGHandle addOntology(HGDBOntology ontology)
	{
		if (DBG)
			printAllOntologies();
		return graph.add(ontology);
	}
	
	/**
	 * @param graph
	 */
	public OntologyDatabase(String hypergraphDBLocation)
	{
		this.graph = ImplUtils.owldb(hypergraphDBLocation);
	}
	
	/**
	 * Creates an Ontology and adds it to the graph, if an Ontology with the
	 * same ontologyID does not yet exist. The graph will create an Internals
	 * object.
	 * 
	 * @param ontologyID
	 *            not null
	 * @param documentIRI
	 *            not null
	 * @return created ontology
	 * @throws HGDBOntologyAlreadyExistsByDocumentIRIException
	 * @throws HGDBOntologyAlreadyExistsByOntologyIDException
	 */
	public HGDBOntology createOWLOntology(OWLOntologyID ontologyID, IRI documentIRI)
		throws HGDBOntologyAlreadyExistsByDocumentIRIException,
			   HGDBOntologyAlreadyExistsByOntologyIDException
	{
		if (ontologyID == null || documentIRI == null)
			throw new IllegalArgumentException();
		if (existsOntologyByDocumentIRI(documentIRI))
			throw new HGDBOntologyAlreadyExistsByDocumentIRIException(
					documentIRI);
		if (existsOntology(ontologyID))
			throw new HGDBOntologyAlreadyExistsByOntologyIDException(ontologyID);

		HGDBOntology o;
		o = new HGDBOntologyImpl(ontologyID, documentIRI, graph);
		addOntology(o);
		return o;
	}

	/**
	 * Creates an Ontology and adds it to the graph, if an Ontology with the
	 * same ontologyID does not yet exist. The graph will create an Internals
	 * object.
	 * 
	 * @param ontologyID
	 *            not null
	 * @param documentIRI
	 *            not null
	 * @return created ontology
	 * @throws HGDBOntologyAlreadyExistsByDocumentIRIException
	 * @throws HGDBOntologyAlreadyExistsByOntologyIDException
	 * @throws HGDBOntologyAlreadyExistsByOntologyUUIDException
	 */
	public HGDBOntology createOWLOntology(OWLOntologyID ontologyID, IRI documentIRI, HGPersistentHandle ontologyUUID)
			throws HGDBOntologyAlreadyExistsByDocumentIRIException,
				   HGDBOntologyAlreadyExistsByOntologyIDException,
				   HGDBOntologyAlreadyExistsByOntologyUUIDException
	{
		if (ontologyID == null || documentIRI == null)
			throw new IllegalArgumentException();
		if (existsOntologyByDocumentIRI(documentIRI))
			throw new HGDBOntologyAlreadyExistsByDocumentIRIException(
					documentIRI);
		if (existsOntology(ontologyID))
			throw new HGDBOntologyAlreadyExistsByOntologyIDException(ontologyID);
		if (graph.get(ontologyUUID) != null)
			throw new HGDBOntologyAlreadyExistsByOntologyUUIDException(
					ontologyUUID);
		HGDBOntology o;
		o = new HGDBOntologyImpl(ontologyID, documentIRI, graph);
		graph.define(ontologyUUID, o);
		return o;
	}
	
	public List<HGDBOntology> getOntologies()
	{
		// 2011.12.01 HGException: Transaction configured as read-only was used
		// to modify data!
		// Therefore wrapped in normal transaction.
		return graph.getTransactionManager().ensureTransaction(
				new Callable<List<HGDBOntology>>()
				{
					public List<HGDBOntology> call()
					{
						// 2011.12.20 added condition OntologyId not null.
						// 2012.04.03 return hg.getAll(graph,
						// hg.and(hg.type(HGDBOntologyImpl.class),
						// hg.not(hg.eq("ontologyID", null))));
						return hg.getAll(
								graph,
								hg.and(hg.type(HGDBOntologyImpl.class),
										hg.not(hg.eq("documentIRI", null))));
					}
				}, HGTransactionConfig.DEFAULT);
		// USE: HGTransactionConfig.READONLY); and ensure >0 ontos in graph to
		// see HGException
		// Transaction configured as read-only was used to modify data!
	}

	public List<HGDBOntology> getDeletedOntologies()
	{
		// for cleanup
		return graph.getTransactionManager().ensureTransaction(
				new Callable<List<HGDBOntology>>()
				{
					public List<HGDBOntology> call()
					{
						// 2012.04.03 Allow equal OIDs if docIRIS are null to
						// emulate in memory behaviour before GC.
						// return hg.getAll(graph,
						// hg.and(hg.type(HGDBOntologyImpl.class),
						// hg.eq("ontologyID", null)));
						return hg.getAll(
								graph,
								hg.and(hg.type(HGDBOntologyImpl.class),
										hg.eq("documentIRI", null)));
					}
				}, HGTransactionConfig.DEFAULT);
	}

	/**
	 * Gets one Ontology by OWLOntologyID.
	 * 
	 * @param ontologyIRI
	 * @return ontology or null if not found.
	 * @throws IllegalStateException
	 *             , if more than one Ontology found.
	 */
	public HGDBOntology getOntologyByID(OWLOntologyID ontologyId)
	{
		// 2012.04.03 HGQueryCondition queryCondition =
		// hg.and(hg.type(HGDBOntologyImpl.class), hg.eq("ontologyID",
		// ontologyId));
		HGQueryCondition queryCondition = hg.and(
				hg.type(HGDBOntologyImpl.class),
				hg.eq("ontologyID", ontologyId),
				hg.not(hg.eq("documentIRI", null)));
		List<HGDBOntologyImpl> l = hg.getAll(graph, queryCondition);
		if (l.size() > 1)
			throw new IllegalStateException(
					"Found more than one ontology by Id");
		return (l.size() == 1 ? l.get(0) : null);
	}

	/**
	 * Gets one Ontology by Document IRI.
	 * 
	 * @param ontologyIRI
	 * @return ontology or null if not found.
	 * @throws IllegalStateException
	 *             , if more than one Ontology found.
	 */
	public HGDBOntology getOntologyByDocumentIRI(IRI documentIRI)
	{
		// 2012.04.03 need to throw on null docIRI
		if (documentIRI == null)
			throw new IllegalArgumentException(
					"DocumentIRI must not be null. Null docIRI is a marker for deleted ontolgies. ");
		HGQueryCondition queryCondition = hg.and(
				hg.type(HGDBOntologyImpl.class),
				hg.eq("documentIRI", documentIRI));
		List<HGDBOntologyImpl> l = hg.getAll(graph, queryCondition);
		if (l.size() > 1)
			throw new IllegalStateException(
					"Found more than one ontology by Id");
		return (l.size() == 1 ? l.get(0) : null);
	}

	public boolean existsOntologyByDocumentIRI(IRI documentIRI)
	{
		return getOntologyByDocumentIRI(documentIRI) != null;
	}

	/**
	 * Gets one Ontology HAndle by OWLOntology IRI.
	 * 
	 * @param ontologyIRI
	 * @return ontology or null if not found.
	 * @throws IllegalStateException
	 *             , if more than one Ontology found.
	 */
	public HGHandle getOntologyHandleByID(OWLOntologyID ontologyId)
	{
		// 2012.04.03 docIRI is null on marked for deletion ontos
		// HGQueryCondition queryCondition =
		// hg.and(hg.type(HGDBOntologyImpl.class), hg.eq("ontologyID",
		// ontologyId));
		HGQueryCondition queryCondition = hg.and(
				hg.type(HGDBOntologyImpl.class),
				hg.eq("ontologyID", ontologyId),
				hg.not(hg.eq("documentIRI", null)));
		List<HGHandle> l = hg.findAll(graph, queryCondition);
		if (l.size() > 1)
			throw new IllegalStateException(
					"Found more than one ontology by Id");
		return (l.size() == 1 ? l.get(0) : null);
	}

	public boolean existsOntology(OWLOntologyID ontologyId)
	{
		return getOntologyByID(ontologyId) != null;
	}

	/**
	 * Deletes all ontologies, by marking them for deletion and allowing the GC
	 * to collect them later.
	 */
	public void deleteAllOntologies()
	{
		List<HGDBOntology> ontologies = getOntologies();
		for (HGDBOntology onto : ontologies)
		{
			deleteOntology(onto.getOntologyID());
		}
	}

	/**
	 * Marks an Ontology for deletion.
	 * 
	 * @param ontologyId
	 * @return
	 */
	public boolean deleteOntology(final OWLOntologyID ontologyId)
	{
		// To speed up a potentially very costly operation, we just set 
		// the ontology ID and DocumentIRI to null
		// so cleanup can remove it later and we remain responsive.
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>() {				
		public Boolean call()
		{
			boolean ontologyFound;
			HGHandle ontologyHandle = getOntologyHandleByID(ontologyId);
			ontologyFound = ontologyHandle != null;
			if (ontologyFound)
			{
				VersionManager vm = new VersionManager(graph, null);
				if (vm.isVersioned(ontologyHandle))
					vm.removeVersioning(ontologyHandle);				
				HGDBOntology o = graph.get(ontologyHandle);
				o.setDocumentIRI(null);
				graph.replace(ontologyHandle, o);
			}
			return ontologyFound;
		}});
	}

	public void printStatistics()
	{
		printStatistics(new PrintWriter(System.out));
	}

	public void printStatistics(PrintWriter w)
	{
		Date now = new Date();
		DecimalFormat f = new DecimalFormat("##########");
		w.println("*************** HYPERGRAPH STATISTICS ***************");
		w.println("* Location     : " + graph.getLocation());
		w.println("* Now is       : "
				+ DateFormat.getDateTimeInstance().format(now));
		w.println("*       LINKS  : " + f.format(getNrOfLinks()));
		w.println("* NoLink ATOMS : " + f.format(getNrOfNonLinkAtoms()));
		w.println("* TOTAL ATOMS  : " + f.format(getNrOfAtoms()));
		w.println("*                                                   ");
		w.println("*      AXIOMS  : "
				+ f.format(getNrOfAtomsByTypePlus(OWLAxiom.class)));
		w.println("*    ENTITIES  : "
				+ f.format(getNrOfAtomsByTypePlus(OWLEntity.class)));
		w.println("*****************************************************");
		w.flush();
	}

	public void printEntityCacheStats(PrintWriter w)
	{
		w.println("----------------------------");
		w.println("- BUILTIN ENTITY CACHE STATS -");
		w.println("- Cache Put : " + OWLDataFactoryInternalsHGDB.CACHE_PUT);
		w.println("- Cache Hit : " + OWLDataFactoryInternalsHGDB.CACHE_HIT);
		w.println("- Cache Miss: " + OWLDataFactoryInternalsHGDB.CACHE_MISS);
		int hitPromille = (int) (OWLDataFactoryInternalsHGDB.CACHE_HIT * 1000.0f / (OWLDataFactoryInternalsHGDB.CACHE_HIT + OWLDataFactoryInternalsHGDB.CACHE_MISS));
		w.println("- Cache Hit%: " + hitPromille / 10.0f);
		w.println("----------------------------");
		w.flush();
	}

	public void printPerformanceStatistics(PrintWriter w)
	{
		w.println(HGDBOntologyInternalsImpl.toStringPerfCounters());
	}

	public void printAllOntologies()
	{
		List<HGDBOntology> l = getOntologies();
		System.out.println("************* ONTOLOGIES IN HYPERGRAPH REPOSITORY "
				+ graph.getLocation() + "*************");
		for (HGDBOntology hgdbMutableOntology : l)
		{
			printOntology(hgdbMutableOntology);
		}
	}

	public void printOntology(HGDBOntology hgdbMutableOntology)
	{
		System.out
				.println("----------------------------------------------------------------------");
		System.out.println("DD IRI "
				+ hgdbMutableOntology.getOntologyID().getDefaultDocumentIRI());
		System.out.println("ON IRI "
				+ hgdbMutableOntology.getOntologyID().getOntologyIRI());
		System.out.println("V  IRI "
				+ hgdbMutableOntology.getOntologyID().getVersionIRI());
		System.out.println("DOCIRI " + hgdbMutableOntology.getDocumentIRI());
	}

	/**
	 * Disposes of the repository and closes the hypergraph database.
	 */
	public void dispose()
	{
		graph.close();
	}

	public HyperGraph getHyperGraph()
	{
		return graph;
	}

	/**
	 * Gets the Number of Atoms (including Links) in the graph.
	 * 
	 * @return
	 */
	public long getNrOfAtoms()
	{
		return graph.count(hg.all());
	}

	/**
	 * Gets the Number of Atoms in the graph that are of a given type.
	 * 
	 * @return
	 */
	public long getNrOfAtomsByType(Class<?> clazz)
	{
		return graph.count(hg.type(clazz));
	}

	/**
	 * Gets the Number of Atoms in the graph that are of a given type.
	 * 
	 * @return
	 */
	public long getNrOfAtomsByTypePlus(Class<?> clazz)
	{
		return graph.count(hg.typePlus(clazz));
	}

	/**
	 * Gets the Number of Links in the graph.
	 * 
	 * @return
	 */
	public long getNrOfLinks()
	{
		return graph.count(hg.typePlus(HGLink.class));
	}

	/**
	 * Gets the Number of Atoms (excluding Links) in the graph.
	 * 
	 * @return
	 */
	public long getNrOfNonLinkAtoms()
	{
		return getNrOfAtoms() - getNrOfLinks();
	}

	private int recLevel;

	/**
	 * Tests, if for a given Entity, ClassExpression, ObjectPropExopression or
	 * Datarange the given axiom can be reached by traversing incidence sets and
	 * returns the path to it.
	 * 
	 * !! Does not return if cycle in graph starting atomHandle. !!
	 * 
	 * Call within Transaction!
	 * 
	 * @param atomHandle
	 *            non null.
	 * @param path
	 *            a path that will be filled with all objects on the path
	 *            including the axiom or unchanged if not found.
	 * @param axiom
	 *            an axiom that is equal to the axiom to be found. May be
	 *            outside of any ontology and outside the hypergraph.
	 * @return true if the axiom is found.
	 */
	private boolean pathToAxiomRecursive(HGHandle atomHandle, Path path,
			OWLAxiom axiom)
	{
		// TODO make cycle safe.
		if (DBG)
			System.out.print("*" + recLevel);
		Object atom = graph.get(atomHandle);
		path.addAtom(atom);
		// Terminal condition 1: ax found
		if (atom instanceof OWLAxiom && axiom.equals(atom))
		{
			if (DBG)
				System.out.println("\r\nFound axiom match: " + atom);
			return true;
		}
		HGRandomAccessResult<HGHandle> iSetRAR = graph.getIncidenceSet(
				atomHandle).getSearchResult();
		// Terminal condition 2: empty incident set.
		while (iSetRAR.hasNext())
		{
			HGHandle incidentAtomHandle = iSetRAR.next();
			Object o = graph.get(incidentAtomHandle);
			if (o != null)
			{
				// we have no cycles up incidence sets starting
				// on an entity.
				if (!(o instanceof OWLAxiom || o instanceof OWLClassExpression
						|| o instanceof OWLObjectPropertyExpression
						|| o instanceof OWLDataRange || o instanceof OWLLiteral || o instanceof OWLFacetRestriction))
				{
					throw new IllegalStateException(
							"We encountered an unexpected object in an incidenceset:"
									+ o);
				}
				recLevel++;
				// Recursive descent
				if (pathToAxiomRecursive(incidentAtomHandle, path, axiom))
				{
					recLevel--;
					return true;
				}
				recLevel--;
			} // else o == null do nothing
		} // while
		iSetRAR.close();
		path.removeLast();
		return false;
	}

	/**
	 * Tries to find a simple path from an Owl object (usually entity) to an
	 * axiom traversing incidence sets. Neither has to be member of any
	 * ontology. Might not return if a cycle can be reached from owlObject.
	 * 
	 * @param owlObject
	 *            an OWLObject that is in the graph.
	 * @param axiom
	 *            an axiom (May not be in the graph, compares by equals.)
	 * @return the path including owlObject and axiom, null if axiom not found.
	 */
	public Path getPathFromOWLObjectToAxiom(final OWLObject owlObject,
			final OWLAxiom axiom)
	{
		return graph.getTransactionManager().ensureTransaction(
				new Callable<Path>()
				{
					public Path call()
					{
						Path p = new Path();
						pathToAxiomRecursive(graph.getHandle(owlObject), p,
								axiom);
						return p;
					}
				}, HGTransactionConfig.READONLY);
	}

}
package org.hypergraphdb.app.owl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hypergraphdb.HGConfiguration;
import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGManagement;
import org.hypergraphdb.app.owl.HGDBApplication;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.handle.SequentialUUIDHandleFactory;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.indexing.HGIndexer;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;

public class ImplUtils
{
	
	/** 
	 * Ensures a HypergraphDB at the HYPERGRAPH_DB_LOCATION.
	 */
	static HyperGraph openHypergraph(String location) {
		HGConfiguration config = new HGConfiguration();
		config.setClassLoader(HGDBOntologyRepository.class.getClassLoader());
		config.setUseSystemAtomAttributes(false);
		// Avoid counting incidence sets and cache all of them, since there's no representation that
		// risks having very large incidence sets, we're using sub-graphs for those cases.
		config.setMaxCachedIncidenceSetSize(Integer.MAX_VALUE);
		SequentialUUIDHandleFactory handleFactory =
            new SequentialUUIDHandleFactory(System.currentTimeMillis(), 0);
		config.setHandleFactory(handleFactory);	
		return HGEnvironment.get(location, config);
	}

	/**
	 * Same role as {@link HGEnvironment.get}, except it also
	 * ensure the graph is properly initialized with OWL model.
	 * This extra step is costly and it's only done the first time
	 * the graph is requested.
	 * 
	 * @param location The database location
	 * @return 
	 */
	static HashMap<String, HyperGraph> owlGraphs = new HashMap<String, HyperGraph>();
	public static HyperGraph owldb(String location) {
		synchronized (owlGraphs) {
			HyperGraph graph = owlGraphs.get(location);
			if (graph == null) {
				if (!HGEnvironment.isOpen(location))
					graph = openHypergraph(location);
				else
					graph = HGEnvironment.get(location);
				HGManagement.ensureInstalled(graph, new HGDBApplication());	
			}
			return graph;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Collection<HGIndexer> getIRIIndexers(HyperGraph graph)
	{
		HGHandle[] typeHandlesNamedObjectsWithIRIDimension = new HGHandle[] {
				graph.getTypeSystem().getTypeHandle(OWLClassHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLDatatypeHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLAnnotationPropertyHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLDataPropertyHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLObjectPropertyHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLNamedIndividualHGDB.class)
		};		
		ArrayList<HGIndexer> L = new ArrayList<HGIndexer>();
		for (HGHandle typeHandle : typeHandlesNamedObjectsWithIRIDimension)
			L.add(new ByPartIndexer(typeHandle, "IRI"));
		return L;
	}
			
	@SuppressWarnings("rawtypes")
	public static HGIndexer getAxiomByHashCodeIndexer(HyperGraph graph ) {
		HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(OWLAxiomHGDB.class);
		return new ByPartIndexer(typeHandle, "hashCode");
	}
	
	public static Set<OWLAnnotationAssertionAxiom> getAnnotationAxioms(
			OWLEntity entity, Set<OWLOntology> ontologies)
	{
		Set<OWLAnnotationAssertionAxiom> result = new HashSet<OWLAnnotationAssertionAxiom>();
		for (OWLOntology ont : ontologies)
		{
			result.addAll(ont.getAnnotationAssertionAxioms(entity.getIRI()));
		}
		return result;
	}

	public static Set<OWLAnnotation> getAnnotations(OWLEntity entity,
			Set<OWLOntology> ontologies)
	{
		Set<OWLAnnotation> result = new HashSet<OWLAnnotation>();
		for (OWLAnnotationAssertionAxiom ax : getAnnotationAxioms(entity,
				ontologies))
		{
			result.add(ax.getAnnotation());
		}
		return result;
	}

	public static Set<OWLAnnotation> getAnnotations(OWLEntity entity,
			OWLAnnotationProperty annotationProperty,
			Set<OWLOntology> ontologies)
	{
		Set<OWLAnnotation> result = new HashSet<OWLAnnotation>();
		for (OWLAnnotationAssertionAxiom ax : getAnnotationAxioms(entity,
				ontologies))
		{
			if (ax.getAnnotation().getProperty().equals(annotationProperty))
			{
				result.add(ax.getAnnotation());
			}
		}
		return result;
	}

	public static int compareSets(Set<? extends OWLObject> set1,
			Set<? extends OWLObject> set2)
	{
		SortedSet<? extends OWLObject> ss1;
		if (set1 instanceof SortedSet)
		{
			ss1 = (SortedSet<? extends OWLObject>) set1;
		} else
		{
			ss1 = new TreeSet<OWLObject>(set1);
		}
		SortedSet<? extends OWLObject> ss2;
		if (set2 instanceof SortedSet)
		{
			ss2 = (SortedSet<? extends OWLObject>) set2;
		} else
		{
			ss2 = new TreeSet<OWLObject>(set2);
		}
		int i = 0;
		Iterator<? extends OWLObject> thisIt = ss1.iterator();
		Iterator<? extends OWLObject> otherIt = ss2.iterator();
		while (i < ss1.size() && i < ss2.size())
		{
			OWLObject o1 = thisIt.next();
			OWLObject o2 = otherIt.next();
			int diff = o1.compareTo(o2);
			if (diff != 0)
			{
				return diff;
			}
			i++;
		}
		return ss1.size() - ss2.size();
	}

}
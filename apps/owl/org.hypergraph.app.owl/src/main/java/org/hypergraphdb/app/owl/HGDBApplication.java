package org.hypergraphdb.app.owl;

import java.util.logging.Logger;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.type.IRIType;
import org.hypergraphdb.app.owl.type.OWLImportsDeclarationType;
import org.hypergraphdb.app.owl.type.OWLNamedObjectType;
import org.hypergraphdb.app.owl.type.OntologyIDType;
import org.hypergraphdb.app.owl.type.TypeUtils;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.type.HGAtomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntologyID;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * HGDBAPP.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBApplication extends HGApplication
{
	public static boolean DBG = true;
	
	private Logger log = Logger.getLogger(HGDBApplication.class.getName());
	
	
	private static HGDBApplication instance;
	
	public static HGDBApplication getInstance() {
		if (instance == null) {
			//TODO make thread safe
			instance = new HGDBApplication();
		}
		return instance;			
	}
	
	private HGDBApplication() {
		super();
	}
	
	/**
	 * 
	 * @param graph
	 */
	private void registerAllAtomTypes(HyperGraph graph)
	{
		registerTypeIRI(graph);
		registerTypeOntologyID(graph);
		registerTypeOWLImportsDeclaration(graph);
		//All Entity types:
		registerTypeOWLNamedObjectTypesHGDB(graph);
		
	}
	
//	/**
//	 * @param graph
//	 */
//	@SuppressWarnings("deprecation")
//	private void registerTypeOWLFacetEnumTypeHGDB(HyperGraph graph) {
//		HGTypeSystem typeSystem = graph.getTypeSystem();
//		
//		if (typeSystem.getTypeHandleIfDefined(OWLFacet.class) == null) {
//			HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
//			HGAtomType type = new OWLFacetEnumType();
//			type.setHyperGraph(graph);
//			typeSystem.addPredefinedType(typeHandle, 
//													type, 
//													OWLFacet.class);
//			log.info("HG IRI type registered.");
//		}
//		
//	}

	@SuppressWarnings("deprecation")
	private void registerTypeIRI(HyperGraph graph) {
		HGTypeSystem typeSystem = graph.getTypeSystem();
		
		if (typeSystem.getTypeHandleIfDefined(IRI.class) == null) {
			HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
			HGAtomType type = new IRIType();
			type.setHyperGraph(graph);
			typeSystem.addPredefinedType(typeHandle, 
													type, 
													IRI.class);
			try
			{
				typeSystem.addPredefinedType(typeHandle, 
						type, 
						Class.forName("org.semanticweb.owlapi.model.IRI$IRIImpl"));
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
			log.info("HG IRI type registered.");
		}
	}
	
	@SuppressWarnings("deprecation")
	private void registerTypeOntologyID(HyperGraph graph) {
		HGTypeSystem typeSystem = graph.getTypeSystem();
		
		if (typeSystem.getTypeHandleIfDefined(OWLOntologyID.class) == null) {
			HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
			HGAtomType type = new OntologyIDType();
			type.setHyperGraph(graph);
			typeSystem.addPredefinedType(typeHandle, 
													type, 
													OWLOntologyID.class);
			log.info("HG OWLOntologyID type registered.");
		}
	}

	@SuppressWarnings("deprecation")
	private void registerTypeOWLImportsDeclaration(HyperGraph graph) {
		HGTypeSystem typeSystem = graph.getTypeSystem();
		
		if (typeSystem.getTypeHandleIfDefined(OWLImportsDeclaration.class) == null) {
			HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
			HGAtomType type = new OWLImportsDeclarationType();
			type.setHyperGraph(graph);
			typeSystem.addPredefinedType(typeHandle, 
													type, 
													OWLImportsDeclarationImpl.class);
			log.info("HG OwlImportsDeclaration/Impl type registered.");
		}
	}
	@SuppressWarnings("deprecation")
	private void registerTypeOWLNamedObjectTypesHGDB(HyperGraph graph) {
		HGTypeSystem typeSystem = graph.getTypeSystem();
		HGHandle oWLNamedObjectTypeHandle = graph.getTypeSystem().getTypeHandle(OWLNamedObject.class);
		HGHandle owlEntityTypeHandle = graph.getTypeSystem().getTypeHandle(OWLEntity.class);
		for (Class<? extends OWLNamedObject> c : OWLNamedObjectType.OWL_NAMED_OBJECT_TYPES_HGDB) {
			if (typeSystem.getTypeHandleIfDefined(c) == null) {
				//marker
				//HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
				OWLNamedObjectType type = new OWLNamedObjectType();				
				type.setType(c);
				type.setHyperGraph(graph);				
				HGHandle typeHandle = graph.add(type);
				typeSystem.setTypeForClass(typeHandle, c);
				//typeSystem.addPredefinedType(typeHandle,type, c);
				log.info("OWLNamedObjectType registered: " + c.getSimpleName());
				graph.getTypeSystem().assertSubtype(oWLNamedObjectTypeHandle, typeHandle);
				graph.getTypeSystem().assertSubtype(owlEntityTypeHandle, typeHandle);
				log.info("Supertype registered: " + c.getSimpleName());
			} else {
				log.warning("NOT registered, was defined: " + c.getSimpleName());				
			}
		}
		graph.getTypeSystem().assertSubtype(oWLNamedObjectTypeHandle, owlEntityTypeHandle);		
		//Assert OWLEntity subsumes OwlNamedObject
		//graph.getTypeSystem().assertSubtype(oWLNamedObjectType, typeHandle);
		//assert(owlEntityType.subsumes(owlEntityType, oWLNamedObjectType));
	}
		
	//
	// HGApplication interface 
	//
	
	public void install(HyperGraph graph)
	{
		registerAllAtomTypes(graph);
		registerIndices(graph);
		ensureBuiltInObjects(graph);
		//registerAllLinkTypes(graph);
		if (DBG) printAllTypes(graph);
		
	}

	/**
	 * @param graph
	 */
	private void registerIndices(HyperGraph graph) {
		HGHandle[] typeHandlesNamedObjectsWithIRIBeanProperty = new HGHandle[] {
				graph.getTypeSystem().getTypeHandle(OWLClassHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLDatatypeHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLAnnotationPropertyHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLDataPropertyHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLObjectPropertyHGDB.class),
				graph.getTypeSystem().getTypeHandle(OWLNamedIndividualHGDB.class)
		};
		for (HGHandle typeHandle : typeHandlesNamedObjectsWithIRIBeanProperty) {
			ByPartIndexer bpI = new ByPartIndexer(typeHandle, "IRI");
			graph.getIndexManager().register(bpI);
		}
	}

	/**
	 * For debug purposes. 
	 */
	private void printAllTypes(HyperGraph graph) {
		System.out.println("PRINTING SUPERTYPES for just registered classes");
		TypeUtils.printAllSupertypes(graph, graph.getTypeSystem().getAtomType(OWLClassHGDB.class));
		TypeUtils.printAllSupertypes(graph, graph.getTypeSystem().getAtomType(OWLDatatypeHGDB.class));
		TypeUtils.printAllSupertypes(graph, graph.getTypeSystem().getAtomType(OWLNamedIndividualHGDB.class));
		TypeUtils.printAllSupertypes(graph, graph.getTypeSystem().getAtomType(OWLDataPropertyHGDB.class));
		TypeUtils.printAllSupertypes(graph, graph.getTypeSystem().getAtomType(OWLObjectPropertyHGDB.class));
		System.out.println("Higher level classes");
		TypeUtils.printAllSupertypes(graph, graph.getTypeSystem().getAtomType(OWLClass.class));
		System.out.println("SUBTYPES: FOR ");
		TypeUtils.printAllSubtypes(graph, graph.getTypeSystem().getAtomType(OWLObjectHGDB.class));
		TypeUtils.printAllSubtypes(graph, graph.getTypeSystem().getAtomType(OWLEntity.class));
		TypeUtils.printAllSubtypes(graph, graph.getTypeSystem().getAtomType(OWLNamedObject.class));		
	}

	/**
	 * @param graph
	 */
	private void ensureBuiltInObjects(HyperGraph graph) {
		OWLDataFactoryHGDB df = new OWLDataFactoryHGDB();
		df.setHyperGraph(graph);
		OWLClass thing = df.getOWLThing(); //this returns no longer a constant 
		OWLClass nothing = df.getOWLNothing(); //this returns no longer a constant
		//WARNING OWLDataProperty topData = df.getOWLTopDataProperty(); //this returns a different object each call		
		//if (graph.getHandle(thing) == null) {
		//	graph.add(thing);
		//	graph.add(nothing);
		//}
		assert(graph.getHandle(nothing) != null);
		assert(graph.getHandle(thing) != null);
	}

//	/**
//	 * @param graph
//	 */
//	private void registerAllLinkTypes(HyperGraph graph) {
////		HGRelType relType = new HGRelType("internals1:1", 				
////				graph.getTypeSystem().getTypeHandle(HGDBOntologyImpl.class),
////				graph.getTypeSystem().getTypeHandle(HGDBOntologyInternalsImpl.class));
////		HGHandle internalsType = graph.add(relType);
////		//
////		relType = new HGRelType("importsDeclaration1:M", 				
////		graph.getTypeSystem().getTypeHandle(HGDBOntologyInternalsImpl.class),
////		graph.getTypeSystem().getTypeHandle(OWLImportsDeclaration.class));
////		HGHandle importsType = graph.add(relType);
//		
//	}
	
	

	public void reset(HyperGraph graph)
	{
		// TODO Auto-generated method stub
	}

	public void uninstall(HyperGraph graph)
	{
		// TODO Auto-generated method stub
	}

	public void update(HyperGraph graph)
	{
		// TODO Auto-generated method stub
	}


}
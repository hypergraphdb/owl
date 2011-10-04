package org.hypergraphdb.app.owl;

import java.util.logging.Logger;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.management.HGManagement;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.type.IRIType;
import org.hypergraphdb.app.owl.type.OWLImportsDeclarationType;
import org.hypergraphdb.app.owl.type.OWLNamedIndividualHGDBType;
import org.hypergraphdb.app.owl.type.OWLNamedObjectType;
import org.hypergraphdb.app.owl.type.OntologyIDType;
import org.hypergraphdb.atom.HGRelType;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.util.HGUtils;
import org.semanticweb.owlapi.CreateValuePartition;
import org.semanticweb.owlapi.model.IRI;
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
		for (Class<? extends OWLNamedObject> c : OWLNamedObjectType.OWL_NAMED_OBJECT_TYPES_HGDB) {
			if (typeSystem.getTypeHandleIfDefined(c) == null) {
				//marker
				HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
				HGAtomType type = new OWLNamedObjectType(c);
				type.setHyperGraph(graph);
				typeSystem.addPredefinedType(typeHandle,type, c);
				log.info("OWLNamedObjectType registered: " + c.getSimpleName());
			} else {
				log.warning("NOT registered, was defined: " + c.getSimpleName());				
			}
		}
	}
		

	//
	// HGApplication interface 
	//
	
	public void install(HyperGraph graph)
	{
		registerAllAtomTypes(graph);
		//registerAllLinkTypes(graph);
		
	}

	/**
	 * @param graph
	 */
	private void registerAllLinkTypes(HyperGraph graph) {
//		HGRelType relType = new HGRelType("internals1:1", 				
//				graph.getTypeSystem().getTypeHandle(HGDBOntologyImpl.class),
//				graph.getTypeSystem().getTypeHandle(HGDBOntologyInternalsImpl.class));
//		HGHandle internalsType = graph.add(relType);
//		//
//		relType = new HGRelType("importsDeclaration1:M", 				
//		graph.getTypeSystem().getTypeHandle(HGDBOntologyInternalsImpl.class),
//		graph.getTypeSystem().getTypeHandle(OWLImportsDeclaration.class));
//		HGHandle importsType = graph.add(relType);
		
	}
	
	

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
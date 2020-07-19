package org.hypergraphdb.app.owl;

import java.util.concurrent.Callable;

import java.util.logging.Logger;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.model.axioms.*;
import org.hypergraphdb.app.owl.type.IRIType;
import org.hypergraphdb.app.owl.type.OWLImportsDeclarationType;
import org.hypergraphdb.app.owl.type.OWLNamedObjectType;
import org.hypergraphdb.app.owl.type.OntologyIDType;
import org.hypergraphdb.app.owl.type.TypeUtils;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.indexing.ByPartIndexer;
import org.hypergraphdb.indexing.ByTargetIndexer;
import org.hypergraphdb.indexing.DirectValueIndexer;
import org.hypergraphdb.indexing.HGIndexer;
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
	private Logger log = Logger.getLogger(HGDBApplication.class.getName());

	/**
	 * 
	 * @param graph
	 */
	private void registerAllAtomTypes(HyperGraph graph)
	{
//		System.out.println("Register types for " + graph.getLocation());
		registerTypeIRI(graph);
		registerTypeOntologyID(graph);
		registerTypeOWLImportsDeclaration(graph);
		// All Entity types:
		registerTypeOWLNamedObjectTypesHGDB(graph);
		registerAxiomTypes(graph);
		registerVersioningTypes(graph);
	}

	@SuppressWarnings("deprecation")
	private void registerTypeIRI(HyperGraph graph)
	{
		HGTypeSystem typeSystem = graph.getTypeSystem();

		if (typeSystem.getTypeHandleIfDefined(IRI.class) == null)
		{
			HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
			HGAtomType type = new IRIType();
			type.setHyperGraph(graph);
			typeSystem.addPredefinedType(typeHandle, type, IRI.class);
			try
			{
				typeSystem.addPredefinedType(typeHandle, type,
						Class.forName("org.semanticweb.owlapi.model.IRI"));
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
//			log.info("HG IRI type registered.");
		}
	}

	@SuppressWarnings("deprecation")
	private void registerTypeOntologyID(HyperGraph graph)
	{
		HGTypeSystem typeSystem = graph.getTypeSystem();

		if (typeSystem.getTypeHandleIfDefined(OWLOntologyID.class) == null)
		{
			HGPersistentHandle typeHandle = graph.getHandleFactory()
					.makeHandle();
			HGAtomType type = new OntologyIDType();
			type.setHyperGraph(graph);
			typeSystem.addPredefinedType(typeHandle, type, OWLOntologyID.class);
//			log.info("HG OWLOntologyID type registered.");
		}
	}

	@SuppressWarnings("deprecation")
	private void registerTypeOWLImportsDeclaration(HyperGraph graph)
	{
		HGTypeSystem typeSystem = graph.getTypeSystem();

		if (typeSystem.getTypeHandleIfDefined(OWLImportsDeclaration.class) == null)
		{
			HGPersistentHandle typeHandle = graph.getHandleFactory()
					.makeHandle();
			HGAtomType type = new OWLImportsDeclarationType();
			type.setHyperGraph(graph);
			typeSystem.addPredefinedType(typeHandle, type,
					OWLImportsDeclarationImpl.class);
//			log.info("HG OwlImportsDeclaration/Impl type registered.");
		}
	}

	private void registerAxiomTypes(HyperGraph graph)
	{
		graph.getTypeSystem().getAtomType(OWLAnnotationAssertionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLAnnotationPropertyDomainAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLAnnotationPropertyRangeAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLAsymmetricObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLClassAssertionHGDB.class);
		graph.getTypeSystem().getAtomType(OWLClassAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDataPropertyAssertionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDataPropertyCharacteristicAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDataPropertyDomainAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDataPropertyRangeAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDatatypeDefinitionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDeclarationAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDifferentIndividualsAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDisjointClassesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDisjointDataPropertiesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDisjointObjectPropertiesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLDisjointUnionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLEquivalentClassesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLEquivalentDataPropertiesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLEquivalentObjectPropertiesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLFunctionalDataPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLFunctionalObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLHasKeyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLIndividualAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLIndividualRelationshipAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLInverseFunctionalObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLInverseObjectPropertiesAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLIrreflexiveObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLLogicalAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLNaryClassAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLNaryIndividualAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLNaryPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLNegativeDataPropertyAssertionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLNegativeObjectPropertyAssertionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLObjectPropertyAssertionAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLObjectPropertyCharacteristicAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLObjectPropertyDomainAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLObjectPropertyRangeAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLPropertyDomainAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLPropertyRangeAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLReflexiveObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSameIndividualAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSubAnnotationPropertyOfAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSubClassOfAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSubDataPropertyOfAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSubObjectPropertyOfAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSubPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSubPropertyChainAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLSymmetricObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLTransitiveObjectPropertyAxiomHGDB.class);
		graph.getTypeSystem().getAtomType(OWLUnaryPropertyAxiomHGDB.class);
	}
	
	private void registerTypeOWLNamedObjectTypesHGDB(HyperGraph graph)
	{
		HGTypeSystem typeSystem = graph.getTypeSystem();
		HGHandle oWLNamedObjectTypeHandle = graph.getTypeSystem().getTypeHandle(OWLNamedObject.class);
		HGHandle owlEntityTypeHandle = graph.getTypeSystem().getTypeHandle(OWLEntity.class);
		for (Class<? extends OWLNamedObject> c : OWLNamedObjectType.OWL_NAMED_OBJECT_TYPES_HGDB)
		{
			if (typeSystem.getTypeHandleIfDefined(c) == null)
			{
				// marker
				// HGPersistentHandle typeHandle =
				// graph.getHandleFactory().makeHandle();
				OWLNamedObjectType type = new OWLNamedObjectType();
				type.type(c);
				type.setHyperGraph(graph);
				HGHandle typeHandle = graph.add(type);
				typeSystem.setTypeForClass(typeHandle, c);
				// typeSystem.addPredefinedType(typeHandle,type, c);
//				log.info("OWLNamedObjectType registered: " + c.getSimpleName());
				graph.getTypeSystem().assertSubtype(oWLNamedObjectTypeHandle, typeHandle);
				graph.getTypeSystem().assertSubtype(owlEntityTypeHandle, typeHandle);
//				log.info("Supertype registered: " + c.getSimpleName());
			}
			else
			{
				log.warning("NOT registered, was defined: " + c.getSimpleName());
			}
		}
		graph.getTypeSystem().assertSubtype(oWLNamedObjectTypeHandle, owlEntityTypeHandle);
		// Assert OWLEntity subsumes OwlNamedObject
		// graph.getTypeSystem().assertSubtype(oWLNamedObjectType, typeHandle);
		// assert(owlEntityType.subsumes(owlEntityType, oWLNamedObjectType));
	}

	private void registerVersioningTypes(HyperGraph graph)
	{
		graph.getTypeSystem().getAtomType(ChangeSet.class);
		graph.getTypeSystem().getAtomType(ChangeLink.class);
		graph.getTypeSystem().getAtomType(Revision.class);		
		graph.getTypeSystem().getAtomType(Revision.class);
		graph.getTypeSystem().getAtomType(VersionedOntology.class);		
	}
	
	//
	// HGApplication interface
	//

	public void install(final HyperGraph graph)
	{
		graph.getTransactionManager().transact(new Callable<Object>(){
		public Object call()
		{
			registerAllAtomTypes(graph);
			registerIndices(graph);
			ensureBuiltInObjects(graph);
			return null;
		}});
	}

	/**
	 * @param graph
	 */
	@SuppressWarnings("rawtypes")
	private void registerIndices(HyperGraph graph)
	{
		//
		// BY_PART_INDEXERS "IRI"
		//
		for (HGIndexer indexer : ImplUtils.getIRIIndexers(graph))
			graph.getIndexManager().register(indexer);

		//
		// BY_TARGET_INDEXERS
		//
		ByTargetIndexer subClass0 = new ByTargetIndexer(graph.getTypeSystem()
				.getTypeHandle(OWLSubClassOfAxiomHGDB.class), 0);
		ByTargetIndexer subClass1 = new ByTargetIndexer(graph.getTypeSystem()
				.getTypeHandle(OWLSubClassOfAxiomHGDB.class), 1);
		graph.getIndexManager().register(subClass0);
		graph.getIndexManager().register(subClass1);

		// Literals:
		ByPartIndexer<String> byLiteral = new ByPartIndexer<String>(graph
				.getTypeSystem().getTypeHandle(OWLLiteralHGDB.class), "literal");
		graph.getIndexManager().register(byLiteral);

		//
		// Axiom HashCode indexer
		//
		graph.getIndexManager().register(
				ImplUtils.getAxiomByHashCodeIndexer(graph));

		// IRI indexer
		graph.getIndexManager().register(
				new DirectValueIndexer<IRI>(graph.getTypeSystem()
						.getTypeHandle(IRI.class)));
	}

	/**
	 * For debug purposes.
	 */
	@SuppressWarnings("unused")
	private void printAllTypes(HyperGraph graph)
	{
//		System.out.println("PRINTING SUPERTYPES for just registered classes");
		TypeUtils.printAllSupertypes(graph,
				graph.getTypeSystem().getAtomType(OWLClassHGDB.class));
		TypeUtils.printAllSupertypes(graph,
				graph.getTypeSystem().getAtomType(OWLDatatypeHGDB.class));
		TypeUtils
				.printAllSupertypes(
						graph,
						graph.getTypeSystem().getAtomType(
								OWLNamedIndividualHGDB.class));
		TypeUtils.printAllSupertypes(graph,
				graph.getTypeSystem().getAtomType(OWLDataPropertyHGDB.class));
		TypeUtils.printAllSupertypes(graph,
				graph.getTypeSystem().getAtomType(OWLObjectPropertyHGDB.class));
//		System.out.println("Higher level classes");
		TypeUtils.printAllSupertypes(graph,
				graph.getTypeSystem().getAtomType(OWLClass.class));
//		System.out.println("SUBTYPES: FOR ");
		TypeUtils.printAllSubtypes(graph,
				graph.getTypeSystem().getAtomType(OWLObjectHGDB.class));
		TypeUtils.printAllSubtypes(graph,
				graph.getTypeSystem().getAtomType(OWLEntity.class));
		TypeUtils.printAllSubtypes(graph,
				graph.getTypeSystem().getAtomType(OWLNamedObject.class));
	}

	/**
	 * @param graph
	 */
	private void ensureBuiltInObjects(HyperGraph graph)
	{
		OWLDataFactoryHGDB df = new OWLDataFactoryHGDB(graph);
		df.setHyperGraph(graph);
		// Ensuring that the following objects are created in the graph.
		df.getOWLThing(); // this returns no longer a constant
		df.getOWLNothing(); // this returns no longer a constant
		df.getOWLTopDataProperty();
		df.getOWLTopObjectProperty();
		df.getOWLBottomDataProperty();
		df.getOWLBottomObjectProperty();
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
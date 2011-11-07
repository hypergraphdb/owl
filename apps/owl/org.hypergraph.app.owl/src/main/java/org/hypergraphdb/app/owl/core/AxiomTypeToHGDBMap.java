package org.hypergraphdb.app.owl.core;

import static org.semanticweb.owlapi.model.AxiomType.AXIOM_TYPES;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.model.axioms.OWLAsymmetricObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDeclarationAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointClassesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointDataPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointObjectPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointUnionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentClassesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentDataPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentObjectPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLFunctionalDataPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLFunctionalObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLInverseFunctionalObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLInverseObjectPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLIrreflexiveObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLObjectPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLObjectPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLReflexiveObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubAnnotationPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubClassOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubDataPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubObjectPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubPropertyChainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSymmetricObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLTransitiveObjectPropertyAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * AxiomTypeMapToHGDB maps all 39 Axiom types as defined by AxiomType.class to HGDB concrete HGLink axiom classes, who's object are stored in the graph.
 * * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 6, 2011
 */
public class AxiomTypeToHGDBMap {
	public final static int INITIAL_MAP_SIZE = 101;

	static {
		m = new HashMap<AxiomType<? extends OWLAxiom>, Class<? extends OWLAxiomHGDB>>(INITIAL_MAP_SIZE);
		mReverse = new HashMap<Class<? extends OWLAxiomHGDB>, AxiomType<? extends OWLAxiom>>(INITIAL_MAP_SIZE);
		logicalAxiomTypesHGDB = new HashSet<Class<? extends OWLAxiomHGDB>>();
		initializeMaps();
		initializeLogicalAxiomSet();
	}
	
	private static Map<AxiomType<? extends OWLAxiom>, Class<? extends OWLAxiomHGDB>> m;
	private static Map<Class<? extends OWLAxiomHGDB>, AxiomType<? extends OWLAxiom>> mReverse;

	private static Set<Class<? extends OWLAxiomHGDB>> logicalAxiomTypesHGDB;
	
	private AxiomTypeToHGDBMap() {
	}
	

	/**
	 * Gets a AxiomTypeHGDB class by hash lookup O(1). 
	 * 
	 * @param axiomType
	 * @return a non abstract subclass of OWLAxiomHGDB that is implementing HGLink
	 */
	public static Class<? extends OWLAxiomHGDB> getAxiomClassHGDB(AxiomType<? extends OWLAxiom> axiomType) {
		return m.get(axiomType);
	}
	
	/**
	 * Gets a AxiomType by hash lookup O(1). 
	 * 
	 * @param axiomClassHGDB
	 * @return AxiomType
	 */
	public static AxiomType<? extends OWLAxiom> getAxiomType( Class<? extends OWLAxiomHGDB> axiomClassHGDB) {
		return mReverse.get(axiomClassHGDB);
	}
	
	/**
	 * Adds both classes to the internal maps. 
	 * Only axiomTypes in AxiomType.AXIOM_TYPES should be added.
	 * 
	 * @param axiomType a non-null axiomType (usually contained in )
	 * @param axiomClassHGDB a non abstract subclass of OWLAxiomHGDB that is implementing HGLink
	 */
	public static void addToMap(AxiomType<? extends OWLAxiom> axiomType, Class<? extends OWLAxiomHGDB> axiomClassHGDB) {
		if (m.containsKey(axiomType)) throw new IllegalArgumentException("axiomType already mapped " + axiomType); 
		if (mReverse.containsKey(axiomClassHGDB)) throw new IllegalArgumentException("axiomClassHGDB already mapped " + axiomClassHGDB);
		if (axiomType == null || axiomClassHGDB == null) throw new IllegalArgumentException("null not allowed");
		if (Modifier.isAbstract(axiomClassHGDB.getModifiers())) throw new IllegalArgumentException("axiomClassHGDB must not be abstract" + axiomClassHGDB);
		if (! HGLink.class.isAssignableFrom(axiomClassHGDB)) throw new IllegalArgumentException("axiomClassHGDB must implement HGLink" + axiomClassHGDB);
		m.put(axiomType, axiomClassHGDB);
		mReverse.put(axiomClassHGDB, axiomType);
	}
	
	private static void initializeMaps() {
        addToMap(AxiomType.SUBCLASS_OF, OWLSubClassOfAxiomHGDB.class);
        addToMap(AxiomType.EQUIVALENT_CLASSES, OWLEquivalentClassesAxiomHGDB.class); //2011.10.13
        addToMap(AxiomType.DISJOINT_CLASSES, OWLDisjointClassesAxiomHGDB.class); //2011.10.13
        //04 addToMap(AxiomType.CLASS_ASSERTION, OWLAxiomHGDB.class);
        //05 addToMap(AxiomType.SAME_INDIVIDUAL, OWLAxiomHGDB.class);
        //06 addToMap(AxiomType.DIFFERENT_INDIVIDUALS, OWLAxiomHGDB.class);
        //07 addToMap(AxiomType.OBJECT_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //08 addToMap(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //09 addToMap(AxiomType.DATA_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //10 addToMap(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        addToMap(AxiomType.OBJECT_PROPERTY_DOMAIN, OWLObjectPropertyDomainAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.OBJECT_PROPERTY_RANGE, OWLObjectPropertyRangeAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.DISJOINT_OBJECT_PROPERTIES, OWLDisjointObjectPropertiesAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.SUB_OBJECT_PROPERTY, OWLSubObjectPropertyOfAxiomHGDB.class);
        addToMap(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, OWLEquivalentObjectPropertiesAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.INVERSE_OBJECT_PROPERTIES, OWLInverseObjectPropertiesAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.SUB_PROPERTY_CHAIN_OF, OWLSubPropertyChainAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, OWLFunctionalObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, OWLInverseFunctionalObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.SYMMETRIC_OBJECT_PROPERTY, OWLSymmetricObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, OWLAsymmetricObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.TRANSITIVE_OBJECT_PROPERTY, OWLTransitiveObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.REFLEXIVE_OBJECT_PROPERTY, OWLReflexiveObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, OWLIrreflexiveObjectPropertyAxiomHGDB.class); //2011.11.07
        addToMap(AxiomType.DATA_PROPERTY_DOMAIN, OWLDataPropertyDomainAxiomHGDB.class);
        addToMap(AxiomType.DATA_PROPERTY_RANGE, OWLDataPropertyRangeAxiomHGDB.class);
        addToMap(AxiomType.DISJOINT_DATA_PROPERTIES, OWLDisjointDataPropertiesAxiomHGDB.class);
        addToMap(AxiomType.SUB_DATA_PROPERTY, OWLSubDataPropertyOfAxiomHGDB.class);
        addToMap(AxiomType.EQUIVALENT_DATA_PROPERTIES, OWLEquivalentDataPropertiesAxiomHGDB.class);
        addToMap(AxiomType.FUNCTIONAL_DATA_PROPERTY, OWLFunctionalDataPropertyAxiomHGDB.class);
        //31 addToMap(AxiomType.DATATYPE_DEFINITION, OWLAxiomHGDB.class);
        addToMap(AxiomType.DISJOINT_UNION, OWLDisjointUnionAxiomHGDB.class); //2011.10.13
        addToMap(AxiomType.DECLARATION, OWLDeclarationAxiomHGDB.class);
        //34 addToMap(AxiomType.SWRL_RULE, OWLAxiomHGDB.class);
        //35 addToMap(AxiomType.ANNOTATION_ASSERTION, OWLAxiomHGDB.class);
        addToMap(AxiomType.SUB_ANNOTATION_PROPERTY_OF, OWLSubAnnotationPropertyOfAxiomHGDB.class);
        //37 addToMap(AxiomType.ANNOTATION_PROPERTY_DOMAIN, OWLAxiomHGDB.class);
        //38 addToMap(AxiomType.ANNOTATION_PROPERTY_RANGE, OWLAxiomHGDB.class);
        //39 addToMap(AxiomType.HAS_KEY, OWLAxiomHGDB.class);
		System.out.println("AxiomTypeMapToHGDB Initialized: " + m.size() + " mappings defined.");
	}	
	
	/**
	 * Gets a set of concrete Axiom HGDB classes that correspond to logical Axiomtypes.
	 * 
	 * @return a unmodifiable set. 
	 */
	public static Set<Class<? extends OWLAxiomHGDB>> getLogicalAxiomTypesHGDB() {
		return logicalAxiomTypesHGDB;
	}

	/**
	 * Adds all Axiom HGDB classes to the set that correspond to locigal Axiomtypes.
	 */
	private static void initializeLogicalAxiomSet() {
		for (AxiomType<?> type : AXIOM_TYPES) {
			if (type.isLogical()) {
				System.out.println("LOGICAL AXIOM: " + type);
				if (m.containsKey(type)) {
					logicalAxiomTypesHGDB.add(m.get(type)); 
				} else {
					//not yet defined in initialize
				}
			}
		}
		//Make unmodifiable after init.
		logicalAxiomTypesHGDB = Collections.unmodifiableSet(logicalAxiomTypesHGDB);
		System.out.println("LogicalAxiomTypesHGDB Initialized, size : " + logicalAxiomTypesHGDB.size());
	}
}

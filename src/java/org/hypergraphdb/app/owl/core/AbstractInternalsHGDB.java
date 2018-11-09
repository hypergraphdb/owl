package org.hypergraphdb.app.owl.core;

import static org.semanticweb.owlapi.util.CollectionFactory.createSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGAtomCache;
import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.hypergraphdb.app.owl.HGDBOntologyInternals;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAnnotationAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAnnotationPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAnnotationPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAsymmetricObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLClassAssertionHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDeclarationAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDifferentIndividualsAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointClassesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointDataPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointObjectPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointUnionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentClassesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentDataPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentObjectPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLFunctionalDataPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLFunctionalObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLHasKeyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLInverseFunctionalObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLInverseObjectPropertiesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLIrreflexiveObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLNegativeDataPropertyAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLNegativeObjectPropertyAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLObjectPropertyAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLObjectPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLObjectPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLReflexiveObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSameIndividualAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubAnnotationPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubClassOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubDataPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubObjectPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSymmetricObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLTransitiveObjectPropertyAxiomHGDB;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

/**
 * AbstractInternalsHGDB.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 29, 2011
 */
public abstract class AbstractInternalsHGDB implements HGDBOntologyInternals, HGGraphHolder {


	protected HyperGraph graph;
	protected HGDBOntologyImpl ontology;
	protected HGHandle ontoHandle;

	protected HGQuery<HGHandle> findAxiomQuery = null;
	protected HGQuery<OWLAnnotation> ontologyAnnotationsQuery = null;
	
	// 2011.10.06 removed protected volatile Map<OWLClass, Set<OWLClassAxiom>>
	// classAxiomsByClass;
	// 2011.10.06 protected volatile Map<OWLClass, Set<OWLSubClassOfAxiom>>
	// subClassAxiomsByLHS;
	// 2011.10.06 protected volatile Map<OWLClass, Set<OWLSubClassOfAxiom>>
	// subClassAxiomsByLHS;
	// 2011.10.13 protected volatile Map<OWLClass,
	// Set<OWLEquivalentClassesAxiom>> equivalentClassesAxiomsByClass;
	// 2011.10.13 protected volatile Map<OWLClass, Set<OWLDisjointClassesAxiom>>
	// disjointClassesAxiomsByClass;
	// 2011.10.13 protected volatile Map<OWLClass, Set<OWLDisjointUnionAxiom>>
	// disjointUnionAxiomsByClass;
	// 2011.11.09 protected volatile Map<OWLClass, Set<OWLHasKeyAxiom>> hasKeyAxiomsByClass;
	// 2011.10.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLSubObjectPropertyOfAxiom>> objectSubPropertyAxiomsByLHS;
	// 2011.10.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLSubObjectPropertyOfAxiom>> objectSubPropertyAxiomsByRHS;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLEquivalentObjectPropertiesAxiom>>
	// equivalentObjectPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression, Set<OWLDisjointObjectPropertiesAxiom>> disjointObjectPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLObjectPropertyDomainAxiom>> objectPropertyDomainAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLObjectPropertyRangeAxiom>> objectPropertyRangeAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLFunctionalObjectPropertyAxiom>>
	// functionalObjectPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLInverseFunctionalObjectPropertyAxiom>>
	// inverseFunctionalPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLSymmetricObjectPropertyAxiom>> symmetricPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLAsymmetricObjectPropertyAxiom>>
	// asymmetricPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLReflexiveObjectPropertyAxiom>> reflexivePropertyAxiomsByProperty;
	//2011.11.14 protected volatile Map<OWLObjectPropertyExpression, Set<OWLIrreflexiveObjectPropertyAxiom>> irreflexivePropertyAxiomsByProperty;
	//2011.11.14 protected volatile Map<OWLObjectPropertyExpression, Set<OWLTransitiveObjectPropertyAxiom>> transitivePropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLObjectPropertyExpression,
	// Set<OWLInverseObjectPropertiesAxiom>> inversePropertyAxiomsByProperty;
	// 2011.10.07 protected volatile Map<OWLDataPropertyExpression,
	// Set<OWLSubDataPropertyOfAxiom>> dataSubPropertyAxiomsByLHS;
	// 2011.10.07 protected volatile Map<OWLDataPropertyExpression,
	// Set<OWLSubDataPropertyOfAxiom>> dataSubPropertyAxiomsByRHS;
	// 2011.11.07 protected volatile Map<OWLDataPropertyExpression,
	// Set<OWLEquivalentDataPropertiesAxiom>>
	// equivalentDataPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLDataPropertyExpression,
	// Set<OWLDisjointDataPropertiesAxiom>>
	// disjointDataPropertyAxiomsByProperty;
	// 2011.11.07 protected volatile Map<OWLDataPropertyExpression,
	// Set<OWLDataPropertyDomainAxiom>> dataPropertyDomainAxiomsByProperty;
	// protected volatile Map<OWLDataPropertyExpression,
	// Set<OWLDataPropertyRangeAxiom>> dataPropertyRangeAxiomsByProperty;
	//2011.11.07 protected volatile Map<OWLDataPropertyExpression, Set<OWLFunctionalDataPropertyAxiom>> functionalDataPropertyAxiomsByProperty;
	//2011.11.08 protected volatile Map<OWLIndividual, Set<OWLClassAssertionAxiom>> classAssertionAxiomsByIndividual;
	//2011.11.08 protected volatile Map<OWLClassExpression, Set<OWLClassAssertionAxiom>> classAssertionAxiomsByClass;
	//2011.11.08 protected volatile Map<OWLIndividual, Set<OWLObjectPropertyAssertionAxiom>> objectPropertyAssertionsByIndividual;
	//2011.11.08 protected volatile Map<OWLIndividual, Set<OWLDataPropertyAssertionAxiom>> dataPropertyAssertionsByIndividual;
	//2011.11.08 protected volatile Map<OWLIndssertionAxiom>> negativeObjectPropertyAssertionAxiomsByIndividual;
	//2011.11.08 protected volatile Map<OWLIndividual, Set<OWLNegativeDataPropertyAssertionAxiom>> negativeDataPropertyAssertionAxiomsByIndividual;
	//2011.11.08 protected volatile Map<OWLIndividual, Set<OWLDifferentIndividualsAxiom>> differentIndividualsAxiomsByIndividual;
	//2011.11.08 protected volatile Map<OWLIndividual, Set<OWLSameIndividualAxiom>> sameIndividualsAxiomsByIndividual;
	//2011.11.14 protected volatile Map<OWLAnnotationSubject, Set<OWLAnnotationAssertionAxiom>> annotationAssertionAxiomsBySubject;

	protected abstract <T extends OWLAxiom> Set<T> getAxiomsInternal(AxiomType<T> axiomType);

//	// NOTE: the parameter is reassigned inside the method, the field that is
//	// passed in is not modified in the original object
//	protected <K extends OWLObject, V extends OWLAxiom> Map<K, Set<V>> fill(Map<K, Set<V>> map, AxiomType<V> type,
//			InitVisitorFactory.InitVisitor<K> visitor) {
//		map = createMap();
//		for (V ax : getAxiomsInternal(type)) {
//			K key = ax.accept(visitor);
//			if (key != null) {
//				addToIndexedSet(key, map, ax);
//			}
//		}
//		return map;
//	}
//
//	// NOTE: the parameter is reassigned inside the method, the field that is
//	// passed in is not modified in the original object
//	protected <K extends OWLObject, V extends OWLAxiom> Map<K, Set<V>> fill(Map<K, Set<V>> map, AxiomType<V> type,
//			InitVisitorFactory.InitCollectionVisitor<K> visitor) {
//		map = createMap();
//		for (V ax : getAxiomsInternal(type)) {
//			Collection<K> keys = ax.accept(visitor);
//			for (K key : keys) {
//				addToIndexedSet(key, map, ax);
//			}
//		}
//		return map;
//	}

//	protected enum Maps {
		// SubClassAxiomsByLHS {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// // System.out
		// // .println("subclassaxiomsbylhs "+System.nanoTime());
		// // new Exception().printStackTrace(System.out);
		// if (impl.subClassAxiomsByLHS == null) {
		// impl.subClassAxiomsByLHS = impl.fill(impl.subClassAxiomsByLHS,
		// SUBCLASS_OF,
		// classsubnamed);
		// }
		// }
		// },
		// SubClassAxiomsByRHS {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.subClassAxiomsByRHS == null) {
		// impl.subClassAxiomsByRHS = impl.fill(impl.subClassAxiomsByRHS,
		// SUBCLASS_OF,
		// classsupernamed);
		// }
		// }
		// },
		// EquivalentClassesAxiomsByClass {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.equivalentClassesAxiomsByClass == null) {
		// impl.equivalentClassesAxiomsByClass = impl.fill(
		// impl.equivalentClassesAxiomsByClass, EQUIVALENT_CLASSES,
		// classcollections);
		// }
		// }
		// },
		// DisjointClassesAxiomsByClass {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.disjointClassesAxiomsByClass == null) {
		// impl.disjointClassesAxiomsByClass = impl.fill(
		// impl.disjointClassesAxiomsByClass, DISJOINT_CLASSES,
		// classcollections);
		// }
		// }
		// },
		// DisjointUnionAxiomsByClass {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.disjointUnionAxiomsByClass == null) {
		// impl.disjointUnionAxiomsByClass =
		// impl.fill(impl.disjointUnionAxiomsByClass,
		// DISJOINT_UNION, classcollections);
		// }
		// }
		// },
		//		HasKeyAxiomsByClass {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.hasKeyAxiomsByClass == null) {
		//					impl.hasKeyAxiomsByClass = impl.fill(impl.hasKeyAxiomsByClass, HAS_KEY, classsupernamed);
		//				}
		//			}
		//		},
		// ObjectSubPropertyAxiomsByLHS {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.objectSubPropertyAxiomsByLHS == null) {
		// impl.objectSubPropertyAxiomsByLHS = impl.fill(
		// impl.objectSubPropertyAxiomsByLHS, SUB_OBJECT_PROPERTY, opsubnamed);
		// }
		// }
		// },
		// ObjectSubPropertyAxiomsByRHS {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.objectSubPropertyAxiomsByRHS == null) {
		// impl.objectSubPropertyAxiomsByRHS = impl.fill(
		// impl.objectSubPropertyAxiomsByRHS, SUB_OBJECT_PROPERTY,
		// opsupernamed);
		// }
		// }
		// },
		// EquivalentObjectPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.equivalentObjectPropertyAxiomsByProperty == null) {
		// impl.equivalentObjectPropertyAxiomsByProperty = impl.fill(
		// impl.equivalentObjectPropertyAxiomsByProperty,
		// EQUIVALENT_OBJECT_PROPERTIES, opcollections);
		// }
		// }
		// },
		//		DisjointObjectPropertyAxiomsByProperty {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.disjointObjectPropertyAxiomsByProperty == null) {
		//					impl.disjointObjectPropertyAxiomsByProperty = impl.fill(
		//							impl.disjointObjectPropertyAxiomsByProperty, DISJOINT_OBJECT_PROPERTIES, opcollections);
		//				}
		//			}
		//		},
		// ObjectPropertyDomainAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.objectPropertyDomainAxiomsByProperty == null) {
		// impl.objectPropertyDomainAxiomsByProperty = impl.fill(
		// impl.objectPropertyDomainAxiomsByProperty, OBJECT_PROPERTY_DOMAIN,
		// opsubnamed);
		// }
		// }
		// },
		// ObjectPropertyRangeAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.objectPropertyRangeAxiomsByProperty == null) {
		// impl.objectPropertyRangeAxiomsByProperty = impl.fill(
		// impl.objectPropertyRangeAxiomsByProperty, OBJECT_PROPERTY_RANGE,
		// opsubnamed);
		// }
		// }
		// },
		// FunctionalObjectPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.functionalObjectPropertyAxiomsByProperty == null) {
		// impl.functionalObjectPropertyAxiomsByProperty = impl.fill(
		// impl.functionalObjectPropertyAxiomsByProperty,
		// FUNCTIONAL_OBJECT_PROPERTY, opsubnamed);
		// }
		// }
		// },
		// InverseFunctionalPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.inverseFunctionalPropertyAxiomsByProperty == null) {
		// impl.inverseFunctionalPropertyAxiomsByProperty = impl.fill(
		// impl.inverseFunctionalPropertyAxiomsByProperty,
		// INVERSE_FUNCTIONAL_OBJECT_PROPERTY, opsubnamed);
		// }
		// }
		// },
		// SymmetricPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.symmetricPropertyAxiomsByProperty == null) {
		// impl.symmetricPropertyAxiomsByProperty = impl.fill(
		// impl.symmetricPropertyAxiomsByProperty, SYMMETRIC_OBJECT_PROPERTY,
		// opsubnamed);
		// }
		// }
		// },
		// AsymmetricPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.asymmetricPropertyAxiomsByProperty == null) {
		// impl.asymmetricPropertyAxiomsByProperty = impl.fill(
		// impl.asymmetricPropertyAxiomsByProperty, ASYMMETRIC_OBJECT_PROPERTY,
		// opsubnamed);
		// }
		// }
		// },
		// ReflexivePropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.reflexivePropertyAxiomsByProperty == null) {
		// impl.reflexivePropertyAxiomsByProperty = impl.fill(
		// impl.reflexivePropertyAxiomsByProperty, REFLEXIVE_OBJECT_PROPERTY,
		// opsubnamed);
		// }
		// }
		// },
		//		IrreflexivePropertyAxiomsByProperty {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.irreflexivePropertyAxiomsByProperty == null) {
		//					impl.irreflexivePropertyAxiomsByProperty = impl.fill(impl.irreflexivePropertyAxiomsByProperty,
		//							IRREFLEXIVE_OBJECT_PROPERTY, opsubnamed);
		//				}
		//			}
		//		},
		//		TransitivePropertyAxiomsByProperty {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.transitivePropertyAxiomsByProperty == null) {
		//					impl.transitivePropertyAxiomsByProperty = impl.fill(impl.transitivePropertyAxiomsByProperty,
		//							TRANSITIVE_OBJECT_PROPERTY, opsubnamed);
		//				}
		//			}
		//		},
		// InversePropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.inversePropertyAxiomsByProperty == null) {
		// impl.inversePropertyAxiomsByProperty = impl.fill(
		// impl.inversePropertyAxiomsByProperty, INVERSE_OBJECT_PROPERTIES,
		// opcollections);
		// }
		// }
		// },
		// DataSubPropertyAxiomsByLHS {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.dataSubPropertyAxiomsByLHS == null) {
		// impl.dataSubPropertyAxiomsByLHS =
		// impl.fill(impl.dataSubPropertyAxiomsByLHS,
		// SUB_DATA_PROPERTY, dpsubnamed);
		// }
		// }
		// },
		// DataSubPropertyAxiomsByRHS {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.dataSubPropertyAxiomsByRHS == null) {
		// impl.dataSubPropertyAxiomsByRHS =
		// impl.fill(impl.dataSubPropertyAxiomsByRHS,
		// SUB_DATA_PROPERTY, dpsupernamed);
		// }
		// }
		// },
		// EquivalentDataPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.equivalentDataPropertyAxiomsByProperty == null) {
		// impl.equivalentDataPropertyAxiomsByProperty = impl.fill(
		// impl.equivalentDataPropertyAxiomsByProperty,
		// EQUIVALENT_DATA_PROPERTIES, dpcollections);
		// }
		// }
		// },
		// DisjointDataPropertyAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.disjointDataPropertyAxiomsByProperty == null) {
		// impl.disjointDataPropertyAxiomsByProperty = impl.fill(
		// impl.disjointDataPropertyAxiomsByProperty, DISJOINT_DATA_PROPERTIES,
		// dpcollections);
		// }
		// }
		// },
		// DataPropertyDomainAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.dataPropertyDomainAxiomsByProperty == null) {
		// impl.dataPropertyDomainAxiomsByProperty = impl.fill(
		// impl.dataPropertyDomainAxiomsByProperty, DATA_PROPERTY_DOMAIN,
		// dpsubnamed);
		// }
		// }
		// },
		// DataPropertyRangeAxiomsByProperty {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.dataPropertyRangeAxiomsByProperty == null) {
		// impl.dataPropertyRangeAxiomsByProperty = impl
		// .fill(impl.dataPropertyRangeAxiomsByProperty, DATA_PROPERTY_RANGE,
		// dpsubnamed);
		// }
		// }
		// },
		//		FunctionalDataPropertyAxiomsByProperty {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.functionalDataPropertyAxiomsByProperty == null) {
		//					impl.functionalDataPropertyAxiomsByProperty = impl.fill(
		//							impl.functionalDataPropertyAxiomsByProperty, FUNCTIONAL_DATA_PROPERTY, dpsubnamed);
		//				}
		//			}
		//		},
		//		ClassAssertionAxiomsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.classAssertionAxiomsByIndividual == null) {
		//					impl.classAssertionAxiomsByIndividual = impl.fill(impl.classAssertionAxiomsByIndividual,
		//							CLASS_ASSERTION, individualsubnamed);
		//				}
		//			}
		//		},
		//		ClassAssertionAxiomsByClass {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.classAssertionAxiomsByClass == null) {
		//					impl.classAssertionAxiomsByClass = impl.fill(impl.classAssertionAxiomsByClass, CLASS_ASSERTION,
		//							classexpressions);
		//				}
		//			}
		//		},
		//		ObjectPropertyAssertionsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.objectPropertyAssertionsByIndividual == null) {
		//					impl.objectPropertyAssertionsByIndividual = impl.fill(impl.objectPropertyAssertionsByIndividual,
		//							OBJECT_PROPERTY_ASSERTION, individualsubnamed);
		//				}
		//			}
		//		},
		//		DataPropertyAssertionsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.dataPropertyAssertionsByIndividual == null) {
		//					impl.dataPropertyAssertionsByIndividual = impl.fill(impl.dataPropertyAssertionsByIndividual,
		//							DATA_PROPERTY_ASSERTION, individualsubnamed);
		//				}
		//			}
		//		},
		//		NegativeObjectPropertyAssertionAxiomsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.negativeObjectPropertyAssertionAxiomsByIndividual == null) {
		//					impl.negativeObjectPropertyAssertionAxiomsByIndividual = impl.fill(
		//							impl.negativeObjectPropertyAssertionAxiomsByIndividual, NEGATIVE_OBJECT_PROPERTY_ASSERTION,
		//							individualsubnamed);
		//				}
		//			}
		//		},
		//		NegativeDataPropertyAssertionAxiomsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.negativeDataPropertyAssertionAxiomsByIndividual == null) {
		//					impl.negativeDataPropertyAssertionAxiomsByIndividual = impl.fill(
		//							impl.negativeDataPropertyAssertionAxiomsByIndividual, NEGATIVE_DATA_PROPERTY_ASSERTION,
		//							individualsubnamed);
		//				}
		//			}
		//		},
		//		DifferentIndividualsAxiomsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.differentIndividualsAxiomsByIndividual == null) {
		//					impl.differentIndividualsAxiomsByIndividual = impl.fill(
		//							impl.differentIndividualsAxiomsByIndividual, DIFFERENT_INDIVIDUALS, icollections);
		//				}
		//			}
		//		},
		//		SameIndividualsAxiomsByIndividual {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.sameIndividualsAxiomsByIndividual == null) {
		//					impl.sameIndividualsAxiomsByIndividual = impl.fill(impl.sameIndividualsAxiomsByIndividual,
		//							SAME_INDIVIDUAL, icollections);
		//				}
		//			}
		//		},
		//		AnnotationAssertionAxiomsBySubject {
		//			@Override
		//			public void initMap(AbstractInternalsHGDB impl) {
		//				if (impl.annotationAssertionAxiomsBySubject == null) {
		//					impl.annotationAssertionAxiomsBySubject = impl.fill(impl.annotationAssertionAxiomsBySubject,
		//							ANNOTATION_ASSERTION, annotsupernamed);
		//				}
		//			}
		//		},
//		ImportsDeclarations {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OntologyAnnotations {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		AxiomsByType {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		LogicalAxiom2AnnotatedAxiomMap {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		GeneralClassAxioms {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		PropertyChainSubPropertyAxioms {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlClassReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlObjectPropertyReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlDataPropertyReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlIndividualReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlAnonymousIndividualReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlDatatypeReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		OwlAnnotationPropertyReferences {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		},
//		DeclarationsByEntity {
//			@Override
//			public void initMap(AbstractInternalsHGDB impl) {
//			}
//		}; // ,
			// lazy init
			// ClassAxiomsByClass {
		// @Override
		// public void initMap(AbstractInternalsHGDB impl) {
		// if (impl.dataPropertyDomainAxiomsByProperty == null) { //2011.10.06
		// triggers init of others.
		// // if (impl.classAxiomsByClass == null) {
		// // Map<OWLClass, Set<OWLClassAxiom>> classAxiomsByClass =
		// impl.createMap(); // masks
		// // // member
		// // // declaration
		// Maps.EquivalentClassesAxiomsByClass.initMap(impl);
		// // for (Map.Entry<OWLClass, Set<OWLEquivalentClassesAxiom>> e :
		// impl.equivalentClassesAxiomsByClass
		// // .entrySet()) {
		// // for (OWLClassAxiom ax : e.getValue()) {
		// // impl.addToIndexedSet(e.getKey(), classAxiomsByClass, ax);
		// // }
		// // }
		// // Maps.SubClassAxiomsByLHS.initMap(impl);
		// // for (Map.Entry<OWLClass, Set<OWLSubClassOfAxiom>> e :
		// impl.subClassAxiomsByLHS
		// // .entrySet()) {
		// // for (OWLClassAxiom ax : e.getValue()) {
		// // impl.addToIndexedSet(e.getKey(), classAxiomsByClass, ax);
		// // }
		// // }
		// Maps.DisjointClassesAxiomsByClass.initMap(impl);
		// // for (Map.Entry<OWLClass, Set<OWLDisjointClassesAxiom>> e :
		// impl.disjointClassesAxiomsByClass
		// // .entrySet()) {
		// // for (OWLClassAxiom ax : e.getValue()) {
		// // impl.addToIndexedSet(e.getKey(), classAxiomsByClass, ax);
		// // }
		// // }
		// Maps.DisjointUnionAxiomsByClass.initMap(impl);
		// // for (Map.Entry<OWLClass, Set<OWLDisjointUnionAxiom>> e :
		// impl.disjointUnionAxiomsByClass
		// // .entrySet()) {
		// // for (OWLClassAxiom ax : e.getValue()) {
		// // impl.addToIndexedSet(e.getKey(), classAxiomsByClass, ax);
		// // }
		// // }
		// impl.classAxiomsByClass = classAxiomsByClass;
		// }
		// }
		// };
		//public abstract void initMap(AbstractInternalsHGDB impl);

//		/**
//		 * locking variant of the init code
//		 */
//		public void initMap(AbstractInternalsHGDB impl, Lock l, Object field) {
//			// if (field == null) {
//			// System.out
//			// .println("OWLOntologyImplInternalsDefaultImpl.Maps.initMap() lock "+this);
//			l.lock();
//			try {
//				initMap(impl);
//			} finally {
//				l.unlock();
//			}
//			// }
//			// else {
//			// System.out
//			// .println("OWLOntologyImplInternalsDefaultImpl.Maps.initMap() NOT LOCKED");
//			// initMap(impl);
//			// }
//		}

//		protected static final InitVisitorFactory.InitVisitor<OWLClass> classsubnamed = new InitVisitorFactory.InitVisitor<OWLClass>(
//				true, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLClassExpression> classexpressions = new InitVisitorFactory.InitVisitor<OWLClassExpression>(
//				true, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLClass> classsupernamed = new InitVisitorFactory.InitVisitor<OWLClass>(
//				false, true);
//		protected static final InitVisitorFactory.InitCollectionVisitor<OWLClass> classcollections = new InitVisitorFactory.InitCollectionVisitor<OWLClass>(
//				true);
//		protected static final InitVisitorFactory.InitCollectionVisitor<OWLObjectPropertyExpression> opcollections = new InitVisitorFactory.InitCollectionVisitor<OWLObjectPropertyExpression>(
//				true);
//		protected static final InitVisitorFactory.InitCollectionVisitor<OWLDataPropertyExpression> dpcollections = new InitVisitorFactory.InitCollectionVisitor<OWLDataPropertyExpression>(
//				true);
//		protected static final InitVisitorFactory.InitCollectionVisitor<OWLIndividual> icollections = new InitVisitorFactory.InitCollectionVisitor<OWLIndividual>(
//				true);
//		protected static final InitVisitorFactory.InitVisitor<OWLObjectPropertyExpression> opsubnamed = new InitVisitorFactory.InitVisitor<OWLObjectPropertyExpression>(
//				true, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLObjectPropertyExpression> opsupernamed = new InitVisitorFactory.InitVisitor<OWLObjectPropertyExpression>(
//				false, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLDataPropertyExpression> dpsubnamed = new InitVisitorFactory.InitVisitor<OWLDataPropertyExpression>(
//				true, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLDataPropertyExpression> dpsupernamed = new InitVisitorFactory.InitVisitor<OWLDataPropertyExpression>(
//				false, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLIndividual> individualsubnamed = new InitVisitorFactory.InitIndividualVisitor<OWLIndividual>(
//				true, true);
//		protected static final InitVisitorFactory.InitVisitor<OWLAnnotationSubject> annotsupernamed = new InitVisitorFactory.InitVisitor<OWLAnnotationSubject>(
//				true, true);
//	}

	public AbstractInternalsHGDB() {

	}

//	protected <K, V> Map<K, V> createMap() {
//		return CollectionFactory.createMap();
//	}

//	/**
//	 * A convenience method that adds an axiom to a set, but checks that the set
//	 * isn't null before the axiom is added. This is needed because many of the
//	 * indexing sets are built lazily.
//	 * 
//	 * @param axiom
//	 *            The axiom to be added.
//	 * @param axioms
//	 *            The set of axioms that the axiom should be added to. May be
//	 *            <code>null</code>.
//	 */
//	public <K extends OWLAxiom> void addAxiomToSet(K axiom, Set<K> axioms) {
//		if (axioms != null && axiom != null) {
//			axioms.add(axiom);
//		}
//	}

//	public <K extends OWLAxiom> void removeAxiomFromSet(K axiom, Set<K> axioms) {
//		if (axioms != null) {
//			axioms.remove(axiom);
//		}
//	}

//	/**
//	 * Adds an axiom to a set contained in a map, which maps some key (e.g. an
//	 * entity such as and individual, class etc.) to the set of axioms.
//	 * 
//	 * @param key
//	 *            The key that indexes the set of axioms
//	 * @param map
//	 *            The map, which maps the key to a set of axioms, to which the
//	 *            axiom will be added.
//	 * @param axiom
//	 *            The axiom to be added
//	 */
//	public <K, V extends OWLAxiom> void addToIndexedSet(K key, Map<K, Set<V>> map, V axiom) {
//		if (map == null) {
//			return;
//		}
//		Set<V> axioms = map.get(key);
//		if (axioms == null) {
//			axioms = createSet();
//			map.put(key, axioms);
//		}
//		axioms.add(axiom);
//	}

//	/**
//	 * Removes an axiom from a set of axioms, which is the value for a specified
//	 * key in a specified map.
//	 * 
//	 * @param key
//	 *            The key that indexes the set of axioms.
//	 * @param map
//	 *            The map, which maps keys to sets of axioms.
//	 * @param axiom
//	 *            The axiom to remove from the set of axioms.
//	 * @param removeSetIfEmpty
//	 *            Specifies whether or not the indexed set should be removed
//	 *            from the map if it is empty after removing the specified axiom
//	 */
//	public <K, V extends OWLAxiom> void removeAxiomFromSet(K key, Map<K, Set<V>> map, V axiom, boolean removeSetIfEmpty) {
//		if (map == null) {
//			return;
//		}
//		Set<V> axioms = map.get(key);
//		if (axioms != null) {
//			axioms.remove(axiom);
//			if (removeSetIfEmpty) {
//				if (axioms.isEmpty()) {
//					map.remove(key);
//				}
//			}
//		}
//	}

	public <E> Set<E> getReturnSet(Set<E> set) {
		if (set == null) {
			return Collections.emptySet();
		}
		return createSet(set);
	}

	/**
	 * We get a lot of lists from HG, so we need a more generic way to create
	 * return sets. Warning: Size set/collection could differ, if collection
	 * contain duplicate elements. hilpold
	 * 
	 * @param <E>
	 * @param collection
	 *            may be null
	 * @return
	 */
	public <E> Set<E> getReturnSet(Collection<E> collection) {
		if (collection == null) {
			return Collections.emptySet();
		}
		return createSet(collection);
	}

//	public <K extends OWLObject, V extends OWLAxiom> Set<V> getAxioms(K key, Map<K, Set<V>> map) {
//		Set<V> axioms = map.get(key);
//		if (axioms != null) {
//			return CollectionFactory.getCopyOnRequestSet(axioms);
//		} else {
//			return Collections.emptySet();
//		}
//	}

//	protected <K, V extends OWLAxiom> Set<V> getAxioms(K key, Map<K, Set<V>> map, boolean create) {
//		Set<V> axioms = map.get(key);
//		if (axioms == null) {
//			if (create) {
//				axioms = createSet();
//				map.put(key, axioms);
//			} else {
//				axioms = Collections.emptySet();
//			}
//		} else {
//			axioms = CollectionFactory.getCopyOnRequestSet(axioms);
//		}
//		return axioms;
//	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls) {
		//index 0 Is it a subclass in the relationship, 1 superclass
		return findAxiomsInIncidenceSet(cls, OWLSubClassOfAxiomHGDB.class, 0);
		
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLSubClassOfAxiom> l = new ArrayList<OWLSubClassOfAxiom>();
//		if (clsHandle != null) {
//			IncidenceSet iSet = graph.getIncidenceSet(clsHandle);
//			for (HGHandle incidentAtomHandle : iSet) {
//				Object o = graph.get(incidentAtomHandle);
//				if (o != null) {
//					if (o instanceof OWLSubClassOfAxiom && ontology.isMember(incidentAtomHandle)) {
//						OWLSubClassOfAxiomHGDB sc = (OWLSubClassOfAxiomHGDB)o;
//						//Is it a superclass in the relationship
//						if (clsHandle.equals(sc.getTargetAt(0))) {
//							l.add(sc);
//						} // else not superclass.
//					} // else other Link or other axiom.
//				} // else incidentAtomHandle not in cache! 
//			} 
//		} else {
//			String msg = ("ClassHandle null. Graph.getHandle(" + cls + ") in getSubClassAxiomsForSubClass(OWLClass) returned null");
//			throw new IllegalStateException(msg);
//		}
//		return getReturnSet(l);
	}	

//2011.12.05 Optimized implementation added for	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls) {
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLSubClassOfAxiom> l = ontology.getAll(hg.and(hg.type(OWLSubClassOfAxiomHGDB.class)
//		// subclass 0, superClass 1
//				, hg.orderedLink(clsHandle, hg.anyHandle())));
//		return getReturnSet(l);
//		// Maps.SubClassAxiomsByLHS.initMap(this);
//		// return getReturnSet(getAxioms(cls, subClassAxiomsByLHS));
//	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass cls) {
		// subclass 0, superClass 1
		return findAxiomsInIncidenceSet(cls, OWLSubClassOfAxiomHGDB.class, 1);

//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLSubClassOfAxiom> l = ontology.getAll(hg.and(hg.type(OWLSubClassOfAxiomHGDB.class)
//		// subclass 0, superClass 1
//				, hg.orderedLink(hg.anyHandle(), clsHandle)));
//		return getReturnSet(l);
//		// Maps.SubClassAxiomsByRHS.initMap(this);
//		// return getReturnSet(getAxioms(cls, subClassAxiomsByRHS));
	}

	public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(OWLClass cls) {		
		return findAxiomsInIncidenceSet(cls, OWLEquivalentClassesAxiomHGDB.class);
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLEquivalentClassesAxiom> l = ontology.getAll(hg.and(hg.type(OWLEquivalentClassesAxiomHGDB.class),
//				hg.incident(clsHandle)));
//		return getReturnSet(l);
		// Maps.EquivalentClassesAxiomsByClass.initMap(this);
		// return getReturnSet(getAxioms(cls, equivalentClassesAxiomsByClass));
	}

	public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass cls) {
		return findAxiomsInIncidenceSet(cls, OWLDisjointClassesAxiomHGDB.class);
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLDisjointClassesAxiom> l = ontology.getAll(hg.and(hg.type(OWLDisjointClassesAxiomHGDB.class),
//				hg.incident(clsHandle)));
//		return getReturnSet(l);
		// Maps.DisjointClassesAxiomsByClass.initMap(this);
		// return getReturnSet(getAxioms(cls, disjointClassesAxiomsByClass));
	}

	public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass cls) {
		return findAxiomsInIncidenceSet(cls, OWLDisjointUnionAxiomHGDB.class);				
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLDisjointUnionAxiom> l = ontology.getAll(hg.and(hg.type(OWLDisjointUnionAxiomHGDB.class),
//				hg.incident(clsHandle)));
//		return getReturnSet(l);
//		// Maps.DisjointUnionAxiomsByClass.initMap(this);
//		// return getReturnSet(getAxioms(owlClass,
//		// getDisjointUnionAxiomsByClass()));
	}

	public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass cls) {
		return findAxiomsInIncidenceSet(cls, OWLHasKeyAxiomHGDB.class);				
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLHasKeyAxiom> l = ontology.getAll(hg.and(hg.type(OWLHasKeyAxiomHGDB.class),
//				hg.link(clsHandle)));
//		return getReturnSet(l);
////		Maps.HasKeyAxiomsByClass.initMap(this);
////		return getReturnSet(getAxioms(cls, getHasKeyAxiomsByClass()));
	}

	// Object properties
	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(
			OWLObjectPropertyExpression property) {
		// subclass 0, superClass 1
		return findAxiomsInIncidenceSet(property, OWLSubObjectPropertyOfAxiomHGDB.class, 0);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLSubObjectPropertyOfAxiom> l = ontology.getAll(hg.and(hg.type(OWLSubObjectPropertyOfAxiomHGDB.class)
//				// subclass 0, superClass 1
//				, hg.orderedLink(propertyHandle, hg.anyHandle())));
//		return getReturnSet(l);
////		 Maps.ObjectSubPropertyAxiomsByLHS.initMap(this);
////		 return getReturnSet(getAxioms(property,
////		 getObjectSubPropertyAxiomsByLHS()));
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(
			OWLObjectPropertyExpression property) {
		// subclass 0, superClass 1
		return findAxiomsInIncidenceSet(property, OWLSubObjectPropertyOfAxiomHGDB.class, 1);				
		
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLSubObjectPropertyOfAxiom> l = ontology.getAll(hg.and(hg.type(OWLSubObjectPropertyOfAxiomHGDB.class)
//		// subclass 0, superClass 1
//				, hg.orderedLink(hg.anyHandle(), propertyHandle)));
//		return getReturnSet(l);
//		// Maps.ObjectSubPropertyAxiomsByRHS.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getObjectSubPropertyAxiomsByRHS()));
	}

	public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(OWLObjectPropertyExpression property) {
		// property 0, domain 1
		return findAxiomsInIncidenceSet(property, OWLObjectPropertyDomainAxiomHGDB.class, 0);				

//		// Maps.ObjectPropertyDomainAxiomsByProperty.initMap(this);
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLObjectPropertyDomainAxiom> l = ontology.getAll(hg.and(hg.type(OWLObjectPropertyDomainAxiomHGDB.class)
//		// property 0, domain 1
//				, hg.orderedLink(propertyHandle, hg.anyHandle())));
//		return getReturnSet(l);
//		// return getReturnSet(getAxioms(property,
//		// getObjectPropertyDomainAxiomsByProperty()));
	}

	public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(OWLObjectPropertyExpression property) {
		// property 0, range 1
		return findAxiomsInIncidenceSet(property, OWLObjectPropertyRangeAxiomHGDB.class, 0);				
		
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLObjectPropertyRangeAxiom> l = ontology.getAll(hg.and(hg.type(OWLObjectPropertyRangeAxiomHGDB.class)
//		// property 0, range 1
//				, hg.orderedLink(propertyHandle, hg.anyHandle())));
//		return getReturnSet(l);
//		// Maps.ObjectPropertyRangeAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getObjectPropertyRangeAxiomsByProperty()));
	}

	public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(OWLObjectPropertyExpression property) {
		//return any index
		return findAxiomsInIncidenceSet(property, OWLInverseObjectPropertiesAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLInverseObjectPropertiesAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLInverseObjectPropertiesAxiomHGDB.class)
//				// first 0, second 1, return any
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.InversePropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getInversePropertyAxiomsByProperty()));
	}

	public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(
			OWLObjectPropertyExpression property) {
		//return any
		return findAxiomsInIncidenceSet(property, OWLEquivalentObjectPropertiesAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLEquivalentObjectPropertiesAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLEquivalentObjectPropertiesAxiomHGDB.class)
//				// properties 0...arity
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.EquivalentObjectPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getEquivalentObjectPropertyAxiomsByProperty()));
	}

	public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(OWLObjectPropertyExpression property) {
		//return any
		return findAxiomsInIncidenceSet(property, OWLDisjointObjectPropertiesAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLDisjointObjectPropertiesAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLDisjointObjectPropertiesAxiomHGDB.class)
//				// properties 0...arity
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.DisjointObjectPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getDisjointObjectPropertyAxiomsByProperty()));
	}

	public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLFunctionalObjectPropertyAxiomHGDB.class);				
		
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLFunctionalObjectPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLFunctionalObjectPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.FunctionalObjectPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getFunctionalObjectPropertyAxiomsByProperty()));
	}

	public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLInverseFunctionalObjectPropertyAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLInverseFunctionalObjectPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLInverseFunctionalObjectPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.InverseFunctionalPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getInverseFunctionalPropertyAxiomsByProperty()));
	}

	public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLSymmetricObjectPropertyAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLSymmetricObjectPropertyAxiom> l = ontology.getAll(hg.and(hg.type(OWLSymmetricObjectPropertyAxiom.class)
//		// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.SymmetricPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getSymmetricPropertyAxiomsByProperty()));
	}

	public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLAsymmetricObjectPropertyAxiomHGDB.class);				

//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLAsymmetricObjectPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLAsymmetricObjectPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.AsymmetricPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getAsymmetricPropertyAxiomsByProperty()));
	}

	public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLReflexiveObjectPropertyAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLReflexiveObjectPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLReflexiveObjectPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.ReflexivePropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getReflexivePropertyAxiomsByProperty()));
	}

	public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLIrreflexiveObjectPropertyAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLIrreflexiveObjectPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLIrreflexiveObjectPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.IrreflexivePropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getIrreflexivePropertyAxiomsByProperty()));
	}

	public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(OWLObjectPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLTransitiveObjectPropertyAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLTransitiveObjectPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLTransitiveObjectPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.TransitivePropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getTransitivePropertyAxiomsByProperty()));
	}

	public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(OWLDataPropertyExpression property) {
		//return any, arity is 1
		return findAxiomsInIncidenceSet(property, OWLFunctionalDataPropertyAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLFunctionalDataPropertyAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLFunctionalDataPropertyAxiomHGDB.class)
//				// property 0 arity 1
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//		// Maps.FunctionalDataPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property, getFunctionalDataPropertyAxiomsByProperty()));
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(OWLDataProperty lhsProperty) {
		// subclass 0, superClass 1
		return findAxiomsInIncidenceSet(lhsProperty, OWLSubDataPropertyOfAxiomHGDB.class, 0);				
//		HGHandle lhsPropertyHandle = graph.getHandle(lhsProperty);
//		List<OWLSubDataPropertyOfAxiom> l = ontology.getAll(hg.and(hg.type(OWLSubDataPropertyOfAxiomHGDB.class)
//		// subclass 0, superClass 1
//				, hg.orderedLink(lhsPropertyHandle, hg.anyHandle())));
//		return getReturnSet(l);
//		// Maps.DataSubPropertyAxiomsByLHS.initMap(this);
//		// return getReturnSet(getAxioms(lhsProperty,
//		// getDataSubPropertyAxiomsByLHS()));
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(OWLDataPropertyExpression property) {
		// subclass 0, superClass 1
		return findAxiomsInIncidenceSet(property, OWLSubDataPropertyOfAxiomHGDB.class, 1);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLSubDataPropertyOfAxiom> l = ontology.getAll(hg.and(hg.type(OWLSubDataPropertyOfAxiomHGDB.class)
//		// subclass 0, superClass 1
//				, hg.orderedLink(hg.anyHandle(), propertyHandle)));
//		return getReturnSet(l);
//		// Maps.DataSubPropertyAxiomsByRHS.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getDataSubPropertyAxiomsByRHS()));
	}

	public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(OWLDataProperty property) {
		// OWLDataPropertyExpression property 0, OWLDataPropertyExpression domain 1
		return findAxiomsInIncidenceSet(property, OWLDataPropertyDomainAxiomHGDB.class, 0);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLDataPropertyDomainAxiom> l = ontology.getAll(hg.and(hg.type(OWLDataPropertyDomainAxiomHGDB.class)
//		// OWLDataPropertyExpression property 0, OWLDataPropertyExpression
//		// domain 1
//				, hg.orderedLink(propertyHandle, hg.anyHandle())));
//		return getReturnSet(l);
//		// Maps.DataPropertyDomainAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getDataPropertyDomainAxiomsByProperty()));
	}

	public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(OWLDataProperty property) {
		// OWLDataPropertyExpression property 0, OWLDataRange range 1
		return findAxiomsInIncidenceSet(property, OWLDataPropertyRangeAxiomHGDB.class, 0);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLDataPropertyRangeAxiom> l = ontology.getAll(hg.and(hg.type(OWLDataPropertyRangeAxiomHGDB.class)
//		// OWLDataPropertyExpression property 0, OWLDataRange range 1
//				, hg.orderedLink(propertyHandle, hg.anyHandle())));
//		return getReturnSet(l);
//		// Maps.DataPropertyRangeAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getDataPropertyRangeAxiomsByProperty()));
	}

	public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(OWLDataProperty property) {
		// any target
		return findAxiomsInIncidenceSet(property, OWLEquivalentDataPropertiesAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLEquivalentDataPropertiesAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLEquivalentDataPropertiesAxiomHGDB.class)
//				// Set<? extends OWLDataPropertyExpression> properties 0
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
//
//		// Maps.EquivalentDataPropertyAxiomsByProperty.initMap(this);
//		// return getReturnSet(getAxioms(property,
//		// getEquivalentDataPropertyAxiomsByProperty()));
	}

	public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(OWLDataProperty property) {
		// any target
		return findAxiomsInIncidenceSet(property, OWLDisjointDataPropertiesAxiomHGDB.class);				
//		HGHandle propertyHandle = graph.getHandle(property);
//		List<OWLDisjointDataPropertiesAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLDisjointDataPropertiesAxiomHGDB.class)
//				// Set<? extends OWLDataPropertyExpression> properties 0
//				, hg.link(propertyHandle)));
//		return getReturnSet(l);
////		Maps.DisjointDataPropertyAxiomsByProperty.initMap(this);
////		return getReturnSet(getAxioms(property, getDisjointDataPropertyAxiomsByProperty()));
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLIndividual individual) {
		// individualHandle index 0, classExpressionHandle index 1
		return findAxiomsInIncidenceSet(individual, OWLClassAssertionHGDB.class, 0);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLClassAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLClassAssertionHGDB.class)
//				// individualHandle index 0, classExpressionHandle index 1 
//				, hg.orderedLink(individualHandle, hg.anyHandle())));
//		return getReturnSet(l);
////		Maps.ClassAssertionAxiomsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getClassAssertionAxiomsByIndividual()));
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClassExpression type) {
		// individualHandle index 0, classExpressionHandle index 1
		return findAxiomsInIncidenceSet(type, OWLClassAssertionHGDB.class, 1);				
//		HGHandle typeHandle = graph.getHandle(type);
//		List<OWLClassAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLClassAssertionHGDB.class)
//				// individualHandle index 0, classExpressionHandle index 1 
//				, hg.orderedLink(hg.anyHandle(), typeHandle)));
//		return getReturnSet(l);
////		Maps.ClassAssertionAxiomsByClass.initMap(this);
////		return getReturnSet(getAxioms(type, getClassAssertionAxiomsByClass()));
		
//		BORIS: to demonstrate an idea:
//		HGQuery q1 = null;
//		
//
//		HGCompiledQuery q = HGQuery<SearchResult>.make("q1",graph, 
//				hg.and(
//		hg.type(OWLClassAssertionHGDB.class) 
//		, hg.orderedLink(hg.anyHandle(), hg.param("type"))));
//		
//		q.set("type", ...).set("asdfasd",asdfasd).execute();
//		HGQuery.query("q1").set("type", type).execute();
	}

	public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(OWLIndividual individual) {
		//subjectHandle 0, propertyHandle 1, objectHandle 2
		return findAxiomsInIncidenceSet(individual, OWLDataPropertyAssertionAxiomHGDB.class, 0);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLDataPropertyAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLDataPropertyAssertionAxiomHGDB.class)
//				//subjectHandle 0, propertyHandle 1, objectHandle 2
//				, hg.orderedLink(individualHandle, hg.anyHandle(), hg.anyHandle())));
//		return getReturnSet(l);						
////		Maps.DataPropertyAssertionsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getDataPropertyAssertionsByIndividual()));
	}

	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(OWLIndividual individual) {
		//subjectHandle 0, propertyHandle 1, objectHandle 2
		return findAxiomsInIncidenceSet(individual, OWLObjectPropertyAssertionAxiomHGDB.class, 0);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLObjectPropertyAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLObjectPropertyAssertionAxiomHGDB.class)
//				//subjectHandle 0, propertyHandle 1, objectHandle 2
//				, hg.orderedLink(individualHandle, hg.anyHandle(), hg.anyHandle())));
//		return getReturnSet(l);				
////		Maps.ObjectPropertyAssertionsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getObjectPropertyAssertionsByIndividual()));
	}

	public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(
			OWLIndividual individual) {
		//subjectHandle 0, propertyHandle 1, objectHandle 2
		return findAxiomsInIncidenceSet(individual, OWLNegativeObjectPropertyAssertionAxiomHGDB.class, 0);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLNegativeObjectPropertyAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLNegativeObjectPropertyAssertionAxiomHGDB.class)
//				//subjectHandle 0, propertyHandle 1, objectHandle 2
//				, hg.orderedLink(individualHandle, hg.anyHandle(), hg.anyHandle())));
//		return getReturnSet(l);				
////		Maps.NegativeObjectPropertyAssertionAxiomsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getNegativeObjectPropertyAssertionAxiomsByIndividual()));
	}

	public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(OWLIndividual individual) {
		//subjectHandle 0, propertyHandle 1, objectHandle 2
		return findAxiomsInIncidenceSet(individual, OWLNegativeDataPropertyAssertionAxiomHGDB.class, 0);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLNegativeDataPropertyAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLNegativeDataPropertyAssertionAxiomHGDB.class)
//				//subjectHandle 0, propertyHandle 1, objectHandle 2
//				, hg.orderedLink(individualHandle, hg.anyHandle(), hg.anyHandle())));
//		return getReturnSet(l);								
////		Maps.NegativeDataPropertyAssertionAxiomsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getNegativeDataPropertyAssertionAxiomsByIndividual()));
	}

	public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLIndividual individual) {
		//any target
		return findAxiomsInIncidenceSet(individual, OWLSameIndividualAxiomHGDB.class);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLSameIndividualAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLSameIndividualAxiomHGDB.class)
//				// List<HGHandle> individualsHandles 
//				, hg.link(individualHandle)));
//		return getReturnSet(l);		
////		Maps.SameIndividualsAxiomsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getSameIndividualsAxiomsByIndividual()));
	}

	public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(OWLIndividual individual) {
		//any target
		return findAxiomsInIncidenceSet(individual, OWLDifferentIndividualsAxiomHGDB.class);				
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLDifferentIndividualsAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLDifferentIndividualsAxiomHGDB.class)
//				// List<HGHandle> individualsHandles 
//				, hg.link(individualHandle)));
//		return getReturnSet(l);
////		Maps.DifferentIndividualsAxiomsByIndividual.initMap(this);
////		return getReturnSet(getAxioms(individual, getDifferentIndividualsAxiomsByIndividual()));
	}
	
	private HGHandle getStoredIRI(final IRI iri) {
		return OWLDataFactoryHGDB.get(this.graph).data.lookupIRIByValue.var("iri", iri).findOne();
//    	HGHandle iriHandle  = graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>() {
//			public HGHandle call() {
//				return hg.findOne(graph, hg.and(hg.type(IRI.class), hg.eq(iri)));
//			}}, HGTransactionConfig.READONLY);
//    	return iriHandle;
	}

	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxiomsBySubject(OWLAnnotationSubject subject) {
		//index subjectHandle 0, propertyHandle 1, valueHandle 2.
		if (subject instanceof IRI /*&& graph.getHandle(subject) == null*/) {
			//We might find the IRI in the store:
			HGHandle storedIRIHandle = getStoredIRI((IRI)subject);
			if (storedIRIHandle != null) {
				//TODO we replace the subject with a freshly loaded java object
				//this is a short term solution
				//findAxiomsInIncidenceSet should accept a handle in the future
				IRI storedIRI = graph.get(storedIRIHandle);
				return findAxiomsInIncidenceSet(storedIRI, OWLAnnotationAssertionAxiomHGDB.class, 0);
			} else {
				return Collections.emptySet();
			}
		}
		return findAxiomsInIncidenceSet(subject, OWLAnnotationAssertionAxiomHGDB.class, 0);
	}
		
//		HGHandle subjectHandle = graph.getHandle(subject);
//		List<OWLAnnotationAssertionAxiom> l = new ArrayList<OWLAnnotationAssertionAxiom>();
//		if (subjectHandle != null) {
//			IncidenceSet iSet = graph.getIncidenceSet(subjectHandle);
//			for (HGHandle incidentAtomHandle : iSet) {
//				Object o = graph.get(incidentAtomHandle);
//				if (o != null) {
//					if (o instanceof OWLAnnotationAssertionAxiom && ontology.isMember(incidentAtomHandle)) {
//						OWLAnnotationAssertionAxiomHGDB axS = (OWLAnnotationAssertionAxiomHGDB)o;
//						
//						if (subjectHandle.equals(axS.getTargetAt(0))) {
//							l.add(axS);
//						} // else not subject.
//					} // else other Link or other axiom.
//				} // else incidentAtomHandle not in cache! 
//			} 
//		} else {
//			String msg = ("SubjectHandle null. Graph.getHandle(" + subject + ") in getAnnotationAssertionAxiomsBySubject(OWLAnnotationSubject) returned null");
//			throw new IllegalStateException(msg);
//		}
//		return getReturnSet(l);

//	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxiomsBySubjectOLD(OWLAnnotationSubject subject) {
//		HGHandle subjectHandle = graph.getHandle(subject);
//		List<OWLAnnotationAssertionAxiom> l = ontology.getAll(hg.and(
//				hg.type(OWLAnnotationAssertionAxiomHGDB.class)
//				//subjectHandle 0, propertyHandle 1, valueHandle 2
//				, hg.orderedLink(subjectHandle, hg.anyHandle(), hg.anyHandle())));
//		return getReturnSet(l);
////2011.11.14		Maps.AnnotationAssertionAxiomsBySubject.initMap(this);
////		return getReturnSet(getAxioms(subject, annotationAssertionAxiomsBySubject, false));
//	}

	/**
	 * See OWLOntology interface documentation.
	 */
	public Set<OWLClassAxiom> getOWLClassAxioms(OWLClass cls) {		
		return findAxiomsInIncidenceSetImplTwo(cls, classDefiningAxiomMatcher);
//2011.12.29 old:		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLClassAxiom> l = new ArrayList<OWLClassAxiom>();
//		if (clsHandle != null) {
//				IncidenceSet iSet = graph.getIncidenceSet(clsHandle);
//				for (HGHandle incidentAtomHandle : iSet) {
//					Object o = graph.get(incidentAtomHandle);
//					if (o != null) {
//						if (o instanceof OWLClassAxiom) {
//							if (ontology.isMember(incidentAtomHandle)) {
//								if (o instanceof OWLSubClassOfAxiomHGDB) {
//									OWLSubClassOfAxiomHGDB sc = (OWLSubClassOfAxiomHGDB)o;
//									//Is it a superclass in the relationship
//									if (clsHandle.equals(sc.getTargetAt(0))) {
//										l.add((OWLClassAxiom)o);
//									}
//								} else {
//									l.add((OWLClassAxiom)o);
//							} // else not this ontology.
//						} // else other Link.
//					} // else incidentAtomHandle not in cache! 
//				} 
//			}// else no entity found
//		} else {
//			String msg = ("ClassHandle null. Graph.getHandle(" + cls + ") in getAxioms(OWLClass) returned null");
//			throw new IllegalStateException(msg);
//		}
//		return getReturnSet(l);
	}

//	/**
//	 * See OWLOntology interface documentation.
//	 */
//	public Set<OWLClassAxiom> getAxiomsOld(OWLClass cls) {
//		HGHandle clsHandle = graph.getHandle(cls);
//		List<OWLClassAxiom> l;
//		if (clsHandle != null) {
//			l = ontology.getAll(hg.or(
//					hg.and(hg.type(OWLSubClassOfAxiomHGDB.class), hg.orderedLink(clsHandle, hg.anyHandle())),
//					hg.and(hg.or(hg.type(OWLEquivalentClassesAxiomHGDB.class),
//							hg.type(OWLDisjointClassesAxiomHGDB.class), hg.type(OWLDisjointUnionAxiomHGDB.class)),
//							hg.incident(clsHandle))));
//
//			// if (clsHandle != null) {
//			// l = ontology.getAll(hg.and(
//			// hg.typePlus(OWLClassAxiom.class)
//			// //links of any arity returned.
//			// ,hg.incident(clsHandle)));
//		} else {
//			String msg = ("ClassHandle null. Graph.getHandle(" + cls + ") in getAxioms(OWLClass) returned null");
//			// l = null;
//			throw new IllegalStateException(msg);
//		}
//		return getReturnSet(l);
//
//		// Maps.ClassAxiomsByClass.initMap(this);
//		// return getReturnSet(getAxioms(cls, getClassAxiomsByClass()));
//	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getOWLIndividualAxioms(org.semanticweb.owlapi.model.OWLIndividual)
	 */
	@Override
	public Set<OWLIndividualAxiom> getOWLIndividualAxioms(OWLIndividual individual) {
		return findAxiomsInIncidenceSetImplTwo(individual, individualDefiningAxiomMatcher);
//		HGHandle individualHandle = graph.getHandle(individual);
//		List<OWLIndividualAxiom> l = new ArrayList<OWLIndividualAxiom>();
//		if (individualHandle != null) {
//			IncidenceSet iSet = graph.getIncidenceSet(individualHandle);
//			for (HGHandle incidentAtomHandle : iSet) {
//				Object o = graph.get(incidentAtomHandle);
//				if (o != null) {
//					if (o instanceof OWLIndividualAxiom) {
//						OWLIndividualAxiom ax = (OWLIndividualAxiom) o;
//						if (ontology.isMember(incidentAtomHandle)) {								
//							//check for 
//							if (isDefiningOWLIndividualAxiomFor(ax, individualHandle)) {
//								l.add(ax);
//							}//else not defining
//						} // else not this ontology.
//					} // else other Link.
//				} // else incidentAtomHandle not in cache! 
//			} 
//		} else {
//			String msg = ("OWLIndividualHandle null. Graph.getHandle(" + individual + ") in getOWLIndividualAxioms(OWLIndividual) returned null");
//			throw new IllegalStateException(msg);
//		}
//		return getReturnSet(l);
	}
			
	// 2011.10.06 public Map<OWLClass, Set<OWLClassAxiom>>
	// getClassAxiomsByClass() {
	// return this.classAxiomsByClass;
	// }
	//
	// public Map<OWLClass, Set<OWLSubClassOfAxiom>> getSubClassAxiomsByLHS() {
	// return this.subClassAxiomsByLHS;
	// }
	//
	// public Map<OWLClass, Set<OWLSubClassOfAxiom>> getSubClassAxiomsByRHS() {
	// return this.subClassAxiomsByRHS;
	// }

	// public Map<OWLClass, Set<OWLEquivalentClassesAxiom>>
	// getEquivalentClassesAxiomsByClass() {
	// return this.equivalentClassesAxiomsByClass;
	// }

	// public Map<OWLClass, Set<OWLDisjointClassesAxiom>>
	// getDisjointClassesAxiomsByClass() {
	// return this.disjointClassesAxiomsByClass;
	// }

	// public Map<OWLClass, Set<OWLDisjointUnionAxiom>>
	// getDisjointUnionAxiomsByClass() {
	// return this.disjointUnionAxiomsByClass;
	// }

	//	public Map<OWLClass, Set<OWLHasKeyAxiom>> getHasKeyAxiomsByClass() {
	//		return this.hasKeyAxiomsByClass;
	//	}

	// public Map<OWLObjectPropertyExpression, Set<OWLSubObjectPropertyOfAxiom>>
	// getObjectSubPropertyAxiomsByLHS() {
	// return this.objectSubPropertyAxiomsByLHS;
	// }
	//
	// public Map<OWLObjectPropertyExpression, Set<OWLSubObjectPropertyOfAxiom>>
	// getObjectSubPropertyAxiomsByRHS() {
	// return this.objectSubPropertyAxiomsByRHS;
	// }

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLEquivalentObjectPropertiesAxiom>>
	// getEquivalentObjectPropertyAxiomsByProperty() {
	// return this.equivalentObjectPropertyAxiomsByProperty;
	// }

	//	public Map<OWLObjectPropertyExpression, Set<OWLDisjointObjectPropertiesAxiom>> getDisjointObjectPropertyAxiomsByProperty() {
	//		return this.disjointObjectPropertyAxiomsByProperty;
	//	}

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLObjectPropertyDomainAxiom>>
	// getObjectPropertyDomainAxiomsByProperty() {
	// return this.objectPropertyDomainAxiomsByProperty;
	// }

	// public Map<OWLObjectPropertyExpression, Set<OWLObjectPropertyRangeAxiom>>
	// getObjectPropertyRangeAxiomsByProperty() {
	// return this.objectPropertyRangeAxiomsByProperty;
	// }

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLFunctionalObjectPropertyAxiom>>
	// getFunctionalObjectPropertyAxiomsByProperty() {
	// return this.functionalObjectPropertyAxiomsByProperty;
	// }

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLInverseFunctionalObjectPropertyAxiom>>
	// getInverseFunctionalPropertyAxiomsByProperty() {
	// return this.inverseFunctionalPropertyAxiomsByProperty;
	// }

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLSymmetricObjectPropertyAxiom>>
	// getSymmetricPropertyAxiomsByProperty() {
	// return this.symmetricPropertyAxiomsByProperty;
	// }

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLAsymmetricObjectPropertyAxiom>>
	// getAsymmetricPropertyAxiomsByProperty() {
	// return this.asymmetricPropertyAxiomsByProperty;
	// }

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLReflexiveObjectPropertyAxiom>>
	// getReflexivePropertyAxiomsByProperty() {
	// return this.reflexivePropertyAxiomsByProperty;
	// }

//2011.11.14 removed	public Map<OWLObjectPropertyExpression, Set<OWLIrreflexiveObjectPropertyAxiom>> getIrreflexivePropertyAxiomsByProperty() {
//		return this.irreflexivePropertyAxiomsByProperty;
//	}
//
//	public Map<OWLObjectPropertyExpression, Set<OWLTransitiveObjectPropertyAxiom>> getTransitivePropertyAxiomsByProperty() {
//		return this.transitivePropertyAxiomsByProperty;
//	}

	// public Map<OWLObjectPropertyExpression,
	// Set<OWLInverseObjectPropertiesAxiom>>
	// getInversePropertyAxiomsByProperty() {
	// return this.inversePropertyAxiomsByProperty;
	// }

	// public Map<OWLDataPropertyExpression, Set<OWLSubDataPropertyOfAxiom>>
	// getDataSubPropertyAxiomsByLHS() {
	// return this.dataSubPropertyAxiomsByLHS;
	// }
	//
	// public Map<OWLDataPropertyExpression, Set<OWLSubDataPropertyOfAxiom>>
	// getDataSubPropertyAxiomsByRHS() {
	// return this.dataSubPropertyAxiomsByRHS;
	// }

	// public Map<OWLDataPropertyExpression,
	// Set<OWLEquivalentDataPropertiesAxiom>>
	// getEquivalentDataPropertyAxiomsByProperty() {
	// return this.equivalentDataPropertyAxiomsByProperty;
	// }

//	public Map<OWLDataPropertyExpression, Set<OWLDisjointDataPropertiesAxiom>> getDisjointDataPropertyAxiomsByProperty() {
//		return this.disjointDataPropertyAxiomsByProperty;
//	}

	// public Map<OWLDataPropertyExpression, Set<OWLDataPropertyDomainAxiom>>
	// getDataPropertyDomainAxiomsByProperty() {
	// return this.dataPropertyDomainAxiomsByProperty;
	// }

	// public Map<OWLDataPropertyExpression, Set<OWLDataPropertyRangeAxiom>>
	// getDataPropertyRangeAxiomsByProperty() {
	// return this.dataPropertyRangeAxiomsByProperty;
	// }

//	public Map<OWLDataPropertyExpression, Set<OWLFunctionalDataPropertyAxiom>> getFunctionalDataPropertyAxiomsByProperty() {
//		return this.functionalDataPropertyAxiomsByProperty;
//	}

//	public Map<OWLIndividual, Set<OWLClassAssertionAxiom>> getClassAssertionAxiomsByIndividual() {
//		return this.classAssertionAxiomsByIndividual;
//	}

//	public Map<OWLClassExpression, Set<OWLClassAssertionAxiom>> getClassAssertionAxiomsByClass() {
//		return this.classAssertionAxiomsByClass;
//	}

//	public Map<OWLIndividual, Set<OWLObjectPropertyAssertionAxiom>> getObjectPropertyAssertionsByIndividual() {
//		return this.objectPropertyAssertionsByIndividual;
//	}

//	public Map<OWLIndividual, Set<OWLDataPropertyAssertionAxiom>> getDataPropertyAssertionsByIndividual() {
//		return this.dataPropertyAssertionsByIndividual;
//	}

//	public Map<OWLIndividual, Set<OWLNegativeObjectPropertyAssertionAxiom>> getNegativeObjectPropertyAssertionAxiomsByIndividual() {
//		return this.negativeObjectPropertyAssertionAxiomsByIndividual;
//	}

//	public Map<OWLIndividual, Set<OWLNegativeDataPropertyAssertionAxiom>> getNegativeDataPropertyAssertionAxiomsByIndividual() {
//		return this.negativeDataPropertyAssertionAxiomsByIndividual;
//	}

//	public Map<OWLIndividual, Set<OWLDifferentIndividualsAxiom>> getDifferentIndividualsAxiomsByIndividual() {
//		return this.differentIndividualsAxiomsByIndividual;
//	}

//	public Map<OWLIndividual, Set<OWLSameIndividualAxiom>> getSameIndividualsAxiomsByIndividual() {
//		return this.sameIndividualsAxiomsByIndividual;
//	}

//	public Map<OWLAnnotationSubject, Set<OWLAnnotationAssertionAxiom>> getAnnotationAssertionAxiomsBySubject() {
//		return this.annotationAssertionAxiomsBySubject;
//	}

	// ----------------------------------------------------------------------
	// HGGraphHolder HGHandleHolder Interfaces
	//

	/**
	 * Sets the graph and sets AtomHandle also, if graph non null.
	 */
	@Override
	public void setHyperGraph(HyperGraph graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#setOntologyHyperNode(org
	 * .hypergraphdb.app.owl.HGDBOntology)
	 */
	@Override
	public void setOntologyHyperNode(HGDBOntology ontology) {
		// TODO ugly, but we need it, because Hypernode Interface does not
		// define convenient add)
		if (ontology == null) throw new IllegalArgumentException("Ontology null gives bad queries");
		this.ontology = (HGDBOntologyImpl) ontology;
		this.ontoHandle = graph.getHandle(ontology);

		// The type is not included in the query because it's actually much faster
		// to load the atom and check the type as a separate filtering step.
		if (OWLDataFactoryHGDB.get(this.graph).ignoreOntologyScope()) {			
			this.findAxiomQuery = HGQuery.make(HGHandle.class, graph).compile( 
					hg.and(/* hg.memberOf(ontoHandle), */ hg.incident(hg.var("entity"))));
		}
		else {
			this.findAxiomQuery = HGQuery.make(HGHandle.class, graph).compile( 
					hg.and(hg.memberOf(ontoHandle), hg.incident(hg.var("entity"))));
		}
		this.ontologyAnnotationsQuery = HGQuery.make(OWLAnnotation.class, graph).compile(
				hg.deref(graph, hg.and(hg.memberOf(ontoHandle), hg.type(OWLAnnotationHGDB.class)))
				);
	}

	//
	// END HGGraphHolder HGHandleHolder Interfaces
	// ----------------------------------------------------------------------

	
	//
	// HELPER 
	//
	/**
	 * Iterates over the incidence set (directly, not recursively) of the given entity and returns all Axioms in the ontology and of the giventype, 
	 * who's target at index targetIndex equals the handle to the entity.
	 */
	private <T extends OWLAxiomHGDB, S extends OWLAxiom> Set<S> findAxiomsInIncidenceSet(OWLObject entity, Class<T> axiomTypeHGDB, int targetIndex) {
		return findAxiomsInIncidenceSetImpl(entity, axiomTypeHGDB, targetIndex);
	}

	/**
	 * Iterates over the incidence set (directly, not recursively) of the given entity and returns all Axioms in the ontology and of the giventype, 
	 * who's targetset contains one (or more) targets that equal the handle to the entity.
	 * @param <T>
	 * @param entity
	 * @param axiomTypeHGDB
	 * @return 
	 */
	private <T extends OWLAxiomHGDB, S extends OWLAxiom> Set<S> findAxiomsInIncidenceSet(OWLObject entity, Class<T> axiomTypeHGDB) {
		return findAxiomsInIncidenceSetImpl(entity, axiomTypeHGDB, -1);
	}

	/**
	 * findAxiomsInIncidenceSetImpl finds all matching axioms in the incidenceset of a given OWLObject (Typically entity).
	 * @param <T> the hgdb axiom storage type. 
	 * @param entity
	 * @param axiomTypeHGDB
	 * @param targetIndex the index to check or -1. -1 looks for the first matching target in the incidence set. 
	 * @param anyTarget 
	 * @return
	 */	
	private <T extends OWLAxiomHGDB, S extends OWLAxiom> Set<S> findAxiomsInIncidenceSetImpl(final OWLObject entity, final Class<T> axiomTypeHGDB, final int targetIndex) {
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<S>>() {
			@SuppressWarnings("unchecked")
			public Set<S> call() {
				HGHandle entityHandle = graph.getHandle(entity);
				//Initial Size of Hashset performance critical
				Set<S> s = Collections.EMPTY_SET; //lazy instantiation
				if (entityHandle == null) {
					HGAtomCache cache = graph.getCache();
					System.out.println(cache.get(entity));
					String msg = ("entityHandle null. Graph.getHandle(" + entity + " Class: " + entity.getClass() + ") in findAxiomsInIncidenceSet(OWLEntity) returned null");
					throw new IllegalStateException(msg);
				}
				HGSearchResult<HGHandle> iSetRAR = findAxiomQuery
						//.var("type", axiomTypeHGDB)
						.var("entity", entityHandle).execute(); 
						//graph.getIncidenceSet(entityHandle).getSearchResult();
				if (iSetRAR.hasNext())
					s = new HashSet<S>();
//				HGRandomAccessResult members = ontology.getIndex(graph).find(ontoHandle.getPersistent());
				while (iSetRAR.hasNext()) {
					HGHandle incidentAtomHandle = iSetRAR.next();
//					if (members.goTo(incidentAtomHandle, true) != HGRandomAccessResult.GotoResult.found)
//						continue;
					Object o = graph.get(incidentAtomHandle);
					if (o == null)
						throw new NullPointerException("DB corruption, no atom for " + incidentAtomHandle);
					if (!o.getClass().equals(axiomTypeHGDB))
						continue;
					OWLAxiomHGDB axHGDB  = (OWLAxiomHGDB)o;					
//					if (o != null) {
//						if (axiomTypeHGDB.isAssignableFrom(o.getClass()) && ontology.isMember(incidentAtomHandle)) {
//						
							if (targetIndex == -1) {
								s.add((S)axHGDB);
								// find first target
//								boolean foundEntityInTargetSet = false;
//								int i = 0;
//								while (!foundEntityInTargetSet && i < axHGDB.getArity()) {
//									foundEntityInTargetSet = entityHandle.equals(axHGDB.getTargetAt(i));
//									if (foundEntityInTargetSet) {
//										if (s == Collections.EMPTY_SET) {
//											s = new HashSet<S>();
//										}
//										if (!s.add((S) axHGDB)) {
//											throw new IllegalStateException("Duplicate Axiom found");
//										}
//									} 							
//									i++;
//								}
							} else { // check only targetIndex
								if (entityHandle.equals(axHGDB.getTargetAt(targetIndex))) {
//									if (s == Collections.EMPTY_SET) {
//										s = new HashSet<S>();
//									}
//									if (!s.add((S) axHGDB)) {
//										throw new IllegalStateException("Duplicate Axiom found");
//									}
									s.add((S)axHGDB);
								} // else entity not the target at the specified targetIndex.
							} 
//						} // else other Link or other axiom.
//					} // else incidentAtomHandle not in cache! 
				} 
				iSetRAR.close();
//				members.close();
				return s;	
			}}, HGTransactionConfig.READONLY);
	}

	/**
	 * findAxiomsInIncidenceSetImpl finds all matching axioms in the incidenceset of a given OWLObject (Typically entity).
	 * Uses a matcher that can analyze each incidence set axiom for multiple axiom types.
	 * 
	 * @param <S>
	 * @param entity
	 * @param axiomMatcher
	 * @return
	 */
	private <S extends OWLAxiom> Set<S> findAxiomsInIncidenceSetImplTwo(final OWLObject entity, final DefiningAxiomMatcher axiomMatcher) {
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<S>>() {
			@SuppressWarnings("unchecked")
			public Set<S> call() {
				HGHandle entityHandle = graph.getHandle(entity);
				//Initial Size of Hashset performance critical
				Set<S> s = Collections.EMPTY_SET; //lazy  new HashSet<S>();
				if (entityHandle == null) {
					String msg = ("entityHandle null. Graph.getHandle(" + entity + " Class: " + entity.getClass() + ") in findAxiomsInIncidenceSet(OWLEntity) returned null");
					throw new IllegalStateException(msg);
				}
				//HGRandomAccessResult<HGHandle> iSetRAR = graph.getIncidenceSet(entityHandle).getSearchResult();
				HGSearchResult<HGHandle> iSetRAR = graph.find(hg.and( 
						   hg.memberOf(ontoHandle),
						   hg.incident(entityHandle))); 
				if (iSetRAR.hasNext())
					s = new HashSet<S>();
				
				while (iSetRAR.hasNext()) {
					HGHandle incidentAtomHandle = iSetRAR.next();
					Object o = graph.get(incidentAtomHandle);
					if (o instanceof OWLAxiomHGDB) {
						OWLAxiomHGDB axHGDB = (OWLAxiomHGDB)o;
						if (axiomMatcher.isDefiningAxiom(axHGDB, entityHandle)) {
//							if (ontology.isMember(incidentAtomHandle)) {
//								if (s == Collections.EMPTY_SET) {
//									s = new HashSet<S>();
//								}
//								s.add((S)axHGDB);
//							} // else not member
							s.add((S)axHGDB);
						} // else no match
					}  // else not axiom.
				} 
				iSetRAR.close();
				return s;	
			}}, HGTransactionConfig.READONLY);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getOWLDataPropertyAxioms(org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public Set<OWLDataPropertyAxiom> getOWLDataPropertyAxioms(OWLDataProperty prop) {
		return findAxiomsInIncidenceSetImplTwo(prop, dataPropertyDefiningAxiomMatcher);
		//2011.12.29 old implementation in *impl
		//		final Set<OWLDataPropertyAxiom> result = createSet();
		//		result.addAll(getDataPropertyDomainAxioms(prop));
		//		result.addAll(getEquivalentDataPropertiesAxioms(prop));
		//		result.addAll(getDisjointDataPropertiesAxioms(prop));
		//		result.addAll(getDataPropertyRangeAxioms(prop));
		//		result.addAll(getFunctionalDataPropertyAxioms(prop));
		//		result.addAll(getDataSubPropertyAxiomsForSubProperty(prop));
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getOWLAnnotationPropertyAxioms(org.semanticweb.owlapi.model.OWLAnnotationProperty)
	 */
	@Override
	public Set<OWLAnnotationAxiom> getOWLAnnotationPropertyAxioms(OWLAnnotationProperty prop) {
		return findAxiomsInIncidenceSetImplTwo(prop, annotationPropertyDefiningAxiomMatcher);

		//2011.12.29 old implementation in *impl
		//		Set<OWLAnnotationAxiom> result = createSet();
		//		for (OWLSubAnnotationPropertyOfAxiom ax : getAxioms(AxiomType.SUB_ANNOTATION_PROPERTY_OF)) {
		//			if (ax.getSubProperty().equals(prop)) {
		//				result.add(ax);
		//			}
		//		}
		//		for (OWLAnnotationPropertyRangeAxiom ax : getAxioms(AxiomType.ANNOTATION_PROPERTY_RANGE)) {
		//			if (ax.getProperty().equals(prop)) {
		//				result.add(ax);
		//			}
		//		}
		//		for (OWLAnnotationPropertyDomainAxiom ax : getAxioms(AxiomType.ANNOTATION_PROPERTY_DOMAIN)) {
		//			if (ax.getProperty().equals(prop)) {
		//				result.add(ax);
		//			}
		//		}
		//		return result;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getOWLObjectPropertyExpressionAxioms(org.semanticweb.owlapi.model.OWLObjectPropertyExpression)
	 */
	@Override
	public Set<OWLObjectPropertyAxiom> getOWLObjectPropertyExpressionAxioms(OWLObjectPropertyExpression prop) {
		return findAxiomsInIncidenceSetImplTwo(prop, objectPropertyDefiningAxiomMatcher);
		
		//2011.12.29 old implementation in *impl
		//		final Set<OWLObjectPropertyAxiom> result = createSet(50);
		//
		//				result.addAll(getAsymmetricObjectPropertyAxioms(prop));
		//				result.addAll(getReflexiveObjectPropertyAxioms(prop));
		//				result.addAll(getSymmetricObjectPropertyAxioms(prop));
		//				result.addAll(getIrreflexiveObjectPropertyAxioms(prop));
		//				result.addAll(getTransitiveObjectPropertyAxioms(prop));
		//				result.addAll(getInverseFunctionalObjectPropertyAxioms(prop));
		//				result.addAll(getFunctionalObjectPropertyAxioms(prop));
		//				result.addAll(getInverseObjectPropertyAxioms(prop));
		//				result.addAll(getObjectPropertyDomainAxioms(prop));
		//				result.addAll(getEquivalentObjectPropertiesAxioms(prop));
		//				result.addAll(getDisjointObjectPropertiesAxioms(prop));
		//				result.addAll(getObjectPropertyRangeAxioms(prop));
		//				result.addAll(getObjectSubPropertyAxiomsForSubProperty(prop));
		//				return result;
	}
	
	// ---------------------------------------------------------------------------------------------------------
	//
	//  DEFINING AXIOM MATCHERS 
	//
	//
	
	/**
	 * DefiningAxiomMatcher determines if the given axiom is a defining axiom for the given entity.
	 * These cases are defined by the manchester OWL API Implementation.
	 * 
	 * E.g. An SubClassAxiom is not a defining axiom for an OWLClass that is superclass in it.
	 * 
	 * @author Thomas Hilpold (CIAO/Miami-Dade County)
	 * @created Dec 29, 2011
	 */
	public interface DefiningAxiomMatcher {
		boolean isDefiningAxiom(OWLAxiomHGDB axiom, HGHandle entityHandle);
	}	
	
	protected DefiningAxiomMatcher classDefiningAxiomMatcher = new DefiningAxiomMatcher() {
		@Override
		public boolean isDefiningAxiom(OWLAxiomHGDB axiom, HGHandle entityHandle) {
			if (!(axiom instanceof OWLClassAxiom)) return false;
			boolean returnValue;
			if (axiom instanceof OWLSubClassOfAxiomHGDB) {
				OWLSubClassOfAxiomHGDB axS = (OWLSubClassOfAxiomHGDB)axiom;
				//Is it a subclass in the relationship
				returnValue = (entityHandle.equals(axS.getTargetAt(0)));
			} else {
				returnValue = true;
			}
			return returnValue;	
		}
	};
	
	protected DefiningAxiomMatcher individualDefiningAxiomMatcher = new DefiningAxiomMatcher() {
		@Override
		public boolean isDefiningAxiom(OWLAxiomHGDB axiom, HGHandle entityHandle) {
			boolean returnValue; 
			// we have no superclass (e.g. OWLClassAxiom) here to check like in the other axiom matchers.
			if (axiom instanceof OWLDeclarationAxiomHGDB) {
				return false;
			}
			
			if (axiom instanceof OWLClassAssertionHGDB) {
				OWLClassAssertionHGDB axS = (OWLClassAssertionHGDB)axiom;
				// individualHandle index 0, classExpressionHandle index 1 
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLObjectPropertyAssertionAxiomHGDB) {
				OWLObjectPropertyAssertionAxiomHGDB axS = (OWLObjectPropertyAssertionAxiomHGDB)axiom;
				//subjectHandle 0, propertyHandle 1, objectHandle 2
				returnValue = entityHandle.equals(axS.getTargetAt(0));			
			} else if (axiom instanceof OWLDataPropertyAssertionAxiomHGDB) {
				OWLDataPropertyAssertionAxiomHGDB axS = (OWLDataPropertyAssertionAxiomHGDB)axiom;
				//subjectHandle 0, propertyHandle 1, objectHandle 2
				returnValue = entityHandle.equals(axS.getTargetAt(0));			
			} else if (axiom instanceof OWLNegativeObjectPropertyAssertionAxiomHGDB) { 
				OWLNegativeObjectPropertyAssertionAxiomHGDB axS = (OWLNegativeObjectPropertyAssertionAxiomHGDB)axiom;
				//subjectHandle 0, propertyHandle 1, objectHandle 2
				returnValue = entityHandle.equals(axS.getTargetAt(0));			
			} else if (axiom instanceof OWLNegativeDataPropertyAssertionAxiomHGDB) { 
				OWLNegativeDataPropertyAssertionAxiomHGDB axS = (OWLNegativeDataPropertyAssertionAxiomHGDB)axiom;
				//subjectHandle 0, propertyHandle 1, objectHandle 2
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLSameIndividualAxiomHGDB) { 
				returnValue = true;
			} else if (axiom instanceof OWLDifferentIndividualsAxiomHGDB) {
				returnValue = true;
			} else {
				//2012.01.03 be more tolerant, as individuals are used in many places.
				//TODO define allowed axioms precisely.
				return false;
				//throw new IllegalStateException("OWLIndividualAxiom : " + axiom + " unknown.");
			}
			return returnValue;
		}		
	};

	protected DefiningAxiomMatcher dataPropertyDefiningAxiomMatcher = new DefiningAxiomMatcher() {
		//2011.12.29 old implementation in *impl
		//		final Set<OWLDataPropertyAxiom> result = createSet();
		//		result.addAll(getDataPropertyDomainAxioms(prop));
		//		result.addAll(getEquivalentDataPropertiesAxioms(prop));
		//		result.addAll(getDisjointDataPropertiesAxioms(prop));
		//		result.addAll(getDataPropertyRangeAxioms(prop));
		//		result.addAll(getFunctionalDataPropertyAxioms(prop));
		//		result.addAll(getDataSubPropertyAxiomsForSubProperty(prop));
		@Override
		public boolean isDefiningAxiom(OWLAxiomHGDB axiom, HGHandle entityHandle) {
			if (!(axiom instanceof OWLDataPropertyAxiom)) return false;
			boolean returnValue; 
			if (axiom instanceof OWLDataPropertyDomainAxiomHGDB) {
				OWLDataPropertyDomainAxiomHGDB axS = (OWLDataPropertyDomainAxiomHGDB)axiom;
				// OWLDataPropertyExpression property 0, OWLDataPropertyExpression domain 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLEquivalentDataPropertiesAxiomHGDB) {
				returnValue = true;			
			} else if (axiom instanceof OWLDisjointDataPropertiesAxiomHGDB) {
				returnValue = true;			
			} else if (axiom instanceof OWLDataPropertyRangeAxiomHGDB) { 
				OWLDataPropertyRangeAxiomHGDB axS = (OWLDataPropertyRangeAxiomHGDB)axiom;
				// OWLDataPropertyExpression property 0, OWLDataRange range 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));			
			} else if (axiom instanceof OWLFunctionalDataPropertyAxiomHGDB) { 
				returnValue = true;
			} else if (axiom instanceof OWLSubDataPropertyOfAxiomHGDB) {
				OWLSubDataPropertyOfAxiomHGDB axS = (OWLSubDataPropertyOfAxiomHGDB) axiom;
				// subclass 0, superClass 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else {
				throw new IllegalStateException("OWLIndividualAxiom : " + axiom + " unknown.");
			}
			return returnValue;
		}
	};

	protected DefiningAxiomMatcher objectPropertyDefiningAxiomMatcher = new DefiningAxiomMatcher() {
		//		1		result.addAll(getAsymmetricObjectPropertyAxioms(prop));
		//		2		result.addAll(getReflexiveObjectPropertyAxioms(prop));
		//		3		result.addAll(getSymmetricObjectPropertyAxioms(prop));
		//		4		result.addAll(getIrreflexiveObjectPropertyAxioms(prop));
		//		5		result.addAll(getTransitiveObjectPropertyAxioms(prop));
		//		6		result.addAll(getInverseFunctionalObjectPropertyAxioms(prop));
		//		7		result.addAll(getFunctionalObjectPropertyAxioms(prop));
		//		8		result.addAll(getInverseObjectPropertyAxioms(prop));
		//		9		result.addAll(getObjectPropertyDomainAxioms(prop));
		//		10		result.addAll(getEquivalentObjectPropertiesAxioms(prop));
		//		11		result.addAll(getDisjointObjectPropertiesAxioms(prop));
		//		12		result.addAll(getObjectPropertyRangeAxioms(prop));
		//		13		result.addAll(getObjectSubPropertyAxiomsForSubProperty(prop));

		@Override
		public boolean isDefiningAxiom(OWLAxiomHGDB axiom, HGHandle entityHandle) {
			if (!(axiom instanceof OWLObjectPropertyAxiom)) return false;
			boolean returnValue; 
			if (axiom instanceof OWLAsymmetricObjectPropertyAxiomHGDB) {
				returnValue = true;
			} else if (axiom instanceof OWLReflexiveObjectPropertyAxiomHGDB) {
				returnValue = true;			
			} else if (axiom instanceof OWLSymmetricObjectPropertyAxiomHGDB) {
				returnValue = true;			
			} else if (axiom instanceof OWLIrreflexiveObjectPropertyAxiomHGDB) { 
				returnValue = true;			
			} else if (axiom instanceof OWLTransitiveObjectPropertyAxiomHGDB) { 
				returnValue = true;
			} else if (axiom instanceof OWLInverseFunctionalObjectPropertyAxiomHGDB) {
				returnValue = true;
			} else if (axiom instanceof OWLFunctionalObjectPropertyAxiomHGDB) {
				returnValue = true;
			} else if (axiom instanceof OWLInverseObjectPropertiesAxiomHGDB) {
				returnValue = true;
			} else if (axiom instanceof OWLObjectPropertyDomainAxiomHGDB) {
				OWLObjectPropertyDomainAxiomHGDB axS = (OWLObjectPropertyDomainAxiomHGDB) axiom;
				// property 0, domain 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLEquivalentObjectPropertiesAxiomHGDB) {
				returnValue = true;
			} else if (axiom instanceof OWLDisjointObjectPropertiesAxiomHGDB) {
				returnValue = true;
			} else if (axiom instanceof OWLObjectPropertyRangeAxiomHGDB) {
				OWLObjectPropertyRangeAxiomHGDB axS = (OWLObjectPropertyRangeAxiomHGDB) axiom;
				//// property 0, range 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLSubObjectPropertyOfAxiomHGDB) {
				OWLSubObjectPropertyOfAxiomHGDB axS = (OWLSubObjectPropertyOfAxiomHGDB) axiom;
				//// subclass 0, superClass 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else {
				returnValue = false;
				//TODO Sure those are all?
				//System.out.println("Found OWLObjectPropertyAxiom : " + axiom + " unknown. Class:" + axiom.getClass());
			}
			return returnValue;
		}
	};

	protected DefiningAxiomMatcher annotationPropertyDefiningAxiomMatcher = new DefiningAxiomMatcher() {
		@Override
		public boolean isDefiningAxiom(OWLAxiomHGDB axiom, HGHandle entityHandle) {
			//		Set<OWLAnnotationAxiom> result = createSet();
			//		for (OWLSubAnnotationPropertyOfAxiom ax : getAxioms(AxiomType.SUB_ANNOTATION_PROPERTY_OF)) {
			//			if (ax.getSubProperty().equals(prop)) {
			//				result.add(ax);
			//			}
			//		}
			//		for (OWLAnnotationPropertyRangeAxiom ax : getAxioms(AxiomType.ANNOTATION_PROPERTY_RANGE)) {
			//			if (ax.getProperty().equals(prop)) {
			//				result.add(ax);
			//			}
			//		}
			//		for (OWLAnnotationPropertyDomainAxiom ax : getAxioms(AxiomType.ANNOTATION_PROPERTY_DOMAIN)) {
			//			if (ax.getProperty().equals(prop)) {
			//				result.add(ax);
			//			}
			//		}
			//2012.01.03 corrected 'or' clause
			if (!(axiom instanceof OWLAnnotationAxiom) 
					|| axiom instanceof OWLAnnotationAssertionAxiomHGDB) return false;
			boolean returnValue;
			if (axiom instanceof OWLSubAnnotationPropertyOfAxiomHGDB) {
				OWLSubAnnotationPropertyOfAxiomHGDB axS = (OWLSubAnnotationPropertyOfAxiomHGDB) axiom;
				// sub  0, super 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLAnnotationPropertyDomainAxiomHGDB) {
				OWLAnnotationPropertyDomainAxiomHGDB axS = (OWLAnnotationPropertyDomainAxiomHGDB) axiom;
				// property 0, range 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else if (axiom instanceof OWLAnnotationPropertyRangeAxiomHGDB) {
				OWLAnnotationPropertyRangeAxiomHGDB axS = (OWLAnnotationPropertyRangeAxiomHGDB) axiom;
				// property 0, range 1
				returnValue = entityHandle.equals(axS.getTargetAt(0));
			} else {
				throw new IllegalStateException("OWLAnnotationAxiom : " + axiom + " unknown. Class: " + axiom.getClass());
			}			
			return returnValue;			
		}
	};

}
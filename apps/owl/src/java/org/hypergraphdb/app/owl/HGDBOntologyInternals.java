package org.hypergraphdb.app.owl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
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
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;

/**
 * HGDBInternals.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 */
public interface HGDBOntologyInternals
{

	void setOntologyHyperNode(HGDBOntology ontology);

	Set<OWLImportsDeclaration> getImportsDeclarations();

	/**
	 * @param importDeclaration
	 *            declaration to be added
	 * @return true if the import declaration was not already present, false
	 *         otherwise
	 */
	boolean addImportsDeclaration(OWLImportsDeclaration importDeclaration);

	/**
	 * @param importDeclaration
	 *            declaration to be added
	 * @return true if the import declaration was present, false otherwise
	 */
	boolean removeImportsDeclaration(OWLImportsDeclaration importDeclaration);

	boolean isEmpty();

	Set<OWLAnnotation> getOntologyAnnotations();

	boolean addOntologyAnnotation(OWLAnnotation ann);

	boolean removeOntologyAnnotation(OWLAnnotation ann);

	public void addAxiomsByType(AxiomType<?> type, OWLAxiom axiom);

	public void removeAxiomsByType(AxiomType<?> type, OWLAxiom axiom);

	// 2011.11.17 Map<OWLAxiom, Set<OWLAxiom>>
	// getLogicalAxiom2AnnotatedAxiomMap();

	// 2011.11.23 boolean containsLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax);

	// 2011.11.23 Set<OWLAxiom> getLogicalAxiom2AnnotatedAxiom(OWLAxiom ax);

	boolean containsAxiomIgnoreAnnotations(OWLAxiom ax);

	Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom ax);

	// void addLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax);

	// void removeLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax);

	Set<OWLClassAxiom> getGeneralClassAxioms();

	// 2011.11.21 void addGeneralClassAxioms(OWLClassAxiom ax);

	// 2011.11.21 void removeGeneralClassAxioms(OWLClassAxiom ax);

	Set<OWLSubPropertyChainOfAxiom> getPropertyChainSubPropertyAxioms();

	// 2011.11.07 void
	// addPropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax);

	// void removePropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax);

	// Map<OWLClass, Set<OWLAxiom>> getOwlClassReferences();

	// void removeOwlClassReferences(OWLClass c, OWLAxiom ax);

	// void addOwlClassReferences(OWLClass c, OWLAxiom ax);

	// boolean containsOwlClassReferences(OWLClass c);
	boolean containsOwlClass(OWLClass c);

	// Map<OWLObjectProperty, Set<OWLAxiom>> getOwlObjectPropertyReferences();

	// void addOwlObjectPropertyReferences(OWLObjectProperty p, OWLAxiom ax);

	// void removeOwlObjectPropertyReferences(OWLObjectProperty p, OWLAxiom ax);

	// boolean containsOwlObjectPropertyReferences(OWLObjectProperty c);
	boolean containsOwlObjectProperty(OWLObjectProperty c);

	// Map<OWLDataProperty, Set<OWLAxiom>> getOwlDataPropertyReferences();

	// void removeOwlDataPropertyReferences(OWLDataProperty c, OWLAxiom ax);

	// void addOwlDataPropertyReferences(OWLDataProperty c, OWLAxiom ax);

	// boolean containsOwlDataPropertyReferences(OWLDataProperty c);
	boolean containsOwlDataProperty(OWLDataProperty c);

	// Map<OWLNamedIndividual, Set<OWLAxiom>> getOwlIndividualReferences();

	// void removeOwlIndividualReferences(OWLNamedIndividual c, OWLAxiom ax);

	// void addOwlIndividualReferences(OWLNamedIndividual c, OWLAxiom ax);

	// boolean containsOwlIndividualReferences(OWLNamedIndividual c);

	// hilpold new:
	/**
	 * Determines, if a an individual exists with the given IRI.
	 */
	boolean containsOwlNamedIndividual(IRI individualIRI);

	// ------------------------------------------------------------------------------------
	// OWL_ENTITY BASIC QUERIES
	//

	Set<OWLAnnotationProperty> getOwlAnnotationProperties();

	Set<OWLClass> getOwlClasses();

	Set<OWLDatatype> getOwlDatatypes();

	/**
	 * Returns all NamedIndividuals as a set.
	 */
	Set<OWLNamedIndividual> getOwlNamedIndividuals();

	Set<OWLDataProperty> getOwlDataProperties();

	Set<OWLObjectProperty> getOwlObjectProperties();

	// End

	// 2010.10.26 Map<OWLAnonymousIndividual, Set<OWLAxiom>>
	// getOwlAnonymousIndividualReferences();
	Set<OWLAnonymousIndividual> getOwlAnonymousIndividuals();

	// void removeOwlAnonymousIndividualReferences(OWLAnonymousIndividual c,
	// OWLAxiom ax);
	//
	// void addOwlAnonymousIndividualReferences(OWLAnonymousIndividual c,
	// OWLAxiom ax);

	// boolean containsOwlAnonymousIndividualReferences(OWLAnonymousIndividual
	// c);

	boolean containsOwlAnonymousIndividual(OWLAnonymousIndividual c);

	// Map<OWLDatatype, Set<OWLAxiom>> getOwlDatatypeReferences();

	// void removeOwlDatatypeReferences(OWLDatatype c, OWLAxiom ax);

	// void addOwlDatatypeReferences(OWLDatatype c, OWLAxiom ax);

	// boolean containsOwlDatatypeReferences(OWLDatatype c);
	boolean containsOwlDatatype(OWLDatatype c);

	// Map<OWLAnnotationProperty, Set<OWLAxiom>>
	// getOwlAnnotationPropertyReferences();

	// void removeOwlAnnotationPropertyReferences(OWLAnnotationProperty c,
	// OWLAxiom ax);

	// boolean containsOwlAnnotationPropertyReferences(OWLAnnotationProperty c);
	boolean containsOwlAnnotationProperty(OWLAnnotationProperty c);

	// hilpold Map<OWLEntity, Set<OWLDeclarationAxiom>>
	// getDeclarationsByEntity();

	// void removeDeclarationsByEntity(OWLEntity c, OWLDeclarationAxiom ax);

	// void addDeclarationsByEntity(OWLEntity c, OWLDeclarationAxiom ax);

	// boolean containsDeclarationsByEntity(OWLEntity c);

	// 2011.10.06 Map<OWLClass, Set<OWLClassAxiom>> getClassAxiomsByClass();
	//
	// Map<OWLClass, Set<OWLSubClassOfAxiom>> getSubClassAxiomsByLHS();
	//
	// Map<OWLClass, Set<OWLSubClassOfAxiom>> getSubClassAxiomsByRHS();

	// 2011.10.13 Map<OWLClass, Set<OWLEquivalentClassesAxiom>>
	// getEquivalentClassesAxiomsByClass();

	// 2011.10.13 Map<OWLClass, Set<OWLDisjointClassesAxiom>>
	// getDisjointClassesAxiomsByClass();

	// 2011.10.13 Map<OWLClass, Set<OWLDisjointUnionAxiom>>
	// getDisjointUnionAxiomsByClass();

	// 2011.11.09 Map<OWLClass, Set<OWLHasKeyAxiom>> getHasKeyAxiomsByClass();

	// 2011.10.07 Map<OWLObjectPropertyExpression,
	// Set<OWLSubObjectPropertyOfAxiom>> getObjectSubPropertyAxiomsByLHS();

	// 2011.10.07 Map<OWLObjectPropertyExpression,
	// Set<OWLSubObjectPropertyOfAxiom>> getObjectSubPropertyAxiomsByRHS();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLEquivalentObjectPropertiesAxiom>>
	// getEquivalentObjectPropertyAxiomsByProperty();

	// Map<OWLObjectPropertyExpression, Set<OWLDisjointObjectPropertiesAxiom>>
	// getDisjointObjectPropertyAxiomsByProperty();

	// 2011.10.07 Map<OWLObjectPropertyExpression,
	// Set<OWLObjectPropertyDomainAxiom>>
	// getObjectPropertyDomainAxiomsByProperty();

	// 2011.10.07 Map<OWLObjectPropertyExpression,
	// Set<OWLObjectPropertyRangeAxiom>>
	// getObjectPropertyRangeAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLFunctionalObjectPropertyAxiom>>
	// getFunctionalObjectPropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLInverseFunctionalObjectPropertyAxiom>>
	// getInverseFunctionalPropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLSymmetricObjectPropertyAxiom>>
	// getSymmetricPropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLAsymmetricObjectPropertyAxiom>>
	// getAsymmetricPropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLReflexiveObjectPropertyAxiom>>
	// getReflexivePropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLIrreflexiveObjectPropertyAxiom>>
	// getIrreflexivePropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLTransitiveObjectPropertyAxiom>>
	// getTransitivePropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLObjectPropertyExpression,
	// Set<OWLInverseObjectPropertiesAxiom>>
	// getInversePropertyAxiomsByProperty();

	// 2011.10.07 Map<OWLDataPropertyExpression, Set<OWLSubDataPropertyOfAxiom>>
	// getDataSubPropertyAxiomsByLHS();

	// 2011.10.07 Map<OWLDataPropertyExpression, Set<OWLSubDataPropertyOfAxiom>>
	// getDataSubPropertyAxiomsByRHS();

	// 2011.11.07 Map<OWLDataPropertyExpression,
	// Set<OWLEquivalentDataPropertiesAxiom>>
	// getEquivalentDataPropertyAxiomsByProperty();

	// 2011.11.07 Map<OWLDataPropertyExpression,
	// Set<OWLDisjointDataPropertiesAxiom>>
	// getDisjointDataPropertyAxiomsByProperty();

	// 2011.10.07 Map<OWLDataPropertyExpression,
	// Set<OWLDataPropertyDomainAxiom>> getDataPropertyDomainAxiomsByProperty();

	// 2011.10.07 Map<OWLDataPropertyExpression, Set<OWLDataPropertyRangeAxiom>>
	// getDataPropertyRangeAxiomsByProperty();

	// 2011.11.07 Map<OWLDataPropertyExpression,
	// Set<OWLFunctionalDataPropertyAxiom>>
	// getFunctionalDataPropertyAxiomsByProperty();

	// 2011.11.08 Map<OWLIndividual, Set<OWLClassAssertionAxiom>>
	// getClassAssertionAxiomsByIndividual();

	// 2011.11.08 Map<OWLClassExpression, Set<OWLClassAssertionAxiom>>
	// getClassAssertionAxiomsByClass();

	// 2011.11.08 Map<OWLIndividual, Set<OWLObjectPropertyAssertionAxiom>>
	// getObjectPropertyAssertionsByIndividual();

	// 2011.11.08 Map<OWLIndividual, Set<OWLDataPropertyAssertionAxiom>>
	// getDataPropertyAssertionsByIndividual();

	// 2011.11.08 Map<OWLIndividual,
	// Set<OWLNegativeObjectPropertyAssertionAxiom>>
	// getNegativeObjectPropertyAssertionAxiomsByIndividual();

	// 2011.11.08 Map<OWLIndividual, Set<OWLNegativeDataPropertyAssertionAxiom>>
	// getNegativeDataPropertyAssertionAxiomsByIndividual();

	// 2011.11.08 Map<OWLIndividual, Set<OWLDifferentIndividualsAxiom>>
	// getDifferentIndividualsAxiomsByIndividual();

	// 2011.11.08 Map<OWLIndividual, Set<OWLSameIndividualAxiom>>
	// getSameIndividualsAxiomsByIndividual();

	// 2011.11.14 Map<OWLAnnotationSubject, Set<OWLAnnotationAssertionAxiom>>
	// getAnnotationAssertionAxiomsBySubject();

	boolean isDeclared(OWLDeclarationAxiom ax);

	Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype datatype);

	Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(OWLAnnotationProperty subProperty);

	Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(OWLAnnotationProperty property);

	Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(OWLAnnotationProperty property);

	/**
	 * A convenience method that adds an axiom to a set, but checks that the set
	 * isn't null before the axiom is added. This is needed because many of the
	 * indexing sets are built lazily.
	 * 
	 * @param axiom
	 *            The axiom to be added.
	 * @param axioms
	 *            The set of axioms that the axiom should be added to. May be
	 *            <code>null</code>.
	 */
	// <K extends OWLAxiom> void addAxiomToSet(K axiom, Set<K> axioms);

	// <K extends OWLAxiom> void removeAxiomFromSet(K axiom, Set<K> axioms);

	/**
	 * Adds an axiom to a set contained in a map, which maps some key (e.g. an
	 * entity such as and individual, class etc.) to the set of axioms.
	 * 
	 * @param key
	 *            The key that indexes the set of axioms
	 * @param map
	 *            The map, which maps the key to a set of axioms, to which the
	 *            axiom will be added.
	 * @param axiom
	 *            The axiom to be added
	 */
	// <K, V extends OWLAxiom> void addToIndexedSet(K key, Map<K, Set<V>> map, V
	// axiom);

	/**
	 * Removes an axiom from a set of axioms, which is the value for a specified
	 * key in a specified map.
	 * 
	 * @param key
	 *            The key that indexes the set of axioms.
	 * @param map
	 *            The map, which maps keys to sets of axioms.
	 * @param axiom
	 *            The axiom to remove from the set of axioms.
	 * @param removeSetIfEmpty
	 *            Specifies whether or not the indexed set should be removed
	 *            from the map if it is empty after removing the specified axiom
	 */
	// <K, V extends OWLAxiom> void removeAxiomFromSet(K key, Map<K, Set<V>>
	// map, V axiom, boolean removeSetIfEmpty);

	<E> Set<E> getReturnSet(Set<E> set);

	// <K extends OWLObject, V extends OWLAxiom> Set<V> getAxioms(K key, Map<K,
	// Set<V>> map);

	Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity entity);

	Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual individual);

	HGHandle findAnonymousIndividual(final OWLAnonymousIndividual individual);

	/**
	 * Hilpold.
	 * 
	 * @param entity
	 * @return true, if at least 1 axiom in the ontology exists that refers
	 *         directly or indirectly to the entity.
	 */
	boolean hasReferencingAxioms(OWLEntity entity);

	boolean hasReferencingAxioms(HGHandle entity);

	// TODO
	Set<OWLAxiom> getReferencingAxioms(OWLEntity owlEntity);

	Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass cls);

	Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass cls);

	Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(OWLClass cls);

	Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass cls);

	Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass owlClass);

	Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass cls);

	// Object properties
	Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(OWLObjectPropertyExpression property);

	Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(OWLObjectPropertyExpression property);

	Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(OWLObjectPropertyExpression property);

	Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(OWLObjectPropertyExpression property);

	Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(OWLObjectPropertyExpression property);

	Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(OWLObjectPropertyExpression property);

	Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(OWLObjectPropertyExpression property);

	Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(OWLDataPropertyExpression property);

	Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(OWLDataProperty lhsProperty);

	Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(OWLDataPropertyExpression property);

	Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(OWLDataProperty property);

	Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(OWLDataProperty property);

	Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(OWLDataProperty property);

	Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(OWLDataProperty property);

	Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLIndividual individual);

	Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClassExpression type);

	Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(OWLIndividual individual);

	Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(OWLIndividual individual);

	Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(OWLIndividual individual);

	Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(OWLIndividual individual);

	Set<OWLSameIndividualAxiom> getSameIndividualAxioms(OWLIndividual individual);

	Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(OWLIndividual individual);

	Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxiomsBySubject(OWLAnnotationSubject subject);

	Set<OWLClassAxiom> getOWLClassAxioms(OWLClass cls);

	boolean containsAxiom(OWLAxiom axiom);

	int getAxiomCount();

	Set<OWLAxiom> getAxioms();

	<T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType);

	<T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, Collection<OWLOntology> importsClosure);

	<T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType);

	Set<OWLLogicalAxiom> getLogicalAxioms();

	int getLogicalAxiomCount();

	/**
	 * @param individual
	 * @return
	 */
	Set<OWLIndividualAxiom> getOWLIndividualAxioms(OWLIndividual individual);

	//
	// 2011.12.29 Axioms for various OWLEntities
	//

	/**
	 * Finds defining axioms in incidence Set for the given entity
	 */

	Set<OWLDataPropertyAxiom> getOWLDataPropertyAxioms(OWLDataProperty prop);

	Set<OWLAnnotationAxiom> getOWLAnnotationPropertyAxioms(OWLAnnotationProperty prop);

	Set<OWLObjectPropertyAxiom> getOWLObjectPropertyExpressionAxioms(OWLObjectPropertyExpression prop);

	// 2012.10.02 Prefixes
	Map<String, String> getPrefixes();

	void setPrefixesFrom(Map<String, String> prefixMap);

	String getPrefix(String prefixName);

	String setPrefix(String prefixName, String prefix);

	String removePrefix(String prefixName);

}

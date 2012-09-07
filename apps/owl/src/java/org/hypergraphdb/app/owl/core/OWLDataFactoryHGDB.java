package org.hypergraphdb.app.owl.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.model.OWLAnonymousIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLDataComplementOfHGDB;
import org.hypergraphdb.app.owl.model.OWLDataIntersectionOfHGDB;
import org.hypergraphdb.app.owl.model.OWLDataOneOfHGDB;
import org.hypergraphdb.app.owl.model.OWLDataUnionOfHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeRestrictionHGDB;
import org.hypergraphdb.app.owl.model.OWLFacetRestrictionHGDB;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectInverseOfHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAnnotationAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAnnotationPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAnnotationPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLAsymmetricObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLClassAssertionHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyAssertionAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyDomainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDataPropertyRangeAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDatatypeDefinitionAxiomHGDB;
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
import org.hypergraphdb.app.owl.model.axioms.OWLSubPropertyChainAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSymmetricObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLTransitiveObjectPropertyAxiomHGDB;
import org.hypergraphdb.app.owl.model.classexpr.OWLObjectComplementOfHGDB;
import org.hypergraphdb.app.owl.model.classexpr.OWLObjectIntersectionOfHGDB;
import org.hypergraphdb.app.owl.model.classexpr.OWLObjectOneOfHGDB;
import org.hypergraphdb.app.owl.model.classexpr.OWLObjectUnionOfHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataAllValuesFromHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataExactCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataHasValueHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataMaxCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataMinCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataSomeValuesFromHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectAllValuesFromHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectExactCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectHasSelfHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectHasValueHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectMaxCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectMinCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLObjectSomeValuesFromHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLBody;
import org.hypergraphdb.app.owl.model.swrl.SWRLBuiltInAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLClassAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLDataPropertyAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLDataRangeAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLDifferentIndividualsAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLHead;
import org.hypergraphdb.app.owl.model.swrl.SWRLIndividualArgumentHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLLiteralArgumentHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLObjectPropertyAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLRuleHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLSameIndividualAtomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLVariableHGDB;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * OWLDataFactoryHGDB.
 * 
 * All Axioms are added to the graph after adding them to an ontology.
 * All other items are added to the graph in this datafactory and might never be part of an axiom that gets added to an ontology.
 *
 * Cleanup considerations: <br>
 * Sophisticated cleanup is needed to remove atoms that are not part of an ontology, if API users decide 
 * to create objects without adding them to an ontology. <br> 
 * Cleanup has to take into account that an API user (editor) might keep objects/atoms that are existentially dependent on an axiom after removing 
 * an axiom in a REDO stack or reuses them for other purposes later.
 * In such a situation the axiom will not be part of the graph anymore but still refer to dependent objects in the graph. 
 * 
 * IRIs as used for Annotations need to be cleaned up too. 
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 28, 2011
 */
public class OWLDataFactoryHGDB implements OWLDataFactory {

	public static boolean DBG = true;
	
	private static OWLDataFactoryHGDB instance = new OWLDataFactoryHGDB();

	//private static OWLClass OWL_THING = new OWLClassHGDB(OWLRDFVocabulary.OWL_THING.getIRI());

	//private static OWLClass OWL_NOTHING = new OWLClassHGDB(OWLRDFVocabulary.OWL_NOTHING.getIRI());

	protected OWLDataFactoryInternalsHGDB data;

	private HyperGraph graph;

	public OWLDataFactoryHGDB() {
		data = new OWLDataFactoryInternalsHGDB(this);
	}

	public static OWLDataFactoryHGDB getInstance() {
		return instance;
	}

	public void purge() {
		data.purge();
	}

	/**
	 * Gets an entity that has the specified IRI and is of the specified type.
	 * 
	 * @param entityType
	 *            The type of the entity that will be returned
	 * @param iri
	 *            The IRI of the entity that will be returned
	 * @return An entity that has the same IRI as this entity and is of the
	 *         specified type
	 */
	@SuppressWarnings("unchecked")
	public <E extends OWLEntity> E getOWLEntity(EntityType<E> entityType, IRI iri) {
		E ret = null;
		if (entityType.equals(EntityType.CLASS)) {
			ret = (E) getOWLClass(iri);
		} else if (entityType.equals(EntityType.OBJECT_PROPERTY)) {
			ret = (E) getOWLObjectProperty(iri);
		} else if (entityType.equals(EntityType.DATA_PROPERTY)) {
			ret = (E) getOWLDataProperty(iri);
		} else if (entityType.equals(EntityType.ANNOTATION_PROPERTY)) {
			ret = (E) getOWLAnnotationProperty(iri);
		} else if (entityType.equals(EntityType.NAMED_INDIVIDUAL)) {
			ret = (E) getOWLNamedIndividual(iri);
		} else if (entityType.equals(EntityType.DATATYPE)) {
			ret = (E) getOWLDatatype(iri);
		}
		return ret;
	}

	public OWLClass getOWLClass(IRI iri) {
		return data.getOWLClass(iri);
	}

	public OWLClass getOWLClass(String iri, PrefixManager prefixManager) {
		return getOWLClass(prefixManager.getIRI(iri));
	}

	public OWLAnnotationProperty getOWLAnnotationProperty(String abbreviatedIRI, PrefixManager prefixManager) {
		return getOWLAnnotationProperty(prefixManager.getIRI(abbreviatedIRI));
	}

	public OWLAnnotationProperty getRDFSLabel() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
	}

	public OWLAnnotationProperty getRDFSComment() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
	}

	public OWLAnnotationProperty getRDFSSeeAlso() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_SEE_ALSO.getIRI());
	}

	public OWLAnnotationProperty getRDFSIsDefinedBy() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_IS_DEFINED_BY.getIRI());
	}

	public OWLAnnotationProperty getOWLVersionInfo() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.OWL_VERSION_INFO.getIRI());
	}

	public OWLAnnotationProperty getOWLBackwardCompatibleWith() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.OWL_BACKWARD_COMPATIBLE_WITH.getIRI());
	}

	public OWLAnnotationProperty getOWLIncompatibleWith() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.OWL_INCOMPATIBLE_WITH.getIRI());
	}

	public OWLAnnotationProperty getOWLDeprecated() {
		return getOWLAnnotationProperty(OWLRDFVocabulary.OWL_DEPRECATED.getIRI());
	}

	public OWLDatatype getOWLDatatype(String abbreviatedIRI, PrefixManager prefixManager) {
		return getOWLDatatype(prefixManager.getIRI(abbreviatedIRI));
	}

	public OWLClass getOWLThing() {
		return getOWLClass(OWLRDFVocabulary.OWL_THING.getIRI());
		//return OWL_THING;
	}

	public OWLClass getOWLNothing() {
		return getOWLClass(OWLRDFVocabulary.OWL_NOTHING.getIRI());
		//return OWL_NOTHING;
	}

	public OWLDataProperty getOWLBottomDataProperty() {
		return getOWLDataProperty(OWLRDFVocabulary.OWL_BOTTOM_DATA_PROPERTY.getIRI());
	}

	public OWLObjectProperty getOWLBottomObjectProperty() {
		return getOWLObjectProperty(OWLRDFVocabulary.OWL_BOTTOM_OBJECT_PROPERTY.getIRI());
	}

	public OWLDataProperty getOWLTopDataProperty() {
		return getOWLDataProperty(OWLRDFVocabulary.OWL_TOP_DATA_PROPERTY.getIRI());
	}

	public OWLObjectProperty getOWLTopObjectProperty() {
		return getOWLObjectProperty(OWLRDFVocabulary.OWL_TOP_OBJECT_PROPERTY.getIRI());
	}

	public OWLDatatype getTopDatatype() {
		return getOWLDatatype(OWLRDFVocabulary.RDFS_LITERAL.getIRI());
	}

	public OWLDatatype getIntegerOWLDatatype() {
		return getOWLDatatype(XSDVocabulary.INTEGER.getIRI());
	}

	public OWLDatatype getFloatOWLDatatype() {
		return getOWLDatatype(XSDVocabulary.FLOAT.getIRI());
	}

	public OWLDatatype getDoubleOWLDatatype() {
		return getOWLDatatype(XSDVocabulary.DOUBLE.getIRI());
	}

	public OWLDatatype getBooleanOWLDatatype() {
		return getOWLDatatype(XSDVocabulary.BOOLEAN.getIRI());
	}

	public OWLDatatype getRDFPlainLiteral() {
		return getOWLDatatype(OWLRDFVocabulary.RDF_PLAIN_LITERAL.getIRI());
	}

	public OWLObjectProperty getOWLObjectProperty(IRI iri) {
		return data.getOWLObjectProperty(iri);
	}

	public OWLDataProperty getOWLDataProperty(IRI iri) {
		return data.getOWLDataProperty(iri);
	}

	public OWLNamedIndividual getOWLNamedIndividual(IRI iri) {
		return data.getOWLNamedIndividual(iri);
	}

	public OWLDataProperty getOWLDataProperty(String curi, PrefixManager prefixManager) {
		return getOWLDataProperty(prefixManager.getIRI(curi));
	}

	public OWLNamedIndividual getOWLNamedIndividual(String curi, PrefixManager prefixManager) {
		return getOWLNamedIndividual(prefixManager.getIRI(curi));
	}

	public OWLObjectProperty getOWLObjectProperty(String curi, PrefixManager prefixManager) {
		return getOWLObjectProperty(prefixManager.getIRI(curi));
	}

	public OWLAnonymousIndividual getOWLAnonymousIndividual(String id) {
		if (id == null) {
			throw new NullPointerException("ID for anonymous individual is null");
		}
		// we can not try to find the individual by id, because we do not know, which Ontology he belongs to.
		// therefore we do not know the context in which the id will be valid.
		//TODO Think about adding to graph again? 
		// we could ensure here that NodeId gets initialized to max(ID) + 1, et.c.
		OWLAnonymousIndividual i = new OWLAnonymousIndividualHGDB(NodeID.getNodeID(id));
		graph.add(i);
		return i; 
	}

	/**
	 * Gets an anonymous individual. The node ID for the individual will be
	 * generated automatically
	 * 
	 * @return The anonymous individual
	 */
	public OWLAnonymousIndividual getOWLAnonymousIndividual() {
		//TODO we could ensure here that NodeId gets initialized to max(ID) + 1, et.c.
		OWLAnonymousIndividual i = new OWLAnonymousIndividualHGDB(NodeID.getNodeID());
		graph.add(i);
		return i;
		//return new OWLAnonymousIndividualImpl(this, NodeID.getNodeID());
	}

	public OWLDatatype getOWLDatatype(IRI iri) {
		return data.getOWLDatatype(iri);
	}

	public OWLLiteralHGDB getOWLLiteral(final String lexicalValue, final String lang, final HGHandle datatype) {
		return graph.getTransactionManager().ensureTransaction(new Callable<OWLLiteralHGDB> () {
			public OWLLiteralHGDB call() {
			HGHandle hliteral = data.lookupLiteral
					.var("literal", lexicalValue)
					.var("lang", lang)
					.var("datatype", datatype).findOne();
			if (hliteral == null) {			
				OWLLiteralHGDB l = new OWLLiteralHGDB(lexicalValue, lang, datatype); 
				graph.add(l);
				return l;
			}
			else
				return graph.get(hliteral);
		}}, HGTransactionConfig.WRITE_UPGRADABLE);
	}
	
	public OWLLiteralHGDB getOWLLiteral(String lexicalValue, OWLDatatype datatype) {
		if (datatype.isRDFPlainLiteral()) {
			int sep = lexicalValue.lastIndexOf('@');
			if (sep != -1) {
				String lex = lexicalValue.substring(0, sep);
				String lang = lexicalValue.substring(sep + 1);
				return getOWLLiteral(lex, lang, graph.getHandle(getRDFPlainLiteral()));
			} else {
				return getOWLLiteral(lexicalValue, "", graph.getHandle(datatype));
			}
		} else {
			return getOWLLiteral(lexicalValue, "", graph.getHandle(datatype));
		}
	}

	public OWLLiteral getOWLLiteral(String lexicalValue, OWL2Datatype datatype) {
		return getOWLLiteral(lexicalValue, getOWLDatatype(datatype.getIRI()));
	}

	public OWLLiteral getOWLLiteral(int value) {
		return getOWLLiteral(Integer.toString(value), "", graph.getHandle(getOWLDatatype(XSDVocabulary.INTEGER.getIRI())));
	}

	public OWLLiteral getOWLLiteral(double value) {
		return getOWLLiteral(Double.toString(value), "", graph.getHandle(getOWLDatatype(XSDVocabulary.DOUBLE.getIRI())));
	}

	public OWLLiteral getOWLLiteral(boolean value) {
		return getOWLLiteral(Boolean.toString(value), "", graph.getHandle(getOWLDatatype(XSDVocabulary.BOOLEAN.getIRI())));
	}

	public OWLLiteral getOWLLiteral(float value) {
		return getOWLLiteral(Float.toString(value), "", graph.getHandle(getOWLDatatype(XSDVocabulary.FLOAT.getIRI())));
	}

	public OWLLiteral getOWLLiteral(String value) {
		return getOWLLiteral(value, "", graph.getHandle(getOWLDatatype(XSDVocabulary.STRING.getIRI())));
	}

	public OWLLiteral getOWLLiteral(String literal, String lang) {
		if (literal == null) {
			throw new NullPointerException("literal argument is null");
		}
		String normalisedLang;
		if (lang == null) {
			normalisedLang = "";
		} else {
			normalisedLang = lang.trim().toLowerCase();
		}
		return getOWLLiteral(literal, normalisedLang, graph.getHandle(getRDFPlainLiteral()));
	}

	/**
	 * @deprecated Use
	 *             {@link #getOWLLiteral(String, org.semanticweb.owlapi.model.OWLDatatype)}
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(String literal, OWLDatatype datatype) {
		return getOWLLiteral(literal, datatype);
	}

	/**
	 * @param literal
	 *            The literal
	 * @param datatype
	 *            The OWL 2 Datatype that will type the literal
	 * @return The typed literal
	 * @deprecated Use
	 *             {@link #getOWLLiteral(String, org.semanticweb.owlapi.vocab.OWL2Datatype)}
	 *             Creates a typed literal that has the specified OWL 2 Datatype
	 *             as its datatype
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(String literal, OWL2Datatype datatype) {
		return getOWLLiteral(literal, datatype);
	}

	/**
	 * @param value
	 *            The value of the literal
	 * @return An <code>OWLTypedConstant</code> whose literal is the lexical
	 *         value of the integer, and whose data type is xsd:integer.
	 * @deprecated Use {@link #getOWLLiteral(int)} Convenience method that
	 *             obtains a literal typed as an integer.
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(int value) {
		return getOWLLiteral(value);
	}

	/**
	 * @param value
	 *            The value of the literal
	 * @return An <code>OWLTypedConstant</code> whose literal is the lexical
	 *         value of the double, and whose data type is xsd:double.
	 * @deprecated Use {@link #getOWLLiteral(double)} Convenience method that
	 *             obtains a literal typed as a double.
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(double value) {
		return getOWLLiteral(value);
	}

	/**
	 * @param value
	 *            The value of the literal
	 * @return An <code>OWLTypedConstant</code> whose literal is the lexical
	 *         value of the boolean, and whose data type is xsd:boolean.
	 * @deprecated Use {@link #getOWLLiteral(boolean)} Convenience method that
	 *             obtains a literal typed as a boolean.
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(boolean value) {
		return getOWLLiteral(value);
	}

	/**
	 * @param value
	 *            The value of the literal
	 * @return An <code>OWLTypedConstant</code> whose literal is the lexical
	 *         value of the float, and whose data type is xsd:float.
	 * @deprecated Use {@link #getOWLLiteral(float)} Convenience method that
	 *             obtains a literal typed as a float.
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(float value) {
		return getOWLLiteral(value);
	}

	/**
	 * @param value
	 *            The value of the literal
	 * @return An <code>OWLTypedConstant</code> whose literal is the lexical
	 *         value of the string, and whose data type is xsd:string.
	 * @deprecated Use {@link #getOWLLiteral(String)} Convenience method that
	 *             obtains a literal typed as a string.
	 */
	@Deprecated
	public OWLLiteral getOWLTypedLiteral(String value) {
		return getOWLLiteral(value);
	}

	/**
	 * @param literal
	 *            The string literal
	 * @param lang
	 *            The language tag. The tag is formed according to <a
	 *            href="http://www.rfc-editor.org/rfc/bcp/bcp47.txt">BCP47</a>
	 *            but the OWL API will not check that the tag conforms to this
	 *            specification - it is up to the caller to ensure this. For
	 *            backwards compatibility, if the value of lang is
	 *            <code>null</code> then this is equivalent to calling the
	 *            getOWLStringLiteral(String literal) method.
	 * @return The OWLStringLiteral that represents the string literal with a
	 *         language tag.
	 * @deprecated Use {@link #getOWLLiteral(String, String)} Gets an
	 *             OWLStringLiteral with a language tag.
	 */
	@Deprecated
	public OWLLiteral getOWLStringLiteral(String literal, String lang) {
		return getOWLLiteral(literal, lang);
	}

	/**
	 * @param literal
	 *            The string literal
	 * @return The string literal for the specfied string
	 * @deprecated Use {@link #getOWLLiteral(String, String)} with the second
	 *             parameter as the empty string (""). Gets a string literal
	 *             without a language tag.
	 */
	@Deprecated
	public OWLLiteral getOWLStringLiteral(String literal) {
		return getOWLLiteral(literal, "");
	}

	public OWLDataOneOf getOWLDataOneOf(Set<? extends OWLLiteral> values) {
		Set<HGHandle> valueHandles = getHandlesSetFor(values);
		OWLDataOneOfHGDB d = new OWLDataOneOfHGDB(valueHandles);
		graph.add(d);
		return d;
	}

	public OWLDataOneOf getOWLDataOneOf(OWLLiteral... values) {
		return getOWLDataOneOf(CollectionFactory.createSet(values));
	}

	public OWLDataComplementOf getOWLDataComplementOf(OWLDataRange dataRange) {
		if (dataRange == null)
			throw new IllegalArgumentException("dataRange null");
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		if (dataRangeHandle == null)
			throw new IllegalStateException("dataRangeHandle null");
		OWLDataComplementOfHGDB i = new OWLDataComplementOfHGDB(dataRangeHandle);
		graph.add(i);
		return i;
		//return new OWLDataComplementOfImpl(this, dataRange);
	}

	public OWLDataIntersectionOf getOWLDataIntersectionOf(OWLDataRange... dataRanges) {
		return getOWLDataIntersectionOf(CollectionFactory.createSet(dataRanges));
		//return getOWLDataIntersectionOf(CollectionFactory.createSet(dataRanges));
	}

	public OWLDataIntersectionOf getOWLDataIntersectionOf(Set<? extends OWLDataRange> dataRanges) {
		Set<HGHandle> dataRangesHandles = getHandlesSetFor(dataRanges);
		OWLDataIntersectionOfHGDB o = new OWLDataIntersectionOfHGDB(dataRangesHandles);
		graph.add(o);
		return o;
		//return new OWLDataIntersectionOfImpl(this, dataRanges);
	}

	public OWLDataUnionOf getOWLDataUnionOf(OWLDataRange... dataRanges) {
		return getOWLDataUnionOf(CollectionFactory.createSet(dataRanges));
	}

	public OWLDataUnionOf getOWLDataUnionOf(Set<? extends OWLDataRange> dataRanges) {
		Set<HGHandle> dataRangesHandles = getHandlesSetFor(CollectionFactory.createSet(dataRanges));
		OWLDataUnionOfHGDB o = new OWLDataUnionOfHGDB(dataRangesHandles);
		graph.add(o);
		return o;
		//return new OWLDataUnionOfImpl(this, dataRanges);
	}

	public OWLDatatypeRestriction getOWLDatatypeRestriction(OWLDatatype datatype, Set<OWLFacetRestriction> facets) {
		HGHandle dataTypeHandle = getOrFindOWLEntityHandleInGraph(datatype);
		Set<HGHandle> facetsHandles = getHandlesSetFor(facets);
		OWLDatatypeRestriction o = new OWLDatatypeRestrictionHGDB(dataTypeHandle, facetsHandles);
		graph.add(o);
		return o;
		//return new OWLDatatypeRestrictionImpl(this, datatype, facets);
	}

	public OWLDatatypeRestriction getOWLDatatypeRestriction(OWLDatatype datatype, OWLFacet facet,
			OWLLiteral typedConstant) {
		HGHandle dataTypeHandle = getOrFindOWLEntityHandleInGraph(datatype);
		OWLFacetRestriction facetRestriction = getOWLFacetRestriction(facet, typedConstant); 
		HGHandle facetRestrictionHandle = graph.getHandle(facetRestriction); 
		OWLDatatypeRestriction o = new OWLDatatypeRestrictionHGDB(dataTypeHandle, Collections.singleton(facetRestrictionHandle));
		graph.add(o);
		return o;

		
		//return new OWLDatatypeRestrictionImpl(this, datatype, Collections.singleton(getOWLFacetRestriction(facet,
				//typedConstant)));
	}

	public OWLDatatypeRestriction getOWLDatatypeRestriction(OWLDatatype dataRange,
			OWLFacetRestriction... facetRestrictions) {
		return getOWLDatatypeRestriction(dataRange, CollectionFactory.createSet(facetRestrictions));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinInclusiveRestriction(int minInclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MIN_INCLUSIVE, getOWLLiteral(minInclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMaxInclusiveRestriction(int maxInclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MAX_INCLUSIVE, getOWLLiteral(maxInclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinMaxInclusiveRestriction(int minInclusive, int maxInclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(),
				getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, getOWLLiteral(minInclusive)),
				getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, maxInclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinExclusiveRestriction(int minExclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MIN_EXCLUSIVE, getOWLLiteral(minExclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMaxExclusiveRestriction(int maxExclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MAX_EXCLUSIVE, getOWLLiteral(maxExclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinMaxExclusiveRestriction(int minExclusive, int maxExclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(),
				getOWLFacetRestriction(OWLFacet.MIN_EXCLUSIVE, getOWLLiteral(minExclusive)),
				getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, maxExclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinInclusiveRestriction(double minInclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MIN_INCLUSIVE, getOWLLiteral(minInclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMaxInclusiveRestriction(double maxInclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MAX_INCLUSIVE, getOWLLiteral(maxInclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinMaxInclusiveRestriction(double minInclusive, double maxInclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(),
				getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, getOWLLiteral(minInclusive)),
				getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, maxInclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinExclusiveRestriction(double minExclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MIN_EXCLUSIVE, getOWLLiteral(minExclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMaxExclusiveRestriction(double maxExclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(), OWLFacet.MAX_EXCLUSIVE, getOWLLiteral(maxExclusive));
	}

	public OWLDatatypeRestriction getOWLDatatypeMinMaxExclusiveRestriction(double minExclusive, double maxExclusive) {
		return getOWLDatatypeRestriction(getIntegerOWLDatatype(),
				getOWLFacetRestriction(OWLFacet.MIN_EXCLUSIVE, getOWLLiteral(minExclusive)),
				getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, maxExclusive));
	}

	public OWLFacetRestriction getOWLFacetRestriction(OWLFacet facet, int facetValue) {
		return getOWLFacetRestriction(facet, getOWLLiteral(facetValue));
	}

	public OWLFacetRestriction getOWLFacetRestriction(OWLFacet facet, double facetValue) {
		return getOWLFacetRestriction(facet, getOWLLiteral(facetValue));
	}

	public OWLFacetRestriction getOWLFacetRestriction(OWLFacet facet, float facetValue) {
		return getOWLFacetRestriction(facet, getOWLLiteral(facetValue));
	}

	public OWLFacetRestriction getOWLFacetRestriction(OWLFacet facet, OWLLiteral facetValue) {
		HGHandle facetValueHandle = graph.getHandle(facetValue);
		OWLFacetRestriction o = new OWLFacetRestrictionHGDB(facet, facetValueHandle);
		graph.add(o);
		return o;
		//return new OWLFacetRestrictionImpl(this, facet, facetValue);
	}

	public OWLObjectIntersectionOf getOWLObjectIntersectionOf(Set<? extends OWLClassExpression> operands) {
		Set<HGHandle> operandHandles = getHandlesSetFor(operands);
		OWLObjectIntersectionOfHGDB o = new OWLObjectIntersectionOfHGDB(operandHandles);
		graph.add(o);
		return o;
		// return new OWLObjectIntersectionOfImpl(this, operands);
	}

	public OWLObjectIntersectionOf getOWLObjectIntersectionOf(OWLClassExpression... operands) {
		return getOWLObjectIntersectionOf(CollectionFactory.createSet(operands));
	}

	public OWLDataAllValuesFrom getOWLDataAllValuesFrom(OWLDataPropertyExpression property, OWLDataRange dataRange) {
		if (dataRange == null) {
			throw new NullPointerException("The filler of the restriction (dataRange) must not be null");
		}
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataAllValuesFrom o = new OWLDataAllValuesFromHGDB(propertyHandle, dataRangeHandle);
		graph.add(o);
		return o;
		// return new OWLDataAllValuesFromImpl(this, property, dataRange);
	}

	public OWLDataExactCardinality getOWLDataExactCardinality(int cardinality, OWLDataPropertyExpression property) {
		HGHandle topDataTypeHandle = graph.getHandle(getTopDatatype());
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataExactCardinality o = new OWLDataExactCardinalityHGDB(propertyHandle, cardinality, topDataTypeHandle);
		graph.add(o);
		return o;
		// return new OWLDataExactCardinalityImpl(this, property, cardinality,
		// getTopDatatype());
	}

	public OWLDataExactCardinality getOWLDataExactCardinality(int cardinality, OWLDataPropertyExpression property,
			OWLDataRange dataRange) {
		if (dataRange == null) {
			throw new NullPointerException("The filler of the restriction (dataRange) must not be null");
		}
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataExactCardinality o = new OWLDataExactCardinalityHGDB(propertyHandle, cardinality, dataRangeHandle);
		graph.add(o);
		return o;
		// return new OWLDataExactCardinalityImpl(this, property, cardinality,
		// dataRange);
	}

	public OWLDataMaxCardinality getOWLDataMaxCardinality(int cardinality, OWLDataPropertyExpression property) {
		HGHandle topDataTypeHandle = graph.getHandle(getTopDatatype());
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataMaxCardinality o = new OWLDataMaxCardinalityHGDB(propertyHandle, cardinality, topDataTypeHandle);
		graph.add(o);
		return o;
		// return new OWLDataMaxCardinalityImpl(this, property, cardinality,
		// getTopDatatype());
	}

	public OWLDataMaxCardinality getOWLDataMaxCardinality(int cardinality, OWLDataPropertyExpression property,
			OWLDataRange dataRange) {
		if (dataRange == null) {
			throw new NullPointerException("The filler of the restriction (dataRange) must not be null");
		}
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataMaxCardinality o = new OWLDataMaxCardinalityHGDB(propertyHandle, cardinality, dataRangeHandle);
		graph.add(o);
		return o;
		// return new OWLDataMaxCardinalityImpl(this, property, cardinality,
		// dataRange);
	}

	public OWLDataMinCardinality getOWLDataMinCardinality(int cardinality, OWLDataPropertyExpression property) {
		HGHandle topDataTypeHandle = graph.getHandle(getTopDatatype());
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataMinCardinality o = new OWLDataMinCardinalityHGDB(propertyHandle, cardinality, topDataTypeHandle);
		graph.add(o);
		return o;
		// return new OWLDataMinCardinalityImpl(this, property, cardinality,
		// getTopDatatype());
	}

	public OWLDataMinCardinality getOWLDataMinCardinality(int cardinality, OWLDataPropertyExpression property,
			OWLDataRange dataRange) {
		if (dataRange == null) {
			throw new NullPointerException("The filler of the restriction (dataRange) must not be null");
		}
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataMinCardinality o = new OWLDataMinCardinalityHGDB(propertyHandle, cardinality, dataRangeHandle);
		graph.add(o);
		return o;
		// return new OWLDataMinCardinalityImpl(this, property, cardinality,
		// dataRange);
	}

	public OWLDataSomeValuesFrom getOWLDataSomeValuesFrom(OWLDataPropertyExpression property, OWLDataRange dataRange) {
		if (dataRange == null) {
			throw new NullPointerException("The filler of the restriction (dataRange) must not be null");
		}
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		HGHandle propertyHandle = graph.getHandle(property);
		OWLDataSomeValuesFrom o = new OWLDataSomeValuesFromHGDB(propertyHandle, dataRangeHandle);
		graph.add(o);
		return o;
		// return new OWLDataSomeValuesFromImpl(this, property, dataRange);
	}

	public OWLDataHasValue getOWLDataHasValue(OWLDataPropertyExpression property, OWLLiteral value) {
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle valueHandle = graph.getHandle(value);
		OWLDataHasValue o = new OWLDataHasValueHGDB(propertyHandle, valueHandle);
		graph.add(o);
		return o;
		// return new OWLDataHasValueImpl(this, property, value);
	}

	public OWLObjectComplementOf getOWLObjectComplementOf(OWLClassExpression operand) {
		HGHandle operandHandle = graph.getHandle(operand);
		OWLObjectComplementOfHGDB o = new OWLObjectComplementOfHGDB(operandHandle);
		graph.add(o);
		return o;
		// TODO consider: shall this ever become part of an ontology? Flyweight?
		// existency dependent on Axiom?
		// return new OWLObjectComplementOfImpl(this, operand);
	}

	public OWLObjectAllValuesFrom getOWLObjectAllValuesFrom(OWLObjectPropertyExpression property,
			OWLClassExpression classExpression) {
		if (classExpression == null) {
			throw new NullPointerException("The filler of the restriction (classExpression) must not be null");
		}
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		HGHandle propertyHandle = graph.getHandle(property);
		OWLObjectAllValuesFrom o = new OWLObjectAllValuesFromHGDB(propertyHandle, classExpressionHandle);
		graph.add(o);
		return o;
		// return new OWLObjectAllValuesFromImpl(this, property,
		// classExpression);
	}

	public OWLObjectOneOf getOWLObjectOneOf(Set<? extends OWLIndividual> values) {
		Set<HGHandle> individualHandles = getHandlesSetFor(values);
		OWLObjectOneOfHGDB o = new OWLObjectOneOfHGDB(individualHandles);
		graph.add(o);
		return o;
		// return new OWLObjectOneOfImpl(this, values);
	}

	public OWLObjectOneOf getOWLObjectOneOf(OWLIndividual... individuals) {
		Set<OWLIndividual> individualsSet = CollectionFactory.createSet(individuals);
		return getOWLObjectOneOf(individualsSet);
	}

	public OWLObjectExactCardinality getOWLObjectExactCardinality(int cardinality, OWLObjectPropertyExpression property) {
		HGHandle OWL_THING_Handle = graph.getHandle(getOWLThing());
		HGHandle propertyHandle = graph.getHandle(property);
		OWLObjectExactCardinality o = new OWLObjectExactCardinalityHGDB(propertyHandle, cardinality, OWL_THING_Handle);
		graph.add(o);
		return o;
		// return new OWLObjectExactCardinalityImpl(this, property, cardinality,
		// OWL_THING);
	}

	public OWLObjectExactCardinality getOWLObjectExactCardinality(int cardinality,
			OWLObjectPropertyExpression property, OWLClassExpression classExpression) {
		if (classExpression == null) {
			throw new NullPointerException("The filler of the restriction (classExpression) must not be null");
		}
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		OWLObjectExactCardinality o = new OWLObjectExactCardinalityHGDB(propertyHandle, cardinality,
				classExpressionHandle);
		graph.add(o);
		return o;
		// return new OWLObjectExactCardinalityImpl(this, property, cardinality,
		// classExpression);
	}

	public OWLObjectMinCardinality getOWLObjectMinCardinality(int cardinality, OWLObjectPropertyExpression property) {
		HGHandle OWL_THING_Handle = graph.getHandle(getOWLThing());
		HGHandle propertyHandle = graph.getHandle(property);
		OWLObjectMinCardinality o = new OWLObjectMinCardinalityHGDB(propertyHandle, cardinality, OWL_THING_Handle);
		graph.add(o);
		return o;
		// return new OWLObjectMinCardinalityImpl(this, property, cardinality,
		// OWL_THING);
	}

	public OWLObjectMinCardinality getOWLObjectMinCardinality(int cardinality, OWLObjectPropertyExpression property,
			OWLClassExpression classExpression) {
		if (classExpression == null) {
			throw new NullPointerException("The filler of the restriction (classExpression) must not be null");
		}
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		OWLObjectMinCardinality o = new OWLObjectMinCardinalityHGDB(propertyHandle, cardinality, classExpressionHandle);
		graph.add(o);
		return o;
		// return new OWLObjectMinCardinalityImpl(this, property, cardinality,
		// classExpression);
	}

	public OWLObjectMaxCardinality getOWLObjectMaxCardinality(int cardinality, OWLObjectPropertyExpression property) {
		HGHandle OWL_THING_Handle = graph.getHandle(getOWLThing());
		HGHandle propertyHandle = graph.getHandle(property);
		OWLObjectMaxCardinality o = new OWLObjectMaxCardinalityHGDB(propertyHandle, cardinality, OWL_THING_Handle);
		graph.add(o);
		return o;
		// return new OWLObjectMaxCardinalityImpl(this, property, cardinality,
		// OWL_THING);
	}

	public OWLObjectMaxCardinality getOWLObjectMaxCardinality(int cardinality, OWLObjectPropertyExpression property,
			OWLClassExpression classExpression) {
		if (classExpression == null) {
			throw new NullPointerException("The filler of the restriction (classExpression) must not be null");
		}
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		OWLObjectMaxCardinality o = new OWLObjectMaxCardinalityHGDB(propertyHandle, cardinality, classExpressionHandle);
		graph.add(o);
		return o;
		// return new OWLObjectMaxCardinalityImpl(this, property, cardinality,
		// classExpression);
	}

	public OWLObjectHasSelf getOWLObjectHasSelf(OWLObjectPropertyExpression property) {
		HGHandle propertyHandle = graph.getHandle(property);
		OWLObjectHasSelf o = new OWLObjectHasSelfHGDB(propertyHandle);
		graph.add(o);
		return o;
		// return new OWLObjectHasSelfImpl(this, property);
	}

	public OWLObjectSomeValuesFrom getOWLObjectSomeValuesFrom(OWLObjectPropertyExpression property,
			OWLClassExpression classExpression) {
		if (classExpression == null) {
			throw new NullPointerException("The filler of the restriction (classExpression) must not be null");
		}
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		OWLObjectSomeValuesFrom o = new OWLObjectSomeValuesFromHGDB(propertyHandle, classExpressionHandle);
		graph.add(o);
		return o;
		// return new OWLObjectSomeValuesFromImpl(this, property,
		// classExpression);
	}

	public OWLObjectHasValue getOWLObjectHasValue(OWLObjectPropertyExpression property, OWLIndividual individual) {
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle individualHandle = graph.getHandle(individual);
		OWLObjectHasValue o = new OWLObjectHasValueHGDB(propertyHandle, individualHandle);
		graph.add(o);
		return o;
		// return new OWLObjectHasValueImpl(this, property, individual);
	}

	public OWLObjectUnionOf getOWLObjectUnionOf(Set<? extends OWLClassExpression> operands) {
		Set<HGHandle> operandHandles = getHandlesSetFor(operands);
		OWLObjectUnionOfHGDB o = new OWLObjectUnionOfHGDB(operandHandles);
		graph.add(o);
		return o;
		// return new OWLObjectUnionOfImpl(this, operands);
	}

	public OWLObjectUnionOf getOWLObjectUnionOf(OWLClassExpression... operands) {
		return getOWLObjectUnionOf(CollectionFactory.createSet(operands));
	}

	public OWLAsymmetricObjectPropertyAxiom getOWLAsymmetricObjectPropertyAxiom(
			OWLObjectPropertyExpression propertyExpression, Set<? extends OWLAnnotation> annotations) {
		if (propertyExpression == null) throw new IllegalArgumentException("propertyExpression null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLAsymmetricObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(propertyExpression);
		axiom = new OWLAsymmetricObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;	
		//return new OWLAsymmetricObjectPropertyAxiomImpl(this, propertyExpression, annotations);
	}

	public OWLAsymmetricObjectPropertyAxiom getOWLAsymmetricObjectPropertyAxiom(
			OWLObjectPropertyExpression propertyExpression) {
		if (propertyExpression == null) throw new IllegalArgumentException("propertyExpression null");
		OWLAsymmetricObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(propertyExpression);
		axiom = new OWLAsymmetricObjectPropertyAxiomHGDB(propertyHandle, EMPTY_ANNOTATIONS_SET);
		axiom.setHyperGraph(graph);
		return axiom;	
		///return getOWLAsymmetricObjectPropertyAxiom(propertyExpression, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyDomainAxiom getOWLDataPropertyDomainAxiom(OWLDataPropertyExpression property,
			OWLClassExpression domain, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (domain == null) throw new IllegalArgumentException("domain null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLDataPropertyDomainAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle domainHandle = graph.getHandle(domain);
		axiom = new OWLDataPropertyDomainAxiomHGDB(propertyHandle, domainHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;			
		//return new OWLDataPropertyDomainAxiomImpl(this, property, domain, annotations);
	}

	public OWLDataPropertyDomainAxiom getOWLDataPropertyDomainAxiom(OWLDataPropertyExpression property,
			OWLClassExpression domain) {
		return getOWLDataPropertyDomainAxiom(property, domain, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyRangeAxiom getOWLDataPropertyRangeAxiom(OWLDataPropertyExpression property,
			OWLDataRange owlDataRange, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (owlDataRange == null) throw new IllegalArgumentException("owlDataRange null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLDataPropertyRangeAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle owlDataRangeHandle = graph.getHandle(owlDataRange);
		axiom = new OWLDataPropertyRangeAxiomHGDB(propertyHandle, owlDataRangeHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;					
		//return new OWLDataPropertyRangeAxiomImpl(this, propery, owlDataRange, annotations);
	}

	public OWLDataPropertyRangeAxiom getOWLDataPropertyRangeAxiom(OWLDataPropertyExpression propery,
			OWLDataRange owlDataRange) {
		return getOWLDataPropertyRangeAxiom(propery, owlDataRange, EMPTY_ANNOTATIONS_SET);
	}

	public OWLSubDataPropertyOfAxiom getOWLSubDataPropertyOfAxiom(OWLDataPropertyExpression subProperty,
			OWLDataPropertyExpression superProperty, Set<? extends OWLAnnotation> annotations) {
		if (subProperty == null)
			throw new IllegalArgumentException("subProperty null");
		if (superProperty == null)
			throw new IllegalArgumentException("superProperty null");
		if (annotations == null)
			throw new IllegalArgumentException("annotations null");
		// subClass, superClass and annotations are in Graph, if created by this
		// Datafactory
		OWLSubDataPropertyOfAxiomHGDB axiom;
		// TODO Implement use of OWLObjectPropertyExpression
		HGHandle subPropertyHandle = getOrFindOWLEntityHandleInGraph((OWLDataProperty) subProperty);
		HGHandle superPropertyHandle = getOrFindOWLEntityHandleInGraph((OWLDataProperty) superProperty);
		if (subPropertyHandle == null || superPropertyHandle == null) {
			throw new IllegalStateException("No Handle for subProperty or superProperty");
		}
		axiom = new OWLSubDataPropertyOfAxiomHGDB(subPropertyHandle, superPropertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		// return new OWLSubDataPropertyOfAxiomImpl(this, subProperty,
		// superProperty, annotations);
	}

	public OWLSubDataPropertyOfAxiom getOWLSubDataPropertyOfAxiom(OWLDataPropertyExpression subProperty,
			OWLDataPropertyExpression superProperty) {
		return getOWLSubDataPropertyOfAxiom(subProperty, superProperty, EMPTY_ANNOTATIONS_SET);
	}

	/**
	 * Gets a declaration for an entity
	 * 
	 * @param owlEntity
	 *            The declared entity.
	 * @return The declaration axiom for the specified entity.
	 * @throws NullPointerException
	 *             if owlEntity is <code>null</code>
	 */

	public OWLDeclarationAxiom getOWLDeclarationAxiom(OWLEntity owlEntity) {
		if (owlEntity == null) {
			throw new NullPointerException("owlEntity");
		}
		return getOWLDeclarationAxiom(owlEntity, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDeclarationAxiom getOWLDeclarationAxiom(OWLEntity owlEntity, Set<? extends OWLAnnotation> annotations) {
		if (owlEntity == null) {
			throw new NullPointerException("owlEntity");
		}
		if (annotations == null) {
			throw new NullPointerException("annotations");
		}
		// owlEntity and annotations are in Graph, if created by this
		// Datafactory
		OWLDeclarationAxiomHGDB axiom;
		HGHandle owlEntityHandle = getOrFindOWLEntityHandleInGraph(owlEntity);
		if (owlEntityHandle == null) {
			// TODO HGDBApplication.ensureBuiltInObjects should add all BUILTING
			// Types
			System.out.println("WARNING: Had to create NONHGDB DeclarationAxiom for :" + owlEntity + " class " + owlEntity.getClass());
			// 2010.10.06 not acceptable anymore: return new
			// OWLDeclarationAxiomImpl(this, owlEntity, annotations);
			throw new IllegalStateException("Could not find owlEntity in Cache or store. "
					+ " This occured on builtin entities before after gc was run. "
					+ owlEntity + " Class: " + owlEntity.getClass());
		}
		// hilpold 2011.10.13
		// An equal (see equals()) one might exist, so we must not add a
		// duplicate to the graph here.
		// But we also do not check, if an equal one exists, just as the
		// original implementation.
		// The axiom shall be added to the graph later, when the user emits a
		// applychanges to the ontology.
		// We do not check, whether the axiom already exists here because the
		// actual equals method is complex and also considers annotations.
		axiom = new OWLDeclarationAxiomHGDB(owlEntityHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
	}

	public OWLDifferentIndividualsAxiom getOWLDifferentIndividualsAxiom(Set<? extends OWLIndividual> individuals,
			Set<? extends OWLAnnotation> annotations) {
		if (individuals == null) throw new IllegalArgumentException("individuals null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLDifferentIndividualsAxiomHGDB axiom;
		Set<HGHandle> individualsHandles = getHandlesSetFor(individuals);
		axiom = new OWLDifferentIndividualsAxiomHGDB(individualsHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLDifferentIndividualsAxiomImpl(this, individuals, annotations);
	}

	public OWLDifferentIndividualsAxiom getOWLDifferentIndividualsAxiom(OWLIndividual... individuals) {
		return getOWLDifferentIndividualsAxiom(CollectionFactory.createSet(individuals));
	}

	public OWLDifferentIndividualsAxiom getOWLDifferentIndividualsAxiom(Set<? extends OWLIndividual> individuals) {
		return getOWLDifferentIndividualsAxiom(individuals, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDisjointClassesAxiom getOWLDisjointClassesAxiom(Set<? extends OWLClassExpression> classExpressions,
			Set<? extends OWLAnnotation> annotations) {
		if (classExpressions == null)
			throw new IllegalArgumentException("classExpressions null");
		if (annotations == null)
			throw new IllegalArgumentException("annotations null");
		// classExpressions and annotations are in Graph, if created by this
		// Datafactory
		OWLDisjointClassesAxiomHGDB axiom;
		Set<HGHandle> classExpressionsHandles = getHandlesSetFor(classExpressions);
		axiom = new OWLDisjointClassesAxiomHGDB(classExpressionsHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		// return new OWLDisjointClassesAxiomImpl(this, classExpressions,
		// annotations);
	}

	public OWLDisjointClassesAxiom getOWLDisjointClassesAxiom(Set<? extends OWLClassExpression> classExpressions) {
		return getOWLDisjointClassesAxiom(classExpressions, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDisjointClassesAxiom getOWLDisjointClassesAxiom(OWLClassExpression... classExpressions) {
		Set<OWLClassExpression> clses = new HashSet<OWLClassExpression>();
		clses.addAll(Arrays.asList(classExpressions));
		return getOWLDisjointClassesAxiom(clses);
	}

	public OWLDisjointDataPropertiesAxiom getOWLDisjointDataPropertiesAxiom(
			Set<? extends OWLDataPropertyExpression> properties, Set<? extends OWLAnnotation> annotations) {
		if (properties == null) throw new IllegalArgumentException("properties null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLDisjointDataPropertiesAxiomHGDB axiom;
		Set<HGHandle> propertiesHandles = getHandlesSetFor(properties);
		axiom = new OWLDisjointDataPropertiesAxiomHGDB(propertiesHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLDisjointDataPropertiesAxiomImpl(this, properties, annotations);
	}

	public OWLDisjointDataPropertiesAxiom getOWLDisjointDataPropertiesAxiom(
			Set<? extends OWLDataPropertyExpression> properties) {
		return getOWLDisjointDataPropertiesAxiom(properties, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDisjointDataPropertiesAxiom getOWLDisjointDataPropertiesAxiom(OWLDataPropertyExpression... properties) {
		return getOWLDisjointDataPropertiesAxiom(CollectionFactory.createSet(properties));
	}

	public OWLDisjointObjectPropertiesAxiom getOWLDisjointObjectPropertiesAxiom(
			OWLObjectPropertyExpression... properties) {
		return getOWLDisjointObjectPropertiesAxiom(CollectionFactory.createSet(properties));
	}

	public OWLDisjointObjectPropertiesAxiom getOWLDisjointObjectPropertiesAxiom(
			Set<? extends OWLObjectPropertyExpression> properties) {
		return getOWLDisjointObjectPropertiesAxiom(properties, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDisjointObjectPropertiesAxiom getOWLDisjointObjectPropertiesAxiom(
			Set<? extends OWLObjectPropertyExpression> properties, Set<? extends OWLAnnotation> annotations) {
		if (properties == null) throw new IllegalArgumentException("properties null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLDisjointObjectPropertiesAxiomHGDB axiom;
		Set<HGHandle> propertiesHandles = getHandlesSetFor(properties);
		axiom = new OWLDisjointObjectPropertiesAxiomHGDB(propertiesHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLDisjointObjectPropertiesAxiomImpl(this, properties, annotations);
	}
	
	public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(Set<? extends OWLClassExpression> classExpressions,
			Set<? extends OWLAnnotation> annotations) {
		if (classExpressions == null) throw new IllegalArgumentException("classExpressions null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		// classExpressions and annotations are in Graph, if created by this
		// Datafactory
		OWLEquivalentClassesAxiomHGDB axiom;
		Set<HGHandle> classExpressionsHandles = getHandlesSetFor(classExpressions);
		axiom = new OWLEquivalentClassesAxiomHGDB(classExpressionsHandles, annotations);
		axiom.setHyperGraph(graph); // 2011.10.06 needed now, that we don't add
									// it to the graph right away.
		return axiom;
		// return new OWLEquivalentClassesAxiomImpl(this, classExpressions,
		// annotations);
	}

	public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(OWLClassExpression clsA, OWLClassExpression clsB) {
		return getOWLEquivalentClassesAxiom(clsA, clsB, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(OWLClassExpression clsA, OWLClassExpression clsB,
			Set<? extends OWLAnnotation> annotations) {
		return getOWLEquivalentClassesAxiom(CollectionFactory.createSet(clsA, clsB), annotations);
	}

	public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(OWLClassExpression... classExpressions) {
		Set<OWLClassExpression> clses = new HashSet<OWLClassExpression>();
		clses.addAll(Arrays.asList(classExpressions));
		return getOWLEquivalentClassesAxiom(clses);
	}

	public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom(Set<? extends OWLClassExpression> classExpressions) {
		return getOWLEquivalentClassesAxiom(classExpressions, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentDataPropertiesAxiom getOWLEquivalentDataPropertiesAxiom(
			Set<? extends OWLDataPropertyExpression> properties, Set<? extends OWLAnnotation> annotations) {
		if (properties == null) throw new IllegalArgumentException("properties null");
		OWLEquivalentDataPropertiesAxiomHGDB axiom;
		Set<HGHandle> propertiesHandles = getHandlesSetFor(properties);
		axiom = new OWLEquivalentDataPropertiesAxiomHGDB(propertiesHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLEquivalentDataPropertiesAxiomImpl(this, properties, annotations);
	}

	public OWLEquivalentDataPropertiesAxiom getOWLEquivalentDataPropertiesAxiom(
			Set<? extends OWLDataPropertyExpression> properties) {
		return getOWLEquivalentDataPropertiesAxiom(properties, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentDataPropertiesAxiom getOWLEquivalentDataPropertiesAxiom(OWLDataPropertyExpression propertyA,
			OWLDataPropertyExpression propertyB) {
		return getOWLEquivalentDataPropertiesAxiom(propertyA, propertyB, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentDataPropertiesAxiom getOWLEquivalentDataPropertiesAxiom(OWLDataPropertyExpression propertyA,
			OWLDataPropertyExpression propertyB, Set<? extends OWLAnnotation> annotations) {
		return getOWLEquivalentDataPropertiesAxiom(CollectionFactory.createSet(propertyA, propertyB), annotations);
	}

	public OWLEquivalentDataPropertiesAxiom getOWLEquivalentDataPropertiesAxiom(OWLDataPropertyExpression... properties) {
		return getOWLEquivalentDataPropertiesAxiom(CollectionFactory.createSet(properties));
	}

	public OWLEquivalentObjectPropertiesAxiom getOWLEquivalentObjectPropertiesAxiom(
			OWLObjectPropertyExpression... properties) {
		if (properties == null) throw new IllegalArgumentException("property null");
		OWLEquivalentObjectPropertiesAxiomHGDB axiom;
		Set<HGHandle> propertiesHandles = getHandlesSetFor(CollectionFactory.createSet(properties));
		axiom = new OWLEquivalentObjectPropertiesAxiomHGDB(propertiesHandles, EMPTY_ANNOTATIONS_SET);
		axiom.setHyperGraph(graph);
		return axiom;
		//return getOWLEquivalentObjectPropertiesAxiom(CollectionFactory.createSet(properties));
	}

	public OWLEquivalentObjectPropertiesAxiom getOWLEquivalentObjectPropertiesAxiom(
			Set<? extends OWLObjectPropertyExpression> properties) {
		return getOWLEquivalentObjectPropertiesAxiom(properties, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentObjectPropertiesAxiom getOWLEquivalentObjectPropertiesAxiom(
			OWLObjectPropertyExpression propertyA, OWLObjectPropertyExpression propertyB) {
		return getOWLEquivalentObjectPropertiesAxiom(propertyA, propertyB, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentObjectPropertiesAxiom getOWLEquivalentObjectPropertiesAxiom(
			OWLObjectPropertyExpression propertyA, OWLObjectPropertyExpression propertyB,
			Set<? extends OWLAnnotation> annotations) {
		return getOWLEquivalentObjectPropertiesAxiom(CollectionFactory.createSet(propertyA, propertyB), annotations);
	}

	public OWLFunctionalDataPropertyAxiom getOWLFunctionalDataPropertyAxiom(OWLDataPropertyExpression property,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLFunctionalDataPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLFunctionalDataPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLFunctionalDataPropertyAxiomImpl(this, property, annotations);
	}

	public OWLFunctionalDataPropertyAxiom getOWLFunctionalDataPropertyAxiom(OWLDataPropertyExpression property) {
		return getOWLFunctionalDataPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLFunctionalObjectPropertyAxiom getOWLFunctionalObjectPropertyAxiom(OWLObjectPropertyExpression property,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLFunctionalObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLFunctionalObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLFunctional ObjectPropertyAxiomImpl(this, property, annotations);
	}

	public OWLFunctionalObjectPropertyAxiom getOWLFunctionalObjectPropertyAxiom(OWLObjectPropertyExpression property) {
		return getOWLFunctionalObjectPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLImportsDeclaration getOWLImportsDeclaration(IRI importedOntologyIRI) {
		// TODO create a HGDB type for it, even though it's not needed to have
		// consistency
		// and get rid of all uk.ac types in the graph.
		return new OWLImportsDeclarationImpl(importedOntologyIRI);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, OWLLiteral object, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (subject == null)	throw new IllegalArgumentException("subject null");
		if (object == null)	throw new IllegalArgumentException("object null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLDataPropertyAssertionAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle subjectHandle = graph.getHandle(subject);
		HGHandle objectHandle = graph.getHandle(object);
		if (propertyHandle == null || subjectHandle == null || objectHandle == null) {
			throw new IllegalStateException("No Handle for property, individual AND/OR object.");
		}
		axiom = new OWLDataPropertyAssertionAxiomHGDB(subjectHandle, propertyHandle, objectHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLDataPropertyAssertionAxiomImpl(this, subject, property, object, annotations);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, OWLLiteral object) {
		return getOWLDataPropertyAssertionAxiom(property, subject, object, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, int value) {
		return getOWLDataPropertyAssertionAxiom(property, subject, getOWLLiteral(value), EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, double value) {
		return getOWLDataPropertyAssertionAxiom(property, subject, getOWLLiteral(value), EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, float value) {
		return getOWLDataPropertyAssertionAxiom(property, subject, getOWLLiteral(value), EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, boolean value) {
		return getOWLDataPropertyAssertionAxiom(property, subject, getOWLLiteral(value), EMPTY_ANNOTATIONS_SET);
	}

	public OWLDataPropertyAssertionAxiom getOWLDataPropertyAssertionAxiom(OWLDataPropertyExpression property,
			OWLIndividual subject, String value) {
		return getOWLDataPropertyAssertionAxiom(property, subject, getOWLLiteral(value), EMPTY_ANNOTATIONS_SET);
	}

	public OWLNegativeDataPropertyAssertionAxiom getOWLNegativeDataPropertyAssertionAxiom(
			OWLDataPropertyExpression property, OWLIndividual subject, OWLLiteral object) {
		return getOWLNegativeDataPropertyAssertionAxiom(property, subject, object, EMPTY_ANNOTATIONS_SET);
	}

	public OWLNegativeDataPropertyAssertionAxiom getOWLNegativeDataPropertyAssertionAxiom(
			OWLDataPropertyExpression property, OWLIndividual subject, OWLLiteral object,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (subject == null) throw new IllegalArgumentException("subject null");
		if (object == null) throw new IllegalArgumentException("object null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLNegativeDataPropertyAssertionAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle subjectHandle = graph.getHandle(subject);
		HGHandle objectHandle = graph.getHandle(object);
		if (propertyHandle == null || subjectHandle == null || objectHandle == null) {
			throw new IllegalStateException("No Handle for property, individual AND/OR object.");
		}
		axiom = new OWLNegativeDataPropertyAssertionAxiomHGDB(subjectHandle, propertyHandle, objectHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLNegativeDataPropertyAssertionImplAxiom(this, subject, property, object, annotations);
	}

	public OWLNegativeObjectPropertyAssertionAxiom getOWLNegativeObjectPropertyAssertionAxiom(
			OWLObjectPropertyExpression property, OWLIndividual subject, OWLIndividual object) {
		return getOWLNegativeObjectPropertyAssertionAxiom(property, subject, object, EMPTY_ANNOTATIONS_SET);
	}

	public OWLNegativeObjectPropertyAssertionAxiom getOWLNegativeObjectPropertyAssertionAxiom(
			OWLObjectPropertyExpression property, OWLIndividual subject, OWLIndividual object,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (subject == null)	throw new IllegalArgumentException("subject null");
		if (object == null)	throw new IllegalArgumentException("object null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLNegativeObjectPropertyAssertionAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle subjectHandle = graph.getHandle(subject);
		HGHandle objectHandle = graph.getHandle(object);
		if (propertyHandle == null || subjectHandle == null || objectHandle == null) {
			throw new IllegalStateException("No Handle for property, individual AND/OR object.");
		}
		axiom = new OWLNegativeObjectPropertyAssertionAxiomHGDB(subjectHandle, propertyHandle, objectHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLNegativeObjectPropertyAssertionAxiomImpl(this, subject, property, object, annotations);
	}

	public OWLObjectPropertyAssertionAxiom getOWLObjectPropertyAssertionAxiom(OWLObjectPropertyExpression property,
			OWLIndividual individual, OWLIndividual object) {
		return getOWLObjectPropertyAssertionAxiom(property, individual, object, EMPTY_ANNOTATIONS_SET);
	}

	public OWLClassAssertionAxiom getOWLClassAssertionAxiom(OWLClassExpression classExpression, OWLIndividual individual) {
		return getOWLClassAssertionAxiom(classExpression, individual, EMPTY_ANNOTATIONS_SET);
	}

	public OWLClassAssertionAxiom getOWLClassAssertionAxiom(OWLClassExpression classExpression,
			OWLIndividual individual, Set<? extends OWLAnnotation> annotations) {
		if (classExpression == null) throw new IllegalArgumentException("classExpression null");
		if (individual == null) throw new IllegalArgumentException("individual null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLClassAssertionHGDB axiom;
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		HGHandle individualHandle = graph.getHandle(individual);
		axiom = new OWLClassAssertionHGDB(individualHandle, classExpressionHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;				
		//return new OWLClassAssertionImpl(this, individual, classExpression, annotations);
	}

	public OWLInverseFunctionalObjectPropertyAxiom getOWLInverseFunctionalObjectPropertyAxiom(
			OWLObjectPropertyExpression property) {
		return getOWLInverseFunctionalObjectPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLInverseFunctionalObjectPropertyAxiom getOWLInverseFunctionalObjectPropertyAxiom(
			OWLObjectPropertyExpression property, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLInverseFunctionalObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLInverseFunctionalObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLInverseFunctionalO bjectPropertyAxiomImpl(this, property, annotations);
	}

	public OWLIrreflexiveObjectPropertyAxiom getOWLIrreflexiveObjectPropertyAxiom(OWLObjectPropertyExpression property,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLIrreflexiveObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLIrreflexiveObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLIrreflexiveObjectPropertyAxiomImpl(this, property, annotations);
	}

	public OWLReflexiveObjectPropertyAxiom getOWLReflexiveObjectPropertyAxiom(OWLObjectPropertyExpression property) {
		return getOWLReflexiveObjectPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLIrreflexiveObjectPropertyAxiom getOWLIrreflexiveObjectPropertyAxiom(OWLObjectPropertyExpression property) {
		return getOWLIrreflexiveObjectPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLObjectPropertyDomainAxiom getOWLObjectPropertyDomainAxiom(OWLObjectPropertyExpression property,
			OWLClassExpression classExpression, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (classExpression == null) throw new IllegalArgumentException("classExpression null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLObjectPropertyDomainAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle classExpressionHandle = graph.getHandle(classExpression);
		axiom = new OWLObjectPropertyDomainAxiomHGDB(propertyHandle, classExpressionHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLObjectPropert yDomainAxiomImpl(this, property, classExpression, annotations);
	}

	public OWLObjectPropertyDomainAxiom getOWLObjectPropertyDomainAxiom(OWLObjectPropertyExpression property,
			OWLClassExpression classExpression) {
		return getOWLObjectPropertyDomainAxiom(property, classExpression, EMPTY_ANNOTATIONS_SET);
	}

	public OWLObjectPropertyRangeAxiom getOWLObjectPropertyRangeAxiom(OWLObjectPropertyExpression property,
			OWLClassExpression range, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (range == null) throw new IllegalArgumentException("range null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		// chain, superProperty and annotations are in Graph, if created
		// by this Datafactory
		OWLObjectPropertyRangeAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle rangeHandle = graph.getHandle(range);
		axiom = new OWLObjectPropertyRangeAxiomHGDB(propertyHandle, rangeHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLObjectPropertyRang eAxiomImpl(this, property, range, annotations);
	}

	public OWLObjectPropertyRangeAxiom getOWLObjectPropertyRangeAxiom(OWLObjectPropertyExpression property,
			OWLClassExpression range) {
		return getOWLObjectPropertyRangeAxiom(property, range, EMPTY_ANNOTATIONS_SET);
	}

	public OWLSubObjectPropertyOfAxiom getOWLSubObjectPropertyOfAxiom(OWLObjectPropertyExpression subProperty,
			OWLObjectPropertyExpression superProperty, Set<? extends OWLAnnotation> annotations) {
		if (subProperty == null)
			throw new IllegalArgumentException("subProperty null");
		if (superProperty == null)
			throw new IllegalArgumentException("superProperty null");
		if (annotations == null)
			throw new IllegalArgumentException("annotations null");
		// subClass, superClass and annotations are in Graph, if created by this
		// Datafactory
		OWLSubObjectPropertyOfAxiomHGDB axiom;
		// TODO Implement use of OWLObjectPropertyExpression
		
		//2011.11.03 Assume it's there.
		HGHandle subPropertyHandle = graph.getHandle(subProperty);
		HGHandle superPropertyHandle = graph.getHandle(superProperty);
		//HGHandle subPropertyHandle = getOrFindOWLEntityHandleInGraph((OWLObjectProperty) subProperty);
		//HGHandle superPropertyHandle = getOrFindOWLEntityHandleInGraph((OWLObjectProperty) superProperty);
		if (subPropertyHandle == null || superPropertyHandle == null) {
			throw new IllegalStateException("No Handle for subProperty or superProperty");
		}
		axiom = new OWLSubObjectPropertyOfAxiomHGDB(subPropertyHandle, superPropertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		// return new OWLSubObjectPropertyOfAxiomImpl(this, subProperty,
		// superProperty, annotations);
	}

	public OWLSubObjectPropertyOfAxiom getOWLSubObjectPropertyOfAxiom(OWLObjectPropertyExpression subProperty,
			OWLObjectPropertyExpression superProperty) {
		return getOWLSubObjectPropertyOfAxiom(subProperty, superProperty, EMPTY_ANNOTATIONS_SET);
	}

	public OWLReflexiveObjectPropertyAxiom getOWLReflexiveObjectPropertyAxiom(OWLObjectPropertyExpression property,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLReflexiveObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLReflexiveObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;				
		//return new OWLReflexiveObjectPropertyAxiomImpl(this, property, annotations);
	}

	public OWLSameIndividualAxiom getOWLSameIndividualAxiom(Set<? extends OWLIndividual> individuals,
			Set<? extends OWLAnnotation> annotations) {
		if (individuals == null) throw new IllegalArgumentException("individuals null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLSameIndividualAxiomHGDB axiom;
		Set<HGHandle> individualsHandles = getHandlesSetFor(individuals);
		axiom = new OWLSameIndividualAxiomHGDB(individualsHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;				
		//return new OWLSameIndividualAxiomImpl(this, individuals, annotations);
	}

	public OWLSameIndividualAxiom getOWLSameIndividualAxiom(OWLIndividual... individuals) {
		Set<OWLIndividual> inds = new HashSet<OWLIndividual>();
		inds.addAll(Arrays.asList(individuals));
		return getOWLSameIndividualAxiom(inds);
	}

	public OWLSameIndividualAxiom getOWLSameIndividualAxiom(Set<? extends OWLIndividual> individuals) {
		return getOWLSameIndividualAxiom(individuals, EMPTY_ANNOTATIONS_SET);
	}

	public OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass,
			Set<? extends OWLAnnotation> annotations) {
		if (subClass == null)
			throw new IllegalArgumentException("subClass null");
		if (superClass == null)
			throw new IllegalArgumentException("superClass null");
		if (annotations == null)
			throw new IllegalArgumentException("annotations null");
		// subClass, superClass and annotations are in Graph, if created by this
		// Datafactory
		OWLSubClassOfAxiomHGDB axiom;
		// TODO Implement use of OWLClassExpression
//2010.10.25		HGHandle subClassHandle = getOrFindOWLEntityHandleInGraph((OWLClass) subClass);
//		HGHandle superClassHandle = getOrFindOWLEntityHandleInGraph((OWLClass) superClass);
		HGHandle subClassHandle = getHyperGraph().getHandle(subClass);
		HGHandle superClassHandle = getHyperGraph().getHandle(superClass);
		if (subClassHandle == null || superClassHandle == null) {
			throw new IllegalStateException("No Handle for subClass or superClass");
		}
		// hilpold 2011.10.06 we do not care, whether the axiom exists here.
		// Just as the original implementation.
		// The axiom shall be added to the graph later, when the user emits a
		// applychanges to the ontology.

		// //Check if OWLDeclarationAxiom already exists.
		// axiom = hg.getOne(graph, hg.and(
		// hg.type(OWLSubClassOfAxiomHGDB.class),
		// hg.link(subClassHandle, superClassHandle)
		// ));
		// if (axiom == null) {
		axiom = new OWLSubClassOfAxiomHGDB(subClassHandle, superClassHandle, annotations);
		axiom.setHyperGraph(graph); // 2011.10.06 needed now, that we don't add
									// it to the graph right away.
		// //TODO maybe we shall not do this here, but wait for appliedChanges
		// in cl,
		// //especially for axiom.
		// graph.add(axiom);
		// }
		return axiom;
		// return new OWLSubClassOfAxiomImpl(this, subClass, superClass,
		// annotations);
	}

	public OWLSubClassOfAxiom getOWLSubClassOfAxiom(OWLClassExpression subClass, OWLClassExpression superClass) {
		return getOWLSubClassOfAxiom(subClass, superClass, EMPTY_ANNOTATIONS_SET);
	}

	public OWLSymmetricObjectPropertyAxiom getOWLSymmetricObjectPropertyAxiom(OWLObjectPropertyExpression property,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLSymmetricObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLSymmetricObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;				
		//return new OWLSymmetricObjectPropertyAxiomImpl(this, property, annotations);
	}

	public OWLSymmetricObjectPropertyAxiom getOWLSymmetricObjectPropertyAxiom(OWLObjectPropertyExpression property) {
		return getOWLSymmetricObjectPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLTransitiveObjectPropertyAxiom getOWLTransitiveObjectPropertyAxiom(OWLObjectPropertyExpression property,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLTransitiveObjectPropertyAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		axiom = new OWLTransitiveObjectPropertyAxiomHGDB(propertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;				
		//return new OWLTransitiveObjectPropertyAxiomImpl(this, property, annotations);
	}

	public OWLTransitiveObjectPropertyAxiom getOWLTransitiveObjectPropertyAxiom(OWLObjectPropertyExpression property) {
		return getOWLTransitiveObjectPropertyAxiom(property, EMPTY_ANNOTATIONS_SET);
	}

	public OWLObjectInverseOf getOWLObjectInverseOf(OWLObjectPropertyExpression property) {
		if (property == null)
			throw new IllegalArgumentException("property null");
		HGHandle propertyHandle = graph.getHandle(property);
		if (propertyHandle == null)
			throw new IllegalStateException("propertyHandle null");
		OWLObjectInverseOfHGDB i = new OWLObjectInverseOfHGDB(propertyHandle);
		graph.add(i);
		return i;
	}

	public OWLInverseObjectPropertiesAxiom getOWLInverseObjectPropertiesAxiom(
			OWLObjectPropertyExpression forwardProperty, OWLObjectPropertyExpression inverseProperty,
			Set<? extends OWLAnnotation> annotations) {
		if (forwardProperty == null) throw new IllegalArgumentException("forwardProperty null");
		if (inverseProperty == null) throw new IllegalArgumentException("inverseProperty null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLInverseObjectPropertiesAxiomHGDB axiom;
		HGHandle forwardPropertyHandle = graph.getHandle(forwardProperty);
		HGHandle inversePropertyHandle = graph.getHandle(inverseProperty);
		axiom = new OWLInverseObjectPropertiesAxiomHGDB(forwardPropertyHandle, inversePropertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLInverseObjectPropertiesAxiomImpl(this, forwardProperty, inverseProperty, annotations);
	}

	public OWLInverseObjectPropertiesAxiom getOWLInverseObjectPropertiesAxiom(
			OWLObjectPropertyExpression forwardProperty, OWLObjectPropertyExpression inverseProperty) {
		return getOWLInverseObjectPropertiesAxiom(forwardProperty, inverseProperty, EMPTY_ANNOTATIONS_SET);
	}

	public OWLSubPropertyChainOfAxiom getOWLSubPropertyChainOfAxiom(List<? extends OWLObjectPropertyExpression> chain,
			OWLObjectPropertyExpression superProperty, Set<? extends OWLAnnotation> annotations) {
		if (chain == null) throw new IllegalArgumentException("chain null");
		if (superProperty == null) throw new IllegalArgumentException("superProperty null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		// chain, superProperty and annotations are in Graph, if created
		// by this Datafactory
		OWLSubPropertyChainAxiomHGDB axiom;
		List<HGHandle> chainElementHandles = getHandlesListFor(chain);
		HGHandle superPropertyHandle = graph.getHandle(superProperty);
		axiom = new OWLSubPropertyChainAxiomHGDB(chainElementHandles, superPropertyHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLSubPropertyChainAxiomImpl(this, chain, superProperty, annotations);
	}

	public OWLSubPropertyChainOfAxiom getOWLSubPropertyChainOfAxiom(List<? extends OWLObjectPropertyExpression> chain,
			OWLObjectPropertyExpression superProperty) {
		return getOWLSubPropertyChainOfAxiom(chain, superProperty, EMPTY_ANNOTATIONS_SET);
	}

	public OWLHasKeyAxiom getOWLHasKeyAxiom(OWLClassExpression ce, Set<? extends OWLPropertyExpression<?, ?>> properties, Set<? extends OWLAnnotation> annotations) {
		if (ce == null) throw new IllegalArgumentException("owlClass null");
		if (properties == null)	throw new IllegalArgumentException("properties null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLHasKeyAxiomHGDB axiom;
		HGHandle owlClassHandle = graph.getHandle(ce);
		Set<HGHandle> propertiesHandles = getHandlesSetFor(properties);
		axiom = new OWLHasKeyAxiomHGDB(owlClassHandle, propertiesHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;		
		//return new OWLHasKeyAxiomImpl(this, ce, properties, annotations);
	}

	public OWLHasKeyAxiom getOWLHasKeyAxiom(OWLClassExpression ce, Set<? extends OWLPropertyExpression<?, ?>> properties) {
		return getOWLHasKeyAxiom(ce, properties, EMPTY_ANNOTATIONS_SET);
	}

	public OWLHasKeyAxiom getOWLHasKeyAxiom(OWLClassExpression ce, OWLPropertyExpression<?, ?>... properties) {
		return getOWLHasKeyAxiom(ce, CollectionFactory.createSet(properties));
	}

	public OWLDisjointUnionAxiom getOWLDisjointUnionAxiom(OWLClass owlClass,
			Set<? extends OWLClassExpression> classExpressions, Set<? extends OWLAnnotation> annotations) {
		if (owlClass == null) throw new IllegalArgumentException("owlClass null");
		if (classExpressions == null) throw new IllegalArgumentException("classExpressions null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		// owlClass, classExpressions and annotations are in Graph, if created
		// by this Datafactory
		OWLDisjointUnionAxiomHGDB axiom;
		Set<HGHandle> classExpressionsHandles = getHandlesSetFor(classExpressions);
		HGHandle owlClassHandle = graph.getHandle(owlClass);
		axiom = new OWLDisjointUnionAxiomHGDB(owlClassHandle, classExpressionsHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		// return new OWLDisjointUnionAxiomImpl(this, owlClass,
		// classExpressions, annotations);
	}

	public OWLDisjointUnionAxiom getOWLDisjointUnionAxiom(OWLClass owlClass,
			Set<? extends OWLClassExpression> classExpressions) {
		return getOWLDisjointUnionAxiom(owlClass, classExpressions, EMPTY_ANNOTATIONS_SET);
	}

	public OWLEquivalentObjectPropertiesAxiom getOWLEquivalentObjectPropertiesAxiom(
			Set<? extends OWLObjectPropertyExpression> properties, Set<? extends OWLAnnotation> annotations) {
		if (properties == null) throw new IllegalArgumentException("property null");
		OWLEquivalentObjectPropertiesAxiomHGDB axiom;
		Set<HGHandle> propertiesHandles = getHandlesSetFor(properties);
		axiom = new OWLEquivalentObjectPropertiesAxiomHGDB(propertiesHandles, annotations);
		axiom.setHyperGraph(graph);
		return axiom;				
		//return new OWLEquivalentObjectPropertiesAxiomImpl(this, properties, annotations);
	}

	public OWLObjectPropertyAssertionAxiom getOWLObjectPropertyAssertionAxiom(OWLObjectPropertyExpression property,
			OWLIndividual individual, OWLIndividual object, Set<? extends OWLAnnotation> annotations) {
		if (property == null) throw new IllegalArgumentException("property null");
		if (individual == null)	throw new IllegalArgumentException("individual null");
		if (object == null)	throw new IllegalArgumentException("object null");
		if (annotations == null) throw new IllegalArgumentException("annotations null");
		OWLObjectPropertyAssertionAxiomHGDB axiom;
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle individualHandle = graph.getHandle(individual);
		HGHandle objectHandle = graph.getHandle(object);
		if (propertyHandle == null || individualHandle == null || objectHandle == null) {
			throw new IllegalStateException("No Handle for property, individual AND/OR object "+
					propertyHandle + "," + individualHandle + "," + objectHandle + 
					" -- " + property + "," + individual + "," + object);
		}
		axiom = new OWLObjectPropertyAssertionAxiomHGDB(individualHandle, propertyHandle, objectHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLObjectPropertyAssertionAxiomImpl(this, individual, property, object, annotations);
	}

	public OWLSubAnnotationPropertyOfAxiom getOWLSubAnnotationPropertyOfAxiom(OWLAnnotationProperty sub,
			OWLAnnotationProperty sup) {
		return getOWLSubAnnotationPropertyOfAxiom(sub, sup, EMPTY_ANNOTATIONS_SET);
	}

	public OWLSubAnnotationPropertyOfAxiom getOWLSubAnnotationPropertyOfAxiom(OWLAnnotationProperty sub,
			OWLAnnotationProperty sup, Set<? extends OWLAnnotation> annotations) {
		if (sub == null)
			throw new IllegalArgumentException("subClass null");
		if (sup == null)
			throw new IllegalArgumentException("superClass null");
		if (annotations == null)
			throw new IllegalArgumentException("annotations null");
		OWLSubAnnotationPropertyOfAxiomHGDB axiom;
		HGHandle subHandle = getOrFindOWLEntityHandleInGraph(sub);
		HGHandle supHandle = getOrFindOWLEntityHandleInGraph(sup);
		if (subHandle == null || supHandle == null) {
			throw new IllegalStateException("No Handle for subProperty or superProperty");
		}
		axiom = new OWLSubAnnotationPropertyOfAxiomHGDB(subHandle, supHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Annotations

	public OWLAnnotationProperty getOWLAnnotationProperty(IRI iri) {
		return data.getOWLAnnotationProperty(iri);
	}

	/**
	 * Gets an annotation
	 * 
	 * @param property
	 *            the annotation property
	 * @param value
	 *            The annotation value
	 * @return The annotation on the specified property with the specified value
	 */
	public OWLAnnotation getOWLAnnotation(OWLAnnotationProperty property, OWLAnnotationValue value) {
		return getOWLAnnotation(property, value, EMPTY_ANNOTATIONS_SET);
	}

	/**
	 * Gets an annotation
	 * 
	 * @param property
	 *            the annotation property
	 * @param value
	 *            The annotation value
	 * @param annotations
	 *            Annotations on the annotation
	 * @return The annotation on the specified property with the specified value
	 */
	public OWLAnnotation getOWLAnnotation(OWLAnnotationProperty property, OWLAnnotationValue value,
			Set<? extends OWLAnnotation> annotations) {
		if (property == null) {
			throw new NullPointerException("Annotation property is null");
		}
		if (value == null) {
			throw new NullPointerException("Annotation value is null");
		}
		HGHandle propertyHandle = graph.getHandle(property);
		// value might be IRI, OWLAnonymousIndividual or OWLLiteral
		// If it's an IRI, it was not created in the DF and we need to check,  
		// whether we have it in the graph already.
		HGHandle valueHandle; 
		if (value instanceof IRI) {
			valueHandle = data.findOrAddIRIHandle((IRI)value);
		} else {
			valueHandle = graph.getHandle(value);
		}		
		Set<HGHandle> annotationsHandles = getHandlesSetFor(annotations);
		if (propertyHandle == null) {
			throw new NullPointerException("Annotation propertyhandle is null for property " + property);
		}
		if (valueHandle == null) {
			throw new NullPointerException("Annotation valueHandle is null for value " + value);
		}		
		OWLAnnotationHGDB a = new OWLAnnotationHGDB(propertyHandle, valueHandle, annotationsHandles);
		graph.add(a);
		return a;
		// return new OWLAnnotationImpl(this, property, value, annotations);
	}

	public OWLAnnotationAssertionAxiom getOWLAnnotationAssertionAxiom(OWLAnnotationSubject subject,
			OWLAnnotation annotation) {
		// PATCH: return
		// getOWLAnnotationAssertionAxiom(annotation.getProperty(), subject,
		// annotation.getValue(), annotation.getAnnotations());
		// ORIG: return getOWLAnnotationAssertionAxiom(annotation.getProperty(),
		// subject, annotation.getValue());
		// The patch makes a difference for the owl, owlfs, rdfxml and turtle
		// serializations of Annotation2.
		return getOWLAnnotationAssertionAxiom(annotation.getProperty(), subject, annotation.getValue(),
				annotation.getAnnotations());
	}

	public OWLAnnotationAssertionAxiom getOWLAnnotationAssertionAxiom(OWLAnnotationSubject subject,
			OWLAnnotation annotation, Set<? extends OWLAnnotation> annotations) {
		return getOWLAnnotationAssertionAxiom(annotation.getProperty(), subject, annotation.getValue(), annotations);
	}

	public OWLAnnotationAssertionAxiom getOWLAnnotationAssertionAxiom(OWLAnnotationProperty property,
			OWLAnnotationSubject subject, OWLAnnotationValue value) {
		return getOWLAnnotationAssertionAxiom(property, subject, value, EMPTY_ANNOTATIONS_SET);
	}

	public OWLAnnotationAssertionAxiom getOWLAnnotationAssertionAxiom(OWLAnnotationProperty property,
			OWLAnnotationSubject subject, OWLAnnotationValue value, Set<? extends OWLAnnotation> annotations) {
		if (property == null) {
			throw new NullPointerException("Annotation property is null");
		}
		if (subject == null) {
			throw new NullPointerException("Annotation subject is null");
		}
		if (value == null) {
			throw new NullPointerException("Annotation value is null");
		}
		HGHandle propertyHandle = graph.getHandle(property);
		HGHandle subjectHandle;
		// IRIs are not created by this Datafactory.
		// therefore add them to graph here, so we can get a handle.
		if (subject instanceof IRI) {
			subjectHandle = data.findOrAddIRIHandle((IRI)subject);
		} else {
			subjectHandle = graph.getHandle(subject);
		}
		HGHandle valueHandle;
		if (value instanceof IRI) {
			valueHandle = data.findOrAddIRIHandle((IRI)value);
		} else {
			valueHandle = graph.getHandle(value);
		}		
		if (propertyHandle == null) {
			throw new NullPointerException("Annotation propertyhandle is null for property " + property + ".");
		}
		if (subjectHandle == null) {
			throw new NullPointerException("Annotation subjectHandle is null for subject " + subject + ".");
		}
		if (valueHandle == null) {
			throw new NullPointerException("Annotation valueHandle is null for value " + value + ".");
		}
		OWLAnnotationAssertionAxiomHGDB a = new OWLAnnotationAssertionAxiomHGDB(subjectHandle, propertyHandle, valueHandle, annotations);
		a.setHyperGraph(graph);
		return a; 
		//return new OWLAnnotationAssertionAxiomHGDB(subject, property, value, annotations);	
	}

	/**
	 * Gets an annotation assertion that specifies that an IRI is deprecated.
	 * The annotation property is owl:deprecated and the value of the annotation
	 * is <code>"true"^^xsd:boolean</code>. (See <a href=
	 * "http://www.w3.org/TR/2009/REC-owl2-syntax-20091027/#Annotation_Properties"
	 * >Annotation Properties</a> in the OWL 2 Specification
	 * 
	 * @param subject
	 *            The IRI to be deprecated.
	 * @return The annotation assertion that deprecates the specified IRI.
	 */
	public OWLAnnotationAssertionAxiom getDeprecatedOWLAnnotationAssertionAxiom(IRI subject) {
		return getOWLAnnotationAssertionAxiom(getOWLDeprecated(), subject, getOWLLiteral(true));
	}

	public OWLAnnotationPropertyDomainAxiom getOWLAnnotationPropertyDomainAxiom(OWLAnnotationProperty prop, IRI domain,
			Set<? extends OWLAnnotation> annotations) {
		HGHandle propertyHandle = graph.getHandle(prop);
		HGHandle domainHandle;
		if (propertyHandle == null) {
			throw new NullPointerException("Annotation propertyHandle is null");
		}
		domainHandle = data.findOrAddIRIHandle(domain);
		if (domainHandle == null) {
			throw new NullPointerException("Annotation domainHandle is null");
		}
		OWLAnnotationPropertyDomainAxiomHGDB a = new OWLAnnotationPropertyDomainAxiomHGDB(propertyHandle, domainHandle, annotations);
		a.setHyperGraph(graph);
		return a;
		// return new OWLAnnotationPropertyDomainAxiomImpl(this, prop, domain, annotations);
	}

	public OWLAnnotationPropertyDomainAxiom getOWLAnnotationPropertyDomainAxiom(OWLAnnotationProperty prop, IRI domain) {
		return getOWLAnnotationPropertyDomainAxiom(prop, domain, EMPTY_ANNOTATIONS_SET);
	}

	public OWLAnnotationPropertyRangeAxiom getOWLAnnotationPropertyRangeAxiom(OWLAnnotationProperty prop, IRI range,
			Set<? extends OWLAnnotation> annotations) {
		HGHandle propertyHandle = graph.getHandle(prop);
		HGHandle rangeHandle;
		if (propertyHandle == null) {
			throw new NullPointerException("Annotation propertyhandle is null");
		}
		rangeHandle = data.findOrAddIRIHandle(range);
		if (rangeHandle == null) {
			throw new NullPointerException("Annotation rangeHandle is null");
		}
		OWLAnnotationPropertyRangeAxiomHGDB a = new OWLAnnotationPropertyRangeAxiomHGDB(propertyHandle, rangeHandle, annotations);
		a.setHyperGraph(graph);
		return a;
		//return new OWLAnnotationPropertyRangeAxiomImpl(this, prop, range, annotations);
	}

	public OWLAnnotationPropertyRangeAxiom getOWLAnnotationPropertyRangeAxiom(OWLAnnotationProperty prop, IRI range) {
		return getOWLAnnotationPropertyRangeAxiom(prop, range, EMPTY_ANNOTATIONS_SET);
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// SWRL
	//
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param iri
	 *            The rule IRI - this parameter is IGNORED since OWL axioms do
	 *            not have IRIs, and is here for backwards compatability.
	 * @param body
	 *            The atoms that make up the body of the rule
	 * @param head
	 *            The atoms that make up the head of the rule
	 * @deprecated Use either
	 *             {@link #getSWRLRule(java.util.Set, java.util.Set, java.util.Set)}
	 *             or {@link #getSWRLRule(java.util.Set, java.util.Set)}
	 *             instead. Gets a SWRL rule which is named with a URI
	 */
	@Deprecated
	public SWRLRule getSWRLRule(IRI iri, Set<? extends SWRLAtom> body, Set<? extends SWRLAtom> head) {
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>(2);
		annos.add(getOWLAnnotation(getOWLAnnotationProperty(IRI.create("http://www.semanticweb.org/owlapi#iri")),
				getOWLLiteral(iri.toQuotedString())));
		return getSWRLRuleImpl(body, head, annos);
	}

	/**
	 * @param nodeID
	 *            The node ID
	 * @param body
	 *            The atoms that make up the body of the rule
	 * @param head
	 *            The atoms that make up the head of the rule
	 * @deprecated Use either
	 *             {@link #getSWRLRule(java.util.Set, java.util.Set, java.util.Set)}
	 *             or {@link #getSWRLRule(java.util.Set, java.util.Set)}
	 *             instead.
	 */
	@Deprecated
	public SWRLRule getSWRLRule(NodeID nodeID, Set<? extends SWRLAtom> body, Set<? extends SWRLAtom> head) {
		Set<OWLAnnotation> annos = new HashSet<OWLAnnotation>(2);
		annos.add(getOWLAnnotation(getOWLAnnotationProperty(IRI.create("http://www.semanticweb.org/owlapi#nodeID")),
				getOWLLiteral(nodeID.toString())));
		return getSWRLRuleImpl(body, head, annos);
	}

	/**
	 * Gets an anonymous SWRL Rule
	 * 
	 * @param body
	 *            The atoms that make up the body
	 * @param head
	 *            The atoms that make up the head
	 * @param annotations
	 *            The annotations for the rule (may be an empty set)
	 * @return An anonymous rule with the specified body and head
	 */
	public SWRLRule getSWRLRule(Set<? extends SWRLAtom> body, Set<? extends SWRLAtom> head,
			Set<OWLAnnotation> annotations) {
		return getSWRLRuleImpl(body, head, annotations);
	}

	/**
	 * Gets a SWRL rule which is anonymous - i.e. isn't named with a URI
	 * 
	 * @param antecedent
	 *            The atoms that make up the antecedent
	 * @param consequent
	 *            The atoms that make up the consequent
	 */
	public SWRLRule getSWRLRule(Set<? extends SWRLAtom> antecedent, Set<? extends SWRLAtom> consequent) {
		return getSWRLRuleImpl(antecedent, consequent, EMPTY_ANNOTATIONS_SET);
	}
	
	protected SWRLRule getSWRLRuleImpl(Set<? extends SWRLAtom> body, Set<? extends SWRLAtom> head, Set<? extends OWLAnnotation> annos) {
		Set<HGHandle> bodyHandles = getHandlesSetFor(body);
		Set<HGHandle> headHandles = getHandlesSetFor(head);
		SWRLBody swrlBody = new SWRLBody(bodyHandles);
		SWRLHead swrlHead = new SWRLHead(headHandles);
		HGHandle bodyHandle = graph.add(swrlBody);
		HGHandle headHandle = graph.add(swrlHead);		
		graph.add(bodyHandle);
		graph.add(headHandle);
		SWRLRuleHGDB ruleAxiom = new SWRLRuleHGDB(bodyHandle, headHandle, annos);
		ruleAxiom.setHyperGraph(graph);
		return ruleAxiom;
	}

	/**
	 * Gets a SWRL class atom, i.e. C(x) where C is a class expression and x is
	 * either an individual id or an i-variable
	 * 
	 * @param predicate
	 *            The class expression that corresponds to the predicate
	 * @param arg
	 *            The argument (x)
	 */
	public SWRLClassAtom getSWRLClassAtom(OWLClassExpression predicate, SWRLIArgument arg) {
		SWRLClassAtomHGDB classAtom = new SWRLClassAtomHGDB(predicate, arg);
		graph.add(classAtom);
		return classAtom;
		//return new SWRLClassAtomImpl(this, predicate, arg);
	}

	/**
	 * Gets a SWRL data range atom, i.e. D(x) where D is an OWL data range and x
	 * is either a constant or a d-variable
	 * 
	 * @param predicate
	 *            The data range that corresponds to the predicate
	 * @param arg
	 *            The argument (x)
	 */
	public SWRLDataRangeAtom getSWRLDataRangeAtom(OWLDataRange predicate, SWRLDArgument arg) {
		SWRLDataRangeAtomHGDB atom = new SWRLDataRangeAtomHGDB(predicate, arg);
		graph.add(atom);
		return atom;
		// return new SWRLDataRangeAtomImpl(this, predicate, arg);
	}

	/**
	 * Gets a SWRL object property atom, i.e. P(x, y) where P is an OWL object
	 * property (expression) and x and y are are either an individual id or an
	 * i-variable.
	 * 
	 * @param property
	 *            The property (P)
	 * @param arg0
	 *            The first argument (x)
	 * @param arg1
	 *            The second argument (y)
	 */
	public SWRLObjectPropertyAtom getSWRLObjectPropertyAtom(OWLObjectPropertyExpression property, SWRLIArgument arg0,
			SWRLIArgument arg1) {
		SWRLObjectPropertyAtomHGDB atom = new SWRLObjectPropertyAtomHGDB(property, arg0, arg1);
		graph.add(atom);
		return atom;		
		// return new SWRLObjectPropertyAtomImpl(this, property, arg0, arg1);
	}

	/**
	 * Gets a SWRL data property atom, i.e. R(x, y) where R is an OWL data
	 * property (expression) and x and y are are either a constant or a
	 * d-variable.
	 * 
	 * @param property
	 *            The property (P)
	 * @param arg0
	 *            The first argument (x)
	 * @param arg1
	 *            The second argument (y)
	 */
	public SWRLDataPropertyAtom getSWRLDataPropertyAtom(OWLDataPropertyExpression property, SWRLIArgument arg0,
			SWRLDArgument arg1) {
		SWRLDataPropertyAtomHGDB atom = new SWRLDataPropertyAtomHGDB(property, arg0, arg1);
		graph.add(atom);
		return atom;				
		// return new SWRLDataPropertyAtomImpl(this, property, arg0, arg1);
	}

	/**
	 * Creates a SWRL Built-In atom.
	 * 
	 * @param builtInIRI
	 *            The SWRL builtIn (see SWRL W3 member submission)
	 * @param args
	 *            A non-empty set of SWRL D-Objects
	 */
	public SWRLBuiltInAtom getSWRLBuiltInAtom(IRI builtInIRI, List<SWRLDArgument> args) {
		SWRLBuiltInAtomHGDB atom = new SWRLBuiltInAtomHGDB(builtInIRI, args);
		graph.add(atom);
		return atom;				
		//return new SWRLBuiltInAtomImpl(this, builtInIRI, args);
	}

	/**
	 * Gets a SWRLVariable.
	 * 
	 * @param var
	 *            The id (IRI) of the variable
	 * @return A SWRLVariable that has the name specified by the IRI
	 */
	public SWRLVariable getSWRLVariable(final IRI var) {
		SWRLVariableHGDB atom = new SWRLVariableHGDB(var);
		graph.add(atom);
		return atom;				
	}
	
	/**
	 * Gets a SWRL individual object.
	 * 
	 * @param individual
	 *            The individual that is the object argument
	 */
	public SWRLIndividualArgument getSWRLIndividualArgument(OWLIndividual individual) {
		HGHandle h = graph.getHandle(individual);
		if (h == null) throw new IllegalArgumentException("Individual handle not found.");
		SWRLIndividualArgumentHGDB atom = new SWRLIndividualArgumentHGDB(h);
		graph.add(atom);
		return atom;
		//return new SWRLIndividualArgumentImpl(this, individual);
	}

	/**
	 * Gets a SWRL constant object.
	 * 
	 * @param literal
	 *            The constant that is the object argument
	 */
	public SWRLLiteralArgument getSWRLLiteralArgument(OWLLiteral literal) {
		//HGHandle h = graph.getHandle(literal);
		//if (h == null) throw new IllegalArgumentException("Literal handle not found.");
		//TODO Do we care here, whether literal is in graph or not? It is.
		SWRLLiteralArgument atom = new SWRLLiteralArgumentHGDB(literal);
		graph.add(atom);
		return atom;
		//return new SWRLLiteralArgumentImpl(this, literal);
	}

	public SWRLDifferentIndividualsAtom getSWRLDifferentIndividualsAtom(SWRLIArgument arg0, SWRLIArgument arg1) {
		SWRLDifferentIndividualsAtom atom = new SWRLDifferentIndividualsAtomHGDB(arg0, arg1);
		graph.add(atom);
		return atom;
		//return new SWRLDifferentIndividualsAtomImpl(this, arg0, arg1);
	}

	public SWRLSameIndividualAtom getSWRLSameIndividualAtom(SWRLIArgument arg0, SWRLIArgument arg1) {
		SWRLSameIndividualAtom atom = new SWRLSameIndividualAtomHGDB(arg0, arg1);
		graph.add(atom);
		return atom;
		//return new SWRLSameIndividualAtomImpl(this, arg0, arg1);
	}

	private static Set<OWLAnnotation> EMPTY_ANNOTATIONS_SET = Collections.emptySet();

	public OWLDatatypeDefinitionAxiom getOWLDatatypeDefinitionAxiom(OWLDatatype datatype, OWLDataRange dataRange) {
		return getOWLDatatypeDefinitionAxiom(datatype, dataRange, EMPTY_ANNOTATIONS_SET);
	}

	public OWLDatatypeDefinitionAxiom getOWLDatatypeDefinitionAxiom(OWLDatatype datatype, OWLDataRange dataRange,
			Set<? extends OWLAnnotation> annotations) {
		if (datatype == null)
			throw new IllegalArgumentException("datatype null");
		if (dataRange == null)
			throw new IllegalArgumentException("dataRange null");
		if (annotations == null)
			throw new IllegalArgumentException("annotations null");
		OWLDatatypeDefinitionAxiomHGDB axiom;
		HGHandle datatypeHandle = getOrFindOWLEntityHandleInGraph(datatype);
		HGHandle dataRangeHandle = graph.getHandle(dataRange);
		if (datatypeHandle == null || dataRangeHandle == null) {
			throw new IllegalStateException("No Handle for datatypeHandle or dataRangeHandle");
		}
		axiom = new OWLDatatypeDefinitionAxiomHGDB(datatypeHandle, dataRangeHandle, annotations);
		axiom.setHyperGraph(graph);
		return axiom;
		//return new OWLDatatypeDefinitionAxiomImpl(this, datatype, dataRange, annotations);
	}


	//
	// IRI methods
	//
	
	/**
	 * Asserts that a given IRI is in the graph.
	 * @param iri
	 * @return
	 */
	public HGHandle assertIRI(IRI iri) {
		return data.findOrAddIRIHandle(iri);
	}

	//
	// Hypergraph
	//
	public void setHyperGraph(HyperGraph graph) {
		if (graph == null)
			throw new IllegalArgumentException("Attempt to set graph null");
		this.graph = graph;
		data.initialize();
	}

	public HyperGraph getHyperGraph() {
		return graph;
	}

	//
	// Helpers: Should be own class later
	//

	/**
	 * Tries to get a handle or find it by classtype and iri in the graph.
	 * 
	 * @param e
	 * @return
	 */
	protected HGHandle getOrFindOWLEntityHandleInGraph(OWLEntity e) {
		HGHandle eHandle = graph.getHandle(e);
		if (eHandle == null) {
			eHandle = hg.findOne(graph, hg.and(hg.type(e.getClass()), hg.eq("IRI", e.getIRI())));
		}
		return eHandle;
	}

	/**
	 * Gets handles for each element from Hypergraph and
	 * 
	 * @param s
	 *            a set of atoms implementing OWLObject and stored in HG.
	 * @return a set of handles (no null, same size as s)
	 */
	private Set<HGHandle> getHandlesSetFor(Set<? extends OWLObject> s) {
		Set<HGHandle> sHandles = new TreeSet<HGHandle>();
		HGHandle h;
		for (OWLObject o : s) {
			h = graph.getHandle(o);
			if (h == null) {
				throw new IllegalArgumentException("s contained an object that we could not get a handle for: " + o);
			} else {
				if (!sHandles.add(h))
					throw new IllegalStateException("we got a duplicate handle");
			}
		}
		assert (s.size() == sHandles.size());
		return sHandles;
	}

	/**
	 * Gets handles for each element from Hypergraph and
	 * 
	 * @param s
	 *            a set of atoms implementing OWLObject and stored in HG.
	 * @return a set of handles (no null, same size as s)
	 */
	private List<HGHandle> getHandlesListFor(List<? extends OWLObject> l) {
		List<HGHandle> lHandles = new ArrayList<HGHandle>(5);
		HGHandle h;
		for (OWLObject o : l) {
			h = graph.getHandle(o);
			if (h == null) {
				throw new IllegalArgumentException("l contained an object that we could not get a handle for: " + o);
			} else {
				lHandles.add(h);
			}
		}
		return lHandles;
	}
	
}
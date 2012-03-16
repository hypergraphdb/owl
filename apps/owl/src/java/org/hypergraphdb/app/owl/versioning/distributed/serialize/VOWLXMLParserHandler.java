package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.CHANGE_SET;
//import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.NAMESPACE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.RENDER_CONFIGURATION;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.REVISION;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_ADD_AXIOM_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_ADD_IMPORT_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_ADD_ONTOLOGY_ANNOTATION_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_MODIFY_ONTOLOGY_ID_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_MODIFY_ONTOLOGY_ID_NEW_ID;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_MODIFY_ONTOLOGY_ID_OLD_ID;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_REMOVE_AXIOM_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_REMOVE_IMPORT_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.VERSIONED_ONTOLOGY;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary.VERSIONED_ONTOLOGY_ROOT;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ABBREVIATED_IRI_ELEMENT;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ANNOTATION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ANNOTATION_ASSERTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ANNOTATION_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ANNOTATION_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ANNOTATION_PROPERTY_RANGE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ANONYMOUS_INDIVIDUAL;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ASYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.BODY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.BUILT_IN_ATOM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.CLASS;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.CLASS_ASSERTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.CLASS_ATOM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATATYPE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATATYPE_DEFINITION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATATYPE_RESTRICTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_ALL_VALUES_FROM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_COMPLEMENT_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_EXACT_CARDINALITY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_HAS_VALUE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_INTERSECTION_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_MAX_CARDINALITY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_MIN_CARDINALITY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_ONE_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_PROPERTY_ATOM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_PROPERTY_RANGE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_SOME_VALUES_FROM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DATA_UNION_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DECLARATION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DIFFERENT_INDIVIDUALS;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DIFFERENT_INDIVIDUALS_ATOM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DISJOINT_CLASSES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DISJOINT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DISJOINT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DISJOINT_UNION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.DL_SAFE_RULE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.EQUIVALENT_CLASSES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.EQUIVALENT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.EQUIVALENT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.FACET_RESTRICTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.FUNCTIONAL_DATA_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.HAS_KEY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.HEAD;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IMPORT;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.INVERSE_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IRI_ELEMENT;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.IRREFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.LITERAL;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.NAMED_INDIVIDUAL;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.NEGATIVE_DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.NEGATIVE_OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_ALL_VALUES_FROM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_COMPLEMENT_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_EXACT_CARDINALITY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_HAS_SELF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_HAS_VALUE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_INTERSECTION_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_INVERSE_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_MAX_CARDINALITY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_MIN_CARDINALITY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_ONE_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_PROPERTY_ATOM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_PROPERTY_CHAIN;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_PROPERTY_RANGE;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_SOME_VALUES_FROM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.OBJECT_UNION_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.ONTOLOGY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.REFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SAME_INDIVIDUAL;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SAME_INDIVIDUAL_ATOM;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SUB_ANNOTATION_PROPERTY_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SUB_CLASS_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SUB_DATA_PROPERTY_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SUB_OBJECT_PROPERTY_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.SYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.TRANSITIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.UNION_OF;
import static org.semanticweb.owlapi.vocab.OWLXMLVocabulary.VARIABLE;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.coode.owlapi.owlxmlparser.AbbreviatedIRIElementHandler;
import org.coode.owlapi.owlxmlparser.AbstractElementHandlerFactory;
import org.coode.owlapi.owlxmlparser.IRIElementHandler;
import org.coode.owlapi.owlxmlparser.LegacyEntityAnnotationElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationAssertionElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationPropertyDomainElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnnotationPropertyRangeElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAnonymousIndividualElementHandler;
import org.coode.owlapi.owlxmlparser.OWLAsymmetricObjectPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLClassAssertionAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLClassElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataAllValuesFromElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataComplementOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataExactCardinalityElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataHasValueElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataIntersectionOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataMaxCardinalityElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataMinCardinalityElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataOneOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataPropertyAssertionAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataPropertyDomainAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataPropertyRangeAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataSomeValuesFromElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDataUnionOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDatatypeDefinitionElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDatatypeElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDatatypeFacetRestrictionElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDatatypeRestrictionElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDeclarationAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDifferentIndividualsAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDisjointClassesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDisjointDataPropertiesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDisjointObjectPropertiesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLDisjointUnionElementHandler;
import org.coode.owlapi.owlxmlparser.OWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLElementHandlerFactory;
import org.coode.owlapi.owlxmlparser.OWLEquivalentClassesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLEquivalentDataPropertiesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLEquivalentObjectPropertiesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLFunctionalDataPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLFunctionalObjectPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLHasKeyElementHandler;
//import org.coode.owlapi.owlxmlparser.OWLImportsHandler;
import org.coode.owlapi.owlxmlparser.OWLIndividualElementHandler;
import org.coode.owlapi.owlxmlparser.OWLInverseFunctionalObjectPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLInverseObjectPropertiesAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLInverseObjectPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLIrreflexiveObjectPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLLiteralElementHandler;
import org.coode.owlapi.owlxmlparser.OWLNegativeDataPropertyAssertionAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLNegativeObjectPropertyAssertionAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectAllValuesFromElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectComplementOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectExactCardinalityElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectExistsSelfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectHasValueElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectIntersectionOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectMaxCardinalityElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectMinCardinalityElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectOneOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectPropertyAssertionAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectPropertyDomainElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectPropertyElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectPropertyRangeAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectSomeValuesFromElementHandler;
import org.coode.owlapi.owlxmlparser.OWLObjectUnionOfElementHandler;
//import org.coode.owlapi.owlxmlparser.OWLOntologyHandler;
import org.coode.owlapi.owlxmlparser.OWLReflexiveObjectPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSameIndividualsAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSubAnnotationPropertyOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSubClassAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSubDataPropertyOfAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSubObjectPropertyChainElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSubObjectPropertyOfAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLSymmetricObjectPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLTransitiveObjectPropertyAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLUnionOfElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.coode.owlapi.owlxmlparser.SWRLAtomListElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLBuiltInAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLClassAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLDataPropertyAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLDifferentIndividualsAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLObjectPropertyAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLRuleElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLSameIndividualAtomElementHandler;
import org.coode.owlapi.owlxmlparser.SWRLVariableElementHandler;
import org.coode.owlapi.owlxmlparser.TranslatedOWLParserException;
import org.coode.owlapi.owlxmlparser.TranslatedUnloadableImportException;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.ChangeSetElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OWLImportsHandlerModified;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OWLOntologyHandlerModified;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OrigSWRLAtomListElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.OrigSWRLVariableElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RenderConfigurationElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RevisionElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VAxiomChangeElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VImportChangeElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOntologyAnnotationChangeElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOntologyIDChangeElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyDocumentElementHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLParserURISyntaxException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * OWLXMLVParserHandler parses the following xml structure:
 * <pre>
 * <vo:VOWLXMLDocument ..>
 * 
 * <vo:RenderConfiguration atts />
 * <vo:VersionedOntology>
 * 		<!-- Contains Revision and Changesets pairwise as configured by RenderConfiguration -->
 *     <vo:Revision atts />
 *     <vo:ChangeSet>
 *     <vo:AConcreteChangeElement>
 *     	   <!-- may contain axioms, import declarations or ontology annotations which are handled by regular
 *     			OWLXML handlers . -->
 *     </vo:AConcreteChangeElement>
 *     </vo:ChangeSet>
 * 
 *     <Ontology >
 *     <!-- optional; contains head revision Ontology Data as described in RenderConFiguration 
 *          which will be parsed by regular OWLXML handlers. 
 *     -->
 *     </Ontology>
 * 	  
 * </vo:VersionedOntology>
 * 
 * <vo:VOWLXMLDocument ..>
 * </pre>
 * 
 * We were forced to extend the superclass, shield all superclass methods and copy a lot of it's code.
 * Reason: Superclass does not allow to set a parent handler for the AxiomHandlers. We need our ChangeElementHandlers to be 
 * parent handlers to axiom, Import and Annotation handlers.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VOWLXMLParserHandler extends OWLXMLParserHandler {

    private OWLOntologyManager owlOntologyManager;

    private VOWLXMLDocument versionedOntologyRoot;

    private List<OWLElementHandler<?>> handlerStack;

    private Map<String, OWLElementHandlerFactory> handlerMap;

    private Map<String, String> prefixName2PrefixMap = new HashMap<String, String>();

    private Locator locator;

    private Stack<URI> bases;

    private OWLOntologyLoaderConfiguration configuration;
    
//    /**
//     * True, if we are currently processing an ontology Object that our superclass shall handle.
//     * Overwritten methods will return superclass's methods values during OWL element processing.
//     */
//    private boolean owlElementProcessingMode;

    public VOWLXMLParserHandler(VOWLXMLDocument voRoot) {
    	this(voRoot, null, new OWLOntologyLoaderConfiguration());
    }

    /**
     * Creates an OWLXML handler with the specified top level handler.  This allows OWL/XML
     * representations of axioms to be embedded in abitrary XML documents e.g. DIG 2.0 documents.
     * (The default handler behaviour expects the top level element to be an Ontology
     * element).
     *
     * @param ontology The ontology object that the XML representation should be parsed into.
     */
    public VOWLXMLParserHandler(VOWLXMLDocument voRoot, OWLElementHandler<?> topHandler, OWLOntologyLoaderConfiguration configuration) {
    	//forced to call
        super(voRoot.getRevisionData(), topHandler, configuration);
        this.versionedOntologyRoot = voRoot;
        this.owlOntologyManager = voRoot.getRevisionData().getOWLOntologyManager();
        //this.ontology = voRoot.getRevisionData();
        this.bases = new Stack<URI>();
        this.configuration = configuration;
        handlerStack = new ArrayList<OWLElementHandler<?>>();
        prefixName2PrefixMap = new HashMap<String, String>();
        prefixName2PrefixMap.put("owl:", Namespaces.OWL.toString());
        prefixName2PrefixMap.put("xsd:", Namespaces.XSD.toString());
        if (topHandler != null) {
            handlerStack.add(0, topHandler);
        }
        handlerMap = new HashMap<String, OWLElementHandlerFactory>();

        addFactory(new AbstractVElementHandlerFactory(VERSIONED_ONTOLOGY_ROOT) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VersionedOntologyDocumentElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(RENDER_CONFIGURATION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new RenderConfigurationElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(VERSIONED_ONTOLOGY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VersionedOntologyElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(REVISION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new RevisionElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(CHANGE_SET) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new ChangeSetElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(V_ADD_AXIOM_CHANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VAxiomChangeElementHandler(handler);
            }
        }, V_REMOVE_AXIOM_CHANGE.getShortName());
        
        addFactory(new AbstractVElementHandlerFactory(V_ADD_IMPORT_CHANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VImportChangeElementHandler(handler);
            }
        }, V_REMOVE_IMPORT_CHANGE.getShortName());

        addFactory(new AbstractVElementHandlerFactory(V_ADD_ONTOLOGY_ANNOTATION_CHANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VOntologyAnnotationChangeElementHandler(handler);
            }
        }, V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE.getShortName());

        addFactory(new AbstractVElementHandlerFactory(V_MODIFY_ONTOLOGY_ID_CHANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VOntologyIDChangeElementHandler(handler);
            }
        }, V_MODIFY_ONTOLOGY_ID_NEW_ID.getShortName(), 
        V_MODIFY_ONTOLOGY_ID_OLD_ID.getShortName());
        
        //
        // Add all OwlOntology related handlers that are also used in the superclass.
        //
        addModifiedOntologyAndAxiomHandlers();
        addOriginalOntologyAndAxiomHandlers();
    }

    private void addModifiedOntologyAndAxiomHandlers() {
        addFactory(new AbstractElementHandlerFactory(ONTOLOGY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLOntologyHandlerModified(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(IMPORT) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLImportsHandlerModified(handler);
            }
        }, "Imports");
    	
    }
	
    /**
	 * 
	 */
	private void addOriginalOntologyAndAxiomHandlers() {

        addFactory(new AbstractElementHandlerFactory(ANNOTATION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAnnotationElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(LITERAL) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLLiteralElementHandler(handler);
            }
        }, "Constant");

        addFactory(new AbstractElementHandlerFactory(CLASS) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLClassElementHandler(handler);
            }
        }, "OWLClass");

        addFactory(new AbstractElementHandlerFactory(ANNOTATION_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAnnotationPropertyElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(ANNOTATION_PROPERTY_DOMAIN) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAnnotationPropertyDomainElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(ANNOTATION_PROPERTY_RANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAnnotationPropertyRangeElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(SUB_ANNOTATION_PROPERTY_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSubAnnotationPropertyOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectPropertyElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_INVERSE_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLInverseObjectPropertyElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataPropertyElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(NAMED_INDIVIDUAL) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLIndividualElementHandler(handler);
            }
        }, "Individual");

        addFactory(new AbstractElementHandlerFactory(DATA_COMPLEMENT_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataComplementOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_ONE_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataOneOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATATYPE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDatatypeElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATATYPE_RESTRICTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDatatypeRestrictionElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_INTERSECTION_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataIntersectionOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_UNION_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataUnionOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(FACET_RESTRICTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDatatypeFacetRestrictionElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_INTERSECTION_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectIntersectionOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_UNION_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectUnionOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_COMPLEMENT_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectComplementOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_ONE_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectOneOfElementHandler(handler);
            }
        });

        // Object Restrictions

        addFactory(new AbstractElementHandlerFactory(OBJECT_SOME_VALUES_FROM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectSomeValuesFromElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_ALL_VALUES_FROM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectAllValuesFromElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_HAS_SELF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectExistsSelfElementHandler(handler);
            }
        }, "ObjectExistsSelf");

        addFactory(new AbstractElementHandlerFactory(OBJECT_HAS_VALUE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectHasValueElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_MIN_CARDINALITY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectMinCardinalityElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_EXACT_CARDINALITY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectExactCardinalityElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_MAX_CARDINALITY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectMaxCardinalityElementHandler(handler);
            }
        });

        // Data Restrictions

        addFactory(new AbstractElementHandlerFactory(DATA_SOME_VALUES_FROM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataSomeValuesFromElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_ALL_VALUES_FROM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataAllValuesFromElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_HAS_VALUE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataHasValueElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_MIN_CARDINALITY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataMinCardinalityElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_EXACT_CARDINALITY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataExactCardinalityElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_MAX_CARDINALITY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataMaxCardinalityElementHandler(handler);
            }
        });

        // Axioms

        addFactory(new AbstractElementHandlerFactory(SUB_CLASS_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSubClassAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(EQUIVALENT_CLASSES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLEquivalentClassesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DISJOINT_CLASSES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDisjointClassesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DISJOINT_UNION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDisjointUnionElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(UNION_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLUnionOfElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(SUB_OBJECT_PROPERTY_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSubObjectPropertyOfAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY_CHAIN) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSubObjectPropertyChainElementHandler(handler);
            }
        }, "SubObjectPropertyChain");

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY_CHAIN) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSubObjectPropertyChainElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(EQUIVALENT_OBJECT_PROPERTIES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLEquivalentObjectPropertiesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DISJOINT_OBJECT_PROPERTIES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDisjointObjectPropertiesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY_DOMAIN) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectPropertyDomainElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY_RANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectPropertyRangeAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(INVERSE_OBJECT_PROPERTIES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLInverseObjectPropertiesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(FUNCTIONAL_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLFunctionalObjectPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(INVERSE_FUNCTIONAL_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLInverseFunctionalObjectPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(SYMMETRIC_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSymmetricObjectPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(ASYMMETRIC_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAsymmetricObjectPropertyElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(REFLEXIVE_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLReflexiveObjectPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(IRREFLEXIVE_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLIrreflexiveObjectPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(TRANSITIVE_OBJECT_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLTransitiveObjectPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(SUB_DATA_PROPERTY_OF) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSubDataPropertyOfAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(EQUIVALENT_DATA_PROPERTIES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLEquivalentDataPropertiesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DISJOINT_DATA_PROPERTIES) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDisjointDataPropertiesAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_PROPERTY_DOMAIN) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataPropertyDomainAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_PROPERTY_RANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataPropertyRangeAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(FUNCTIONAL_DATA_PROPERTY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLFunctionalDataPropertyAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(SAME_INDIVIDUAL) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLSameIndividualsAxiomElementHandler(handler);
            }
        }, "SameIndividuals");

        addFactory(new AbstractElementHandlerFactory(DIFFERENT_INDIVIDUALS) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDifferentIndividualsAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(CLASS_ASSERTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLClassAssertionAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY_ASSERTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLObjectPropertyAssertionAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(NEGATIVE_OBJECT_PROPERTY_ASSERTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLNegativeObjectPropertyAssertionAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(NEGATIVE_DATA_PROPERTY_ASSERTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLNegativeDataPropertyAssertionAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_PROPERTY_ASSERTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDataPropertyAssertionAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(ANNOTATION_ASSERTION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAnnotationAssertionElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory("EntityAnnotation") {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new LegacyEntityAnnotationElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DECLARATION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDeclarationAxiomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(IRI_ELEMENT) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new IRIElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(ABBREVIATED_IRI_ELEMENT) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new AbbreviatedIRIElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(ANONYMOUS_INDIVIDUAL) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLAnonymousIndividualElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(HAS_KEY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLHasKeyElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATATYPE_DEFINITION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OWLDatatypeDefinitionElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DL_SAFE_RULE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLRuleElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(BODY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OrigSWRLAtomListElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(HEAD) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OrigSWRLAtomListElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(VARIABLE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new OrigSWRLVariableElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(CLASS_ATOM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLClassAtomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(OBJECT_PROPERTY_ATOM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLObjectPropertyAtomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DATA_PROPERTY_ATOM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLDataPropertyAtomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(BUILT_IN_ATOM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLBuiltInAtomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(DIFFERENT_INDIVIDUALS_ATOM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLDifferentIndividualsAtomElementHandler(handler);
            }
        });

        addFactory(new AbstractElementHandlerFactory(SAME_INDIVIDUAL_ATOM) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new SWRLSameIndividualAtomElementHandler(handler);
            }
        });		
	}

//    @Override
//	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
////        try {
////        }
//    }
    public VOWLXMLDocument getDocumentRoot() {
    	return versionedOntologyRoot;
    }
	
    private void addFactory(OWLElementHandlerFactory factory, String... legacyElementNames) {
        handlerMap.put(factory.getElementName(), factory);
        for (String elementName : legacyElementNames) {
            handlerMap.put(elementName, factory);
        }
    }

    public OWLOntology getOntology() {
        return versionedOntologyRoot.getRevisionData();
    }

    public OWLDataFactory getDataFactory() {
        return getOWLOntologyManager().getOWLDataFactory();
    }

    @Override
	public void startDocument() throws SAXException {

    }

    @Override
	public void endDocument() throws SAXException {

    }

    @Override
	public void characters(char ch[], int start, int length) throws SAXException {
    	//Copied from superclass
        if (!handlerStack.isEmpty()) {
            try {
                OWLElementHandler<?> handler = handlerStack.get(0);
                if (handler.isTextContentPossible()) {
                    handler.handleChars(ch, start, length);
                }
            }
            catch (OWLException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	try {
        processXMLBase(attributes);
        if (localName.equals(OWLXMLVocabulary.PREFIX.getShortName())) {
            String name = attributes.getValue(OWLXMLVocabulary.NAME_ATTRIBUTE.getShortName());
            String iriString = attributes.getValue(OWLXMLVocabulary.IRI_ATTRIBUTE.getShortName());
            if (name != null && iriString != null) {
                if (name.endsWith(":")) {
                    prefixName2PrefixMap.put(name, iriString);
                }
                else {
                    prefixName2PrefixMap.put(name + ":", iriString);
                }
            }
            return;
        }
        OWLElementHandlerFactory handlerFactory = handlerMap.get(localName);
        if (handlerFactory != null) {
            OWLElementHandler<?> handler = handlerFactory.createHandler(this);
            if (!handlerStack.isEmpty()) {
                OWLElementHandler<?> topElement = handlerStack.get(0);
                handler.setParentHandler(topElement);
            }
            handlerStack.add(0, handler);
            for (int i = 0; i < attributes.getLength(); i++) {
                handler.attribute(attributes.getLocalName(i), attributes.getValue(i));
            }
            handler.startElement(localName);
        }
    }
    catch (OWLParserException e) {
        throw new TranslatedOWLParserException(e);
    }
//try {
//            processXMLBase(attributes);
//            //super.processXMLBase(attributes);
//            if (localName.equals(OWLXMLVocabulary.PREFIX.getShortName())) {
//                String name = attributes.getValue(OWLXMLVocabulary.NAME_ATTRIBUTE.getShortName());
//                String iriString = attributes.getValue(OWLXMLVocabulary.IRI_ATTRIBUTE.getShortName());
//                if (name != null && iriString != null) {
//                    if (name.endsWith(":")) {
//                        prefixName2PrefixMap.put(name, iriString);
//                    }
//                    else {
//                        prefixName2PrefixMap.put(name + ":", iriString);
//                    }
//                }
//                super.startElement(uri, localName, qName, attributes);
//                return;
//            }
//            // Shall we process this element?
//            if (isVersionedElement(localName)) {
//            	//owlElementProcessingMode = false;
//	            OWLElementHandlerFactory handlerFactory = handlerMap.get(localName);
//	            if (handlerFactory != null) {
//	                OWLElementHandler<?> handler = handlerFactory.createHandler(this);
//	                if (!handlerStack.isEmpty()) {
//	                    OWLElementHandler<?> topElement = handlerStack.get(0);
//	                    handler.setParentHandler(topElement);
//	                }
//	                handlerStack.add(0, handler);
//	                for (int i = 0; i < attributes.getLength(); i++) {
//	                    handler.attribute(attributes.getLocalName(i), attributes.getValue(i));
//	                }
//	                handler.startElement(localName);
//	            }
//            } else {
//            	//owlElementProcessingMode = true;
//            	//Not versioning related, let super handle ontology data or fail.
//            	super.startElement(uri, localName, qName, attributes);
//            }
//        }
//        catch (OWLParserException e) {
//            throw new TranslatedOWLParserException(e);
//        }
    }

    protected void processXMLBase(Attributes attributes) {
        String base = attributes.getValue(Namespaces.XML.toString(), "base");
        if (base != null) {
            bases.push(URI.create(base));
        }
        else {
            bases.push(bases.peek());
        }
    }

    /**
     * Return the base URI for resolution of relative URIs
     *
     * @return base URI or null if unavailable (xml:base not present and the
     *         document locator does not provide a URI)
     */
    public URI getBase() {
    		return bases.peek();
    }

    @Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
    	//code copied from superclass
            try {
                if (localName.equals(OWLXMLVocabulary.PREFIX.getShortName())) {
                    return;
                }
                if (!handlerStack.isEmpty()) {
                    OWLElementHandler<?> handler = handlerStack.remove(0);
                    handler.endElement();
                }
                bases.pop();
            }
            catch (OWLParserException e) {
                // Temporarily translate to a SAX parse exception
                throw new TranslatedOWLParserException(e);
            }
            catch (UnloadableImportException e) {
                // Temporarily translate to a SAX parse exception
                throw new TranslatedUnloadableImportException(e);
            }
    }
    
    @Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixName2PrefixMap.put(prefix, uri);
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;

        URI base = null;
        try {
            String systemId = locator.getSystemId();
            if (systemId != null)
                base = new URI(systemId);
        }
        catch (URISyntaxException e) {
        }

        bases.push(base);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getConfiguration()
	 */
	@Override
	public OWLOntologyLoaderConfiguration getConfiguration() {
        return configuration;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getLineNumber()
	 */
	@Override
	public int getLineNumber() {
        if (locator != null) {
            return locator.getLineNumber();
        }
        else {
            return -1;
        }
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getColumnNumber()
	 */
	@Override
	public int getColumnNumber() {
        if (locator != null) {
            return locator.getColumnNumber();
        }
        else {
            return -1;
        }
	}

    private Map<String, IRI> iriMap = new HashMap<String, IRI>();

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getIRI(java.lang.String)
	 */
	@Override
	public IRI getIRI(String iriStr) throws OWLParserException {
        try {
            IRI iri = iriMap.get(iriStr);
            if (iri == null) {
                URI uri = new URI(iriStr);
                if (!uri.isAbsolute()) {
                    URI base = getBase();
                    if (base == null)
                        throw new OWLXMLParserException("Unable to resolve relative URI", getLineNumber(), getColumnNumber());
//                    iri = IRI.create(getBase().resolve(uri));
                    iri = IRI.create(base + iriStr);
                }
                else {
                    iri = IRI.create(uri);
                }
                iriMap.put(iriStr, iri);
            }
            return iri;
        }
        catch (URISyntaxException e) {
            throw new OWLParserURISyntaxException(e, getLineNumber(), getColumnNumber());
        }
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getAbbreviatedIRI(java.lang.String)
	 */
	@Override
	public IRI getAbbreviatedIRI(String abbreviatedIRI) throws OWLParserException {
        String normalisedAbbreviatedIRI = getNormalisedAbbreviatedIRI(abbreviatedIRI);
        int sepIndex = normalisedAbbreviatedIRI.indexOf(':');
        String prefixName = normalisedAbbreviatedIRI.substring(0, sepIndex + 1);
        String localName = normalisedAbbreviatedIRI.substring(sepIndex + 1);
        String base = prefixName2PrefixMap.get(prefixName);
        if (base == null) {
            throw new OWLXMLParserException("Prefix name not defined: " + prefixName, getLineNumber(), getColumnNumber());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(localName);
        return getIRI(sb.toString());
	}

    private String getNormalisedAbbreviatedIRI(String input) {
        if (input.indexOf(':') != -1) {
            return input;
        }
        else {
            return ":" + input;
        }
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getPrefixName2PrefixMap()
	 */
	@Override
	public Map<String, String> getPrefixName2PrefixMap() {
        return prefixName2PrefixMap;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#resolveEntity(java.lang.String, java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        // superclass will refer it to defaulthandler, we could omit it here.
		return super.resolveEntity(publicId, systemId);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getOWLOntologyManager()
	 */
	@Override
	public OWLOntologyManager getOWLOntologyManager() {
        return owlOntologyManager;
	}
}
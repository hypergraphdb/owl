package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

/**
 * T006DataRangeTest.
 * 
 * This test is using graph and ontology counts in Hypergraph mode.
 * 
 * http://www.w3.org/TR/2009/REC-owl2-syntax-20091027/#Literals
 * 
 * DataRange :=
 *   Datatype |
 *   DataIntersectionOf |
 *   DataUnionOf |
 *   DataComplementOf |
 *   DataOneOf |
 *   DatatypeRestriction
 *
 * Ad DatatypeRestriction. Manchester Syntax:
 * 
 * 
 * D facet 	Meaning
 * 	< x, <= x, > x, >= x 	
 * length x, maxLength x, minLength x, 
 * pattern regexp, totalDigits x, fractionDigits x 	
 * 
 *  e.g. [>4, <2, maxLength "2"^^xsd.], also parsable: [maxLength "huhu@es"^^xsd:int], mind the reasoner.
 *
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 31, 2011
 */
public class T006DataRangeTest extends OntologyManagerTest {

	public T006DataRangeTest(int useImplementation) {
		super(useImplementation);
	}

	ManchesterOWLSyntaxEditorParser parser;
	ShortFormEntityChecker sfec;

	public OWLClassExpression createClassExpr(String text) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(m.getOWLDataFactory(), text); //takes long in HG
		parser.setOWLEntityChecker(sfec);
		try {
			return parser.parseClassExpression();
		} catch (ParserException e) {
			e.printStackTrace();
			throw new RuntimeException("Parser exception in Unit Test.", e);
		}
	}

	public OWLLiteral createLiteral(String text) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(m.getOWLDataFactory(), text); //takes long in HG
		parser.setOWLEntityChecker(sfec);
		try {
			return parser.parseLiteral();
		} catch (ParserException e) {
			e.printStackTrace();
			throw new RuntimeException("Parser exception in Unit Test.", e);
		}
	}

	public OWLDataRange createDataRange(String text) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(m.getOWLDataFactory(), text);
		parser.setOWLEntityChecker(sfec);
		try {
			return parser.parseDataRange();
		} catch (ParserException e) {
			e.printStackTrace();
			throw new RuntimeException("Parser exception in Unit Test.", e);
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		if (!isInitialized()) {
			super.setUp();
			TestData.fillOntology(df, o);
			// o = m.loadOntology(IRI.create(TestData.baseOntoPhysURI + "0"));
		}
		sfec = new ShortFormEntityChecker(new TestBidirectionalShortFormProviderAdapter(o, df,
				new SimpleShortFormProvider()));
	}

	/**
	 * Tests 3 Data Range Expressions: not..DataComplementOf(D), and...DataIntersectionOf(D1..Dn), or...DataUnionOf(D1..Dn).
	 */
	@Test
	public void testDataRange0SomeNotOrAnd() {
		long preDfNrDataRangesGraph = -1, preAxNrDataRangesGraph = -1, addNrDataRangesGraph = -1, remNrDataRangesGraph = -1;
		if (r != null) preDfNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		String clsExpr1 = " A_R some not not not not not A_DN ";
		String clsExpr2 = " B_R some (A_DN or  B_DN and (C_DN and (AA_DN or BB_DN))) ";
		String clsExpr3 = " C_R only (A_DN or  (B_DN and (xsd:byte))) ";
		
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLDatatype a_DN = df.getOWLDatatype(IRI.create("A_DN"));
		OWLDatatype aa_DN = df.getOWLDatatype(IRI.create("AA_DN"));
		OWLDatatype byte_DN = df.getOWLDatatype(XSDVocabulary.BYTE.getIRI());
		OWLClassExpression ce1 = createClassExpr(clsExpr1); //takes extremely long in HG more than 3 secs
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(a_CN, ce3);

		
		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		if (r != null) preAxNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) assertTrue(preAxNrDataRangesGraph == preDfNrDataRangesGraph + 11);
		// result!
		//int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preRef_a_DN = o.getReferencingAxioms(a_DN).size(); 
		int preRef_aa_DN = o.getReferencingAxioms(aa_DN).size(); 
		int preRef_byte_DN = o.getReferencingAxioms(byte_DN).size(); 
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		if (r != null) addNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) assertTrue(addNrDataRangesGraph == preAxNrDataRangesGraph);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addRef_a_DN = o.getReferencingAxioms(a_DN).size(); 
		int addRef_aa_DN = o.getReferencingAxioms(aa_DN).size(); 
		int addRef_byte_DN = o.getReferencingAxioms(byte_DN).size(); 
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 1);
		assertTrue(addRef_a_DN == preRef_a_DN + 3);
		assertTrue(addRef_aa_DN == preRef_aa_DN + 1);
		assertTrue(addRef_byte_DN == preRef_byte_DN + 2);
		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(aa_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(aa_DN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(aa_DN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(byte_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(byte_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(byte_DN).contains(axiom3));
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		if (r != null) remNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) assertTrue(addNrDataRangesGraph == remNrDataRangesGraph); //we just keep them in the graph.
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(a_DN).size() == 1); //declaration axiom
		assertTrue(o.getReferencingAxioms(aa_DN).size() == 1); //declaration axiom
		assertTrue(o.getReferencingAxioms(byte_DN).isEmpty());
	}
	
	/**
	 * Tests DataOneOf(v1...vn) literal enumerations {}.
	 */
	@Test
	public void testDataRange1LiteralEnumeration() {
		long preDfNrDataRangesGraph = -1, preAxNrDataRangesGraph = -1, addNrDataRangesGraph = -1, remNrDataRangesGraph = -1;
		if (r != null) preDfNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		String literal =  " \"15\"^^xsd:integer ";
		String clsExpr1 = " A_R some not {\"15\"^^xsd:integer, \"arregato@jp\", \"en-US\"^^xsd:language} ";
		//Multiple same literals should be collapsed into 1.
		String clsExpr2 = " B_R some not {\"arregato@jp\", \"en-US\"^^xsd:language, " + literal + "," + literal + "," + literal +  "} ";
		String clsExpr3 = " C_R only (A_DN or  (B_DN and {\"15\"^^xsd:float})) ";
		
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLDatatype float_DN = df.getFloatOWLDatatype();
		OWLDatatype integer_DN = df.getIntegerOWLDatatype();
		OWLLiteral l2 = createLiteral(literal);
		OWLClassExpression ce1 = createClassExpr(clsExpr1); //takes extremely long in HG more than 3 secs
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(a_CN, ce3);
		
		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		if (r != null) preAxNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		//TODO should be more than 7!
		if (r != null) assertTrue(preAxNrDataRangesGraph == preDfNrDataRangesGraph + 7);
		// result!
		int preRef_integer_DN = o.getReferencingAxioms(integer_DN).size(); 
		int preRef_float_DN = o.getReferencingAxioms(float_DN).size(); 
		
		int indexL2 = axiom2.toString().indexOf(l2.toString());
		assertTrue(indexL2 >= 0);
		// only one occurance of l2 == none after first.
		assertTrue(axiom2.toString().substring(indexL2 + l2.toString().length()).indexOf(l2.toString()) == -1);
		assertTrue(axiom1.toString().indexOf(ce1.toString()) >= 0);
		assertTrue(axiom2.toString().indexOf(ce2.toString()) >= 0);
		assertTrue(axiom2.toString().indexOf(ce3.toString()) >= 0);
		assertTrue(axiom3.toString().indexOf(ce3.toString()) >= 0);

		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		if (r != null) addNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) assertTrue(addNrDataRangesGraph == preAxNrDataRangesGraph);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addRef_integer_DN = o.getReferencingAxioms(integer_DN).size(); 
		int addRef_float_DN = o.getReferencingAxioms(float_DN).size(); 
		//boolean x;
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 4); //integer, language, float, plainliteral
		assertTrue(addRef_integer_DN == preRef_integer_DN + 2); 
		assertTrue(addRef_float_DN == preRef_float_DN + 2);
		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(integer_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(integer_DN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(integer_DN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(float_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom3));
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		if (r != null) remNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) assertTrue(addNrDataRangesGraph == remNrDataRangesGraph); //we just keep them in the graph.
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(integer_DN).size() == 0); //no declaration axiom
		assertTrue(o.getReferencingAxioms(float_DN).size() == 0); //no declaration axiom
	}
	
	
	/**
	 * Tests DatatypeRestriction(DN f1 v1 ... fn vn) using facetrestrictions, facets and literals.
	 */
	@Test
	public void testDataRange2DataTypeRestriction() {
		long preDfNrDataRangesGraph = -1, preAxNrDataRangesGraph = -1, addNrDataRangesGraph = -1, remNrDataRangesGraph = -1;
		long preDfNrFacetRestrictionsGraph = -1, preAxNrFacetRestrictionsGraph = -1, addNrFacetRestrictionsGraph = -1, remNrFacetRestrictionsGraph = -1;
		if (r != null) preDfNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) preDfNrFacetRestrictionsGraph = r.getNrOfAtomsByTypePlus(OWLFacetRestriction.class);
		String literal =  " \"asda\"^^xsd:string ";
		String clsExpr1 = "A_R some A_DN[length 10 , minLength 9 , maxLength 10 , pattern \"aPattern\"^^xsd:string , >= 9 , > 10 , <= 11 , < 11 , totalDigits 5 , fractionDigits \"huh\"]";
		//Multiple same literals should be collapsed into 1.
		String clsExpr2 = "A_R some A_DN[length 10 , minLength 9 , maxLength 10 , pattern " + literal + " , >= 9 , > 10 , <= 11 , < 11 , totalDigits 5 , fractionDigits \"huh\"]";;
		String clsExpr3 = " C_R only (A_DN or  (B_DN and {\"15\"^^xsd:float})) ";
		
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLDatatype float_DN = df.getFloatOWLDatatype();
		OWLDatatype integer_DN = df.getIntegerOWLDatatype();
		OWLLiteral l2 = createLiteral(literal);
		OWLClassExpression ce1 = createClassExpr(clsExpr1); //takes extremely long in HG more than 3 secs
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(a_CN, ce3);
		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		if (r != null) preAxNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) preAxNrFacetRestrictionsGraph = r.getNrOfAtomsByTypePlus(OWLFacetRestriction.class);
		//TODO should be more than 7!
		if (r != null) assertTrue(preAxNrDataRangesGraph == preDfNrDataRangesGraph + 5);
		if (r != null) assertTrue(preAxNrFacetRestrictionsGraph == preDfNrFacetRestrictionsGraph + 20);
		// result!
		int preRef_integer_DN = o.getReferencingAxioms(integer_DN).size(); 
		int preRef_float_DN = o.getReferencingAxioms(float_DN).size(); 
		
		int indexL2 = axiom2.toString().indexOf(l2.toString());
		assertTrue(indexL2 >= 0);
		// only one occurance of l2 == none after first.
		assertTrue(axiom2.toString().substring(indexL2 + l2.toString().length()).indexOf(l2.toString()) == -1);
		assertTrue(axiom1.toString().indexOf(ce1.toString()) >= 0);
		assertTrue(axiom2.toString().indexOf(ce2.toString()) >= 0);
		assertTrue(axiom2.toString().indexOf(ce3.toString()) >= 0);
		assertTrue(axiom3.toString().indexOf(ce3.toString()) >= 0);

		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		if (r != null) addNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) addNrFacetRestrictionsGraph = r.getNrOfAtomsByTypePlus(OWLFacetRestriction.class);
		if (r != null) assertTrue(addNrDataRangesGraph == preAxNrDataRangesGraph);
		if (r != null) assertTrue(addNrFacetRestrictionsGraph == preAxNrFacetRestrictionsGraph);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addRef_integer_DN = o.getReferencingAxioms(integer_DN).size(); 
		int addRef_float_DN = o.getReferencingAxioms(float_DN).size(); 
		//boolean x;
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 4); //integer, language, float, plainliteral
		assertTrue(addRef_integer_DN == preRef_integer_DN + 2); 
		assertTrue(addRef_float_DN == preRef_float_DN + 2);
		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(integer_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(integer_DN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(integer_DN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(float_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom3));
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		if (r != null) remNrDataRangesGraph = r.getNrOfAtomsByTypePlus(OWLDataRange.class);
		if (r != null) remNrFacetRestrictionsGraph = r.getNrOfAtomsByTypePlus(OWLFacetRestriction.class);
		if (r != null) assertTrue(addNrDataRangesGraph == remNrDataRangesGraph); //we just keep them in the graph.
		if (r != null) assertTrue(addNrFacetRestrictionsGraph== remNrFacetRestrictionsGraph); //we just keep them in the graph.
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(integer_DN).size() == 0); //no declaration axiom
		assertTrue(o.getReferencingAxioms(float_DN).size() == 0); //no declaration axiom		
	}

}

package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * T005LiteralsTest.
 * 
 * This test is using graph and ontology counts in Hypergraph mode.
 * 
 * http://www.w3.org/TR/2009/REC-owl2-syntax-20091027/#Literals
 * 
 * Literal := typedLiteral | stringLiteralNoLanguage | stringLiteralWithLanguage
 * typedLiteral := lexicalForm '^^' Datatype
 * lexicalForm := quotedString
 * stringLiteralNoLanguage := quotedString
 * stringLiteralWithLanguage := quotedString languageTag
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 26, 2011
 */
public class T005LiteralsTest extends OntologyManagerTest {

	public T005LiteralsTest(int useImplementation) {
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
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(m.getOWLDataFactory(), text);
		parser.setOWLEntityChecker(sfec);
		try {
			return parser.parseLiteral();
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

	@Test
	public void testLiterals0TypedLiteral() {
		long preDfNrLiteralsGraph = -1, preAxNrLiteralsGraph = -1, addNrLiteralsGraph = -1, remNrLiteralsGraph = -1;
		if (r != null) preDfNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		String literal1 = " \"15\"^^xsd:float ";
		String literal2 = " \"xxxxxxxxx\"^^xsd:integer ";
		String literal3 = " \"0.001E1000\"^^xsd:float ";
		String clsExpr1 = " (A_R value \"15\"^^xsd:integer) or (A_R value " + literal1 + " ) ";
		String clsExpr2 = " B_R value " + literal2;
		String clsExpr3 = " C_R value " + literal3;
		
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLDatatype integer_DN = df.getIntegerOWLDatatype();
		OWLDatatype float_DN = df.getFloatOWLDatatype();
		OWLClassExpression ce1 = createClassExpr(clsExpr1); //takes extremely long in HG more than 3 secs
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(a_CN, ce3);
		OWLLiteral l1 = createLiteral(literal1);
		OWLLiteral l2 = createLiteral(literal2);
		OWLLiteral l3 = createLiteral(literal3);

		assertTrue(axiom1.toString().indexOf(l1.toString()) >= 0);
		assertTrue(axiom2.toString().indexOf(l2.toString()) >= 0);
		assertTrue(axiom3.toString().indexOf(l3.toString()) >= 0);
		
		assertTrue(l1.getSignature().contains(l1.getDatatype()));
		assertTrue(l1.getLiteral().length() == 2);
		assertTrue(l1.getLang().equals(""));
		assertTrue(l2.getSignature().contains(l2.getDatatype()));
		assertTrue(l2.getLiteral().length() == 9);
		assertTrue(l2.getLang().equals(""));
		assertTrue(l3.getSignature().contains(l3.getDatatype()));
		assertTrue(l3.getLiteral().length() == 10);
		assertTrue(l3.getLang().equals(""));
		
		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		if (r != null) preAxNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(preAxNrLiteralsGraph == preDfNrLiteralsGraph + 7);
		// result!
		//int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preRef_Integer_DN = o.getReferencingAxioms(integer_DN).size(); 
		int preRef_Float_DN = o.getReferencingAxioms(float_DN).size(); 
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		if (r != null) addNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(addNrLiteralsGraph == preAxNrLiteralsGraph);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addRef_Integer_DN = o.getReferencingAxioms(integer_DN).size(); 
		int addRef_Float_DN = o.getReferencingAxioms(float_DN).size(); 
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 2);
		assertTrue(addRef_Integer_DN == preRef_Integer_DN + 2);
		assertTrue(addRef_Float_DN == preRef_Float_DN + 3);
		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(integer_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(integer_DN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(integer_DN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom3));
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		if (r != null) remNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(addNrLiteralsGraph == remNrLiteralsGraph);
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(integer_DN).isEmpty());
		assertTrue(o.getReferencingAxioms(float_DN).isEmpty());
	}
	
	@Test
	public void testLiterals0StringLiteralNoLanguage() {
		long preDfNrLiteralsGraph = -1, preAxNrLiteralsGraph = -1, addNrLiteralsGraph = -1, remNrLiteralsGraph = -1;
		if (r != null) preDfNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		String clsExpr1 = " (A_R value \"15\" ) or (A_R value \"15\"^^rdf:PlainLiteral ) ";
		String clsExpr2 = " B_R value  \"xxxxxxxxx\" ";
		String clsExpr3 = " C_R value \"0.001E1000\"^^rdf:PlainLiteral ";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLDatatype plainLiteral_DN = df.getRDFPlainLiteral();
		OWLClassExpression ce1 = createClassExpr(clsExpr1); //takes extremely long in HG more than 3 secs
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(a_CN, ce3);

		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		if (r != null) preAxNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(preAxNrLiteralsGraph == preDfNrLiteralsGraph + 4);
		// result!
		int preRef_plainLiteral_DN = o.getReferencingAxioms(plainLiteral_DN).size(); 
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).isEmpty());
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		if (r != null) addNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(addNrLiteralsGraph == preAxNrLiteralsGraph);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addRef_plainLiteral_DN = o.getReferencingAxioms(plainLiteral_DN).size(); 
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 1);
		assertTrue(addRef_plainLiteral_DN == preRef_plainLiteral_DN + 3);
		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).contains(axiom3));
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		if (r != null) remNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(addNrLiteralsGraph == remNrLiteralsGraph);
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).isEmpty());
	}
	
	@Test
	public void testLiterals0StringLiteralWithLanguage() {
		long preDfNrLiteralsGraph = -1, preAxNrLiteralsGraph = -1, addNrLiteralsGraph = -1, remNrLiteralsGraph = -1;
		if (r != null) preDfNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		String literal1 = " \"15@spanish\"^^rdf:PlainLiteral";
		String literal2 = " \"xxxxxx@x\"";		
		String literal3 = " \"0.001@E1000\"^^rdf:PlainLiteral ";
		String clsExpr1 = " (A_R value \"15@english\") or (A_R value " + literal1 + ") ";
		String clsExpr2 = " B_R value " + literal2;
		String clsExpr3 = " C_R value " + literal3;
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLDatatype plainLiteral_DN = df.getRDFPlainLiteral();
		OWLClassExpression ce1 = createClassExpr(clsExpr1); //takes extremely long in HG more than 3 secs
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(a_CN, ce3);

		OWLLiteral l1 = createLiteral(literal1);
		OWLLiteral l2 = createLiteral(literal2);
		OWLLiteral l3 = createLiteral(literal3);

		// Test Literal and axiom construction
		assertTrue(axiom1.toString().indexOf(l1.toString()) >= 0);
		assertTrue(axiom2.toString().indexOf(l2.toString()) >= 0);
		assertTrue(axiom3.toString().indexOf(l3.toString()) >= 0);
		
		assertTrue(l1.getSignature().contains(df.getRDFPlainLiteral()));
		assertTrue(l1.getLiteral().length() == 2);
		assertTrue(l1.getLang().equals("spanish"));
		assertTrue(l2.getSignature().contains(df.getRDFPlainLiteral()));
		assertTrue(l2.getLiteral().length() == 8);
		//FOUND PARSER ERROR ON LITERAL l2: getLiteral returns xxxxxx@x ==8, should be 6..
		assertTrue(l2.getLang().equals(""));
		assertTrue(l3.getSignature().contains(df.getRDFPlainLiteral()));
		assertTrue(l3.getLiteral().length() == 5);
		assertTrue(l3.getLang().equals("E1000"));

		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		if (r != null) preAxNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(preAxNrLiteralsGraph == preDfNrLiteralsGraph + 7);
		// result!
		int preRef_plainLiteral_DN = o.getReferencingAxioms(plainLiteral_DN).size(); 
		o.getReferencingAxioms(plainLiteral_DN).isEmpty();
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		if (r != null) addNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(addNrLiteralsGraph == preAxNrLiteralsGraph);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addRef_plainLiteral_DN = o.getReferencingAxioms(plainLiteral_DN).size(); 
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 1);
		assertTrue(addRef_plainLiteral_DN == preRef_plainLiteral_DN + 3);
		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).contains(axiom3));
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		if (r != null) remNrLiteralsGraph = r.getNrOfAtomsByType(OWLLiteralHGDB.class);
		if (r != null) assertTrue(addNrLiteralsGraph == remNrLiteralsGraph);
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(plainLiteral_DN).isEmpty());
	}	
}

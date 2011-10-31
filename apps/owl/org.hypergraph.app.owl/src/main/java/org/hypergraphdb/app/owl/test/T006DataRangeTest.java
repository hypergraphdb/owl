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
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

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
	public void testDataRange0SomeNot() {
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
		boolean x = addAxiomCount == preAxiomCount + 3;
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
		assertTrue(o.getReferencingAxioms(aa_DN).size() == 1); //declarartion axiom
		assertTrue(o.getReferencingAxioms(byte_DN).isEmpty());
	}
	
}

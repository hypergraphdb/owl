package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * T007DataPropertyRestrictionTest.
 * 
 * 
 * 
 * DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
 * DataAllValuesFrom := 'DataAllValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')' 
 * DataHasValue := 'DataHasValue' '(' DataPropertyExpression Literal ')' 
 *  
 * DataMinCardinality := 'DataMinCardinality' '(' nonNegativeInteger DataPropertyExpression [ DataRange ] ')'
 * DataMaxCardinality := 'DataMaxCardinality' '(' nonNegativeInteger DataPropertyExpression [ DataRange ] ')' 
 * DataExactCardinality := 'DataExactCardinality' '(' nonNegativeInteger DataPropertyExpression [ DataRange ] ')' 
 * 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 1, 2011
 */
public class T007DataPropertyRestrictionTest  extends OntologyManagerTest {
	
	public T007DataPropertyRestrictionTest(int useImplementation) {
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

	@Test
	public void testClassExpression0DataPropertyRestrictionsAxioms() {
//		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
//		OWLClass b_CN = df.getOWLClass(IRI.create("B_CN")); 
//		OWLClass c_CN = df.getOWLClass(IRI.create("C_CN")); 
//		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, b_CN);
//		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, b_CN, c_CN);
//		Set<OWLClass> aSet = new HashSet<OWLClass>();
//		aSet.add(b_CN);
//		aSet.add(c_CN);
//		OWLDisjointUnionAxiom axiom3 = df.getOWLDisjointUnionAxiom(a_CN, aSet);
//		//References Before Addition
//		int preAxiomCount = o.getAxiomCount();
//		//TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some result!
//		//int preAxiomsB_CN = o.getAxioms(b_CN).size();
//		//int preAxiomsC_CN = o.getAxioms(c_CN).size();
//		int preRefA_CN = o.getReferencingAxioms(a_CN).size(); //check this!
//		int preRefB_CN = o.getReferencingAxioms(b_CN).size();
//		int preRefC_CN = o.getReferencingAxioms(c_CN).size();
//		//add
//		m.addAxiom(o, axiom1);
//		m.addAxiom(o, axiom2);
//		m.addAxiom(o, axiom3);
//		int addAxiomCount = o.getAxiomCount();
//		int addRefA_CN = o.getReferencingAxioms(a_CN).size();
//		int addRefB_CN = o.getReferencingAxioms(b_CN).size();
//		int addRefC_CN = o.getReferencingAxioms(c_CN).size();
//		//assert after addition
//		assertTrue(addAxiomCount == preAxiomCount + 3);
//		//TODO assertTrue(addAxiomsA_CN == preAxiomsA_CN + 3);
//		//assertTrue(addAxiomsB_CN == preAxiomsB_CN + 1);
//		//assertTrue(addAxiomsC_CN == preAxiomsC_CN + 1);
//		assertTrue(addRefA_CN == preRefA_CN + 3);
//		assertTrue(addRefB_CN == preRefB_CN + 3);
//		assertTrue(addRefC_CN == preRefC_CN + 2);		
//		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
//		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
//		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
//		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
//		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom2));
//		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom3));		
//		//Remove
//		m.removeAxiom(o, axiom1);
//		m.removeAxiom(o, axiom2);
//		m.removeAxiom(o, axiom3);
//		//assert before addition == after removal
//		assertTrue(o.getAxiomCount() == preAxiomCount);
//		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
//		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
//		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
//		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
//		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom2));
//		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom3));		

	}
	
	
	@Test
	public void testClassExpression1DPR_OnlySomeBool() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty c_R = df.getOWLDataProperty(IRI.create("C_R")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		String clsExpr = "A_R only (not integer[>0, <10] or not float[>=0.1 , < 10.5] and A_DN[>100, < 1000])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_R).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_R).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		
	}
	
	@Test
	public void testClassExpression2DPR_value() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLClass c_CN = df.getOWLClass(IRI.create("C_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLLiteral b_v = createLiteral("\"Hulehup@de\"^^xsd:string"); 
		String clsExpr = "A_R some (not integer[>0, <10] or not float[>=0.1 , < 10.5] and B_DN[>10, < 20])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		//assertTrue(o.getReferencingAxioms(b_v).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		//assertFalse(o.getReferencingAxioms(b_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));
	}

	@Test
	public void testClassExpression3DPR_exactlyMinMax() {
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		OWLObjectProperty cc_PN = df.getOWLObjectProperty(IRI.create("CC_PN")); 
		OWLObjectProperty bb_PN = df.getOWLObjectProperty(IRI.create("BB_PN")); 
		//and is ObjectIntersection
		String clsExpr = "A_R max 5  (A_R max 5 A_DN) and (A_R min 3 B_DN)";
		String clsExpr2 = "A_R min 100  ";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(cc_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(bb_PN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(cc_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(bb_PN).contains(axiom1));

	}
	
	@Test
	public void testClassExpression4DPR_exactlyMinMaxQualified() {
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		OWLObjectProperty c_PN = df.getOWLObjectProperty(IRI.create("C_PN")); 
		OWLObjectProperty b_PN = df.getOWLObjectProperty(IRI.create("B_PN")); 		
		String clsExpr = "(A_R max 5 A_DN) and (A_R min 3 B_DN[leg])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(b_PN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(b_PN).contains(axiom1));
		
	}

	@Test
	public void testClassExpression5DPR_nAryDataRangeOnlySome() {
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		OWLObjectProperty c_PN = df.getOWLObjectProperty(IRI.create("C_PN")); 
		OWLObjectProperty b_PN = df.getOWLObjectProperty(IRI.create("B_PN")); 		
		String clsExpr = "A_CN and (C_CN or (A_CN or (A_PN exactly 5  (C_CN and not (B_PN max 0 (C_PN min 3 AA_CN))))))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(b_PN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(b_PN).contains(axiom1));
		
	}
	
	
	
}

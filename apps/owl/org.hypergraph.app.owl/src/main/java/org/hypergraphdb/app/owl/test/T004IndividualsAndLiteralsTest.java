package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * T004IndividualsAndLiteralsTest.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 26, 2011
 */
public class T004IndividualsAndLiteralsTest extends OntologyManagerTest {

	public T004IndividualsAndLiteralsTest(int useImplementation) {
		super(useImplementation);
	}

	ManchesterOWLSyntaxEditorParser parser;
	ShortFormEntityChecker sfec;

	public OWLClassExpression createClassExpr(String text) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(m.getOWLDataFactory(), text);
		parser.setOWLEntityChecker(sfec);
		try {
			return parser.parseClassExpression();
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
	public void testIndividuals0Anonymous() {
		String clsExpr1 = " { _:a1} or ( { _:a2, _:a3 }  and { _:a1, _:a4 }) and not { _:a5 }";
		String clsExpr2 = "  { _:a2 }";
		String clsExpr3 = "  { _:a3, _:a3, _:a3 }";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLAnonymousIndividual  _a1 = df.getOWLAnonymousIndividual("a1");
		OWLAnonymousIndividual  _a3 = df.getOWLAnonymousIndividual("_:a3");
		OWLAnonymousIndividual  _a5 = df.getOWLAnonymousIndividual("_:a5");
		OWLClassExpression ce1 = createClassExpr(clsExpr1);
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);

		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(ce1, ce3);

		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		// TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some
		// result!
		int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preAxioms_a1 = o.getAxioms(_a1).size();
		int preAxioms_a3 = o.getAxioms(_a3).size();
		int preAxioms_a5 = o.getAxioms(_a5).size();
		int preRef_a1 = o.getReferencingAxioms(_a1).size(); 
		int preRef_a3 = o.getReferencingAxioms(_a3).size(); 
		int preRef_a5 = o.getReferencingAxioms(_a5).size();
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addAxiomsA_CN = o.getAxioms(a_CN).size();
		int addAxioms_a1 = o.getAxioms(_a1).size();
		int addAxioms_a3 = o.getAxioms(_a3).size();
		int addAxioms_a5 = o.getAxioms(_a5).size();
		int addRef_a1 = o.getReferencingAxioms(_a1).size(); 
		int addRef_a3 = o.getReferencingAxioms(_a3).size(); 
		int addRef_a5 = o.getReferencingAxioms(_a5).size();
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount);
		assertTrue(addAxioms_a1 == preAxioms_a1);
		assertTrue(addAxioms_a3 == preAxioms_a3);
		assertTrue(addAxioms_a5 == preAxioms_a5);
		assertTrue(addAxiomsA_CN == preAxiomsA_CN + 2);
		assertTrue(addRef_a1 == preRef_a1 + 2);
		assertTrue(addRef_a3 == preRef_a3 + 3);
		assertTrue(addRef_a5 == preRef_a5 + 2);
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a1).contains(axiom1));
		assertTrue(o.getReferencingAxioms(_a1).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a3).contains(axiom1));
		assertTrue(o.getReferencingAxioms(_a3).contains(axiom2));
		assertTrue(o.getReferencingAxioms(_a3).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a5).contains(axiom1));
		assertFalse(o.getReferencingAxioms(_a5).contains(axiom2));
		assertTrue(o.getReferencingAxioms(_a5).contains(axiom3));
		assertTrue(o.getReferencedAnonymousIndividuals().size() == 5);
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(_a1).isEmpty());
		assertTrue(o.getReferencingAxioms(_a3).isEmpty());
		assertTrue(o.getReferencingAxioms(_a5).isEmpty());
		assertTrue(o.getReferencedAnonymousIndividuals().size() == 0);
	}

	@Test
	public void testIndividuals1Named() {
		String clsExpr1 = " { <A_aN>} or ( {<B_aN>, <C_aN> }  and { <AA_aN>, <BB_aN> }) and not { <BB_aN> }";
		String clsExpr2 = "  { <B_aN> }";
		String clsExpr3 = "  { <C_aN>, <C_aN>, <C_aN> }";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLNamedIndividual  A_aN = df.getOWLNamedIndividual(IRI.create("A_aN"));
		OWLNamedIndividual  C_aN = df.getOWLNamedIndividual(IRI.create("C_aN"));
		OWLNamedIndividual  BB_aN = df.getOWLNamedIndividual(IRI.create("BB_aN"));
		OWLClassExpression ce1 = createClassExpr(clsExpr1);
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);

		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(ce1, ce3);

		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		// TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some
		// result!
		int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preAxiomsA_aN = o.getAxioms(A_aN).size();
		int preAxiomsC_aN = o.getAxioms(C_aN).size();
		int preAxiomsBB_aN = o.getAxioms(BB_aN).size();
		int preRefA_aN = o.getReferencingAxioms(A_aN).size(); 
		int preRefC_aN = o.getReferencingAxioms(C_aN).size(); 
		int preRefBB_aN = o.getReferencingAxioms(BB_aN).size();
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addAxiomsA_CN = o.getAxioms(a_CN).size();
		int addAxiomsA_aN = o.getAxioms(A_aN).size();
		int addAxiomsC_aN = o.getAxioms(C_aN).size();
		int addAxiomsBB_aN = o.getAxioms(BB_aN).size();
		int addRefA_aN = o.getReferencingAxioms(A_aN).size(); 
		int addRefC_aN = o.getReferencingAxioms(C_aN).size(); 
		int addRefBB_aN = o.getReferencingAxioms(BB_aN).size();
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount);
		assertTrue(addAxiomsA_aN == preAxiomsA_aN);
		assertTrue(addAxiomsC_aN == preAxiomsC_aN);
		assertTrue(addAxiomsBB_aN == preAxiomsBB_aN);
		assertTrue(addAxiomsA_CN == preAxiomsA_CN + 2);
		assertTrue(addRefA_aN == preRefA_aN + 2);
		assertTrue(addRefC_aN == preRefC_aN + 3);
		assertTrue(addRefBB_aN == preRefBB_aN + 2);
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(A_aN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(A_aN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(C_aN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(C_aN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(C_aN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(BB_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(BB_aN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(BB_aN).contains(axiom3));
		assertTrue(o.getIndividualsInSignature().size() == 6);
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(A_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(A_aN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(A_aN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(C_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(C_aN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(C_aN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(BB_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(BB_aN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(BB_aN).contains(axiom3));
	}

	@Test
	public void testIndividuals2Anonymous_ObjectHasValue() {
		String clsExpr1 = "  <A_PN> value _:a1 or ( inverse <B_PN> value _:a2 ) or ( inverse <B_PN> value _:a2 ) or ( inverse <B_PN> value _:a2 )";
		String clsExpr2 = "  <A_PN> value _:a3 or ( inverse <B_PN> value _:a4 )";
		String clsExpr3 = "  <B_PN> value _:a1 or ( inverse <C_PN> value _:a5 )";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN"));
		OWLObjectProperty a_PN = df.getOWLObjectProperty(IRI.create("A_CN"));
		OWLObjectProperty c_PN = df.getOWLObjectProperty(IRI.create("C_CN"));
		OWLAnonymousIndividual  _a1 = df.getOWLAnonymousIndividual("a1");
		OWLAnonymousIndividual  _a3 = df.getOWLAnonymousIndividual("_:a3");
		OWLAnonymousIndividual  _a5 = df.getOWLAnonymousIndividual("_:a5");
		OWLClassExpression ce1 = createClassExpr(clsExpr1);
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);

		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(ce1, ce3);

		// References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount = o.getSignature().size();
		// TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some
		// result!
		int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preAxioms_a1 = o.getAxioms(_a1).size();
		int preAxioms_a3 = o.getAxioms(_a3).size();
		int preAxioms_a5 = o.getAxioms(_a5).size();
		int preRef_A_PN = o.getReferencingAxioms(a_PN).size(); 
		int preRef_C_PN = o.getReferencingAxioms(c_PN).size(); 
		int preRef_a1 = o.getReferencingAxioms(_a1).size(); 
		int preRef_a3 = o.getReferencingAxioms(_a3).size(); 
		int preRef_a5 = o.getReferencingAxioms(_a5).size();
		// add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount = o.getSignature().size();
		int addAxiomsA_CN = o.getAxioms(a_CN).size();
		int addAxioms_a1 = o.getAxioms(_a1).size();
		int addAxioms_a3 = o.getAxioms(_a3).size();
		int addAxioms_a5 = o.getAxioms(_a5).size();
		int addRef_A_PN = o.getReferencingAxioms(a_PN).size(); 
		int addRef_C_PN = o.getReferencingAxioms(c_PN).size(); 
		int addRef_a1 = o.getReferencingAxioms(_a1).size(); 
		int addRef_a3 = o.getReferencingAxioms(_a3).size(); 
		int addRef_a5 = o.getReferencingAxioms(_a5).size();
		// assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount);
		assertTrue(addAxioms_a1 == preAxioms_a1);
		assertTrue(addAxioms_a3 == preAxioms_a3);
		assertTrue(addAxioms_a5 == preAxioms_a5);
		assertTrue(addAxiomsA_CN == preAxiomsA_CN + 2);
		assertTrue(addRef_A_PN == preRef_A_PN + 2);
		assertTrue(addRef_C_PN == preRef_C_PN + 3);
		assertTrue(addRef_a1 == preRef_a1 + 2);
		assertTrue(addRef_a3 == preRef_a3 + 3);
		assertTrue(addRef_a5 == preRef_a5 + 2);
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(a_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_PN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_PN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a1).contains(axiom1));
		assertTrue(o.getReferencingAxioms(_a1).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a3).contains(axiom1));
		assertTrue(o.getReferencingAxioms(_a3).contains(axiom2));
		assertTrue(o.getReferencingAxioms(_a3).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a5).contains(axiom1));
		assertFalse(o.getReferencingAxioms(_a5).contains(axiom2));
		assertTrue(o.getReferencingAxioms(_a5).contains(axiom3));
		assertTrue(o.getReferencedAnonymousIndividuals().size() == 5);
		// Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		// assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);		
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_PN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_PN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(_a1).isEmpty());
		assertTrue(o.getReferencingAxioms(_a3).isEmpty());
		assertTrue(o.getReferencingAxioms(_a5).isEmpty());
		assertTrue(o.getReferencedAnonymousIndividuals().size() == 0);
	}
}

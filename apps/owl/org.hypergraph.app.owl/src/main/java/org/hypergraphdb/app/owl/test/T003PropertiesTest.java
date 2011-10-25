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
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * T003PropertiesTest.
 * Reliant on Testdata.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 21, 2011
 */
public class T003PropertiesTest extends OntologyManagerTest {


	public T003PropertiesTest(int useImplementation) {
		super(useImplementation);
	}

	ManchesterOWLSyntaxEditorParser parser;
	ShortFormEntityChecker sfec;

	public OWLClassExpression createClassExpr(String text) {
		ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(m.getOWLDataFactory(), text);
		parser.setOWLEntityChecker(sfec);
		try {
			return parser.parseClassExpression();
		}
		catch (ParserException e) {
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
			//o = m.loadOntology(IRI.create(TestData.baseOntoPhysURI + "0"));
		}
		sfec = new ShortFormEntityChecker(new TestBidirectionalShortFormProviderAdapter(o, df, new SimpleShortFormProvider()));
	}

	@Test
	public void testProperties0InverseOf() {
		String clsExpr1 = " inverse  inverse inverse inverse inverse <B_PN> Self";
		String clsExpr2 = " inverse <B_PN> Self";
		String clsExpr3 = " <B_PN> Self";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLObjectProperty b_PN = df.getOWLObjectProperty(IRI.create("B_PN"));
		OWLClassExpression ce1 = createClassExpr(clsExpr1);
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);

		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(ce1, ce3);

		//References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount   = o.getSignature().size();
		//TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some result!
		int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preAxiomsB_PN = o.getAxioms(b_PN).size();
		int preRefA_CN = o.getReferencingAxioms(a_CN).size(); //check this!
		int preRefB_PN = o.getReferencingAxioms(b_PN).size();
		//add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount   = o.getSignature().size();		
		//TODO 2011.10.24 HG FAILS ON GETAXIOMS WITH:
		int addAxiomsA_CN = o.getAxioms(a_CN).size();
		int addAxiomsB_PN = o.getAxioms(b_PN).size();
		int addRefA_CN = o.getReferencingAxioms(a_CN).size(); //check this!
		int addRefB_PN = o.getReferencingAxioms(b_PN).size();
		//assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount);
		//TODO assertTrue(addAxiomsA_CN == preAxiomsA_CN + 3);
		//assertTrue(addAxiomsB_CN == preAxiomsB_CN + 1);
		//assertTrue(addAxiomsC_CN == preAxiomsC_CN + 1);
		assertTrue(addAxiomsA_CN == preAxiomsA_CN + 2);
		assertTrue(addAxiomsB_PN  == preAxiomsB_PN);
		assertTrue(addRefB_PN == preRefB_PN + 3);		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(b_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(b_PN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(b_PN).contains(axiom3));		
		//Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		//assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(b_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(b_PN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(b_PN).contains(axiom3));		
	}

	@Test
	public void testProperties1TopBottom() {
		String clsExpr1 = " inverse  inverse inverse inverse inverse topObjectProperty Self";
		String clsExpr2 = " inverse bottomObjectProperty Self";
		String clsExpr3 = " topObjectProperty Self";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLObjectProperty top_PN = df.getOWLTopObjectProperty();
		OWLObjectProperty bottom_PN = df.getOWLBottomObjectProperty();
		OWLClassExpression ce1 = createClassExpr(clsExpr1);
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLClassExpression ce3 = createClassExpr(clsExpr3);

		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, ce1);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, ce2, ce3);
		OWLSubClassOfAxiom axiom3 = df.getOWLSubClassOfAxiom(ce1, ce3);

		//References Before Addition
		int preAxiomCount = o.getAxiomCount();
		int preSignatureCount   = o.getSignature().size();
		//TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some result!
		int preAxiomsA_CN = o.getAxioms(a_CN).size();
		int preAxiomsTop_PN = o.getAxioms(top_PN).size();
		int preAxiomsBottom_PN = o.getAxioms(bottom_PN).size();
		int preRefA_CN = o.getReferencingAxioms(a_CN).size(); //check this!
		int preRefTop_PN = o.getReferencingAxioms(top_PN).size();
		int preRefBottom_PN = o.getReferencingAxioms(bottom_PN).size();
		//add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		int addAxiomCount = o.getAxiomCount();
		int addSignatureCount   = o.getSignature().size();		
		//TODO 2011.10.24 HG FAILS ON GETAXIOMS WITH:
		int addAxiomsA_CN = o.getAxioms(a_CN).size();
		int addAxiomsTop_PN = o.getAxioms(top_PN).size();
		int addAxiomsBottom_PN = o.getAxioms(bottom_PN).size();
		int addRefA_CN = o.getReferencingAxioms(a_CN).size(); //check this!
		int addRefTop_PN = o.getReferencingAxioms(top_PN).size();
		int addRefBottom_PN = o.getReferencingAxioms(bottom_PN).size();
		//assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		assertTrue(addSignatureCount == preSignatureCount + 2); //top/bottom?
		//TODO assertTrue(addAxiomsA_CN == preAxiomsA_CN + 3);
		//assertTrue(addAxiomsB_CN == preAxiomsB_CN + 1);
		//assertTrue(addAxiomsC_CN == preAxiomsC_CN + 1);
		assertTrue(addAxiomsA_CN == preAxiomsA_CN + 2);
		assertTrue(addAxiomsTop_PN  == preAxiomsTop_PN);
		assertTrue(addAxiomsBottom_PN  == preAxiomsBottom_PN);
		assertTrue(addRefA_CN == preRefA_CN + 2);		
		assertTrue(addRefTop_PN == preRefTop_PN + 3);		
		assertTrue(addRefBottom_PN == preRefBottom_PN + 1);		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(top_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(top_PN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(top_PN).contains(axiom3));
		assertTrue(o.getReferencingAxioms(bottom_PN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(bottom_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(bottom_PN).contains(axiom3));
		//Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2); //Exception in handleremoved, because builtin was not member of ontology?.
		m.removeAxiom(o, axiom3);
		//assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertTrue(o.getSignature().size() == preSignatureCount);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(top_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(top_PN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(top_PN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(bottom_PN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(bottom_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(bottom_PN).contains(axiom3));
	}

}

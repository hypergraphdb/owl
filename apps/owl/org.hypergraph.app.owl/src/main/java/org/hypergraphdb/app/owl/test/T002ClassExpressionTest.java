package org.hypergraphdb.app.owl.test;


import java.util.HashSet;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.parser.ParserUtil;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

/**
 * ClassExpressionTest.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 21, 2011
 */
public class T002ClassExpressionTest extends OntologyManagerTest {

	
	public T002ClassExpressionTest(int useImplementation) {
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
		if (o == null) {
			super.setUp();
			TestData.fillOntology(df, o);
			//o = m.loadOntology(IRI.create(TestData.baseOntoPhysURI + "0"));
		}
		HashSet<OWLOntology> h = new HashSet<OWLOntology>();
		h.add(o);
	    sfec = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(h, new SimpleShortFormProvider()));
	}

	
	/**
	 * 
	 */
	@Test
	public void testClassExpressionAddRemove() {
		String clsExpr = "(<A_CN> OR (<A_CN> OR (<B_CN>)))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom djA = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, djA);
		
		m.removeAxiom(o, djA);
	}

	@Test
	public void testClassExpressionAddRemove2() {
		String clsExpr = "(<A_CN> OR (<B_CN> OR (<C_CN>)))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom djA = df.getOWLDisjointClassesAxiom(ce);

		String clsExpr2 = "(<A_CN> OR (<B_CN> OR (<C_CN> AND <AA_CN>)))";
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLEquivalentClassesAxiom eqA = df.getOWLEquivalentClassesAxiom(ce2);
		m.addAxiom(o, djA);
		m.addAxiom(o, eqA);
		//Query
		OWLClass c_CN = df.getOWLClass(IRI.create("C_CN")); 
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		assertTrue(o.getReferencingAxioms(c_CN).contains(djA));
		assertTrue(o.getReferencingAxioms(c_CN).contains(eqA));
		assertFalse(o.getReferencingAxioms(aa_CN).contains(djA));
		assertTrue(o.getReferencingAxioms(aa_CN).contains(eqA));
		m.removeAxiom(o, djA);
		m.removeAxiom(o, eqA);
		assertFalse(o.getReferencingAxioms(c_CN).contains(djA));
		assertFalse(o.getReferencingAxioms(c_CN).contains(eqA));
		assertFalse(o.getReferencingAxioms(aa_CN).contains(djA));
		assertFalse(o.getReferencingAxioms(aa_CN).contains(eqA));
	}

	@Test
	public void testClassExpressionAddRemove3() {
		String clsExpr = "(<A_CN> OR (<A_CN> OR (<B_CN>)))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom djA = df.getOWLDisjointClassesAxiom(ce);
	}
}

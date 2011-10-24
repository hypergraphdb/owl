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
 * ClassExpressionTest.
 * Reliant on Testdata.
 * 
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
		if (!isInitialized()) {
			super.setUp();
			TestData.fillOntology(df, o);
			//o = m.loadOntology(IRI.create(TestData.baseOntoPhysURI + "0"));
		}
		HashSet<OWLOntology> h = new HashSet<OWLOntology>();
		h.add(o);
	    sfec = new ShortFormEntityChecker(new BidirectionalShortFormProviderAdapter(h, new SimpleShortFormProvider()));
	}
	
	@Test
	public void testClassExpression0Axioms() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLClass b_CN = df.getOWLClass(IRI.create("B_CN")); 
		OWLClass c_CN = df.getOWLClass(IRI.create("C_CN")); 
		OWLSubClassOfAxiom axiom1 = df.getOWLSubClassOfAxiom(a_CN, b_CN);
		OWLDisjointClassesAxiom axiom2 = df.getOWLDisjointClassesAxiom(a_CN, b_CN, c_CN);
		Set<OWLClass> aSet = new HashSet<OWLClass>();
		aSet.add(b_CN);
		aSet.add(c_CN);
		OWLDisjointUnionAxiom axiom3 = df.getOWLDisjointUnionAxiom(a_CN, aSet);
		//References Before Addition
		int preAxiomCount = o.getAxiomCount();
		//TODO int preAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO works, some result!
		int preAxiomsB_CN = o.getAxioms(b_CN).size();
		int preAxiomsC_CN = o.getAxioms(c_CN).size();
		int preRefA_CN = o.getReferencingAxioms(a_CN).size(); //check this!
		int preRefB_CN = o.getReferencingAxioms(b_CN).size();
		int preRefC_CN = o.getReferencingAxioms(c_CN).size();
		//add
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		m.addAxiom(o, axiom3);
		int addAxiomCount = o.getAxiomCount();
		//int addAxiomsA_CN = o.getAxioms(a_CN).size(); //TODO 2011.10.24 HG FAILS ON GETAXIOMS WITH:
		/**
		java.lang.ClassCastException: org.hypergraphdb.query.impl.UnionResult cannot be cast to org.hypergraphdb.HGRandomAccessResult
		at org.hypergraphdb.query.impl.ZigZagIntersectionResult.init(ZigZagIntersectionResult.java:140)
		at org.hypergraphdb.query.impl.IntersectionQuery.execute(IntersectionQuery.java:52)
		at org.hypergraphdb.query.impl.IntersectionQuery.execute(IntersectionQuery.java:43)
		at org.hypergraphdb.query.impl.UnionQuery.execute(UnionQuery.java:42)
		at org.hypergraphdb.query.cond2qry.ExpressionBasedQuery.execute(ExpressionBasedQuery.java:640)
		at org.hypergraphdb.HyperGraph.find(HyperGraph.java:1504)
		at org.hypergraphdb.HGQuery$hg$9.call(HGQuery.java:1105)
		at org.hypergraphdb.HGQuery$hg$9.call(HGQuery.java:1)
		at org.hypergraphdb.transaction.HGTransactionManager.transact(HGTransactionManager.java:397)
		at org.hypergraphdb.transaction.HGTransactionManager.ensureTransaction(HGTransactionManager.java:323)
		at org.hypergraphdb.HGQuery$hg.getAll(HGQuery.java:1098)
		at org.hypergraphdb.HyperGraph.getAll(HyperGraph.java:1509)
		at org.hypergraphdb.atom.HGSubgraph.getAll(HGSubgraph.java:196)
		at org.hypergraphdb.app.owl.core.AbstractInternalsHGDB.getAxioms(AbstractInternalsHGDB.java:1102)
		at org.hypergraphdb.app.owl.HGDBOntologyImpl.getAxioms(HGDBOntologyImpl.java:1026)
		at org.hypergraphdb.app.owl.test.T002ClassExpressionTest.testClassExpression0Axioms(T002ClassExpressionTest.java:107)
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
		at java.lang.reflect.Method.invoke(Method.java:597)
		at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:44)
		at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:15)
		at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:41)
		at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:20)
		at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:28)
		at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:76)
		at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:50)
		at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)
		at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)
		at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)
		at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)
		at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)
		at org.junit.runners.ParentRunner.run(ParentRunner.java:236)
		at org.junit.runners.Suite.runChild(Suite.java:128)
		at org.junit.runners.Suite.runChild(Suite.java:24)
		at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)
		at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)
		at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)
		at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)
		at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)
		at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:31)
		at org.junit.runners.ParentRunner.run(ParentRunner.java:236)
		at org.junit.runners.Suite.runChild(Suite.java:128)
		at org.junit.runners.Suite.runChild(Suite.java:24)
		at org.junit.runners.ParentRunner$3.run(ParentRunner.java:193)
		at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:52)
		at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:191)
		at org.junit.runners.ParentRunner.access$000(ParentRunner.java:42)
		at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:184)
		at org.junit.runners.ParentRunner.run(ParentRunner.java:236)
		at org.eclipse.jdt.internal.junit4.runner.JUnit4TestReference.run(JUnit4TestReference.java:49)
		at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:38)
		at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:467)
		at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:683)
		at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:390)
		at org.eclipse.pde.internal.junit.runtime.RemotePluginTestRunner.main(RemotePluginTestRunner.java:62)
		at org.eclipse.pde.internal.junit.runtime.CoreTestApplication.run(CoreTestApplication.java:23)
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
		at java.lang.reflect.Method.invoke(Method.java:597)
		at org.eclipse.equinox.internal.app.EclipseAppContainer.callMethodWithException(EclipseAppContainer.java:587)
		at org.eclipse.equinox.internal.app.EclipseAppHandle.run(EclipseAppHandle.java:198)
		at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.runApplication(EclipseAppLauncher.java:110)
		at org.eclipse.core.runtime.internal.adaptor.EclipseAppLauncher.start(EclipseAppLauncher.java:79)
		at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:369)
		at org.eclipse.core.runtime.adaptor.EclipseStarter.run(EclipseStarter.java:179)
		at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
		at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
		at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
		at java.lang.reflect.Method.invoke(Method.java:597)
		at org.eclipse.equinox.launcher.Main.invokeFramework(Main.java:619)
		at org.eclipse.equinox.launcher.Main.basicRun(Main.java:574)
		at org.eclipse.equinox.launcher.Main.run(Main.java:1407)
		at org.eclipse.equinox.launcher.Main.main(Main.java:1383)

*/

		//int addAxiomsB_CN = o.getAxioms(b_CN).size();
		//int addAxiomsC_CN = o.getAxioms(c_CN).size();
		int addRefA_CN = o.getReferencingAxioms(a_CN).size();
		int addRefB_CN = o.getReferencingAxioms(b_CN).size();
		int addRefC_CN = o.getReferencingAxioms(c_CN).size();
		//assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 3);
		//TODO assertTrue(addAxiomsA_CN == preAxiomsA_CN + 3);
		//assertTrue(addAxiomsB_CN == preAxiomsB_CN + 1);
		//assertTrue(addAxiomsC_CN == preAxiomsC_CN + 1);
		assertTrue(addRefA_CN == preRefA_CN + 3);
		assertTrue(addRefB_CN == preRefB_CN + 3);
		assertTrue(addRefC_CN == preRefC_CN + 2);		
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom2));
		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom3));		
		//Remove
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		m.removeAxiom(o, axiom3);
		//assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom3));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom3));		
	}

	@Test
	public void testClassExpression1BooleanAddRemove() {
		String clsExpr = "(<A_CN> OR (<A_CN> OR (<B_CN>)))";
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLClass b_CN = df.getOWLClass(IRI.create("B_CN")); 
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		int preAxiomCount = o.getAxiomCount();
		//int preAxiomsA_CN = o.getAxioms(a_CN).size();
		//int preAxiomsB_CN = o.getAxioms(b_CN).size();
		int preRefA_CN = o.getReferencingAxioms(a_CN).size();
		int preRefB_CN = o.getReferencingAxioms(b_CN).size();
		m.addAxiom(o, axiom1);
		int addAxiomCount = o.getAxiomCount();
		//int addAxiomsA_CN = o.getAxioms(a_CN).size();
		//int addAxiomsB_CN = o.getAxioms(b_CN).size();
		int addRefA_CN = o.getReferencingAxioms(a_CN).size();
		int addRefB_CN = o.getReferencingAxioms(b_CN).size();
		//assert after addition
		assertTrue(addAxiomCount == preAxiomCount + 1);
		//assertTrue(addAxiomsA_CN == preAxiomsA_CN + 0); //because it's in a class expression and not defined.
		//assertTrue(addAxiomsB_CN == preAxiomsB_CN + 0); //because it's in a class expression and not defined.
		assertTrue(addRefA_CN == preRefA_CN + 1);
		assertTrue(addRefB_CN == preRefB_CN + 1);
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(b_CN).contains(axiom1));
		//Remove
		m.removeAxiom(o, axiom1);
		//assert before addition == after removal
		assertTrue(o.getAxiomCount() == preAxiomCount);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(b_CN).contains(axiom1));

	}

	@Test
	public void testClassExpression2BooleanAddRemove() {
		OWLClass c_CN = df.getOWLClass(IRI.create("C_CN")); 
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		String clsExpr = "(<A_CN> OR (<B_CN> OR (<C_CN>)))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);

		String clsExpr2 = "(<A_CN> or (<B_CN> or (<C_CN> and not <AA_CN>)))";
		OWLClassExpression ce2 = createClassExpr(clsExpr2);
		OWLEquivalentClassesAxiom axiom2 = df.getOWLEquivalentClassesAxiom(ce2);
		//int preAxiomsC_CN = o.getAxioms(c_CN).size();
		m.addAxiom(o, axiom1);
		m.addAxiom(o, axiom2);
		//Query
		//int addAxiomsC_CN = o.getAxioms(c_CN).size();
		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(aa_CN).contains(axiom2));
		//assertTrue(addAxiomsC_CN == preAxiomsC_CN + 0); //because it's in a class expression and not defined.		
		m.removeAxiom(o, axiom1);
		m.removeAxiom(o, axiom2);
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom2));
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom2));
		//assertTrue(o.getAxioms(c_CN).size() == preAxiomsC_CN);		
	}

	@Test
	public void testClassExpression3NotEnumerationAddRemove() {
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		OWLNamedIndividual c_aN = df.getOWLNamedIndividual(IRI.create("C_aN")); 
		OWLNamedIndividual b_aN = df.getOWLNamedIndividual(IRI.create("B_aN")); 
		String clsExpr = "<A_CN> and (<C_CN> or (<A_CN> or ({<A_aN>, <C_aN>} and (<AA_CN> and {<B_aN>}))))";
		//defines B
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_aN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(b_aN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(aa_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(b_aN).contains(axiom1));
	}
	
	@Test
	public void testClassExpression4OPR_OnlySomeOr() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLObjectProperty c_PN = df.getOWLObjectProperty(IRI.create("C_PN")); 
		OWLObjectProperty a_PN = df.getOWLObjectProperty(IRI.create("A_PN")); 
		String clsExpr = "<A_PN> only (<C_PN> some (<A_PN> only (<C_PN> some  (<A_PN> only (<C_PN> some <A_CN>)))))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_PN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_PN).contains(axiom1));		
	}
	
	@Test
	public void testClassExpression5OPR_value() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLClass c_CN = df.getOWLClass(IRI.create("C_CN")); 
		OWLObjectProperty c_PN = df.getOWLObjectProperty(IRI.create("C_PN")); 
		OWLObjectProperty a_PN = df.getOWLObjectProperty(IRI.create("A_PN")); 
		OWLNamedIndividual b_aN = df.getOWLNamedIndividual(IRI.create("B_aN")); 
		String clsExpr = "<A_CN> and (<C_CN> or (<A_CN> or (<A_PN> value <A_aN> and (<A_CN> and not (<C_PN> value <B_aN>)))))";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getReferencingAxioms(b_aN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_PN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(c_PN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(b_aN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_PN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(c_PN).contains(axiom1));
	}

	public void testClassExpression6OPR_exactlyMinMax() {
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		OWLObjectProperty cc_PN = df.getOWLObjectProperty(IRI.create("CC_PN")); 
		OWLObjectProperty bb_PN = df.getOWLObjectProperty(IRI.create("BB_PN")); 		
		String clsExpr = "<A_PN> only (<B_PN> some (<C_PN> only (<AA_PN> some  (<BB_PN> only (<CC_PN> some <AA_CN>)))))";
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

	public void testClassExpression7OPR_exactlyMinMaxQualified() {
		OWLClass aa_CN = df.getOWLClass(IRI.create("AA_CN")); 
		OWLObjectProperty c_PN = df.getOWLObjectProperty(IRI.create("C_PN")); 
		OWLObjectProperty b_PN = df.getOWLObjectProperty(IRI.create("B_PN")); 		
		String clsExpr = "<A_CN> and (<C_CN> or (<A_CN> or (<A_PN> exactly 5  (<C_CN> and not (<B_PN> max 0 (<C_PN> min 3 <AA_CN>))))))";
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

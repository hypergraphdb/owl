package org.hypergraphdb.app.owl.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.model.OWLDataIntersectionOfHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeRestrictionHGDB;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataExactCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataHasValueHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataMaxCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataMinCardinalityHGDB;
import org.hypergraphdb.app.owl.model.classexpr.restrict.OWLDataSomeValuesFromHGDB;
import org.hypergraphdb.app.owl.util.Path;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
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
	public void testClassExpression00DataPropertyRestrictionsAxioms() {
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
	public void testClassExpression01DPR_OnlyBool() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLDatatype a_DN = df.getOWLDatatype(IRI.create("A_DN")); 
		String clsExpr = "A_R only (not xsd:integer[>0, <10] or not xsd:float[>=0.1 , < 10.5] and A_DN[>100, < 1000])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		assertTrue(o.getTBoxAxioms(false).contains(axiom1));
		assertFalse(o.getABoxAxioms(false).contains(axiom1));
		
		if (r != null) {
			Path p = r.getPathFromOWLObjectToAxiom(a_DN, axiom1);
			assertTrue(p.startsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataAllValuesFrom.class,					
					OWLAxiom.class));
			assertTrue(p.endsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataAllValuesFrom.class,					
					OWLAxiom.class));
			assertTrue(p.indexOfObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataAllValuesFrom.class,					
					OWLAxiom.class) == 0);
			assertTrue(p.countObjectsOfClass(OWLDataIntersectionOfHGDB.class) == 1);
			assertTrue(p.containsObjectOfClass(OWLDataAllValuesFrom.class));
			assertFalse(p.hasDuplicates());
			assertTrue(p.indexOfObjectsOfClasses(OWLAxiom.class) == 4);
			assertTrue(p.size() == 5);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		
	}
	
	@Test
	public void testClassExpression02DPR_SomeBool() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLDatatype a_DN = df.getOWLDatatype(IRI.create("A_DN")); 
		String clsExpr = "A_R some (not xsd:integer[>0, <10] or not xsd:float[>=0.1 , < 10.5] and A_DN[>100, < 1000])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		assertTrue(o.getTBoxAxioms(false).contains(axiom1));
		assertFalse(o.getABoxAxioms(false).contains(axiom1));
		
		if (r != null) {
			Path p = r.getPathFromOWLObjectToAxiom(a_DN, axiom1);
			assertTrue(p.startsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataSomeValuesFromHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.endsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataSomeValuesFromHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.indexOfObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataSomeValuesFromHGDB.class,					
					OWLAxiomHGDB.class) == 0);
			assertTrue(p.countObjectsOfClass(OWLDataIntersectionOfHGDB.class) == 1);
			assertTrue(p.containsObjectOfClass(OWLDataSomeValuesFromHGDB.class));
			assertFalse(p.hasDuplicates());
			assertTrue(p.indexOfObjectsOfClasses(OWLAxiom.class) == 4);
			assertTrue(p.size() == 5);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		
	}
	
	@Test
	public void testClassExpression03DPR_value() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLDatatype float_DN = df.getFloatOWLDatatype(); 
		String clsExpr = " A_R value \"99.9999999\"^^xsd:float ";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		assertTrue(o.getTBoxAxioms(false).contains(axiom1));
		assertFalse(o.getABoxAxioms(false).contains(axiom1));
		
		if (r != null) {
			Path p = r.getPathFromOWLObjectToAxiom(float_DN, axiom1);
			assertTrue(p.startsWithObjectsOfClasses(OWLDatatype.class, 
					OWLLiteralHGDB.class, 
					OWLDataHasValueHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.endsWithObjectsOfClasses(OWLDatatype.class, 
					OWLLiteralHGDB.class, 
					OWLDataHasValueHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.indexOfObjectsOfClasses(OWLDatatype.class, 
					OWLLiteralHGDB.class, 
					OWLDataHasValueHGDB.class,					
					OWLAxiomHGDB.class) == 0);
			assertTrue(p.countObjectsOfClass(OWLDataHasValueHGDB.class) == 1);
			assertTrue(p.containsObjectOfClass(OWLDataHasValueHGDB.class));
			assertFalse(p.hasDuplicates());
			assertTrue(p.indexOfObjectsOfClasses(OWLAxiom.class) == 3);
			assertTrue(p.size() == 4);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(float_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(float_DN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		
	}

	@Test
	public void testClassExpression03DPR_Min() {
		long preDfObj = -1;
		if (r != null) { 
			preDfObj = r.getNrOfAtomsByType(OWLDataMinCardinalityHGDB.class);
		}
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		String clsExpr = "A_R min 5 ";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		if (r != null) {
			assertTrue(r.getNrOfAtomsByType(OWLDataMinCardinalityHGDB.class) == preDfObj +1);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		if (r != null) {
			assertTrue(r.getNrOfAtomsByType(OWLDataMinCardinalityHGDB.class) == preDfObj +1);
		}
		OWLDataMinCardinality ceH = (OWLDataMinCardinality) ce;
		assertTrue(ceH.getCardinality() == 5);
		assertTrue(ceH.getFiller() != null); //rdfs plainliteral, why?
		assertTrue(ceH.getDataPropertiesInSignature().contains(a_R));
		assertTrue(ceH.getObjectPropertiesInSignature().size() == 0);
		assertTrue(ceH.getSignature().contains(a_R));
		assertTrue(ceH.getProperty() == a_R);
	}

	@Test
	public void testClassExpression04DPR_Max() {
		long preDfObj = -1;
		if (r != null) { 
			preDfObj = r.getNrOfAtomsByType(OWLDataMaxCardinalityHGDB.class);
		}
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		String clsExpr = "A_R max 5 ";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		if (r != null) {
			assertTrue(r.getNrOfAtomsByType(OWLDataMaxCardinalityHGDB.class) == preDfObj +1);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		if (r != null) {
			assertTrue(r.getNrOfAtomsByType(OWLDataMaxCardinalityHGDB.class) == preDfObj +1);
		}
		OWLDataMaxCardinality ceH = (OWLDataMaxCardinality) ce;
		assertTrue(ceH.getCardinality() == 5);
		assertTrue(ceH.getFiller() != null); //rdfs plainliteral, why?
		assertTrue(ceH.getDataPropertiesInSignature().contains(a_R));
		assertTrue(ceH.getObjectPropertiesInSignature().size() == 0);
		assertTrue(ceH.getSignature().contains(a_R));
		assertTrue(ceH.getProperty() == a_R);
	}

	@Test
	public void testClassExpression05DPR_Exactly() {
		long preDfObj = -1;
		if (r != null) { 
			preDfObj = r.getNrOfAtomsByType(OWLDataExactCardinalityHGDB.class);
		}
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		String clsExpr = "A_R exactly 5 ";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		if (r != null) {
			assertTrue(r.getNrOfAtomsByType(OWLDataExactCardinalityHGDB.class) == preDfObj +1);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		if (r != null) {
			assertTrue(r.getNrOfAtomsByType(OWLDataExactCardinalityHGDB.class) == preDfObj +1);
		}
		OWLDataExactCardinality ceH = (OWLDataExactCardinality) ce;
		assertTrue(ceH.getCardinality() == 5);
		assertTrue(ceH.getFiller() != null); //rdfs plainliteral, why?
		assertTrue(ceH.getDataPropertiesInSignature().contains(a_R));
		assertTrue(ceH.getObjectPropertiesInSignature().size() == 0);
		assertTrue(ceH.getSignature().contains(a_R));
		assertTrue(ceH.getProperty() == a_R);
	}

	@Test
	public void testClassExpression06DPR_MinQualified() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLDatatype a_DN = df.getOWLDatatype(IRI.create("A_DN")); 
		String clsExpr = "A_R min 5 (not xsd:integer[>0, <10] or not xsd:float[>=0.1 , < 10.5] and A_DN[>100, < 1000])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		assertTrue(o.getTBoxAxioms(false).contains(axiom1));
		assertFalse(o.getABoxAxioms(false).contains(axiom1));
		
		if (r != null) {
			Path p = r.getPathFromOWLObjectToAxiom(a_DN, axiom1);
			assertTrue(p.startsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataMinCardinalityHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.endsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataMinCardinalityHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.indexOfObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataMinCardinalityHGDB.class,					
					OWLAxiomHGDB.class) == 0);
			assertTrue(p.countObjectsOfClass(OWLDataMinCardinalityHGDB.class) == 1);
			assertTrue(p.containsObjectOfClass(OWLDataMinCardinalityHGDB.class));
			assertFalse(p.hasDuplicates());
			assertTrue(p.indexOfObjectsOfClasses(OWLAxiom.class) == 4);
			assertTrue(p.size() == 5);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		
	}
	
	@Test
	public void testClassExpression07DPR_MaxQualified() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLDatatype a_DN = df.getOWLDatatype(IRI.create("A_DN")); 
		String clsExpr = "A_R max 5 (not xsd:integer[>0, <10] or not xsd:float[>=0.1 , < 10.5] and A_DN[>100, < 1000])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		assertTrue(o.getTBoxAxioms(false).contains(axiom1));
		assertFalse(o.getABoxAxioms(false).contains(axiom1));
		
		if (r != null) {
			Path p = r.getPathFromOWLObjectToAxiom(a_DN, axiom1);
			assertTrue(p.startsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataMaxCardinalityHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.endsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataMaxCardinalityHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.indexOfObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataMaxCardinalityHGDB.class,					
					OWLAxiomHGDB.class) == 0);
			assertTrue(p.countObjectsOfClass(OWLDataMaxCardinalityHGDB.class) == 1);
			assertTrue(p.containsObjectOfClass(OWLDataMaxCardinalityHGDB.class));
			assertFalse(p.hasDuplicates());
			assertTrue(p.indexOfObjectsOfClasses(OWLAxiom.class) == 4);
			assertTrue(p.size() == 5);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		
	}

	@Test
	public void testClassExpression08DPR_ExactlyQualified() {
		OWLClass a_CN = df.getOWLClass(IRI.create("A_CN")); 
		OWLDataProperty a_R = df.getOWLDataProperty(IRI.create("A_R")); 
		OWLDatatype a_DN = df.getOWLDatatype(IRI.create("A_DN")); 
		String clsExpr = "A_R exactly 1 (not xsd:integer[>0, <10] or not xsd:float[>=0.1 , < 10.5] and A_DN[>100, < 1000])";
		OWLClassExpression ce = createClassExpr(clsExpr);
		OWLDisjointClassesAxiom axiom1 = df.getOWLDisjointClassesAxiom(a_CN, ce);
		m.addAxiom(o, axiom1);
		assertTrue(o.getAxioms().contains(axiom1));
		assertTrue(o.getTBoxAxioms(false).contains(axiom1));
		assertFalse(o.getABoxAxioms(false).contains(axiom1));
		
		if (r != null) {
			Path p = r.getPathFromOWLObjectToAxiom(a_DN, axiom1);
			assertTrue(p.startsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataExactCardinalityHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.endsWithObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataExactCardinalityHGDB.class,					
					OWLAxiomHGDB.class));
			assertTrue(p.indexOfObjectsOfClasses(OWLDatatype.class, 
					OWLDatatypeRestrictionHGDB.class, 
					OWLDataIntersectionOfHGDB.class,
					OWLDataExactCardinalityHGDB.class,					
					OWLAxiomHGDB.class) == 0);
			assertTrue(p.countObjectsOfClass(OWLDataExactCardinalityHGDB.class) == 1);
			assertTrue(p.containsObjectOfClass(OWLDataExactCardinalityHGDB.class));
			assertFalse(p.hasDuplicates());
			assertTrue(p.indexOfObjectsOfClasses(OWLAxiom.class) == 4);
			assertTrue(p.size() == 5);
		}
		assertTrue(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertTrue(o.getReferencingAxioms(a_R).contains(axiom1));
		m.removeAxiom(o, axiom1);
		assertFalse(o.getReferencingAxioms(a_CN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_DN).contains(axiom1));
		assertFalse(o.getReferencingAxioms(a_R).contains(axiom1));		

	}


	@Test
	public void testClassExpression09DPR_nAryDataRangeOnly() {
		//DataAllValuesFrom := 'DataAllValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')'
		//see 8.4 Data Property Restrictions 
		// 
		// we don't seem to have classes for this in the OWL API.
		// 2011.11.02 waiting for email response from OWL-API list
		assertTrue("NOT YET IMPLEMENTED", false);
		
	}

	@Test
	public void testClassExpression10DPR_nAryDataRangeSome() {
		//DataSomeValuesFrom := 'DataSomeValuesFrom' '(' DataPropertyExpression { DataPropertyExpression } DataRange ')' 
		//see 8.4 Data Property Restrictions 
		// 
		// we don't seem to have classes for this in the OWL API.
		// 2011.11.02 waiting for email response from OWL-API list
		assertTrue("NOT YET IMPLEMENTED", false);
	}

	
	
}

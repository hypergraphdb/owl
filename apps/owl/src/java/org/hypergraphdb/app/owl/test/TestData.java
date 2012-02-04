package org.hypergraphdb.app.owl.test;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByDocumentIRIException;
import org.hypergraphdb.app.owl.exception.HGDBOntologyAlreadyExistsByOntologyIDException;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLMutableOntology;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * TestData creates some ontologies for a given repository.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 */
public class TestData {
	
	static Logger log = Logger.getLogger(TestData.class.getCanonicalName());
	
	public static String baseOntoURI = "http://www.miamidade.gov/ontologies/generatedTestData/TestA";
	public static String baseOntoPhysURI = "hgdb://generatedTestData/TestA";
	
	public static void ensureTestData(HGDBOntologyRepository r, int howMany) {
		//PHGDBOntologyManagerImpl m = HGDBOntologyRepository.createOWLOntologyManager();
		OWLDataFactoryHGDB df = OWLDataFactoryHGDB.getInstance();
		df.setHyperGraph(r.getHyperGraph());
		
		if (howMany < 0) throw new IllegalArgumentException("howMany < 0");
		for (int i =0; i < howMany; i++) {
			OWLOntologyID ontologyID = new OWLOntologyID(IRI.create(baseOntoURI + i));
			IRI documentIRI = IRI.create(baseOntoPhysURI + i);
			if (!r.existsOntology(ontologyID)) {
				OWLOntology o;
				try {
					o = r.createOWLOntology(ontologyID, documentIRI);
					fillOntology(df, o);
				} catch (HGDBOntologyAlreadyExistsByDocumentIRIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (HGDBOntologyAlreadyExistsByOntologyIDException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				log.info("Ontology already exists: " + ontologyID);
			}
		}		
	}
	
	public static void fillOntology(OWLDataFactory m, OWLOntology ont) {
		fillOntologyClasses(m, ont);
		fillOntologyDatatypes(m, ont);
		fillOntologyNamedIndividuals(m, ont);
		fillOntologyDataProperty(m, ont);
		fillOntologyObjectProperty(m, ont);
		fillOntologyAnnotationProperty(m, ont);		
	}
	public static void fillOntologyClasses(OWLDataFactory df, OWLOntology ont) {
		OWLClass a,b,c, aa, bb;		
		a = df.getOWLClass(IRI.create("A_CN"));
		b = df.getOWLClass(IRI.create("B_CN"));
		c = df.getOWLClass(IRI.create("C_CN"));
		aa = df.getOWLClass(IRI.create("AA_CN"));
		bb = df.getOWLClass(IRI.create("BB_CN"));
		List<OWLClass> l = Arrays.asList(new OWLClass[]{a, b, c, aa, bb});
		for (OWLClass cls : l) {
			addAxiom(ont, df.getOWLDeclarationAxiom(cls));
		}
		addAxiom(ont, df.getOWLSubClassOfAxiom(aa, a));
		addAxiom(ont, df.getOWLSubClassOfAxiom(bb, b));
		
	}
	public static void fillOntologyDatatypes(OWLDataFactory df, OWLOntology ont) {
		OWLDatatype a,b,c, aa, bb;		
		a = df.getOWLDatatype(IRI.create("A_DN"));
		b = df.getOWLDatatype(IRI.create("B_DN"));
		c = df.getOWLDatatype(IRI.create("C_DN"));
		aa = df.getOWLDatatype(IRI.create("AA_DN"));
		bb = df.getOWLDatatype(IRI.create("BB_DN"));
		List<OWLDatatype> l = Arrays.asList(new OWLDatatype[]{a, b, c, aa, bb});
		for (OWLDatatype dt : l) {
			addAxiom(ont, df.getOWLDeclarationAxiom(dt));
		}
	}

	public static void fillOntologyNamedIndividuals(OWLDataFactory df, OWLOntology ont) {
		OWLNamedIndividual a,b,c, aa, bb, cc;		
		a = df.getOWLNamedIndividual(IRI.create("A_aN"));
		b = df.getOWLNamedIndividual(IRI.create("B_aN"));
		c = df.getOWLNamedIndividual(IRI.create("C_aN"));
		aa = df.getOWLNamedIndividual(IRI.create("AA_aN"));
		bb = df.getOWLNamedIndividual(IRI.create("BB_aN"));
		cc = df.getOWLNamedIndividual(IRI.create("CC_aN"));
		List<OWLNamedIndividual> l = Arrays.asList(new OWLNamedIndividual[]{a, b, c, aa, bb, cc});
		for (OWLNamedIndividual dt : l) {
			addAxiom(ont, df.getOWLDeclarationAxiom(dt));
		}		
	}

	public static void fillOntologyDataProperty(OWLDataFactory df, OWLOntology ont) {
		OWLDataProperty a,b,c, aa, bb;		
		a = df.getOWLDataProperty(IRI.create("A_R"));
		b = df.getOWLDataProperty(IRI.create("B_R"));
		c = df.getOWLDataProperty(IRI.create("C_R"));
		aa = df.getOWLDataProperty(IRI.create("AA_R"));
		bb = df.getOWLDataProperty(IRI.create("BB_R"));
		List<OWLDataProperty> l = Arrays.asList(new OWLDataProperty[]{a, b, c, aa, bb});
		for (OWLDataProperty cls : l) {
			addAxiom(ont, df.getOWLDeclarationAxiom(cls));
		}
		addAxiom(ont, df.getOWLSubDataPropertyOfAxiom(aa, a));
		addAxiom(ont, df.getOWLSubDataPropertyOfAxiom(bb, b));
	}

	public static void fillOntologyObjectProperty(OWLDataFactory df, OWLOntology ont) {
		OWLObjectProperty a,b,c, aa, bb;		
		a = df.getOWLObjectProperty(IRI.create("A_PN"));
		b = df.getOWLObjectProperty(IRI.create("B_PN"));
		c = df.getOWLObjectProperty(IRI.create("C_PN"));
		aa = df.getOWLObjectProperty(IRI.create("AA_PN"));
		bb = df.getOWLObjectProperty(IRI.create("BB_PN"));
		List<OWLObjectProperty> l = Arrays.asList(new OWLObjectProperty[]{a, b, c, aa, bb});
		for (OWLObjectProperty cls : l) {
			addAxiom(ont, df.getOWLDeclarationAxiom(cls));
		}
		addAxiom(ont, df.getOWLSubObjectPropertyOfAxiom(aa, a));
		addAxiom(ont, df.getOWLSubObjectPropertyOfAxiom(bb, b));
	}
	
	public static void fillOntologyAnnotationProperty(OWLDataFactory df, OWLOntology ont) {
		OWLAnnotationProperty a,b,c, aa, bb;		
		a = df.getOWLAnnotationProperty(IRI.create("A_A"));
		b = df.getOWLAnnotationProperty(IRI.create("B_A"));
		c = df.getOWLAnnotationProperty(IRI.create("C_A"));
		aa = df.getOWLAnnotationProperty(IRI.create("AA_A"));
		bb = df.getOWLAnnotationProperty(IRI.create("BB_A"));
		List<OWLAnnotationProperty> l = Arrays.asList(new OWLAnnotationProperty[]{a, b, c, aa, bb});
		for (OWLAnnotationProperty cls : l) {
			addAxiom(ont, df.getOWLDeclarationAxiom(cls));
		}
		addAxiom(ont, df.getOWLSubAnnotationPropertyOfAxiom(aa, a));
		addAxiom(ont, df.getOWLSubAnnotationPropertyOfAxiom(bb, b));
	}
	
	public static void addAxiom(OWLOntology o, OWLAxiom a) {
        ((OWLMutableOntology)o).applyChange(new AddAxiom(o, a));
	}
}

package org.hypergraphdb.app.owl.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.test.versioning.TestContext;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * Static test utility methods...
 * 
 * @author borislav
 *
 */
public class TU
{
	public static ThreadLocal<TestContext> ctx = new ThreadLocal<TestContext>();
	
	public static TestContext ctx() { return ctx.get(); }
	
	public static TestContext newCtx(String dblocation)
	{
		TestContext ctx = new TestContext();
		ctx.graph = HGEnvironment.get(dblocation);
		ctx.r = new OntologyDatabase(dblocation);
		ctx.m = HGOntologyManagerFactory.getOntologyManager(dblocation);
		ctx.df = ctx.m.getOWLDataFactory();
		return ctx;
	}
	
	public static <T> Set<T> set(T...items)
	{
		return new HashSet<T>(Arrays.asList(items));
	}

	public static <T> List<T> list(T...items)
	{
		return Arrays.asList(items);
	}
	
	public static IRI iri(String name)
	{
		if (!name.startsWith("http:"))
			name = ctx().ontology().getOntologyID().getOntologyIRI().toString() + "#" + name;
		return IRI.create(name);
	}
	
	public static OWLClass owlClass(String name)
	{
		return ctx().df().getOWLClass(iri(name));
	}
	
	public static OWLNamedIndividual individual(String name)
	{
		return ctx().df().getOWLNamedIndividual(iri(name));
	}
	
	public static OWLAxiom a(OWLAxiom ax)
	{
		ctx().manager().applyChange(new AddAxiom(ctx().ontology(), ax));
		return ax;
	}
	
	public static OWLAxiom declare(OWLEntity e)
	{
		return ctx().df().getOWLDeclarationAxiom(e);
	}
	
	public static OWLAxiom aInstanceOf(OWLClassExpression cl, OWLIndividual ind)
	{
		OWLAxiom ax = ctx().df().getOWLClassAssertionAxiom(cl, ind);
		a(ax);
		return ax;
	}
	
	public static OWLAxiom aSubclassOf(OWLClassExpression sub, OWLClassExpression sup)
	{
		OWLAxiom ax = ctx().df().getOWLSubClassOfAxiom(sub, sup);
		a(ax);
		return ax;
	}

	public static OWLAxiom aProp(OWLObjectPropertyExpression property, OWLIndividual individual, OWLIndividual object)
	{
		OWLAxiom ax = ctx().df().getOWLObjectPropertyAssertionAxiom(property, individual, object);
		a(ax);
		return ax;
	}

	public static OWLAxiom aProp(OWLDataPropertyExpression property, OWLIndividual individual, OWLLiteral value)
	{
		OWLAxiom ax = ctx().df().getOWLDataPropertyAssertionAxiom(property, individual, value);
		a(ax);
		return ax;
	}

	public static OWLDataProperty dprop(String name)
	{
		return ctx().df().getOWLDataProperty(iri(name));
	}

	public static OWLObjectProperty oprop(String name)
	{
		return ctx().df().getOWLObjectProperty(iri(name));
	}
	
	public static OWLLiteral literal(String value)
	{
		return ctx().df().getOWLLiteral(value);
	}
}
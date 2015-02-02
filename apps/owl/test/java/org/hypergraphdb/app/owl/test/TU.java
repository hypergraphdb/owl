package org.hypergraphdb.app.owl.test;

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
	public static OntologyContext ctx;
	
	public static IRI iri(String name)
	{
		if (!name.startsWith("http:"))
			name = ctx.ontology().getOntologyID().getOntologyIRI().toString() + "#" + name;
		return IRI.create(name);
	}
	
	public static OWLClass owlClass(String name)
	{
		return ctx.df().getOWLClass(iri(name));
	}
	
	public static OWLNamedIndividual individual(String name)
	{
		return ctx.df().getOWLNamedIndividual(iri(name));
	}
	
	public static OWLAxiom a(OWLAxiom ax)
	{
		ctx.manager().addAxiom(ctx.ontology(), ax);
		return ax;
	}
	
	public static OWLAxiom declare(OWLEntity e)
	{
		return ctx.df().getOWLDeclarationAxiom(e);
	}
	
	public static OWLAxiom aInstanceOf(OWLClassExpression cl, OWLIndividual ind)
	{
		OWLAxiom ax = ctx.df().getOWLClassAssertionAxiom(cl, ind);
		a(ax);
		return ax;
	}
	
	public static OWLAxiom aSubclassOf(OWLClassExpression sub, OWLClassExpression sup)
	{
		OWLAxiom ax = ctx.df().getOWLSubClassOfAxiom(sub, sup);
		a(ax);
		return ax;
	}

	public static OWLAxiom aProp(OWLObjectPropertyExpression property, OWLIndividual individual, OWLIndividual object)
	{
		OWLAxiom ax = ctx.df().getOWLObjectPropertyAssertionAxiom(property, individual, object);
		a(ax);
		return ax;
	}

	public static OWLAxiom aProp(OWLDataPropertyExpression property, OWLIndividual individual, OWLLiteral value)
	{
		OWLAxiom ax = ctx.df().getOWLDataPropertyAssertionAxiom(property, individual, value);
		a(ax);
		return ax;
	}

	public static OWLDataProperty dprop(String name)
	{
		return ctx.df().getOWLDataProperty(iri(name));
	}

	public static OWLObjectProperty oprop(String name)
	{
		return ctx.df().getOWLObjectProperty(iri(name));
	}
	
	public static OWLLiteral literal(String value)
	{
		return ctx.df().getOWLLiteral(value);
	}
}
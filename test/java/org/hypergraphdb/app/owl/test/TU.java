package org.hypergraphdb.app.owl.test;


import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.test.versioning.TestContext;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity.ActionType;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
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
	
	public static IRI resourceIri(String resourcePath)
	{
		URL url = TU.class.getResource(resourcePath);
		if (url == null)
			throw new RuntimeException("Can't find resource " + resourcePath);
		try { return IRI.create(url); }
		catch (Exception ex) { throw new RuntimeException(ex); }
	}
	
	public static IRI iri(String name)
	{
		if (!name.startsWith("http:"))
			if (ctx().prefix != null)
				name = ctx().prefix + "#" + name;
			else
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

	public static OWLAxiom aProp(OWLDataPropertyExpression property, OWLIndividual individual, String value)
	{
		return aProp(property, individual, literal(value));
	}

	public static OWLAxiom aProp(OWLDataPropertyExpression property, OWLIndividual individual, int value)
	{
		return aProp(property, individual, ctx().df().getOWLLiteral(value));
	}
	
	public static OWLDataPropertyHGDB dprop(String name)
	{
		return (OWLDataPropertyHGDB)ctx().df().getOWLDataProperty(iri(name));
	}

	public static OWLObjectPropertyHGDB oprop(String name)
	{
		return (OWLObjectPropertyHGDB)ctx().df().getOWLObjectProperty(iri(name));
	}
	
	public static OWLLiteralHGDB literal(String value)
	{
		return (OWLLiteralHGDB)ctx().df().getOWLLiteral(value);
	}
	
	public static ActivityResult versionUpdate(HGHandle ontoHandle, 
											   ActionType actionType, 
											   OntologyDatabasePeer initiatingPeer, 
											   OntologyDatabasePeer otherPeer)
			throws ExecutionException, InterruptedException
	{
		RemoteOntology remoteOnto = initiatingPeer.remoteOnto(ontoHandle, initiatingPeer.remoteRepo(otherPeer.getPeer().getIdentity()));
		return initiatingPeer.getPeer().getActivityManager().initiateActivity(
				new VersionUpdateActivity(initiatingPeer.getPeer())
					.remoteOntology(initiatingPeer.getHyperGraph().getHandle(remoteOnto))
					.action(actionType)).get();		
	}
}
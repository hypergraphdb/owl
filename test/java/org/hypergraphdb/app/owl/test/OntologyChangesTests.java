package org.hypergraphdb.app.owl.test;

import static org.hypergraphdb.app.owl.test.TU.aProp;
import static org.hypergraphdb.app.owl.test.TU.declare;
import static org.hypergraphdb.app.owl.test.TU.dprop;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.oprop;
import static org.hypergraphdb.app.owl.test.TU.owlClass;

import java.util.List;

import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.model.axioms.OWLClassAssertionHGDB;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLAxiom;

public class OntologyChangesTests extends BaseTestOwl
{
	@Before
	public void createOntologyContext()
	{
		TU.ctx.set(TU.newCtx(dblocation));
		ctx = TU.ctx();
		String ontologyIri = iri_prefix + "_querytests";
		TU.ctx().prefix = ontologyIri;
		TU.ctx().o = super.make(ontologyIri);		
		declare(oprop("hasFriend"));
		declare(dprop("isNamed"));
		declare(individual("Bob"));
		aProp(dprop("isNamed"), individual("Bob"), "Bob");
		declare(individual("Tom"));
		aProp(dprop("isNamed"), individual("Tom"), "Tom");
		declare(individual("Elle"));
		aProp(dprop("isNamed"), individual("Elle"), "Elle");
		declare(individual("Anne"));
		aProp(dprop("isNamed"), individual("Anne"), "Anne");
		aProp(oprop("hasFriend"), individual("Elle"), individual("Bob"));
		aProp(oprop("hasFriend"), individual("Tom"), individual("Bob"));
		declare(owlClass("Person"));
	}

	
	@Test
	public void testIdempotentAdd()
	{
		aInstanceOf(owlClass("Person"), individual("Bob"));
		List<OWLAxiom> L = ctx.graph.getAll(hg.and(
			hg.type(OWLClassAssertionHGDB.class),
			hg.orderedLink(individual("Bob").getAtomHandle(), owlClass("Person").getAtomHandle())
		)); 		
		Assert.assertEquals(1, L.size());
		aInstanceOf(owlClass("Person"), individual("Bob"));
		aInstanceOf(owlClass("Person"), individual("Bob"));
		aInstanceOf(owlClass("Person"), individual("Bob"));
		aInstanceOf(owlClass("Person"), individual("Bob"));
		aInstanceOf(owlClass("Person"), individual("Bob"));
		L = ctx.graph.getAll(hg.and(
				hg.type(OWLClassAssertionHGDB.class),
				hg.orderedLink(individual("Bob").getAtomHandle(), owlClass("Person").getAtomHandle())
			));
		Assert.assertEquals(1, L.size());
	}	
}

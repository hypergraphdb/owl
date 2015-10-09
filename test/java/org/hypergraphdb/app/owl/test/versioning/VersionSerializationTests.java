package org.hypergraphdb.app.owl.test.versioning;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyImpl;
import org.hypergraphdb.app.owl.OntologyDatabase;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.ActivityUtils;
import org.hypergraphdb.util.HGUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.*;

import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;

import static org.hypergraphdb.app.owl.test.TU.*;

public class VersionSerializationTests extends VersioningTestBase
{
	
	@BeforeClass public static void setupDatabase()
	{
		System.out.println("Using db location " + dblocation + " for VersioningTestBase.");
		HGUtils.dropHyperGraphInstance(dblocation);
		TestContext ctx = new TestContext();
		ctx.graph = HGEnvironment.get(dblocation);
		ctx.r = new OntologyDatabase(dblocation);
		ctx.m = HGOntologyManagerFactory.getOntologyManager(dblocation);
		ctx.df = ctx.m.getOWLDataFactory();
		TU.ctx.set(ctx);
	}
	
	@Before public void beforeTest() throws Exception
	{ 
		ctx = (TestContext)TU.ctx();
		ctx.o = (HGDBOntology)ctx.m.createOntology(IRI.create(iri_prefix + "_" + "serialization")); 
		ctx.vr = new VersionManager(ctx.graph, "testuser");
		ctx.vo = ctx.vr.versioned(ctx.graph.getHandle(ctx.o));				
	}
	
	@After public void afterTest() throws Exception
	{
		remove(ctx.vo);
		assertTrue(ctx.graph.findAll(hg.type(HGDBOntologyImpl.class)).isEmpty());
	}

	/**
	 * Test ability to serialize and deserialize an empty ontology.
	 */
	@Test
	public void serializeEmpty()
	{
		String asxml = ActivityUtils.renderVersionedOntology(ctx.vo);
		System.out.println(asxml);
		remove(ctx.vo);
		VersionedOntology vo2 = ActivityUtils.storeClonedOntology(ctx.m, 
				ActivityUtils.parseVersionedDoc(ctx.m, new StringDocumentSource(asxml)));
		assertEquals(ctx.vo.getAtomHandle(), vo2.getAtomHandle());
		assertEquals(ctx.vo.getRootRevision(), vo2.getRootRevision());
		assertEquals(ctx.vo.getCurrentRevision(), vo2.getCurrentRevision());
		assertEquals(ctx.vo.getOntology(), vo2.getOntology());
		ctx.vo = vo2;
	}

	/**
	 * Test ability to serialize an ontology with a flushed change set
	 * but no revision yet - the changeset shouldn't be serialized. 
	 */
	@Test
	public void serializeNoRevisions()
	{
		a(aInstanceOf(owlClass("Bla"), individual("foo")));
		assertEquals(1, ctx.vo.changes().size());
		String asxml = ActivityUtils.renderVersionedOntology(ctx.vo);
		System.out.println(asxml);
		remove(ctx.vo);
		VersionedOntology vo2 = ActivityUtils.storeClonedOntology(ctx.m, 
									ActivityUtils.parseVersionedDoc(ctx.m, new StringDocumentSource(asxml)));
		assertEquals(ctx.vo.getAtomHandle(), vo2.getAtomHandle());
		assertEquals(ctx.vo.getRootRevision(), vo2.getRootRevision());
		assertEquals(ctx.vo.getCurrentRevision(), vo2.getCurrentRevision());
		assertEquals(ctx.vo.getOntology(), vo2.getOntology());
		assertTrue(vo2.changes().isEmpty());	
		ctx.vo = vo2;
	}
	
	/**
	 * Test ability to serialize an ontology with one revision only. 
	 */
	@Test
	public void serializeOneRevision()
	{
		a(aInstanceOf(owlClass("Bla"), individual("foo")));
		assertEquals(1, ctx.vo.changes().size());
		ctx.vo.commit("unittest", "one revision serialization");
		assertTrue(ctx.vo.changes().isEmpty());
		assertNotSame(ctx.vo.getRootRevision(), ctx.vo.getCurrentRevision());
		String asxml = ActivityUtils.renderVersionedOntology(ctx.vo);
		System.out.println(asxml);
		remove(ctx.vo);
		VersionedOntology vo2 = ActivityUtils.storeClonedOntology(ctx.m, 
				ActivityUtils.parseVersionedDoc(ctx.m, new StringDocumentSource(asxml)));
		assertEquals(ctx.vo.getAtomHandle(), vo2.getAtomHandle());
		assertEquals(ctx.vo.getRootRevision(), vo2.getRootRevision());
		assertEquals(ctx.vo.getCurrentRevision(), vo2.getCurrentRevision());
		assertEquals(ctx.vo.getOntology(), vo2.getOntology());
		assertTrue(vo2.changes().isEmpty());
		assertEquals(vo2.revision().parents(), ctx.vo.revision().parents());
		ctx.vo = vo2;
	}

	/**
	 * This tests the ability to have more than one revision with multiple
	 * intermediate ChangeSet commits in between.
	 */
	@Test
	public void serializeTwoRevisionsMultipleChangeCommits()
	{
		Revision initialRevision = ctx.vo.revision();
		declare(owlClass("User"));
		declare(owlClass("Employee"));
		aSubclassOf(owlClass("User"), owlClass("Employee"));
		Revision revision1 = ctx.vo.commit("anonymous", "First version");
		
		declare(individual("Pedro"));
		aInstanceOf(owlClass("Employee"), individual("Pedro"));
		aSubclassOf(owlClass("User"), owlClass("Customer"));
		declare(owlClass("LoyalCustomer"));
		
		aSubclassOf(owlClass("Customer"), owlClass("LoyalCustomer"));
		aInstanceOf(owlClass("LoyalCustomer"), individual("Mary"));
		aInstanceOf(owlClass("LoyalCustomer"), individual("Tom"));
		
		aInstanceOf(owlClass("Customer"), individual("John"));
		aInstanceOf(owlClass("Employee"), individual("Fred"));
		declare(oprop("isServing"));		
		aProp(oprop("isServing"), individual("Fred"), individual("Tom"));
		// no changes between last flush and the creation of a new revision
		Revision revision2 = ctx.vo.commit("administrator", "Second version by admin");		
		
		// some more in working set, shouldn't be serialized
		aProp(dprop("hasAge"), individual("Mary"), literal("54"));
				
		String asxml = ActivityUtils.renderVersionedOntology(ctx.vo);
		System.out.println(asxml);
		remove(ctx.vo);
		
		VersionedOntology vo2 = ActivityUtils.storeClonedOntology(ctx.m, 
									ActivityUtils.parseVersionedDoc(ctx.m, new StringDocumentSource(asxml)));
		assertEquals(ctx.vo.getAtomHandle(), vo2.getAtomHandle());
		assertEquals(ctx.vo.getRootRevision(), vo2.getRootRevision());
		assertEquals(ctx.vo.getCurrentRevision(), vo2.getCurrentRevision());
		assertEquals(ctx.vo.getOntology(), vo2.getOntology());
		assertTrue(vo2.changes().isEmpty());
		assertEquals(TU.set(vo2.revision().parents().toArray()), TU.set(revision1.getAtomHandle()));
		ctx.vo = vo2;
	}
	
	public static void main(String []argv)
	{
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(Request.method(VersionSerializationTests.class, "serializeTwoRevisionsMultipleChangeCommits"));
		System.out.println("Failures " + result.getFailureCount());
		if (result.getFailureCount() > 0)
		{
			for (Failure failure : result.getFailures())
			{
				failure.getException().printStackTrace();
			}
		}
	}
}

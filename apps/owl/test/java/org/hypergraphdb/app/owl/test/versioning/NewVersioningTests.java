package org.hypergraphdb.app.owl.test.versioning;

import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;
import static org.hypergraphdb.app.owl.test.TU.aProp;
import static org.hypergraphdb.app.owl.test.TU.dprop;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.literal;
import static org.hypergraphdb.app.owl.test.TU.oprop;
import static org.hypergraphdb.app.owl.test.TU.owlClass;

import java.io.File;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.newver.ChangeMark;
import org.hypergraphdb.app.owl.newver.ChangeSet;
import org.hypergraphdb.app.owl.newver.Revision;
import org.hypergraphdb.app.owl.newver.VersionManager;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.util.HGUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class NewVersioningTests
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbtest";
	
	static final String iri_prefix = "hgdb://UNITTESTONT_VERSIONED";
	
	TestContext ctx; 
		
	@BeforeClass public static void setup() throws Exception
	{
		HGUtils.dropHyperGraphInstance(dblocation);
		TestContext ctx = new TestContext();
		ctx.graph = HGEnvironment.get(dblocation);
		ctx.r = new HGDBOntologyRepository(dblocation);
		ctx.m = new HGOntologyManagerFactory().getOntologyManager(dblocation);
		ctx.df = ctx.m.getOWLDataFactory();
		TU.ctx = ctx;
	}
	
	
	@Rule public MyTestName testName = new MyTestName();
	static int i = 0;
	@Before public void beforeTest() throws Exception
	{ 
		System.out.println("before test");
		ctx = (TestContext)TU.ctx;
		ctx.o = (HGDBOntology)ctx.m.createOntology(IRI.create(
				iri_prefix + "_" + (++i))); 
		ctx.vr = new VersionManager(ctx.graph, "testuser");
		ctx.vo = ctx.vr.versioned(ctx.graph.getHandle(ctx.o));				
	}
	
	@After public void afterTest() 
	{
		System.out.println("after test");
	}
	
	//@Test 
	public void testNewUnVersioned() throws Exception
	{
		OWLOntology newonto = ctx.manager().createOntology(IRI.create("hgdb://newunversioned"));
		HGHandle hOnto = ctx.graph().getHandle(newonto);
		Assert.assertNotNull(hOnto);
		Assert.assertFalse(ctx.vrepo().isVersioned(hOnto));
	}
	
	//@Test 
	public void testVersioned() throws Exception
	{
		Assert.assertTrue(ctx.vrepo().isVersioned(ctx.vonto().getOntology()));
		HGDBOntology o2 = (HGDBOntology)ctx.m.createOntology(IRI.create("hgdb://newversioned"));
		Assert.assertFalse(ctx.vrepo().isVersioned(ctx.graph.getHandle(o2)));
	}
	
	//@Test 
	public void testSimpleChanges()
	{
		aInstanceOf(owlClass("ClassCommit"), individual("IndividualCommit"));	
		ChangeSet<VersionedOntology> changeSet = ctx.vonto().changes();
		// should have some changes here
		System.out.println(changeSet.changes());
		Revision revisionBefore = ctx.vonto().revision();
		// new revision should be created
		ctx.vonto().commit("test", "first changes");
		// no current changes
		System.out.println(ctx.vonto().changes());
		Assert.assertEquals(0, ctx.vonto().changes().size());		
		Revision lastCommitted = ctx.vonto().revision().parents().iterator().next();
		Assert.assertEquals(revisionBefore, lastCommitted);
		ChangeSet<?> fromLastCommitted = ctx.vonto().changes(ctx.vonto().revision()).get(0);
		Assert.assertEquals(changeSet, fromLastCommitted);		
	}
	
	@Test public void testManyRevisions()
	{
		aInstanceOf(owlClass("ClassCommit"), individual("IndividualCommit"));
		aProp(dprop("myLabel"), individual("IndividualCommit"), literal("Something"));
		ctx.vonto().commit("test", "first changes");
		aInstanceOf(owlClass("ClassPerson"), individual("Mucho"));		
		aProp(oprop("byUser"), individual("IndividualCommit"), individual("Mucho"));		
		ChangeMark mark = ctx.vonto().flushChanges(); // starts a new working change set, without creating a new revision
		ChangeSet<VersionedOntology> set = ctx.graph.get(mark.changeset());
		Assert.assertEquals(2, set.size());
		mark = ctx.vonto().flushChanges(); // empty changes flushed...
		set = ctx.graph.get(mark.changeset());
		Assert.assertEquals(0, set.size());
		ctx.vonto().commit("test", "second changes");
		Assert.assertEquals(2, ctx.vonto().changes(ctx.vonto().revision()).size());
	}
}
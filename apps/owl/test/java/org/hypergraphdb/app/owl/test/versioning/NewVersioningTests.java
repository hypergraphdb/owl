package org.hypergraphdb.app.owl.test.versioning;

import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;
import static org.hypergraphdb.app.owl.test.TU.aProp;
import static org.hypergraphdb.app.owl.test.TU.aSubclassOf;
import static org.hypergraphdb.app.owl.test.TU.declare;
import static org.hypergraphdb.app.owl.test.TU.dprop;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.literal;
import static org.hypergraphdb.app.owl.test.TU.oprop;
import static org.hypergraphdb.app.owl.test.TU.owlClass;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntology;
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
import org.junit.Rule;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class NewVersioningTests extends VersioningTestBase
{
		
	public static void main(String []argv)
	{
		try
		{
			setupDatabase();
			NewVersioningTests t = new NewVersioningTests();						
			t.beforeTest();
			t.testSimpleMerge();
			t.afterTest();
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
		}
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
	
	@Test 
	public void testNewUnVersioned() throws Exception
	{
		OWLOntology newonto = ctx.manager().createOntology(IRI.create("hgdb://newunversioned"));
		HGHandle hOnto = ctx.graph().getHandle(newonto);
		Assert.assertNotNull(hOnto);
		Assert.assertFalse(ctx.vrepo().isVersioned(hOnto));
	}
	
	@Test 
	public void testVersioned() throws Exception
	{
		Assert.assertTrue(ctx.vrepo().isVersioned(ctx.vonto().getOntology()));
		HGDBOntology o2 = (HGDBOntology)ctx.m.createOntology(IRI.create("hgdb://newversioned"));
		Assert.assertFalse(ctx.vrepo().isVersioned(ctx.graph.getHandle(o2)));
	}
	
	@Test 
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
		HGHandle lastCommitted = ctx.vonto().revision().parents().iterator().next();
		Assert.assertEquals(revisionBefore.getAtomHandle(), lastCommitted);
		ChangeSet<?> fromLastCommitted = ctx.vonto().changes(ctx.vonto().revision()).get(0);
		Assert.assertEquals(changeSet, fromLastCommitted);		
	}
	
	@Test 
	public void testManyRevisions()
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
		Assert.assertEquals(3, ctx.vonto().changes(ctx.vonto().revision()).size());
	}
	
	@Test
	public void testTags()
	{
		declare(owlClass("ClassCommit"));
		declare(owlClass("ClassChangePush"));
		aSubclassOf(owlClass("ClassCommit"), owlClass("ClassChangePush"));
		Revision firstRevision = ctx.vonto().revision();				
		ctx.vonto().revision().tag("initial");				
		ctx.vonto().commit("test", "first changes");
		Revision secondRevision = ctx.vonto().revision();		
		secondRevision.tag("basic classes");
		declare(owlClass("User"));
		declare(oprop("hasAuthor"));
		aInstanceOf(owlClass("User"), individual("Veve"));
		ctx.vonto().flushChanges();
		aProp(oprop("hasAuthor"), individual("GrandRelease"), individual("Veve"));
		ctx.vonto().commit("test2", "second changes");
		Revision thirdRevision = ctx.vonto().revision();
		Revision found = ctx.vrepo().revisionWithTag("initial");
		Assert.assertEquals(firstRevision, found);
		found.tag("initial2");
		found = ctx.vrepo().revisionWithTag("basic classes");
		Assert.assertEquals(secondRevision, found);
		found = ctx.vrepo().revisionWithTag("initial2");
		Assert.assertEquals(firstRevision, found);
		Assert.assertTrue(thirdRevision.tags().isEmpty());
		try
		{
			thirdRevision.tag("initial2");
		}
		catch (IllegalArgumentException ex)
		{
			Assert.assertTrue(ex.getMessage().contains("already used"));
		}
		Assert.assertEquals(HGUtils.set("initial", "initial2"), firstRevision.tags());
		secondRevision.untag("basic classes");
		Assert.assertTrue(secondRevision.tags().isEmpty());
	}
	
	@Test
	public void testLabels()
	{
		declare(owlClass("ClassCommit"));
		declare(owlClass("ClassChangePush"));
		aSubclassOf(owlClass("ClassCommit"), owlClass("ClassChangePush"));
		Revision firstRevision = ctx.vonto().revision();				
		ctx.vonto().revision().label("initial");				
		ctx.vonto().commit("test", "first changes");
		Revision secondRevision = ctx.vonto().revision();		
		declare(owlClass("User"));
		declare(oprop("hasAuthor"));
		aInstanceOf(owlClass("User"), individual("Veve"));
		ctx.vonto().flushChanges();
		secondRevision.label("basic classes"); // still labeling revision not change mark		
		aProp(oprop("hasAuthor"), individual("GrandRelease"), individual("Veve"));
		ctx.vonto().commit("test2", "second changes");
		firstRevision.label("initial2");
		Revision thirdRevision = ctx.vonto().revision();
		Assert.assertEquals(HGUtils.set(firstRevision), 
							ctx.vrepo().revisionsWithLabel("initial"));
		Assert.assertEquals(HGUtils.set(secondRevision),
							ctx.vrepo().revisionsWithLabel("basic classes"));		
		thirdRevision.label("initial2");		
		Assert.assertEquals(HGUtils.set(firstRevision, thirdRevision),
				ctx.vrepo().revisionsWithLabel("initial2"));			
		thirdRevision.unlabel("initial2");
		Assert.assertTrue(thirdRevision.labels().isEmpty());
		Assert.assertEquals(HGUtils.set("initial", "initial2"), firstRevision.labels());
	}	
	
	@Test
	public void testSimpleMerge()
	{
		// we create base revision and two conflicting child revisions
		// which we then merge
		declare(owlClass("ClassCommit"));
		declare(owlClass("ClassChangePush"));
		aSubclassOf(owlClass("ClassCommit"), owlClass("ClassChangePush"));
		ctx.vonto().commit("test", "first changes");
		Revision baseRevision = ctx.vonto().revision();				
		declare(owlClass("User"));
		declare(oprop("hasAuthor"));
		aInstanceOf(owlClass("User"), individual("Veve"));
		Revision B1 = ctx.vonto().commit("test", "branch 1");				
		// go back to first revision
		ctx.vonto().goTo(baseRevision);
		// branch with something else...
		aInstanceOf(owlClass("ClassCommit"), individual("PushBranchOnBaseA"));
		aInstanceOf(owlClass("ClassCommit"), individual("PushBranchOnBaseB"));
		declare(dprop("hasTimestamp"));
		aProp(dprop("hasTimestamp"), 
			  individual("PushBranchOnBaseA"), 
			  literal(Long.toString(System.currentTimeMillis())));
		Revision B2 = ctx.vonto().commit("test", "branch 2");		
		Revision merged = ctx.vonto().merge("test2", "simple merging", B1, B2);
		Assert.assertEquals(HGUtils.set(ctx.graph.getHandle(B1), 
										ctx.graph.getHandle(B2)),
							merged.parents());
	}
}
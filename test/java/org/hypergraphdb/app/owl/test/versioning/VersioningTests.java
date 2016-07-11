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
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedMetadata;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.util.HGUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;


public class VersioningTests extends VersioningTestBase
{
	@BeforeClass public static void setupDatabase()
	{
		System.out.println("Using db location " + dblocation + " for VersioningTestBase.");
		HGUtils.dropHyperGraphInstance(dblocation);		
		TU.ctx.set(TU.newCtx(dblocation));
	}
	
	public static void main(String []argv)
	{
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(Request.method(VersioningTests.class, "testSimpleChanges"));
		System.out.println("Failures " + result.getFailureCount());
		if (result.getFailureCount() > 0)
		{
			for (Failure failure : result.getFailures())
			{
				failure.getException().printStackTrace();
			}
		}		
	}
		
	
	@Rule public MyTestName testName = new MyTestName();
	static int i = 0;
	@Before public void beforeTest() throws Exception
	{ 
		System.out.println("before test");
		ctx = (TestContext)TU.ctx();
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
		HGCompositeType type = (HGCompositeType)ctx.graph.getTypeSystem().getAtomType(Revision.class);
		for (Iterator<String> iter = type.getDimensionNames(); iter.hasNext(); )
			System.out.println("Dim : " + iter.next());
		aInstanceOf(owlClass("ClassCommit"), individual("IndividualCommit"));	
		ChangeSet<VersionedOntology> changeSet = ctx.vonto().changes();
		// should have some changes here
		Revision revisionBefore = ctx.vonto().revision();
		// new revision should be created
		ctx.vonto().commit("test", "first changes");
		System.out.println("handle = " + ctx.vonto().revision().getAtomHandle());
		Assert.assertEquals("first changes", ctx.vonto().revision().comment());
		// re-open graph so caches get flushed and we still have everything stored
		// properly 
		ctx.graph().close();
		TU.ctx.set(TU.newCtx(dblocation));
		ctx.vo = ctx.vr.versioned(ctx.o.getAtomHandle());
		System.out.println("handle = " + ctx.vonto().revision().getAtomHandle());
		Assert.assertEquals("first changes", ctx.vonto().revision().comment());
		Assert.assertTrue(System.currentTimeMillis() > ctx.vonto().revision().timestamp() &&
						  (System.currentTimeMillis() - 1000*60) < ctx.vonto().revision().timestamp());
		// no current changes
		Assert.assertEquals(0, ctx.vonto().changes().size());		
		HGHandle lastCommitted = ctx.vonto().revision().parents().iterator().next();
		Assert.assertEquals(revisionBefore.getAtomHandle(), lastCommitted);
		List<Change<VersionedOntology>> fromLastCommitted = versioning.changes(ctx.graph, 
																			ctx.vonto().getCurrentRevision(), 
																			lastCommitted);
		Assert.assertEquals(changeSet.changes(), fromLastCommitted);		
	}
	
	@Test
	public void testNoChangesRevisions()
	{
		HGHandle currentChanges = ctx.vonto().getWorkingChanges();
		Revision rev = ctx.vonto().commit("test", "no changes revision");
		assertEquals(currentChanges, 
				ctx.graph().findOne(hg.and(hg.type(ChangeLink.class), 
									       hg.orderedLink(rev.getAtomHandle(), 
									    		   		  rev.parents().iterator().next()))));
		aInstanceOf(owlClass("ClassNoChangesTest"), individual("NoEmptySetIfFlushAlready"));
		aProp(dprop("theCount"), individual("NoEmptySetIfFlushAlready"), literal("10"));
		currentChanges = ctx.vonto().getWorkingChanges();
		rev = ctx.vonto().commit("test", "flushed changes revision");
		assertEquals(currentChanges, ctx.graph().findOne(hg.and(hg.type(ChangeLink.class),
				hg.orderedLink(rev.getAtomHandle(), rev.parents().iterator().next()))));
	}
	
	@Test 
	public void testManyRevisions()
	{
		aInstanceOf(owlClass("ClassCommit"), individual("IndividualCommit"));
		aProp(dprop("myLabel"), individual("IndividualCommit"), literal("Something"));
		System.out.println(ctx.vonto().changes().changes());		
		ctx.vonto().commit("test", "first changes");
		HGHandle previousRev = ctx.vonto().getCurrentRevision();		
		aInstanceOf(owlClass("ClassPerson"), individual("Mucho"));		
		aProp(oprop("byUser"), individual("IndividualCommit"), individual("Mucho"));
		System.out.println(ctx.vonto().changes().changes());
		ctx.vonto().commit("test", "second changes");
		// a commit doesn't create a new change record if there are not working set changes and
		// we already have a change record after the current/last revision
		Assert.assertEquals(2, versioning.changes(ctx.graph, 
												  ctx.vonto().getCurrentRevision(), 
												  previousRev).size());
	}
	
	@Test
	public void testLabels()
	{
		VersionedMetadata<VersionedOntology> metadata = ctx.vonto().metadata();
		declare(owlClass("ClassCommit"));
		declare(owlClass("ClassChangePush"));
		aSubclassOf(owlClass("ClassCommit"), owlClass("ClassChangePush"));
		Revision firstRevision = ctx.vonto().revision();				
		metadata.label(firstRevision.getAtomHandle(), "initial");				
		ctx.vonto().commit("test", "first changes");
		Revision secondRevision = ctx.vonto().revision();		
		declare(owlClass("User"));
		declare(oprop("hasAuthor"));
		aInstanceOf(owlClass("User"), individual("Veve"));
		 // still labeling revision not change mark
		metadata.label(secondRevision.getAtomHandle(), "basic classes");		
		aProp(oprop("hasAuthor"), individual("GrandRelease"), individual("Veve"));
		ctx.vonto().commit("test2", "second changes");
		metadata.label(firstRevision.getAtomHandle(), "initial2");
		Revision thirdRevision = ctx.vonto().revision();
		Assert.assertEquals(HGUtils.set(firstRevision), 
							metadata.revisionsWithLabel("initial"));
		Assert.assertEquals(HGUtils.set(secondRevision),
							metadata.revisionsWithLabel("basic classes"));		
		metadata.label(thirdRevision.getAtomHandle(), "initial2");		
		Assert.assertEquals(HGUtils.set(firstRevision, thirdRevision),
				metadata.revisionsWithLabel("initial2"));			
		metadata.unlabel(thirdRevision.getAtomHandle(), "initial2");
		Assert.assertTrue(metadata.labels(thirdRevision.getAtomHandle()).isEmpty());
		Assert.assertEquals(HGUtils.set("initial", "initial2"), 
							metadata.labels(firstRevision.getAtomHandle()));
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
		Revision merged = ctx.vonto().merge("test2", "simple merging", "branch 1", B1, B2);
		ctx.vonto().goTo(merged);
		Assert.assertEquals(HGUtils.set(ctx.graph.getHandle(B1), 
										ctx.graph.getHandle(B2)),
							merged.parents());
	}
		
	@Test
	public void testDeleteRevisionLinear()
	{
		declare(owlClass("ClassCommit"));
		declare(owlClass("ClassChangePush"));
		aSubclassOf(owlClass("ClassCommit"), owlClass("ClassChangePush"));
		ctx.vonto().commit("test", "first changes");
		Revision r1 = ctx.vonto().revision();				
		declare(owlClass("User"));
		declare(oprop("hasAuthor"));
		aInstanceOf(owlClass("User"), individual("Veve"));
		Revision r2 = ctx.vonto().commit("test", "second changes", "branch 1");
		assertNotNull(ctx.vonto().metadata().findBranch("branch 1"));
		assertEquals(ctx.vonto().revision().branchHandle(), 
					 ctx.vonto().metadata().findBranch("branch 1").getAtomHandle());
		ctx.vonto().dropHeadRevision(r2.getAtomHandle());
		assertEquals(r1.getAtomHandle(), ctx.vonto().getCurrentRevision());
		assertNull(ctx.vonto().metadata().findBranch("branch 1"));
	}
	
	@Test
	public void testDeleteRevisionMultipleParents()
	{
		// Delete possible here because each parent from different branch.
		throw new RuntimeException("TODO");
	}
	
	@Test
	public void testDeleteRevisionMultipleParentsForbidden()
	{
		// Delete impossible because two parents have the same branch.
		throw new RuntimeException("TODO");
	}
	
}
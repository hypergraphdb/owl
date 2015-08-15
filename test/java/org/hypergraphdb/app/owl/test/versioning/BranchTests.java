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
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.util.HGUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

public class BranchTests extends VersioningTestBase
{
	@BeforeClass public static void setupDatabase()
	{
		System.out.println("Using db location " + dblocation + " for VersioningTestBase.");
		HGUtils.dropHyperGraphInstance(dblocation);		
		TU.ctx.set(TU.newCtx(dblocation));
	}

	@Before public void beforeTest() throws Exception
	{ 
		ctx = (TestContext)TU.ctx();
		ctx.o = (HGDBOntology)ctx.m.createOntology(IRI.create(iri_prefix + "_" + "branching")); 
		ctx.vr = new VersionManager(ctx.graph, "testuser");
		ctx.vo = ctx.vr.versioned(ctx.graph.getHandle(ctx.o));				
	}
	
	@Test
	public void testBranchAsVersionedChange()
	{
		// creating a branch is like any other change, except it's
		// tied to a commit
		// if the revision is deleted altogether, wiped out, then
		// the branch atom should be gone with it
		
	}
	
	@Test
	public void testSimpleBranchLine()
	{
		declare(owlClass("ClassCommit"));
		declare(owlClass("ClassChangePush"));
		aSubclassOf(owlClass("ClassCommit"), owlClass("ClassChangePush"));
		Revision revision1 = ctx.vonto().revision();
		Assert.assertNull(revision1.branchHandle());
		// master branch
		ctx.vonto().commit("test", "first changes", "master");
		// we should have a branch named "master" automatically created now
		final HGHandle masterBranch = hg.findOne(ctx.graph, hg.and(hg.type(Branch.class), hg.eq("name", "master")));
		Assert.assertNotNull(masterBranch);
		Revision revision2 = ctx.vonto().revision();
		Assert.assertEquals(masterBranch, revision2.branchHandle());
		declare(owlClass("User"));
		declare(oprop("hasAuthor"));
		aInstanceOf(owlClass("User"), individual("Veve"));
		//ctx.vonto().flushChanges();
		aProp(oprop("hasAuthor"), individual("GrandRelease"), individual("Veve"));
		Revision revision3 = ctx.vonto().commit("test", "second changes");
		Assert.assertEquals(masterBranch, revision3.branchHandle());
		// get off branch 
		Revision revision4 = ctx.vonto().commit("test", "third changes", null);
		Assert.assertNull(revision4.branchHandle());
		final OWLAxiom hasYearAxiom = aProp(dprop("hasYear"), individual("GrandRelease"), literal("2012"));
		aInstanceOf(owlClass("ClassCommit"), individual("BranchTestPush"));
		final Revision revision5 = ctx.vonto().commit("test", "forth changes");
		Assert.assertNull(ctx.vonto().revision().branch());
		// now go back to branch right when it was closed and we shouldn't
		// see the latest changes
		ctx.vonto().goTo(masterBranch);
		Assert.assertFalse(ctx.o.containsAxiom(hasYearAxiom));
		ctx.vonto().goTo(revision5);
		Assert.assertTrue(ctx.o.containsAxiom(hasYearAxiom));
	}
	
	public static void main(String []argv)
	{
		for (int i = 0; i < 100; i++)
		{
			JUnitCore junit = new JUnitCore();
			Result result = junit.run(Request.method(BranchTests.class, 
										"testSimpleBranchLine"));
			System.out.println("Failures " + result.getFailureCount());
			if (result.getFailureCount() > 0)
			{
				for (Failure failure : result.getFailures())
				{
					failure.getException().printStackTrace();
				}
				break;
			}
		}
	}	
}

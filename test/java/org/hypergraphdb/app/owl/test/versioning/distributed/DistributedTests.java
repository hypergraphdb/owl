package org.hypergraphdb.app.owl.test.versioning.distributed;

import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;
import static org.hypergraphdb.app.owl.test.TU.aSubclassOf;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.owlClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import mjson.Json;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.test.versioning.TestContext;
import org.hypergraphdb.app.owl.test.versioning.VersionedOntologiesTestData;
import org.hypergraphdb.app.owl.test.versioning.VersioningTestBase;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetNewRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.hypergraphdb.peer.workflow.WorkflowState;
import org.hypergraphdb.util.HGUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class DistributedTests extends VersioningTestBase
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbdistributed";
 
	HyperGraphPeer peer1, peer2;
	VersionManager vm1, vm2;
	TestContext ctx1, ctx2;
	OntologyDatabasePeer repo1, repo2;
	
	@Before
	public void createPeers() throws Exception
	{
		if (Boolean.getBoolean("use.xmpp"))
		{
			peer1 = newXMPPPeer("junit1", "password");
			peer2 = newXMPPPeer("junit2", "password");			
		}
		else
		{
			peer1 = newInMemoryPeer("peer1");
			peer2 = newInMemoryPeer("peer2");
		}
		while (peer2.getConnectedPeers().isEmpty())
			Thread.sleep(100);
		vm1 = new VersionManager(peer1.getGraph(),"testpeer1");
		vm2 = new VersionManager(peer2.getGraph(), "testpeer2");
		
		ctx1 = TU.newCtx(peer1.getGraph().getLocation());
		ctx2 = TU.newCtx(peer2.getGraph().getLocation());
		repo1 = new OntologyDatabasePeer(peer1);
		repo2 = new OntologyDatabasePeer(peer2);
	}
	
	@After
	public void stopPeers()
	{
		dropPeer(peer1);
		dropPeer(peer2);
	}
	
	public HyperGraphPeer newInMemoryPeer(String name) throws InterruptedException, ExecutionException, ClassNotFoundException
	{
		File location = new File(new File(dblocation), name);
		HGUtils.dropHyperGraphInstance(location.getAbsolutePath());
		HyperGraph graph = HGEnvironment.get(location.getAbsolutePath());		
		Json config = Json.object("interfaceType", InProcessPeerInterface.class.getName(),
				                  "bootstrap", Json.array(
			Json.object("class", AffirmIdentityBootstrap.class.getName(), "config", Json.object())
        ));		
		HyperGraphPeer peer = new HyperGraphPeer(config, graph);
		if (!peer.start().get())
		{
			peer.getStartupFailedException().printStackTrace(System.err);
			Assert.fail("Exception during peer startup, see console for stack trace.");
		}
		peer.getActivityManager().registerActivityType(GetNewRevisionsActivity.TYPENAME, GetNewRevisionsActivity.class);
		peer.getActivityManager().registerActivityType(VersionUpdateActivity.TYPENAME, VersionUpdateActivity.initializedClass());
		return peer;
	}
	
	public HyperGraphPeer newXMPPPeer(String user, String password) throws InterruptedException, ExecutionException, ClassNotFoundException
	{
		File location = new File(new File(dblocation), user);
		HGUtils.dropHyperGraphInstance(location.getAbsolutePath());
		HyperGraph graph = HGEnvironment.get(location.getAbsolutePath());
		String connectionString = "hgpeer://" + user + ":" + 
				password +  "@" + "hypergraphdb.org" + "#" + "junit@conference.chat.evalhalla.com";
		HyperGraphPeer peer = ImplUtils.peer(connectionString, 
											 graph.getLocation());		
		if (!peer.start().get())
		{
			peer.getStartupFailedException().printStackTrace(System.err);
			Assert.fail("Exception during peer startup, see console for stack trace.");
		}
		peer.getActivityManager().registerActivityType(GetNewRevisionsActivity.TYPENAME, GetNewRevisionsActivity.class);
		peer.getActivityManager().registerActivityType(VersionUpdateActivity.TYPENAME, VersionUpdateActivity.initializedClass());
		return peer;
	}
	
	public void dropPeer(HyperGraphPeer peer)
	{
		peer.stop();
		peer.getGraph().close();
		HGUtils.dropHyperGraphInstance(peer.getGraph().getLocation());
	}
	
	@Test
	public void testGetRevisions() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
				new GetNewRevisionsActivity(peer2, ctx2.graph.getHandle(remoteOnto))).get();
	}
	
	@Test public void cloneEmpty() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.emptyRevisionGraph(iri_prefix + "peer1data", null);		
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action(VersionUpdateActivity.ActionType.clone)).get();
		assertTrue(VersionedOntologiesTestData.compareOntologies(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));		
	}
	
	@Test public void testClone() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer2)
			.remoteOntology(ctx2.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.clone.name());
		peer2.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		assertTrue(VersionedOntologiesTestData.compareOntologies(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));
	}
	
	/**
	 * Same as clone but initiated from the sending end.
	 * @throws Exception
	 */
	@Test public void testPublish() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo1.remoteOnto(sourceOntoHandle, repo1.remoteRepo(peer2.getIdentity()));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer1)
			.remoteOntology(ctx1.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.publish.name());
		peer1.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		assertTrue(VersionedOntologiesTestData.compareOntologies(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));
	}
	
	@Test
	public void testPullRevisionChanges() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
	    // Important to get the persistent handle here because we are using it against two
		// separate HG instance! 
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle().getPersistent();
//		System.out.println("ontology handle=" + sourceOntoHandle);
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action(VersionUpdateActivity.ActionType.clone)).get();
		ctx2.vo = vm2.versioned(sourceOntoHandle);
//		versioning.printRevisionGraph(ctx1.graph(), ctx1.vonto());
//		if (ctx2.vonto().toString().contains("fixme-VHDBOntologyRepository"))
//			System.out.println(ctx2.graph().getAll(
//				hg.and(hg.type(VersionedOntology.class), 
//					   hg.eq("ontology", sourceOntoHandle))));
//		versioning.printRevisionGraph(ctx2.graph(), ctx2.vonto());		
		assertEquals(1, ctx2.vo.heads().size());
		assertEquals(ctx1.vonto().revisions(), ctx2.vonto().revisions());
		ctx2.o = ctx2.vo.ontology();	
		// now make some changes at peer 1 (source location) and pull them from peer 2
		TU.ctx.set(ctx1);
		aSubclassOf(owlClass("Employee"), owlClass("Manager"));
		aInstanceOf(owlClass("Manager"), individual("Rapacious"));
		HGHandle parentRevision = ctx1.vonto().getCurrentRevision().getPersistent();		
		ctx1.vo.commit("testuser", "Pull my new changes");
		HGHandle childRevision = ctx1.vonto().getCurrentRevision().getPersistent();
//		versioning.printRevisionGraph(ctx1.graph(), ctx1.vonto());
//		versioning.printRevisionGraph(ctx2.graph(), ctx2.vonto());
		// now make some changes to peer2 so version diverge
		TU.ctx.set(ctx2);
		aInstanceOf(owlClass("LoyalCustomer"), individual("Brandon Broom"));
		ctx2.vo.commit("testuser2", "New difference.");
//		versioning.printRevisionGraph(ctx1.graph(), ctx1.vonto());
//		versioning.printRevisionGraph(ctx2.graph(), ctx2.vonto());
		assertEquals(1, ctx2.vo.heads().size());
		peer2.getActivityManager().initiateActivity(
				new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action("pull")).get();		
//		versioning.printRevisionGraph(ctx1.graph(), ctx1.vonto());
//		versioning.printRevisionGraph(ctx2.graph(), ctx2.vonto());
		assertEquals(2, ctx2.vo.heads().size());
		assertTrue(ctx2.vo.heads().contains(ctx1.vo.getCurrentRevision()));		
		ChangeSet<VersionedOntology> cs1 = versioning.changes(ctx1.graph(), childRevision, parentRevision);
		ChangeSet<VersionedOntology> cs2 = versioning.changes(ctx2.graph(), childRevision, parentRevision);
		assertEquals(cs1, cs2);
		assertTrue(VersionedOntologiesTestData.compareChangeLists(ctx1.graph(), 
																  ctx2.graph(), 
																  cs1.changes(), 
																  cs2.changes()));
	}

	@Test
	public void testPushRevisionChanges() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		if (ctx1.vo.changes().size() > 0)
			ctx1.vo.commit("testuser1", "flush leftover changes");
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action(VersionUpdateActivity.ActionType.clone)).get();
		HGHandle parentRevision = ctx1.vonto().getCurrentRevision().getPersistent();
		ctx2.vo = vm2.versioned(sourceOntoHandle);
		ctx2.o = ctx2.vo.ontology();		
		TU.ctx.set(ctx2);
		aInstanceOf(owlClass("LoyalCustomer"), individual("Brandon_Broom"));
		aInstanceOf(owlClass("LoyalCustomer"), individual("Clair_Zuckerbergengerber"));
		ctx2.vo.commit("testuser2", "New difference.");
		// push changes from peer2 to peer1
		peer2.getActivityManager().initiateActivity(
				new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action("push")).get();
		HGHandle childRevision = ctx2.vo.getCurrentRevision().getPersistent();
		ctx1.vo.goTo((Revision)ctx1.graph.get(ctx2.vo.getCurrentRevision().getPersistent()));
		assertEquals(ctx2.vo.getCurrentRevision(), ctx1.vo.getCurrentRevision());		
		ChangeSet<VersionedOntology> cs1 = versioning.changes(ctx1.graph(), childRevision, parentRevision);
		ChangeSet<VersionedOntology> cs2 = versioning.changes(ctx2.graph(), childRevision, parentRevision);
		assertEquals(cs1, cs2);
		assertTrue(VersionedOntologiesTestData.compareChangeLists(ctx1.graph(), 
																  ctx2.graph(), 
																  cs1.changes(), 
																  cs2.changes()));
	}
	
	@Test 
	public void testCloneWithBranches() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		ctx1.vo.commit("testuser", "create branch", "TestBranch1");
		VersionedOntologiesTestData.makeRevision(ctx1);		
		VersionedOntologiesTestData.makeRevision(ctx1);
		ctx1.vo.commit("testuser", "create branch", "TestBranch2");
		VersionedOntologiesTestData.makeRevision(ctx1);
		ctx1.vo.commit("testuser", "made some changes");
		ctx1.vo.goTo("TestBranch1");
		VersionedOntologiesTestData.makeRevision(ctx1);
		VersionedOntologiesTestData.makeRevision(ctx1);
		ctx1.vo.commit("testuser", "made changes to branch 1");
		ctx1.vo.goTo("TestBranch2");
		VersionedOntologiesTestData.makeRevision(ctx1);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();		
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto)).action("clone")).get();
		assertTrue(VersionedOntologiesTestData.compareOntologies(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));
	}

	@Test 
	public void testBranchConflicts() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		ctx1.vo.commit("testuser", "create branch", "TestBranch1");
		VersionedOntologiesTestData.makeRevision(ctx1);		

		// clone from peer1 to peer2
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();		
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
				new VersionUpdateActivity(peer2)
					.remoteOntology(ctx2.graph.getHandle(remoteOnto)).action(VersionUpdateActivity.ActionType.clone)).get();
		ctx2.vo = vm2.versioned(sourceOntoHandle);
		ctx2.o = ctx2.vo.ontology();		
		
		// now create a conflict: rename a branch at peer1 should simply propagate at peer2
		Branch branch1 = ctx1.vo.metadata().findBranch("TestBranch1");
		ctx1.vo.metadata().renameBranch(branch1, "TestBranch1_NewName");
		
		// create another branch at peer1 
		ctx1.vo.commit("testuser", "create branch", "TestBranch2");
		VersionedOntologiesTestData.makeRevision(ctx1);

		// and then a branch with the same name at peer2, that's a conflict
		VersionedOntologiesTestData.makeRevision(ctx2);
		ctx2.vo.commit("testuser", "create branch", "TestBranch2");
		VersionedOntologiesTestData.makeRevision(ctx2);

		// Now pulling changes should result in 1 conflict
		VersionUpdateActivity updateActivity = new VersionUpdateActivity(peer2)
			.remoteOntology(ctx2.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.pull);
		peer2.getActivityManager().initiateActivity(updateActivity).get();
		Assert.assertEquals(WorkflowState.Failed, updateActivity.getState());
		Assert.assertTrue(updateActivity.completedMessage().contains("1 branch conflict"));
		
		// change new peer2 branch name
		Branch branch2 = ctx2.vo.metadata().findBranch("TestBranch2");
		ctx2.vonto().metadata().renameBranch(branch2, "Test Branch 3");
		
		// now we should no more conflicts
		updateActivity = new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action("pull");
		peer2.getActivityManager().initiateActivity(updateActivity).get();
		Assert.assertEquals(WorkflowState.Completed, updateActivity.getState());		
	}
	
	@Test
	public void testPullWithMerge() throws Exception
	{
		HGDBOntology o = ctx1.m.importOntology(
				IRI.create(getClass().getResource("/ontologies/opencirmupper.owl").toURI()));
		ctx1.vo = vm1.versioned(o.getAtomHandle());
		RemoteOntology remoteOnto = repo2.remoteOnto(o.getAtomHandle(), 
										repo2.remoteRepo(peer1.getIdentity()));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer2)
			.remoteOntology(ctx2.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.clone.name());		
		peer2.getActivityManager().initiateActivity(activity);
		activity.getFuture().get();
		Assert.assertTrue(activity.getState().isCompleted());
		
		// TODO....
	}
	
	@Test
	public void testSynchChangesOnBothSides() throws Exception
	{
		
	}
	
	public static void main(String []argv)
	{
		JUnitCore junit = new JUnitCore();
		Result result = null;
		do
		{
			result = junit.run(Request.method(DistributedTests.class, "testCloneWithBranches"));
		} while (false && result.getFailureCount() == 0);
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
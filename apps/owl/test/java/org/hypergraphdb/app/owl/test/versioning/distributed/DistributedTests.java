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
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.test.versioning.TestContext;
import org.hypergraphdb.app.owl.test.versioning.VersionedOntologiesTestData;
import org.hypergraphdb.app.owl.test.versioning.VersioningTestBase;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.VDHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetNewRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap;
import org.hypergraphdb.util.HGUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class DistributedTests extends VersioningTestBase
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbdistributed";
 
	HyperGraphPeer peer1, peer2;
	VersionManager vm1, vm2;
	TestContext ctx1, ctx2;
	VDHGDBOntologyRepository repo1, repo2;
	
	@Before
	public void createPeers() throws Exception
	{
		peer1 = newPeer("peer1");
		peer2 = newPeer("peer2");
		while (peer2.getConnectedPeers().isEmpty())
			Thread.sleep(100);
		vm1 = new VersionManager(peer1.getGraph(),"testpeer1");
		vm2 = new VersionManager(peer2.getGraph(), "testpeer2");
		
		ctx1 = TU.newCtx(peer1.getGraph().getLocation());
		ctx2 = TU.newCtx(peer2.getGraph().getLocation());
		repo1 = new VDHGDBOntologyRepository(peer1);
		repo2 = new VDHGDBOntologyRepository(peer2);
	}
	
	@After
	public void stopPeers()
	{
		peer1.stop();
		peer2.stop();
	}
	
	public HyperGraphPeer newPeer(String name) throws InterruptedException, ExecutionException, ClassNotFoundException
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
				.action("pull")).get();
		assertTrue(VersionedOntologiesTestData.compareOntologies(vm1.versioned(sourceOntoHandle), 
																 vm1.getGraph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.getGraph()));		
	}
	
	@Test public void testClone() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto)).action("pull")).get();
		assertTrue(VersionedOntologiesTestData.compareOntologies(vm1.versioned(sourceOntoHandle), 
																 vm1.getGraph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.getGraph()));
	}

	@Test
	public void testPullRevisionChanges() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action("pull")).get();
		// now make some changes at peer 1 (source location) and pull them from peer 2
		TU.ctx.set(ctx1);
		aSubclassOf(owlClass("Employee"), owlClass("Manager"));
		aInstanceOf(owlClass("Manager"), individual("Rapacious"));
		ctx1.vo.commit("testuser", "Pull my new changes");
		peer2.getActivityManager().initiateActivity(
				new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto))
				.action("pull")).get();
		assertEquals(ctx1.vo.getCurrentRevision(), ctx2.vo.getCurrentRevision());
		assertEquals(ctx1.vo.changes(ctx1.vo.revision()), ctx2.vo.changes(ctx2.vo.revision()));
	}

	@Test
	public void testPushRevisionChanges() throws Exception
	{
		
	}
	
	@Test
	public void testPushWithConflict() throws Exception
	{
		
	}
	
	@Test
	public void testSynchChangesOnBothSides() throws Exception
	{
		
	}
	
	public static void main(String []argv)
	{
		JUnitCore junit = new JUnitCore();
		Result result = junit.run(Request.method(DistributedTests.class, "testPullRevisionChanges"));
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
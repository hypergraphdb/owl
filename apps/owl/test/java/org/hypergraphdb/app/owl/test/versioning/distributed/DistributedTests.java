package org.hypergraphdb.app.owl.test.versioning.distributed;

import java.io.File;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import mjson.Json;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.test.versioning.VersionSerializationTests;
import org.hypergraphdb.app.owl.test.versioning.VersionedOntologiesTestData;
import org.hypergraphdb.app.owl.test.versioning.VersioningTestBase;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteRepository;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetNewRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.peer.HyperGraphPeer;
import org.hypergraphdb.peer.bootstrap.AffirmIdentityBootstrap;
import org.hypergraphdb.peer.workflow.ActivityResult;
import org.hypergraphdb.util.HGUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.junit.Assert.*;

public class DistributedTests extends VersioningTestBase
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbdistributed";
 
	HyperGraphPeer peer1, peer2;
	
	@Before
	public void createPeers() throws Exception
	{
		peer1 = newPeer("peer1");
		peer2 = newPeer("peer2");
		while (peer2.getConnectedPeers().isEmpty())
			Thread.sleep(100);		
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
		TU.ctx.set(TU.newCtx(peer1.getGraph().getLocation()));
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteRepository remoteRepo = new RemoteRepository();
		remoteRepo.setName("peer1");
		remoteRepo.setPeer(peer1.getIdentity());
		RemoteOntology remoteOnto = new RemoteOntology();
		remoteOnto.setRepository(remoteRepo);
		remoteOnto.setOntologyHandle(sourceOntoHandle);
		ActivityResult result = peer2.getActivityManager().initiateActivity(new GetNewRevisionsActivity(peer2, remoteOnto)).get();
	}
	
	@Test
	public void testClone() throws Exception
	{
		TU.ctx.set(TU.newCtx(peer1.getGraph().getLocation()));
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteRepository remoteRepo = new RemoteRepository();
		remoteRepo.setName("peer1");
		remoteRepo.setPeer(peer1.getIdentity());
		RemoteOntology remoteOnto = new RemoteOntology();
		remoteOnto.setRepository(remoteRepo);
		remoteOnto.setOntologyHandle(sourceOntoHandle);
		ActivityResult result = peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(new RemoteOntology(sourceOntoHandle, new RemoteRepository(peer1.getIdentity())))
				.action("pull")).get();
		
	}

	@Test
	public void testPullRevisionChanges() throws Exception
	{
		
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
		Result result = junit.run(Request.method(DistributedTests.class, "testClone"));
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
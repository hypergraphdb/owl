package org.hypergraphdb.app.owl.test.versioning.distributed;

import static org.hypergraphdb.app.owl.test.TU.a;
import static org.hypergraphdb.app.owl.test.TU.aInstanceOf;
import static org.hypergraphdb.app.owl.test.TU.aProp;
import static org.hypergraphdb.app.owl.test.TU.aSubclassOf;
import static org.hypergraphdb.app.owl.test.TU.declare;
import static org.hypergraphdb.app.owl.test.TU.individual;
import static org.hypergraphdb.app.owl.test.TU.oprop;
import static org.hypergraphdb.app.owl.test.TU.owlClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;
import mjson.Json;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.HGDBImportConfig;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.test.versioning.TestContext;
import org.hypergraphdb.app.owl.test.versioning.VersionedOntologiesTestData;
import org.hypergraphdb.app.owl.test.versioning.VersioningTestBase;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.util.OntologyComparator;
import org.hypergraphdb.app.owl.versioning.Branch;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.Change;
import org.hypergraphdb.app.owl.versioning.ChangeLink;
import org.hypergraphdb.app.owl.versioning.VersionManager;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.hypergraphdb.app.owl.versioning.versioning;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.distributed.OntologyDatabasePeer;
import org.hypergraphdb.app.owl.versioning.distributed.RemoteOntology;
import org.hypergraphdb.app.owl.versioning.distributed.activity.GetNewRevisionsActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity;
import org.hypergraphdb.app.owl.versioning.distributed.activity.VersionUpdateActivity.ActionType;
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
import org.semanticweb.owlapi.model.OWLAxiom;

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
				password +  "@" + "hypergraphdb.org" + "#" + "junit@conference.hypergraphdb.org";
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
		ActivityResult result = peer2.getActivityManager().initiateActivity(
				new GetNewRevisionsActivity(peer2, ctx2.graph.getHandle(remoteOnto))).get();
		Assert.assertNull(result.getException());
		Assert.assertEquals(TU.ctx().vonto().revisions().size(),
				((GetNewRevisionsActivity)result.getActivity()).delta().revisions.size());
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
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));		
	}
	
	@Test public void testClone() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();		
		VersionedOntology vo1 = vm1.versioned(sourceOntoHandle);
		 // we need something in working set to test it's not cloned
		assertFalse(vo1.changes().changes().isEmpty());
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer2)
			.remoteOntology(ctx2.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.clone.name());
		peer2.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		
		VersionedOntology vo2 = vm2.versioned(sourceOntoHandle);
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(vo1, 
																 vm1.graph(), 
																 vo2, 
																 vm2.graph()));
		// working set changes should not be cloned!
		OntologyComparator.ComparatorDelta delta = OntologyComparator.compare(vo1.ontology(), vo2.ontology());
		assertTrue(delta.hasChanges());
	}
	
	@Test public void testCloneSmall() throws Exception
	{
		TU.ctx.set(ctx1);
		ctx1.o = (HGDBOntology)ctx1.m.createOntology(IRI.create(iri_prefix + "peer1data")); 
		ctx1.vr = new VersionManager(ctx1.graph, "testuser");
		ctx1.vo = ctx1.vr.versioned(ctx1.graph.getHandle(ctx1.o));		
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();				
		a(declare(owlClass("User")));		
//		VersionedOntology vo1 = vm1.versioned(sourceOntoHandle);
//		assertEquals(vo1, ctx1.vo);
		ctx1.vo.commit("test", "version 1");
		a(declare(owlClass("Employee")));		
//		 // we need something in working set to test it's not cloned
		assertFalse(ctx1.vo.changes().changes().isEmpty());
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer2)
			.remoteOntology(ctx2.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.clone.name());
		peer2.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		
		ctx2.vo = vm2.versioned(sourceOntoHandle);
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(ctx1.vo, 
																 vm1.graph(), 
																 ctx2.vo, 
																 vm2.graph()));
		// working set changes should not be cloned!				
		OntologyComparator.ComparatorDelta delta = OntologyComparator.compare(ctx1.vo.ontology(), ctx2.vo.ontology());
		assertEquals(1, delta.getRemovedAxioms().size());
		assertEquals(delta.getRemovedAxioms().get(0),
					 ((VAxiomChange)ctx1.vo.changes().changes().get(0)).getAxiom());
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
		// peer1 publishing to peer2
		VersionUpdateActivity activity = new VersionUpdateActivity(peer1)
			.remoteOntology(ctx1.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.publish.name());
		peer1.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		ctx2.vo = vm2.versioned(sourceOntoHandle);
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));
		assertTrue(ctx1.vonto().ontology().getAxioms().containsAll(ctx2.vonto().ontology().getAxioms()));		
		// Now if we push again the same ontology with no changes, the RemoteOnto should be unchanged
		RemoteOntology remoteAtPeer2 = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		Set<HGHandle> saveHeads = new HashSet<HGHandle>();
		saveHeads.addAll(remoteAtPeer2.getRevisionHeads());
		HGHandle lastMeta = remoteAtPeer2.getLastMetaChange();
		activity = new VersionUpdateActivity(peer1).remoteOntology(ctx1.graph().getHandle(remoteOnto))
												   .action(VersionUpdateActivity.ActionType.push.name());
		peer1.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(vm1.versioned(sourceOntoHandle), 
				 vm1.graph(), 
				 vm2.versioned(sourceOntoHandle), 
				 vm2.graph()));
		assertTrue(ctx1.vonto().ontology().getAxioms().containsAll(ctx2.vonto().ontology().getAxioms()));
		assertEquals(saveHeads, remoteAtPeer2.getRevisionHeads());
		assertEquals(lastMeta, remoteAtPeer2.getLastMetaChange());
	}
	
	@Test public void testPublishWithInitialDataButNoRevisions() throws Exception
	{
		TU.ctx.set(ctx1);		
		ctx1.o = (HGDBOntology)ctx1.m.createOntology(IRI.create(iri_prefix + "data_but_no_revisions"));
		a(declare(owlClass("User")));
		a(declare(owlClass("Employee")));
		aSubclassOf(owlClass("User"), owlClass("Employee"));
		a(declare(individual("Pedro")));
		aInstanceOf(owlClass("Employee"), individual("Pedro"));
		aSubclassOf(owlClass("User"), owlClass("Customer"));
		a(declare(owlClass("LoyalCustomer")));
		
		aSubclassOf(owlClass("Customer"), owlClass("LoyalCustomer"));
		aInstanceOf(owlClass("LoyalCustomer"), individual("Mary"));
		aInstanceOf(owlClass("LoyalCustomer"), individual("Tom"));
		
		aInstanceOf(owlClass("Customer"), individual("John"));
		aInstanceOf(owlClass("Employee"), individual("Fred"));
		a(declare(oprop("isServing")));		
		aProp(oprop("isServing"), individual("Fred"), individual("Tom"));		
		
		// now make into versioned and publish
		ctx1.vr = new VersionManager(ctx1.graph(), "testuser");
		ctx1.vo = ctx1.vr.versioned(ctx1.graph().getHandle(ctx1.o));
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
		RemoteOntology remoteOnto = repo1.remoteOnto(sourceOntoHandle, repo1.remoteRepo(peer2.getIdentity()));
		VersionUpdateActivity activity = new VersionUpdateActivity(peer1)
			.remoteOntology(ctx1.graph.getHandle(remoteOnto))
			.action(VersionUpdateActivity.ActionType.publish.name());
		peer1.getActivityManager().initiateActivity(activity).get();
		assertEquals(WorkflowState.Completed, activity.getState());
		ctx2.o = vm2.versioned(sourceOntoHandle).ontology(); 
		assertTrue(VersionedOntologiesTestData.compareOntologies(ctx1.o, ctx2.ontology()));		
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
		List<Change<VersionedOntology>> cs1 = versioning.changes(ctx1.graph(), childRevision, parentRevision);
		List<Change<VersionedOntology>> cs2 = versioning.changes(ctx2.graph(), childRevision, parentRevision);
		assertEquals(cs1, cs2);
		assertTrue(VersionedOntologiesTestData.compareChangeLists(ctx1.graph(), 
																  ctx2.graph(), 
																  cs1, 
																  cs2));
	}

	@Test
	public void testPushRevisionChanges() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		if (ctx1.vo.changes().size() > 0)
			ctx1.vo.commit("testuser1", "flush leftover changes");
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();
//		// clone from peer1 into peer2		
		TU.versionUpdate(sourceOntoHandle, ActionType.clone, repo2, repo1);		
		HGHandle parentRevision = ctx1.vonto().getCurrentRevision().getPersistent();
		ctx2.vo = vm2.versioned(sourceOntoHandle);
		ctx2.o = ctx2.vo.ontology();		
		TU.ctx.set(ctx2);
		aInstanceOf(owlClass("LoyalCustomer"), individual("Brandon_Broom"));
		aInstanceOf(owlClass("LoyalCustomer"), individual("Clair_Zuckerbergengerber"));
		ctx2.vo.commit("testuser2", "New difference.");
		// push changes from peer2 to peer1
		TU.versionUpdate(sourceOntoHandle, ActionType.push, repo2, repo1);
		HGHandle childRevision = ctx2.vo.getCurrentRevision().getPersistent();
		ctx1.vo.goTo((Revision)ctx1.graph.get(ctx2.vo.getCurrentRevision().getPersistent()));
		assertEquals(ctx2.vo.getCurrentRevision(), ctx1.vo.getCurrentRevision());		
		List<Change<VersionedOntology>> cs1 = versioning.changes(ctx1.graph(), childRevision, parentRevision);
		List<Change<VersionedOntology>> cs2 = versioning.changes(ctx2.graph(), childRevision, parentRevision);
		assertEquals(cs1, cs2);
		assertTrue(VersionedOntologiesTestData.compareChangeLists(ctx1.graph(), 
																  ctx2.graph(), 
																  cs1, 
																  cs2));
	}
	
	@Test 
	public void testCloneWithBranches() throws Exception
	{
		TU.ctx.set(ctx1);		
		// an ontology with some random data
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		Assert.assertNotNull(ctx1.vo.revision());		
		// we create an branch with no change set
		ctx1.vo.commit("testuser", "create branch 1", "TestBranch1");
		Assert.assertNotNull(ctx1.vo.revision());
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);
		Assert.assertNotNull(ctx1.vo.revision());
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);
		Assert.assertNotNull(ctx1.vo.revision());
		ctx1.vo.commit("testuser", "create branch 2", "TestBranch2");
		Assert.assertNotNull(ctx1.vo.revision());
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);
		ctx1.vo.commit("testuser", "made some changes to branch 2");
		List<ChangeLink> allchangelinks = hg.getAll(ctx1.graph(), hg.type(ChangeLink.class));
		for (ChangeLink chlink : allchangelinks)
		{
			System.out.println("Checking link " + chlink);
			if (ctx1.graph().get(chlink.parent()) == null)
			{
				System.out.println("Change " + ctx1.graph().get(chlink.change()));
				System.out.println("Child " + ctx1.graph().get(chlink.child()));
				throw new NullPointerException("no parent");
			}
			else if (ctx1.graph().get(chlink.change()) == null)
				throw new NullPointerException("no change");
			else if (ctx1.graph().get(chlink.child()) == null)
				throw new NullPointerException("no child");
			System.out.println("link ok");
		}		
		ctx1.vo.goTo("TestBranch1");
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);
		ctx1.vo.commit("testuser", "made changes to branch 1");
		allchangelinks = hg.getAll(ctx1.graph(), hg.type(ChangeLink.class));
		for (ChangeLink chlink : allchangelinks)
		{
			System.out.println("Checking link " + chlink);
			if (ctx1.graph().get(chlink.parent()) == null)
			{
				System.out.println("Change " + ctx1.graph().get(chlink.change()));
				System.out.println("Child " + ctx1.graph().get(chlink.child()));
				throw new NullPointerException("no parent");
			}
			else if (ctx1.graph().get(chlink.change()) == null)
				throw new NullPointerException("no change");
			else if (ctx1.graph().get(chlink.child()) == null)
				throw new NullPointerException("no child");
			System.out.println("link ok");
		}
		ctx1.vo.goTo("TestBranch2");
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();		
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
			new VersionUpdateActivity(peer2)
				.remoteOntology(ctx2.graph.getHandle(remoteOnto)).action("clone")).get();
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(vm1.versioned(sourceOntoHandle), 
																 vm1.graph(), 
																 vm2.versioned(sourceOntoHandle), 
																 vm2.graph()));
	}

	@Test 
	public void testAFewSmallPushes() throws Exception
	{
		TU.ctx.set(ctx1);		
		ctx1.newonto(IRI.create(iri_prefix + "peer1data"), true);
		HGHandle ontoHandle = TU.ctx().o.getAtomHandle();		
		// Publish yet empty repository
		TU.versionUpdate(ontoHandle, ActionType.publish, repo1, repo2);
		ctx2.setonto(ontoHandle);		
		ctx1.assertEqualOntology(ctx2);
		a(declare(owlClass("A")));
		ctx1.vo.commit("testuser", "push 1");
		TU.versionUpdate(ontoHandle, ActionType.push, repo1, repo2);
		ctx2.vonto().goTo(ctx2.vonto().revision().branch().name());
		ctx1.assertEqualOntology(ctx2);
	}
	
	@Test 
	public void testPushWithNewBranches() throws Exception
	{
		TU.ctx.set(ctx1);		
		ctx1.newonto(IRI.create(iri_prefix + "peer1data"), true);
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();		
		a(declare(owlClass("Event")));
		a(declare(owlClass("Character")));
		ctx1.vo.commit("testuser1", "Initial commit.");
		TU.versionUpdate(sourceOntoHandle, ActionType.publish, repo1, repo2);
		ctx2.setonto(sourceOntoHandle);		
		ctx1.assertEqualOntology(ctx2);
//		ctx1.vo.metadata().createBranch(ctx1.vonto().getCurrentRevision(), "branch1", "testuser1");
		aSubclassOf(owlClass("User"), owlClass("Employee"));
		ctx1.vo.commit("testuser1", "Commit newly created branch", "branch1");
		Assert.assertEquals("branch1", ctx1.vonto().revision().branch().name());
		ActivityResult result = TU.versionUpdate(sourceOntoHandle, ActionType.push, repo1, repo2);
		Assert.assertNull(result.getException());
		// test new branch is also transferred
		Assert.assertEquals("master", ctx2.vonto().revision().branch().name());
		assertTrue(VersionedOntologiesTestData.compareOntologyRevisions(
				 ctx1.vonto(), 
				 ctx1.graph(), 
				 ctx2.vonto(), 
				 ctx2.graph()));		
		ctx2.vonto().goTo("branch1");
		ctx1.assertEqualOntology(ctx2);
	}
	
	@Test 
	public void testBranchConflicts() throws Exception
	{
		TU.ctx.set(ctx1);
		VersionedOntologiesTestData.revisionGraph_1(iri_prefix + "peer1data", null);
		ctx1.vo.commit("testuser", "create branch", "TestBranch1");
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);		

		// clone from peer1 to peer2
		HGHandle sourceOntoHandle = TU.ctx().o.getAtomHandle();		
		RemoteOntology remoteOnto = repo2.remoteOnto(sourceOntoHandle, repo2.remoteRepo(peer1.getIdentity()));
		peer2.getActivityManager().initiateActivity(
				new VersionUpdateActivity(peer2)
					.remoteOntology(ctx2.graph.getHandle(remoteOnto))
					.action(VersionUpdateActivity.ActionType.clone)).get();
		ctx2.vo = vm2.versioned(sourceOntoHandle);
		ctx2.o = ctx2.vo.ontology();		
		
		// now create a conflict: rename a branch at peer1 should simply propagate at peer2
		Branch branch1 = ctx1.vo.metadata().findBranch("TestBranch1");
		ctx1.vo.metadata().renameBranch(branch1, "TestBranch1_NewName");
		
		// create another branch at peer1 
		ctx1.vo.commit("testuser", "create branch", "TestBranch2");
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx1);

		// and then a branch with the same name at peer2, that's a conflict
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx2);
		ctx2.vo.commit("testuser", "create branch", "TestBranch2");
		VersionedOntologiesTestData.makeSomeOntologyChanges(ctx2);

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
	
	/**
	 * Test case where we have to merge changes on the same branches 
	 */
	@Test
	public void testPullWithMerge() throws Exception
	{
		ctx1.o = ctx1.m.importOntology(
				IRI.create(getClass().getResource("/ontologies/opencirmupper.owl").toURI()), 
				new HGDBImportConfig());
		ctx1.vo = vm1.versioned(ctx1.o.getAtomHandle());
		ActivityResult result = TU.versionUpdate(ctx1.o.getAtomHandle(), ActionType.publish, repo1, repo2);
		Assert.assertTrue(result.getActivity().getState().isCompleted());
		ctx2.setonto(ctx1.o.getAtomHandle());				
		
		// now, both sides will diverge
		TU.ctx.set(ctx1);
		OWLAxiom a1 = a(declare(owlClass("Context1Class")));
		OWLAxiom a2 = aInstanceOf(owlClass("Context1Class"), individual("Context1_Individual"));
		ctx1.vonto().commit("testuser1", "Commit on peer 1");
		
		TU.ctx.set(ctx2);
		a(declare(owlClass("Context2Class")));
		aInstanceOf(owlClass("Context2Class"), individual("Context2_Individual"));
		ctx2.vonto().commit("testuser2", "Commit on peer 2");
		
		// Pull changes peer 1 -> peer 2
		Assert.assertTrue(TU.versionUpdate(ctx1.o.getAtomHandle().getPersistent(), ActionType.pull, repo2, repo1)
				.getActivity().getState().isCompleted());
		Set<HGHandle> heads = ctx2.vonto().heads();
		List<Revision> masterHeads = new ArrayList<Revision>();
		for (HGHandle revHandle : heads)
		{
			Revision rev = ctx2.graph().get(revHandle);
			if ("master".equals(rev.branch().name()))
				masterHeads.add(rev);
		}
		ctx2.vonto().merge("testuser2", "merge", "master", masterHeads.toArray(new Revision[0]));
		ctx2.vonto().goTo("master");
		System.out.println(ctx2.o.getAxioms());
		Assert.assertTrue(ctx2.o.containsAxiom(a1));
		Assert.assertTrue(ctx2.o.containsAxiom(a2));
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
		} while (result.getFailureCount() == 0 && false);
		System.out.println("Failures " + result.getFailureCount());
		if (result.getFailureCount() > 0)
		{
			for (Failure failure : result.getFailures())
			{
				failure.getException().printStackTrace();
			}
		}
		System.exit(0);
	}
	
}
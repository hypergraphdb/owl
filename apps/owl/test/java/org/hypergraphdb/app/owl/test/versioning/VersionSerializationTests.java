package org.hypergraphdb.app.owl.test.versioning;

import org.hypergraphdb.app.owl.HGDBOntology;

import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.newver.VersionManager;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.app.owl.versioning.distributed.activity.ActivityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.IRI;

public class VersionSerializationTests extends VersioningTestBase
{
	
	@Before public void beforeTest() throws Exception
	{ 
		ctx = (TestContext)TU.ctx;
		ctx.o = (HGDBOntology)ctx.m.createOntology(IRI.create(iri_prefix + "_" + "serialization")); 
		ctx.vr = new VersionManager(ctx.graph, "testuser");
		ctx.vo = ctx.vr.versioned(ctx.graph.getHandle(ctx.o));				
	}
	
	@After public void afterTest() throws Exception
	{
		ctx.vr.removeVersioning(ctx.o.getAtomHandle());
	}
	
	@Test
	public void serializeEmpty()
	{
		VersionedOntology vo = make("emptyserialized");
		String asxml = ActivityUtils.renderVersionedOntology(vo);
		System.out.println(asxml);
		remove(vo);
		VersionedOntology vo2 = ActivityUtils.storeVersionedOntology(new StringDocumentSource(asxml), ctx.m);
		assertEquals(vo.getAtomHandle(), vo2.getAtomHandle());
		assertEquals(vo.getRootRevision(), vo2.getRootRevision());
		assertEquals(vo.getCurrentRevision(), vo2.getCurrentRevision());
		assertEquals(vo.getOntology(), vo2.getOntology());
	}
}

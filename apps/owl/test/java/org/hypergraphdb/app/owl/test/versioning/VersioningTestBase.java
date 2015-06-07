package org.hypergraphdb.app.owl.test.versioning;

import java.io.File;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.HGOntologyManagerFactory;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.test.TU;
import org.hypergraphdb.util.HGUtils;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

public class VersioningTestBase
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbtest";
	
	static final String iri_prefix = "hgdb://UNITTESTONT_VERSIONED";
	
	TestContext ctx; 
	
	@BeforeClass public static void setupDatabase()
	{
		System.out.println("Using db location " + dblocation + " for VersioningTestBase.");
		HGUtils.dropHyperGraphInstance(dblocation);
		TestContext ctx = new TestContext();
		ctx.graph = HGEnvironment.get(dblocation);
		ctx.r = new HGDBOntologyRepository(dblocation);
		ctx.m = HGOntologyManagerFactory.getOntologyManager(dblocation);
		ctx.df = ctx.m.getOWLDataFactory();
		TU.ctx = ctx;
	}
	
	protected VersionedOntology make(String iri, OWLAxiom...axioms)
	{
		try
		{
			HGDBOntology onto = (HGDBOntology)ctx.m.createOntology(IRI.create(iri_prefix + "/" + iri));
			for (OWLAxiom ax:axioms)
			{
				ctx.m.addAxiom(onto, ax);
			}
			return ctx.vr.versioned(ctx.graph.getHandle(ctx.o));
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
	protected void remove(VersionedOntology vo)
	{
		if (ctx.graph.get(vo.getOntology()) != null)
			ctx.vr.removeVersioning(vo.getOntology());
		if (ctx.graph.getHandle(vo.ontology()) != null)
			ctx.m.removeOntology(vo.ontology());
		new GarbageCollector(ctx.repo()).runGarbageCollection();
	}
}
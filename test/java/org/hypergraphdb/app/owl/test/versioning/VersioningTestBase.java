package org.hypergraphdb.app.owl.test.versioning;

import java.io.File;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

public class VersioningTestBase
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbtest";
	
	protected static final String iri_prefix = "hgdb://UNITTESTONT_VERSIONED";
	
	TestContext ctx; 
		
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
		if (ctx.graph.getHandle(vo.ontology()) != null)
			ctx.m.removeOntology(vo.ontology());
		new GarbageCollector(ctx.repo()).runGarbageCollection();
	}
}
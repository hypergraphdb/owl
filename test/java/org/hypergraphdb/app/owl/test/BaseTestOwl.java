package org.hypergraphdb.app.owl.test;

import java.io.File;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.test.versioning.TestContext;
import org.hypergraphdb.util.HGUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * 
 * <p>
 * Base class for HGDB-OWL unit tests.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class BaseTestOwl
{
	static final String dblocation = 
			System.getProperty("java.io.tmpdir") + 
			File.separator + 
			"hgdbtest";
	
	protected static final String iri_prefix = "hgdb://UNITTESTONT";
	
	TestContext ctx; 

	@BeforeClass
	public static void beforeclass()
	{
		HGUtils.dropHyperGraphInstance(dblocation);
	}

	@AfterClass
	public static void afterclass()
	{
		HGUtils.dropHyperGraphInstance(dblocation);
	}
	
	protected HGDBOntology make(String iri, OWLAxiom...axioms)
	{
		try
		{
			HGDBOntology onto = (HGDBOntology)ctx.m.createOntology(IRI.create(iri_prefix + "/" + iri));
			for (OWLAxiom ax:axioms)
			{
				ctx.m.addAxiom(onto, ax);
			}
			return onto;
		}
		catch (Exception ex)
		{
			throw new RuntimeException(ex);
		}
	}
	
}

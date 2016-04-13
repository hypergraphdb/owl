package org.hypergraphdb.app.owl.test;

import java.net.URL;
import static org.junit.Assert.*;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

public class ImportTests extends BaseTestOwl
{
	@Test
	public void testImport() throws Exception
	{
		ctx = TU.newCtx(dblocation);
		URL testurl = ImportTests.class.getResource("/ontologies/opencirmupper.owl");
		assertNotNull(testurl);
		ctx.m.importOntology(IRI.create(testurl));
		assertNotNull(ctx.m.getOntology(IRI.create("http://opencirm.org/upper")));
		ctx.graph().close();
		ctx  = TU.newCtx(dblocation);
		assertNotNull(ctx.m.getOntology(IRI.create("http://opencirm.org/upper")));
	}
}

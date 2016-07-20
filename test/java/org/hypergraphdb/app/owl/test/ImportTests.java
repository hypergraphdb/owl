package org.hypergraphdb.app.owl.test;

import java.net.URL;
import static org.junit.Assert.*;

import org.hypergraphdb.app.owl.HGDBImportConfig;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;

public class ImportTests extends BaseTestOwl
{
	@Test
	public void testImport() throws Exception
	{
		ctx = TU.newCtx(dblocation);
		URL testurl = ImportTests.class.getResource("/ontologies/opencirmupper.owl");
		assertNotNull(testurl);
		ctx.m.importOntology(IRI.create(testurl), new HGDBImportConfig());
		assertNotNull(ctx.m.getOntology(IRI.create("http://opencirm.org/upper")));
		ctx.graph().close();
		ctx  = TU.newCtx(dblocation);
		assertNotNull(ctx.m.getOntology(IRI.create("http://opencirm.org/upper")));
	}
	
	@Test
	public void testImportImports() throws Exception
	{
		ctx = TU.newCtx(dblocation);
		ctx.m.addIRIMapper(new OWLOntologyIRIMapper() {
			@Override
			public IRI getDocumentIRI(IRI ontologyIRI)
			{
				if ("http://test.org/compleximports/A.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/imports/A.owl"); 
				else if ("http://test.org/compleximports/B.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/imports/B.owl");
				else if ("http://test.org/compleximports/C.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/imports/C.owl"); 
				else if ("http://test.org/compleximports/D.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/imports/D.owl"); 
				else
					return ontologyIRI;

			}
			
		});
		URL testurl = ImportTests.class.getResource("/imports/D.owl");
		assertNotNull(testurl);
		ctx.m.importOntology(IRI.create(testurl), new HGDBImportConfig().storeAllImported(true));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/A.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/B.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/C.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/D.owl")));
		ctx.graph().close();
		ctx  = TU.newCtx(dblocation);
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/A.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/B.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/C.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/D.owl")));
	}
	
	@Test
	public void testImportCyclicImports() throws Exception
	{
		ctx = TU.newCtx(dblocation);
		ctx.m.addIRIMapper(new OWLOntologyIRIMapper() {
			@Override
			public IRI getDocumentIRI(IRI ontologyIRI)
			{
				if ("http://test.org/compleximports/AC.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/importscyclic/AC.owl"); 
				else if ("http://test.org/compleximports/BC.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/importscyclic/BC.owl");
				else if ("http://test.org/compleximports/CC.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/importscyclic/CC.owl"); 
				else if ("http://test.org/compleximports/DC.owl".equals(ontologyIRI.toString()))
					return TU.resourceIri("/importscyclic/DC.owl"); 
				else
					return ontologyIRI;

			}
			
		});
		URL testurl = ImportTests.class.getResource("/importscyclic/DC.owl");
		assertNotNull(testurl);
		ctx.m.importOntology(IRI.create(testurl), new HGDBImportConfig().storeAllImported(true));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/AC.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/BC.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/CC.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/DC.owl")));
		ctx.graph().close();
		ctx  = TU.newCtx(dblocation);
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/AC.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/BC.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/CC.owl")));
		assertNotNull(ctx.m.getOntology(IRI.create("http://test.org/compleximports/DC.owl")));
	}	
}
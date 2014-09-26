package org.hypergraphdb.app.owl;
	    
import java.io.File;

import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.coode.owlapi.latex.LatexOntologyStorer;
import org.coode.owlapi.obo.renderer.OBOFlatFileOntologyStorer;
import org.coode.owlapi.owlxml.renderer.OWLXMLOntologyStorer;
import org.coode.owlapi.rdf.rdfxml.RDFXMLOntologyStorer;
import org.coode.owlapi.turtle.TurtleOntologyStorer;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.util.NonMappingOntologyIRIMapper;

import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;
import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOntologyStorer;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxOntologyStorer;
			 
public class HGOntologyManagerFactory implements OWLOntologyManagerFactory
{
	private String graphLocation()
	{
		String location = System.getProperty("hgdbowl.testdb");
		if (location == null)
			return System.getProperty("java.io.tmpdir") + File.separator + "hgdbowl.testdb";
		else
			return location;
	}
	
	@Override
	public OWLOntologyManager buildOWLOntologyManager()
	{
		HyperGraph graph = ImplUtils.owldb(graphLocation()); 
		return  HGDBOWLManager.createOWLOntologyManager(OWLDataFactoryHGDB.get(graph),
				new HGDBOntologyRepository(graph.getLocation()));	
	}

	private OWLOntologyManager inMemoryManager(OWLDataFactory dataFactory)
	{
        OWLOntologyManager ontologyManager = new OWLOntologyManagerImpl(dataFactory);
        ontologyManager.addOntologyStorer(new RDFXMLOntologyStorer());
        ontologyManager.addOntologyStorer(new OWLXMLOntologyStorer());
        ontologyManager.addOntologyStorer(new OWLFunctionalSyntaxOntologyStorer());
        ontologyManager.addOntologyStorer(new ManchesterOWLSyntaxOntologyStorer());
        ontologyManager.addOntologyStorer(new OBOFlatFileOntologyStorer());
        ontologyManager.addOntologyStorer(new KRSS2OWLSyntaxOntologyStorer());
        ontologyManager.addOntologyStorer(new TurtleOntologyStorer());
        ontologyManager.addOntologyStorer(new LatexOntologyStorer());

        ontologyManager.addIRIMapper(new NonMappingOntologyIRIMapper());

        ontologyManager.addOntologyFactory(new EmptyInMemOWLOntologyFactory());
        ontologyManager.addOntologyFactory(new ParsableOWLOntologyFactory());

        return ontologyManager;
		
	}
	
	@Override
	public OWLOntologyManager buildOWLOntologyManager(OWLDataFactory df)
	{
		if (df instanceof OWLDataFactoryHGDB)			
			return  HGDBOWLManager.createOWLOntologyManager(
				(OWLDataFactoryHGDB)df, 
				new HGDBOntologyRepository(((OWLDataFactoryHGDB)df).getHyperGraph().getLocation()));	
		else
			return this.inMemoryManager(df);
	}

	@Override
	public OWLDataFactory getFactory()
	{
		HyperGraph graph = ImplUtils.owldb(graphLocation()); 
		return  OWLDataFactoryHGDB.get(graph);				
	}
}
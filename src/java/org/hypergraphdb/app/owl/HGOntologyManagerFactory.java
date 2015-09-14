package org.hypergraphdb.app.owl;
	    
import java.io.File;
import java.util.concurrent.Callable;

import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxParserFactory;
import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.coode.owlapi.latex.LatexOntologyStorer;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxParserFactory;
import org.coode.owlapi.obo.parser.OBOParserFactory;
import org.coode.owlapi.obo.renderer.OBOFlatFileOntologyStorer;
import org.coode.owlapi.owlxml.renderer.OWLXMLOntologyStorer;
import org.coode.owlapi.owlxmlparser.OWLXMLParserFactory;
import org.coode.owlapi.rdf.rdfxml.RDFXMLOntologyStorer;
import org.coode.owlapi.rdfxml.parser.RDFXMLParserFactory;
import org.coode.owlapi.turtle.TurtleOntologyStorer;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.util.Context;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLOntologyStorer;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.util.NonMappingOntologyIRIMapper;

import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;
import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOntologyStorer;
import uk.ac.manchester.cs.owl.owlapi.turtle.parser.TurtleOntologyParserFactory;
import de.uulm.ecs.ai.owlapi.krssparser.KRSS2OWLParserFactory;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxOntologyStorer;
			 
/**
 * <p>
 * An implementation of the <code>OWLOntologyManagerFactory</code> that 
 * creates a HyperGraphDB specific OWL ontology manager. 
 * </p>
 * 
 * <p>
 * There is one and only one <code>HGDBOWLOntologyManager</code> per open
 * database. Because the OWLAPI interface does not offer any configuration
 * parameters for the construction of a new OWL manager (besides the OWLDataFactory), 
 * by default this factory connects to the database located at 
 * <code>System.getProperty("hgdbowl.defaultdb")</code>. If that system property is
 * not set, then it will open a HGDB instance at <code>TMP_DIR/hgdbowl.defaultdb</code>
 * where TMP_DIR is the current OS provided temp directory.   
 * </p>
 * 
 * <p>
 * Normally you'd obtain a manager by first obtaining an OWLDataFactory through
 * the {@link OWLDataFactoryHGDB#get(HyperGraph)} method and then calling
 * {@link #buildOWLOntologyManager(OWLDataFactory)}. This is the way to ensure
 * that the OWL manager is using the database you want it to use.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class HGOntologyManagerFactory implements OWLOntologyManagerFactory
{
	public static String graphLocation()
	{
		String location = System.getProperty("hgdbowl.defaultdb");
		if (location == null)
			return System.getProperty("java.io.tmpdir") + File.separator + "hgdbowl.defaultdb";
		else
			return location;
	}
	
	private static OWLOntologyManager inMemoryManager(OWLDataFactory dataFactory)
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
	
	private static HGDBOntologyManagerImpl createOWLOntologyManager(final OWLDataFactoryHGDB dataFactory,
													 		 final HGDBOntologyRepository repository)
	{
		final HGDBOntologyManagerImpl ontologyManager = new HGDBOntologyManagerImpl(dataFactory, repository);
		ontologyManager.addOntologyStorer(new RDFXMLOntologyStorer());
		ontologyManager.addOntologyStorer(new OWLXMLOntologyStorer());
		ontologyManager.addOntologyStorer(new OWLFunctionalSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer(new ManchesterOWLSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer(new OBOFlatFileOntologyStorer());
		ontologyManager.addOntologyStorer(new KRSS2OWLSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer(new TurtleOntologyStorer());
		ontologyManager.addOntologyStorer(new LatexOntologyStorer());
		ontologyManager.addOntologyStorer(new HGDBStorer());
		ontologyManager.addOntologyStorer(new VOWLXMLOntologyStorer());
//		ontologyManager.addIRIMapper(new HGDBIRIMapper(repository));
//		ontologyManager.addIRIMapper(new NonMappingOntologyIRIMapper());
		ontologyManager.addOntologyFactory(new EmptyInMemOWLOntologyFactory());
		ontologyManager.addOntologyFactory(new ParsableOWLOntologyFactory());
		ontologyManager.addOntologyFactory(new HGDBOntologyFactory());		
		return ontologyManager;
	}
	
	/**
	 * Get the {@link HGDBOntologyManager} for this graph database location.
	 * There is a single HGDB ontology manager per open database.
	 */
	public static HGDBOntologyManager getOntologyManager(final String graphLocation)
	{
		final HyperGraph graph = ImplUtils.owldb(graphLocation);
		return Context.of(graph).singleton(HGDBOntologyManager.class, new Callable<HGDBOntologyManager>() {
			public HGDBOntologyManager call()
			{
				return createOWLOntologyManager(OWLDataFactoryHGDB.get(graph), 
						   new HGDBOntologyRepository(graphLocation));
				
			}		
		});
	}
	
	@Override
	public OWLOntologyManager buildOWLOntologyManager()
	{
		return getOntologyManager(graphLocation());
	}
	
	@Override
	public OWLOntologyManager buildOWLOntologyManager(OWLDataFactory df)
	{
		if (df instanceof OWLDataFactoryHGDB)			
			return createOWLOntologyManager(
				(OWLDataFactoryHGDB)df, 
				new HGDBOntologyRepository(((OWLDataFactoryHGDB)df).getHyperGraph().getLocation()));	
		else
			return inMemoryManager(df);
	}

	@Override
	public OWLDataFactory getFactory()
	{
		return getDataFactory();
	}

	public static OWLDataFactory getDataFactory()
	{
		HyperGraph graph = ImplUtils.owldb(graphLocation()); 
		return  OWLDataFactoryHGDB.get(graph);				
	}
	
	static
	{
		// 2011.11.29 Parsers to load from files:
		// Register useful parsers
		OWLParserFactoryRegistry registry = OWLParserFactoryRegistry.getInstance();
		registry.registerParserFactory(new ManchesterOWLSyntaxParserFactory());
		registry.registerParserFactory(new KRSS2OWLParserFactory());
		registry.registerParserFactory(new OBOParserFactory());
		registry.registerParserFactory(new TurtleOntologyParserFactory());
		registry.registerParserFactory(new OWLFunctionalSyntaxParserFactory());
		registry.registerParserFactory(new OWLXMLParserFactory());
		registry.registerParserFactory(new RDFXMLParserFactory());
	}	
}
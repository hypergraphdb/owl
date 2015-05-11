package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.io.IOException;
import java.io.Writer;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.newver.VersionedOntology;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.semanticweb.owlapi.io.AbstractOWLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.OWLRendererIOException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.Namespaces;

/**
 * VOWLXMLVersionedOntologyRenderer.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLVersionedOntologyRenderer extends AbstractOWLRenderer
{
	private HGDBOntologyManager manager;
	/**
	 * @param owlOntologyManager
	 */
	public VOWLXMLVersionedOntologyRenderer(HGDBOntologyManager owlOntologyManager)
	{
		this.manager = owlOntologyManager;
	}

	@Override
	public void render(OWLOntology ontology, Writer writer) throws OWLRendererException
	{
		HGHandle ontoHandle =  manager.getOntologyRepository().getHyperGraph().getHandle(ontology);
		if (!manager.getVersionManager().isVersioned(ontoHandle))
			new OWLRendererException("The given ontology is not versioned." + ontology);			
		render(manager.getVersionManager().versioned(ontoHandle), writer);
	}

	public void render(VersionedOntology vonto, Writer writer) throws OWLRendererException
	{
		render(vonto, writer, new VOWLXMLRenderConfiguration());
	}

	public void render(VersionedOntology vonto, Writer writer, VOWLXMLRenderConfiguration configuration)
			throws OWLRendererException
	{
		StopWatch s = new StopWatch(true);
		try
		{
			VOWLXMLWriter vw = new VOWLXMLWriter(writer, vonto);

			vw.startDocument(vonto);

			vw.writePrefix("rdf:", Namespaces.RDF.toString());
			vw.writePrefix("rdfs:", Namespaces.RDFS.toString());
			vw.writePrefix("xsd:", Namespaces.XSD.toString());
			vw.writePrefix("owl:", Namespaces.OWL.toString());
			vw.writePrefix(VOWLXMLVocabulary.NAMESPACE_PREFIX.toString(), VOWLXMLVocabulary.NAMESPACE.toString());

			VOWLXMLObjectRenderer vren = new VOWLXMLObjectRenderer(vw, configuration);
			vren.visit(configuration);
			vren.visit(vonto);
			vw.endDocument();
			writer.flush();
			s.stop("VOWLXMLVersionedOntologyRenderer Render Process " + 
					vonto.getAtomHandle() + " Elements: "
					+ vw.getStartElementCount() + " Duration: ");
		}
		catch (IOException e)
		{
			throw new OWLRendererIOException(e);
		}
	}
}
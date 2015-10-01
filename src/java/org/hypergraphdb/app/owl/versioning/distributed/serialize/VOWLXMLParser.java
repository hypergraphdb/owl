package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.coode.owlapi.owlxmlparser.OWLXMLParser;
//import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.coode.owlapi.owlxmlparser.TranslatedOWLParserException;
import org.coode.owlapi.owlxmlparser.TranslatedUnloadableImportException;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLParserSAXException;
//import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * VOWLXMLParser.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLParser extends OWLXMLParser
{

	/**
	 * 
	 * @param documentSource
	 * @param versionedOntologyRoot
	 *            holds the result
	 * @param loaderConfig
	 * @return
	 * @throws OWLParserException
	 * @throws IOException
	 * @throws OWLOntologyChangeException
	 * @throws UnloadableImportException
	 */
	public OWLOntologyFormat parse(HyperGraph graph,
								   OWLOntologyDocumentSource documentSource, 
								   VOWLXMLDocument versionedOntologyRoot,
								   OWLOntologyLoaderConfiguration loaderConfig) 
	    throws OWLParserException, IOException, OWLOntologyChangeException, UnloadableImportException
	{
		InputSource isrc = null;
		try
		{
			System.setProperty("entityExpansionLimit", "100000000");
			VOWLXMLVersionedOntologyFormat format = new VOWLXMLVersionedOntologyFormat();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			isrc = getInputSource(documentSource); // TODO that null parameter
													// was just to compile with
													// 3.4.4
			VOWLXMLParserHandler handler = new VOWLXMLParserHandler(graph, 
																	versionedOntologyRoot, 
																	null, 
																	loaderConfig);
			parser.parse(isrc, handler);
			Map<String, String> prefix2NamespaceMap = handler.getPrefixName2PrefixMap();
			for (String prefix : prefix2NamespaceMap.keySet())
			{
				format.setPrefix(prefix, prefix2NamespaceMap.get(prefix));
			}
			return format;
		}
		catch (ParserConfigurationException e)
		{
			// serious trouble if this happens
			throw new OWLRuntimeException(e);
		}
		catch (TranslatedOWLParserException e)
		{
			throw e.getParserException();
		}
		catch (TranslatedUnloadableImportException e)
		{
			throw e.getUnloadableImportException();
		}
		catch (SAXException e)
		{
			// General exception
			throw new OWLParserSAXException(e);
		}
		finally
		{
			if (isrc != null && isrc.getByteStream() != null)
			{
				isrc.getByteStream().close();
			}
			else if (isrc != null && isrc.getCharacterStream() != null)
			{
				isrc.getCharacterStream().close();
			}
		}
	}
}
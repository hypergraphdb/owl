package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.coode.owlapi.owlxmlparser.OWLXMLParser;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.coode.owlapi.owlxmlparser.TranslatedOWLParserException;
import org.coode.owlapi.owlxmlparser.TranslatedUnloadableImportException;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.app.owl.versioning.ChangeSet;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLParserSAXException;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * VOWLXMLParser.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLParser extends OWLXMLParser {
	
	/**
	 * 
	 * @param documentSource
	 * @param versionedOntologyRoot holds the result
	 * @param loaderConfig
	 * @return
	 * @throws OWLParserException
	 * @throws IOException
	 * @throws OWLOntologyChangeException
	 * @throws UnloadableImportException
	 */
	public OWLOntologyFormat parse(OWLOntologyDocumentSource documentSource, VersionedOntologyRoot  versionedOntologyRoot, OWLOntologyLoaderConfiguration loaderConfig) throws OWLParserException, IOException, OWLOntologyChangeException, UnloadableImportException {
    	InputSource isrc = null;
    	try {
            System.setProperty("entityExpansionLimit", "100000000");
            VOWLXMLVersionedOntologyFormat format = new VOWLXMLVersionedOntologyFormat();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            isrc = getInputSource(documentSource);
            VOWLXMLParserHandler handler = new VOWLXMLParserHandler(versionedOntologyRoot, null, loaderConfig);
            parser.parse(isrc, handler);
            Map<String, String> prefix2NamespaceMap = handler.getPrefixName2PrefixMap();
            for(String prefix : prefix2NamespaceMap.keySet()) {
                format.setPrefix(prefix, prefix2NamespaceMap.get(prefix));
            }
            return format;
        }
        catch (ParserConfigurationException e) {
            // What the hell should be do here?  In serious trouble if this happens
            throw new OWLRuntimeException(e);
        }
        catch (TranslatedOWLParserException e) {
            throw e.getParserException();
        }
        catch (TranslatedUnloadableImportException e) {
            throw e.getUnloadableImportException();
        }
        catch (SAXException e) {
            // General exception
            throw new OWLParserSAXException(e);
		} finally {
			if (isrc != null && isrc.getByteStream() != null) {
				isrc.getByteStream().close();
			} else if (isrc != null && isrc.getCharacterStream() != null) {
				isrc.getCharacterStream().close();
			}
		}
    }
    
    /**
     * This class represents the toplevel structure of an VOWLXML document.
     * Use this to see, what the parsed versionedOntology contains in relation 
     * to the original ontology. 
     * 
     * VersionedOntologyRoot.
     * @author Thomas Hilpold (CIAO/Miami-Dade County)
     * @created Feb 29, 2012
     */
    public static class VersionedOntologyRoot {
    	private VOWLRenderConfiguration renderConfig;
    	private VersionedOntology versionedOntology;
    	private OWLOntology revisionData;
    	private int headRevisionIndex;
    	private HGPersistentHandle versionedOntologyID;
    	
    	/**
    	 * 
    	 * @param vo the VersionedOntology to merge the parsed results into; may be null.
    	 * @param onto the empty OWLOntoloy to add all revision Data to; may be null.
    	 */
    	public VersionedOntologyRoot(VersionedOntology vo, OWLOntology onto) {
    		setRenderConfig(new VOWLRenderConfiguration());
    		setVersionedOntology(vo);
    		setRevisionData(onto);
    	}
		/**
		 * Will be null, if no 
		 * @return the versionedOntology
		 */
		protected VersionedOntology getVersionedOntology() {
			return versionedOntology;
		}
		/**
		 * @param versionedOntology the versionedOntology to set
		 */
		protected void setVersionedOntology(VersionedOntology versionedOntology) {
			this.versionedOntology = versionedOntology;
		}
		/**
		 * @return the renderConfig
		 */
		protected VOWLRenderConfiguration getRenderConfig() {
			return renderConfig;
		}
		/**
		 * @param renderConfig the renderConfig to set
		 */
		protected void setRenderConfig(VOWLRenderConfiguration renderConfig) {
			this.renderConfig = renderConfig;
		}
		/**
		 * @return the revisionData
		 */
		protected OWLOntology getRevisionData() {
			return revisionData;
		}
		/**
		 * @param revisionData the revisionData to set
		 */
		protected void setRevisionData(OWLOntology revisionData) {
			this.revisionData = revisionData;
		}
		/**
		 * @return the headRevisionIndex
		 */
		protected int getHeadRevisionIndex() {
			return headRevisionIndex;
		}
		/**
		 * @param headRevisionIndex the headRevisionIndex to set
		 */
		protected void setHeadRevisionIndex(int headRevisionIndex) {
			this.headRevisionIndex = headRevisionIndex;
		}
    	
    }

}

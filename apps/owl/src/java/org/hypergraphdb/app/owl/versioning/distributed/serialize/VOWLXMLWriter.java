package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.coode.owlapi.owlxml.renderer.OWLXMLWriter;
import org.coode.xml.XMLWriter;
import org.coode.xml.XMLWriterFactory;
import org.coode.xml.XMLWriterNamespaceManager;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.OWLRendererIOException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.util.VersionInfo;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWLFacet;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;

/**
 * VOWLXMLWriter.
 * 
 * Based on org.coode.owlapi.owlxml.renderer.OWLXMLWriter
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public class VOWLXMLWriter extends OWLXMLWriter {
	
    private XMLWriter writer;
    private Map<String, String> iriPrefixMap = new TreeMap<String, String>(new StringLengthComparator());
	private int startElementCount;
		

	/**
	 * @param writer
	 * @param ontology
	 */
	public VOWLXMLWriter(Writer writer, VersionedOntology vontology) {
		super(writer, vontology.getWorkingSetData());
		startElementCount = 0;
		//we never use the superclass besides the forced super call.
		HGDBOntology ontology = vontology.getWorkingSetData();
		
        XMLWriterNamespaceManager nsm = new XMLWriterNamespaceManager(Namespaces.OWL.toString());
        nsm.setPrefix("xsd", Namespaces.XSD.toString());
        nsm.setPrefix("rdf", Namespaces.RDF.toString());
        nsm.setPrefix("rdfs", Namespaces.RDFS.toString());
        nsm.setPrefix("xml", Namespaces.XML.toString());
        nsm.setPrefix("vo", VOWLXMLVocabulary.NAMESPACE.toString());
        String base = Namespaces.OWL.toString();
        if (ontology != null && !ontology.isAnonymous()) {
            base = ontology.getOntologyID().getOntologyIRI().toString();
        }
        this.writer = XMLWriterFactory.getInstance().createXMLWriter(writer, nsm, base);
	}
	
    public void startDocument(VersionedOntology vontology) throws OWLRendererException {
        try {
            writer.startDocument(VOWLXMLVocabulary.VERSIONED_ONTOLOGY_ROOT.toString());
        }
        catch (IOException e) {
            throw new OWLRendererIOException(e);
        }
    }

    public void startOntologyData(OWLOntology ontology)  {
    	try {
    		writer.writeComment("Head Revision Ontology Data Start");
	        writeStartElement(OWLXMLVocabulary.ONTOLOGY);
	        if (!ontology.isAnonymous()) {
	            writer.writeAttribute(Namespaces.OWL + "ontologyIRI", ontology.getOntologyID().getOntologyIRI().toString());
	            if (ontology.getOntologyID().getVersionIRI() != null) {
	                writer.writeAttribute(Namespaces.OWL + "versionIRI", ontology.getOntologyID().getVersionIRI().toString());
	            }
	        }
    	} catch (IOException e) {
    		throw new OWLRuntimeException(e);
    	}
    }
    
    public void endOntologyData()  {
    	writeEndElement();
    }    
    
    /**
     * Writes a datatype attributed (used on Literal elements).  The full datatype IRI is written out
     * @param datatype The datatype
     */
    public void writeAttribute(String attributeName, String value) {
        try {
            writer.writeAttribute(attributeName.toString(), value);
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }
    
    public void writeCommment(String comment) {
        try {
            writer.writeComment(comment);
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }

    
	//
	//
	// COPIED CODE:
	//

    /**
     * String comparator that takes length into account before natural ordering.
     * XXX stateless, might be used through a singleton
     */
    private static final class StringLengthComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            int diff = o1.length() - o2.length();
            if (diff != 0) {
                return diff;
            }
            return o1.compareTo(o2);
        }
    }


    public Map<String, String> getIRIPrefixMap() {
        return iriPrefixMap;
    }

    public XMLWriterNamespaceManager getNamespaceManager() {
        return writer.getNamespacePrefixes();
    }

    /**
     * A convenience method to write a prefix.
     * @param prefixName The name of the prefix (e.g.  owl: is the prefix name for the OWL prefix)
     * @param iri The prefix iri
     */
    public void writePrefix(String prefixName, String iri) throws IOException {
        writer.writeStartElement(OWLXMLVocabulary.PREFIX.getURI().toString());
        if (prefixName.endsWith(":")) {
            String attName = prefixName.substring(0, prefixName.length() - 1);
            writer.writeAttribute(OWLXMLVocabulary.NAME_ATTRIBUTE.getURI().toString(), attName);
        }
        else {
            writer.writeAttribute(OWLXMLVocabulary.NAME_ATTRIBUTE.getURI().toString(), prefixName);
        }
        writer.writeAttribute(OWLXMLVocabulary.IRI_ATTRIBUTE.getURI().toString(), iri);
        writer.writeEndElement();
        iriPrefixMap.put(iri, prefixName);
    }

    /**
     * Gets an IRI attribute value for a full IRI.  If the IRI has a prefix that coincides with
     * a written prefix then the compact IRI will be returned, otherwise the full IRI will be returned.
     * @param iri The IRI
     * @return Either the compact version of the IRI or the full IRI.
     */
    public String getIRIString(URI iri) {
        String fullIRI = iri.toString();
        for (String prefixName : iriPrefixMap.keySet()) {
            if (fullIRI.startsWith(prefixName)) {
                StringBuilder sb = new StringBuilder();
                sb.append(iriPrefixMap.get(prefixName));
                sb.append(fullIRI.substring(prefixName.length()));
                return sb.toString();
            }
        }
        return fullIRI;
    }


    public void startDocument(OWLOntology ontology) throws OWLRendererException {
    	throw new UnsupportedOperationException();
//        try {
//            writer.startDocument(OWLXMLVocabulary.ONTOLOGY.toString());
//            if (!ontology.isAnonymous()) {
//                writer.writeAttribute(Namespaces.OWL + "ontologyIRI", ontology.getOntologyID().getOntologyIRI().toString());
//                if (ontology.getOntologyID().getVersionIRI() != null) {
//                    writer.writeAttribute(Namespaces.OWL + "versionIRI", ontology.getOntologyID().getVersionIRI().toString());
//                }
//            }
//        }
//        catch (IOException e) {
//            throw new OWLRendererIOException(e);
//        }
    }
    
    /**
     * 2012.02.27 Hilpold
     */
    public void writeStartElement(VOWLXMLVocabulary name) {
    	startElementCount ++;
        try {
            writer.writeStartElement(name.getURI().toString());
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }


    public void endDocument() {
        try {
            writer.endDocument();
            writer.writeComment(VersionInfo.getVersionInfo().getGeneratedByMessage());
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }


    public void writeStartElement(OWLXMLVocabulary name) {
    	startElementCount ++;
        try {
            writer.writeStartElement(name.getURI().toString());
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }



    public void writeEndElement() {
        try {
            writer.writeEndElement();
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }


    /**
     * Writes a datatype attributed (used on Literal elements).  The full datatype IRI is written out
     * @param datatype The datatype
     */
    public void writeDatatypeAttribute(OWLDatatype datatype) {
        try {
            writer.writeAttribute(OWLXMLVocabulary.DATATYPE_IRI.getURI().toString(), datatype.getIRI().toString());
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }

    public void writeNodeIDAttribute(NodeID nodeID) {
        try {
            writer.writeAttribute(OWLXMLVocabulary.NODE_ID.getURI().toString(), nodeID.toString());
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }
    }

    public void writeIRIAttribute(IRI iri) {
        try {
            String attName = OWLXMLVocabulary.IRI_ATTRIBUTE.getURI().toString();
            String value = iri.toString();
            if (value.startsWith(writer.getXMLBase())) {
                writer.writeAttribute(attName, value.substring(writer.getXMLBase().length(), value.length()));
            }
            else {
                String val = getIRIString(iri.toURI());
                if (!val.equals(iri.toString())) {
                    writer.writeAttribute(OWLXMLVocabulary.ABBREVIATED_IRI_ATTRIBUTE.getURI().toString(), val);
                }
                else {
                    writer.writeAttribute(attName, val);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Writes an IRI element for a given IRI
     * @param iri The IRI to be written as an element.  If the IRI can be abbreviated
     * then an AbbreviatedIRI element will be written
     * @throws IOException
     */
    public void writeIRIElement(IRI iri) {
    	startElementCount ++;
        try {
            String iriString = iri.toString();
            if (iriString.startsWith(writer.getXMLBase())) {
                writeStartElement(OWLXMLVocabulary.IRI_ELEMENT);
                writeTextContent(iriString.substring(writer.getXMLBase().length(), iriString.length()));
                writeEndElement();
            }
            else {
                String val = getIRIString(iri.toURI());
                if (!val.equals(iriString)) {
                    writeStartElement(OWLXMLVocabulary.ABBREVIATED_IRI_ELEMENT);
                    writer.writeTextContent(val);
                    writeEndElement();
                }
                else {
                    writeStartElement(OWLXMLVocabulary.IRI_ELEMENT);
                    writer.writeTextContent(val);
                    writeEndElement();
                }
            }
        }
        catch (IOException e) {
            throw new OWLRuntimeException(e);
        }

    }

    public void writeLangAttribute(String lang) {
        try {
            writer.writeAttribute(Namespaces.XML + "lang", lang);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeCardinalityAttribute(int cardinality) {
        try {
            writer.writeAttribute(OWLXMLVocabulary.CARDINALITY_ATTRIBUTE.getURI().toString(), Integer.toString(cardinality));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeTextContent(String text) {
        try {
            writer.writeTextContent(text);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeFacetAttribute(OWLFacet facet) {
        try {
            writer.writeAttribute(OWLXMLVocabulary.DATATYPE_FACET.getURI().toString(), facet.getIRI().toString());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeAnnotationURIAttribute(URI uri) {
        try {
            writer.writeAttribute(OWLXMLVocabulary.ANNOTATION_URI.toString(), uri.toString());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
	/**
	 * Returns the number of times our writer writes startElement.
	 * @return the startElementCount
	 */
	public int getStartElementCount() {
		return startElementCount;
	}
}

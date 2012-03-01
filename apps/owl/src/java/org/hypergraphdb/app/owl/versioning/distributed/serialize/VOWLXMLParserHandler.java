package org.hypergraphdb.app.owl.versioning.distributed.serialize;

import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.CHANGE_SET;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.NAMESPACE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.RENDER_CONFIGURATION;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.REVISION;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_ADD_AXIOM_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_ADD_IMPORT_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_ADD_ONTOLOGY_ANNOTATION_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_NEW_ID;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_MODIFY_ONTOLOGY_ID_OLD_ID;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_REMOVE_AXIOM_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_REMOVE_IMPORT_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.VERSIONED_ONTOLOGY;
import static org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary.VERSIONED_ONTOLOGY_ROOT;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.coode.owlapi.owlxmlparser.OWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLElementHandlerFactory;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.coode.owlapi.owlxmlparser.TranslatedOWLParserException;
import org.coode.owlapi.owlxmlparser.TranslatedUnloadableImportException;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.ChangeSetElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RenderConfigurationElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.RevisionElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VOWLChangeElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyElementHandler;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VersionedOntologyRootElementHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.OWLParserURISyntaxException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWLXMLVocabulary;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * OWLXMLVParserHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 29, 2012
 */
public class VOWLXMLParserHandler extends OWLXMLParserHandler {

    private OWLOntologyManager owlOntologyManager;

    private VOWLXMLParser.VersionedOntologyRoot versionedOntologyRoot;

    private List<OWLElementHandler<?>> vhandlerStack;

    private Map<String, OWLElementHandlerFactory> vhandlerMap;

    private Map<String, String> prefixName2PrefixMap = new HashMap<String, String>();

    private Locator locator;

    private Stack<URI> bases;

    private OWLOntologyLoaderConfiguration configuration;
    
    /**
     * True, if we are currently processing an ontology Object that our superclass shall handle.
     * Overwritten methods will return superclass's methods values during OWL element processing.
     */
    private boolean owlElementProcessingMode;

    public VOWLXMLParserHandler(VOWLXMLParser.VersionedOntologyRoot voRoot) {
    	this(voRoot, null, new OWLOntologyLoaderConfiguration());
    }

    /**
     * Creates an OWLXML handler with the specified top level handler.  This allows OWL/XML
     * representations of axioms to be embedded in abitrary XML documents e.g. DIG 2.0 documents.
     * (The default handler behaviour expects the top level element to be an Ontology
     * element).
     *
     * @param ontology The ontology object that the XML representation should be parsed into.
     */
    public VOWLXMLParserHandler(VOWLXMLParser.VersionedOntologyRoot voRoot, OWLElementHandler<?> topHandler, OWLOntologyLoaderConfiguration configuration) {
        super(voRoot.getRevisionData(), topHandler, configuration);
        this.versionedOntologyRoot = voRoot;
        //this.ontology = voRoot.getRevisionData();
        this.bases = new Stack<URI>();
        this.configuration = configuration;
        vhandlerStack = new ArrayList<OWLElementHandler<?>>();
        prefixName2PrefixMap = new HashMap<String, String>();
        prefixName2PrefixMap.put("owl:", Namespaces.OWL.toString());
        prefixName2PrefixMap.put("xsd:", Namespaces.XSD.toString());
        if (topHandler != null) {
            vhandlerStack.add(0, topHandler);
        }
        vhandlerMap = new HashMap<String, OWLElementHandlerFactory>();

        addFactory(new AbstractVElementHandlerFactory(VERSIONED_ONTOLOGY_ROOT) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VersionedOntologyRootElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(RENDER_CONFIGURATION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new RenderConfigurationElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(VERSIONED_ONTOLOGY) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VersionedOntologyElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(REVISION) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new RevisionElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(CHANGE_SET) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new ChangeSetElementHandler(handler);
            }
        });

        addFactory(new AbstractVElementHandlerFactory(V_ADD_AXIOM_CHANGE) {
            public OWLElementHandler<?> createHandler(OWLXMLParserHandler handler) {
                return new VOWLChangeElementHandler(handler);
            }
        }, V_ADD_AXIOM_CHANGE.getShortName(), 
           V_ADD_IMPORT_CHANGE.getShortName(),
           V_ADD_ONTOLOGY_ANNOTATION_CHANGE.getShortName(),
           V_MODIFY_ONTOLOGY_ID_CHANGE.getShortName(),
           V_MODIFY_ONTOLOGY_ID_NEW_ID.getShortName(),
           V_MODIFY_ONTOLOGY_ID_OLD_ID.getShortName(),
           V_REMOVE_AXIOM_CHANGE.getShortName(),
           V_REMOVE_IMPORT_CHANGE.getShortName(),
           V_REMOVE_ONTOLOGY_ANNOTATION_CHANGE.getShortName());
    }
	
	
//    @Override
//	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
////        try {
////        }
//    }
    
    private void addFactory(OWLElementHandlerFactory factory, String... legacyElementNames) {
        vhandlerMap.put(factory.getElementName(), factory);
        for (String elementName : legacyElementNames) {
            vhandlerMap.put(elementName, factory);
        }
    }

    public OWLOntology getOntology() {
        return versionedOntologyRoot.getRevisionData();
    }

    public OWLDataFactory getDataFactory() {
        return getOWLOntologyManager().getOWLDataFactory();
    }

    @Override
	public void startDocument() throws SAXException {

    }


    @Override
	public void endDocument() throws SAXException {

    }


    @Override
	public void characters(char ch[], int start, int length) throws SAXException {
        if (!vhandlerStack.isEmpty()) {
            try {
                OWLElementHandler<?> handler = vhandlerStack.get(0);
                if (handler.isTextContentPossible()) {
                    handler.handleChars(ch, start, length);
                }
            }
            catch (OWLException e) {
                throw new SAXException(e);
            }
        }
    }


    @Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            processXMLBase(attributes);
            super.processXMLBase(attributes);
            if (localName.equals(OWLXMLVocabulary.PREFIX.getShortName())) {
                String name = attributes.getValue(OWLXMLVocabulary.NAME_ATTRIBUTE.getShortName());
                String iriString = attributes.getValue(OWLXMLVocabulary.IRI_ATTRIBUTE.getShortName());
                if (name != null && iriString != null) {
                    if (name.endsWith(":")) {
                        prefixName2PrefixMap.put(name, iriString);
                    }
                    else {
                        prefixName2PrefixMap.put(name + ":", iriString);
                    }
                }
                super.startElement(uri, localName, qName, attributes);
                return;
            }
            // Shall we process this element?
            if (isVersionedElement(localName)) {
            	owlElementProcessingMode = false;
	            OWLElementHandlerFactory handlerFactory = vhandlerMap.get(localName);
	            if (handlerFactory != null) {
	                OWLElementHandler<?> handler = handlerFactory.createHandler(this);
	                if (!vhandlerStack.isEmpty()) {
	                    OWLElementHandler<?> topElement = vhandlerStack.get(0);
	                    handler.setParentHandler(topElement);
	                }
	                vhandlerStack.add(0, handler);
	                for (int i = 0; i < attributes.getLength(); i++) {
	                    handler.attribute(attributes.getLocalName(i), attributes.getValue(i));
	                }
	                handler.startElement(localName);
	            }
            } else {
            	owlElementProcessingMode = true;
            	//Not versioning related, let super handle ontology data or fail.
            	super.startElement(uri, localName, qName, attributes);
            }
        }
        catch (OWLParserException e) {
            throw new TranslatedOWLParserException(e);
        }
    }

    protected void processXMLBase(Attributes attributes) {
        String base = attributes.getValue(Namespaces.XML.toString(), "base");
        if (base != null) {
            bases.push(URI.create(base));
        }
        else {
            bases.push(bases.peek());
        }
    }

    /**
     * Return the base URI for resolution of relative URIs
     *
     * @return base URI or null if unavailable (xml:base not present and the
     *         document locator does not provide a URI)
     */
    public URI getBase() {
    	if (owlElementProcessingMode) {
    		return super.getBase();
    	} else {
    		return bases.peek();
    	}
    }


    @Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (localName.equals(OWLXMLVocabulary.PREFIX.getShortName())) {
                return;
            }
            if (isVersionedElement(localName)) {
            	if (!vhandlerStack.isEmpty()) {
            		OWLElementHandler<?> handler = vhandlerStack.remove(0);
            		handler.endElement();
            	}
            } 
            bases.pop();
            // We let super handle it, so the base gets popped.
            // This should work, because if we end a versionedElement the super.handlerStack 
            // will be empty as there is no versioned object inside an OWLObject, 
            // respective an OWLOntology. 
            super.endElement(uri, localName, qName);
        }
        catch (OWLParserException e) {
            // Temporarily translate to a SAX parse exception
            throw new TranslatedOWLParserException(e);
        }
        catch (UnloadableImportException e) {
            // Temporarily translate to a SAX parse exception
            throw new TranslatedUnloadableImportException(e);
        }
    }
    
    public boolean isVersionedElement(String localName) {
    	return vhandlerMap.containsKey(localName);
    }

    @Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
        prefixName2PrefixMap.put(prefix, uri);
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;

        URI base = null;
        try {
            String systemId = locator.getSystemId();
            if (systemId != null)
                base = new URI(systemId);
        }
        catch (URISyntaxException e) {
        }

        bases.push(base);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getConfiguration()
	 */
	@Override
	public OWLOntologyLoaderConfiguration getConfiguration() {
        return configuration;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getLineNumber()
	 */
	@Override
	public int getLineNumber() {
        if (locator != null) {
            return locator.getLineNumber();
        }
        else {
            return -1;
        }
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getColumnNumber()
	 */
	@Override
	public int getColumnNumber() {
        if (locator != null) {
            return locator.getColumnNumber();
        }
        else {
            return -1;
        }
	}

    private Map<String, IRI> iriMap = new HashMap<String, IRI>();

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getIRI(java.lang.String)
	 */
	@Override
	public IRI getIRI(String iriStr) throws OWLParserException {
        try {
            IRI iri = iriMap.get(iriStr);
            if (iri == null) {
                URI uri = new URI(iriStr);
                if (!uri.isAbsolute()) {
                    URI base = getBase();
                    if (base == null)
                        throw new OWLXMLParserException("Unable to resolve relative URI", getLineNumber(), getColumnNumber());
//                    iri = IRI.create(getBase().resolve(uri));
                    iri = IRI.create(base + iriStr);
                }
                else {
                    iri = IRI.create(uri);
                }
                iriMap.put(iriStr, iri);
            }
            return iri;
        }
        catch (URISyntaxException e) {
            throw new OWLParserURISyntaxException(e, getLineNumber(), getColumnNumber());
        }
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getAbbreviatedIRI(java.lang.String)
	 */
	@Override
	public IRI getAbbreviatedIRI(String abbreviatedIRI) throws OWLParserException {
        String normalisedAbbreviatedIRI = getNormalisedAbbreviatedIRI(abbreviatedIRI);
        int sepIndex = normalisedAbbreviatedIRI.indexOf(':');
        String prefixName = normalisedAbbreviatedIRI.substring(0, sepIndex + 1);
        String localName = normalisedAbbreviatedIRI.substring(sepIndex + 1);
        String base = prefixName2PrefixMap.get(prefixName);
        if (base == null) {
            throw new OWLXMLParserException("Prefix name not defined: " + prefixName, getLineNumber(), getColumnNumber());
        }
        StringBuilder sb = new StringBuilder();
        sb.append(base);
        sb.append(localName);
        return getIRI(sb.toString());
	}

    private String getNormalisedAbbreviatedIRI(String input) {
        if (input.indexOf(':') != -1) {
            return input;
        }
        else {
            return ":" + input;
        }
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getPrefixName2PrefixMap()
	 */
	@Override
	public Map<String, String> getPrefixName2PrefixMap() {
        return prefixName2PrefixMap;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#resolveEntity(java.lang.String, java.lang.String)
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        // superclass will refer it to defaulthandler, we could omit it here.
		return super.resolveEntity(publicId, systemId);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLXMLParserHandler#getOWLOntologyManager()
	 */
	@Override
	public OWLOntologyManager getOWLOntologyManager() {
        return owlOntologyManager;
	}
}

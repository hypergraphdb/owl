package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VPrefixMapElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 3, 2012
 */
public class VPrefixMapEntryElementHandler extends AbstractVOWLElementHandler<Object> {

	private String prefixName;
	private String namespace;
	
	/**
	 * @param handler
	 */
	public VPrefixMapEntryElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		reset();
	}

	public void reset() {
		prefixName = null;
		namespace = null;
	}
	
	@Override
	public void attribute(String localName, String value) throws OWLParserException {
        if (localName.equals("prefixName")) {
        	prefixName = value;
        }
        if (localName.equals("namespace")) {
        	namespace = value;
        }
    }

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws OWLXMLParserException {
		//reset();
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		if (prefixName == null) throw new OWLParserException("PrefixEntry prefixName could not be parsed.");
		if (namespace == null) throw new OWLParserException("PrefixEntry namespace could not be parsed.");
		getParentHandler().handleChild(this);
	}

	/**
	 * Unsupported exception thrown here; use other methods to get parsed objects.
	 */
	@Override
	public Object getOWLObject() throws OWLXMLParserException {
		throw new UnsupportedOperationException("Use the other methods to get prefixName and namespace");
	}

	/**
	 * @return the prefixName
	 */
	public String getPrefixName() {
		return prefixName;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}
}
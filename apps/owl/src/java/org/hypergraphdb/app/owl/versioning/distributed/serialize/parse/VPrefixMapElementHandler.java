package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.HashMap;
import java.util.Map;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VPrefixMapElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 3, 2012
 */
public class VPrefixMapElementHandler extends AbstractVOWLElementHandler<Object> {

	private Map<String, String> prefixMap;
	
	/**
	 * @param handler
	 */
	public VPrefixMapElementHandler(OWLXMLParserHandler handler) {
		super(handler);
		reset();
	}

	public void reset() {
		prefixMap = new HashMap<String, String>();
	}
	
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#startElement(java.lang.String)
	 */
	@Override
	public void startElement(String name) throws OWLXMLParserException {
		//reset();
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.AbstractVOWLElementHandler#handleChild(org.hypergraphdb.app.owl.versioning.distributed.serialize.parse.VPrefixMapEntryElementHandler)
	 */
	@Override
	public void handleChild(VPrefixMapEntryElementHandler h) throws OWLXMLParserException {
		prefixMap.put(h.getPrefixName(), h.getNamespace());
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		getParentHandler().handleChild(this);
	}

	/**
	 * Get's the map of prefixes to be a applied to the OWLOntologyEx
	 */
	@Override
	public Object getOWLObject() throws OWLXMLParserException {
		return prefixMap;
	}
}
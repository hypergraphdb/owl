package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.versioning.change.VAddPrefixChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VPrefixChange;
import org.hypergraphdb.app.owl.versioning.change.VRemovePrefixChange;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLVocabulary;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VPrefixChangeElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public class VPrefixChangeElementHandler extends VOWLChangeElementHandler {

	private VPrefixChange prefixChange;
	private String prefixName;
	private String prefix;
	
	/**
	 * @param handler
	 */
	public VPrefixChangeElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}	
	
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public VOWLChange getOWLObject() throws OWLXMLParserException {
		if (prefixChange == null) throw new OWLXMLParserException("Failed to parse prefix Change", getLineNumber(), getColumnNumber());
		return prefixChange;
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#attribute(java.lang.String, java.lang.String)
	 */
	@Override
	public void attribute(String localName, String value) throws OWLParserException {
		if (localName.equals("prefixName")) {
			prefixName = value.trim();
		} else if (localName.equals("prefix")) {
			prefix = value.trim();
		} else {
			throw new OWLParserException("attribute not recognized: " + localName);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		String name = getElementName();
		if (prefixName == null) throw new IllegalStateException("Prefix was null, parsing error.");
		Pair<String, String> prefixPair = new Pair<String, String>(prefixName, prefix);
		HGHandle prefixPairHandle = getHyperGraph().add(prefixPair);
		if (name.equals(VOWLXMLVocabulary.V_ADD_PREFIX_CHANGE.getShortName())) {
			prefixChange = new VAddPrefixChange(prefixPairHandle);
			getParentHandler().handleChild(this);
		} else if (name.equals(VOWLXMLVocabulary.V_REMOVE_PREFIX_CHANGE.getShortName())) {
			prefixChange = new VRemovePrefixChange(prefixPairHandle);
			getParentHandler().handleChild(this);
		} else {
			getHyperGraph().remove(prefixPairHandle, true);
			throw new IllegalStateException("element unknown");
		}
	}
}
package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;
import org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.UnloadableImportException;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 18-Dec-2006<br><br>
 */
public class OWLImportsHandlerModified extends AbstractOWLElementHandler<OWLImportsDeclaration> {
	OWLImportsDeclaration importsDeclaration;
	
    public OWLImportsHandlerModified(OWLXMLParserHandler handler) {
        super(handler);
    }

    public void endElement() throws OWLParserException, UnloadableImportException {
        IRI ontIRI = getIRI(getText().trim());
        importsDeclaration = getOWLDataFactory().getOWLImportsDeclaration(ontIRI);
        //2012.03.06 this is the reason we modified the class, we wanted to disable: 
        // getOWLOntologyManager().applyChange(new AddImport(getOntology(), decl));
        // getOWLOntologyManager().makeLoadImportRequest(decl, getConfiguration());
    }

    public OWLImportsDeclaration getOWLObject() {
    	if (importsDeclaration == null) throw new IllegalStateException("importsdeclaration null");
        return importsDeclaration;
    }

    @Override
	public boolean isTextContentPossible() {
        return true;
    }
}
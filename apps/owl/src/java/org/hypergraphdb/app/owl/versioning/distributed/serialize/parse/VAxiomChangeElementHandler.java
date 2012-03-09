package org.hypergraphdb.app.owl.versioning.distributed.serialize.parse;

import java.util.Set;
import java.util.concurrent.Callable;

import org.coode.owlapi.owlxmlparser.AbstractOWLAxiomElementHandler;
import org.coode.owlapi.owlxmlparser.OWLXMLParserException;
import org.coode.owlapi.owlxmlparser.OWLXMLParserHandler;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.hypergraphdb.app.owl.versioning.change.VAddAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VRemoveAxiomChange;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLVocabulary;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.UnloadableImportException;

/**
 * VAxiomChangeElementHandler.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 5, 2012
 */
public class VAxiomChangeElementHandler extends VOWLChangeElementHandler {

	private VAxiomChange axiomChange;
	private OWLAxiomHGDB axiom;
	
	/**
	 * @param handler
	 */
	public VAxiomChangeElementHandler(OWLXMLParserHandler handler) {
		super(handler);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.AbstractOWLElementHandler#handleChild(org.coode.owlapi.owlxmlparser.AbstractOWLAxiomElementHandler)
	 */
	@Override
	public void handleChild(AbstractOWLAxiomElementHandler _handler) throws OWLXMLParserException {
		//this axiom is not yet added to the graph
		axiom = (OWLAxiomHGDB)_handler.getOWLObject();
		// add axiom to graph
		// This will add the axiom and all annotations.
		addAxiomToGraph();
		System.out.println("PARSED Axiom change: "  + axiom);
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#endElement()
	 */
	@Override
	public void endElement() throws OWLParserException, UnloadableImportException {
		String name = getElementName();
		if (axiom == null ) {
			throw new IllegalStateException("Error: axiom was null." + axiom);
		}
		HGHandle axiomHandle = getHyperGraph().getHandle(axiom);
		if (axiomHandle == null) {
			throw new IllegalStateException("Error: axiom must be added to graph." + axiom);
		}
		if (name.equals(VOWLVocabulary.V_ADD_AXIOM_CHANGE.getShortName())) {
			//HGHandle axiomHandle = getHyperGraph().add(axiom);
			axiomChange = new VAddAxiomChange(axiomHandle);
			getParentHandler().handleChild(this);
		} else if (name.equals(VOWLVocabulary.V_REMOVE_AXIOM_CHANGE.getShortName())) {
			//HGHandle axiomHandle = getHyperGraph().add(axiom);
			axiomChange = new VRemoveAxiomChange(axiomHandle);
			getParentHandler().handleChild(this);
		} else {
			throw new OWLParserException("Unknown Element" + name);
		}
	}

	/* (non-Javadoc)
	 * @see org.coode.owlapi.owlxmlparser.OWLElementHandler#getOWLObject()
	 */
	@Override
	public VOWLChange getOWLObject() throws OWLXMLParserException {
		if (axiomChange == null) throw new OWLXMLParserException("Handler axiomChange was null on get", getLineNumber(), getColumnNumber());
		return axiomChange;
	}

	/**
	 * Adds a parsed axiom and all associated annotations to the graph without adding it to any ontology.
	 * This is almost code duplication from HGDBOntologyInternal addAxiomByType.
	 */
	private void addAxiomToGraph() {
		final HyperGraph graph = getHyperGraph();
		graph.getTransactionManager().ensureTransaction(new Callable<Boolean>() {
			public Boolean call() {
				OWLAxiomHGDB axiomHGDB = (OWLAxiomHGDB) axiom;
				// 2012.02.06 hilpold hashCode indexed
				// ensure hashCode Calculated before storage
				axiomHGDB.hashCode();
				axiomHGDB.setLoadAnnotations(false);
				//if (DBG) ontology.printGraphStats("Before AddAxiom");
				// 2011.10.06 hilpold adding to graph here instead of
				// previously in Datafactory
				// 2012.01.05 hilpold copy axioms operation from onto 1 to onto 2 will have axiom in graph already: 
				// Therefore we need to check, if it's already in graph?
				HGHandle axiomHandle = graph.getHandle(axiom);
				if (axiomHandle == null) {
					axiomHandle = graph.add(axiom);
				}
				// ---- ontology.add(axiomHandle);
				//
				// OWLAnnotation handling (== AxiomAnnotatedBy links
				// added to graph)
				//
				Set<OWLAnnotation> annos = axiom.getAnnotations();
				for (OWLAnnotation anno : annos) {
					HGHandle annoHandle = graph.getHandle(anno);
					if (annoHandle == null) throw new IllegalStateException("AnnotationHandle null.");
					if (hg.findOne(graph,
							hg.and(hg.type(AxiomAnnotatedBy.class), hg.orderedLink(axiomHandle, annoHandle))) != null) {
						// link exists between the given
						// axiom and current annotation.
						//2012.01.05 that's ok, might be an undo anyways.
						//throw new IllegalStateException(
								//"Added axiom with existing AxiomAnnotatedLink to annotation " + anno);
					} else {
						//Link does not exist -> create
						AxiomAnnotatedBy link = new AxiomAnnotatedBy(axiomHandle, annoHandle);
						graph.add(link);
					}
				}
				///if (DBG) ontology.printGraphStats("After AddAxiom");
				return true;
			}
		});
	}
}
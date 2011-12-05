package org.hypergraphdb.app.owl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.NNF;
/**
 * OWLAxiomHGDB.
 * 
 * OWLAnnotations are expected to be connected in the graph by AxiomAnnotatedBy Links,
 * which must exist once the axiom was stored.
 * 
 * Annotations will be loaded from the graph, when the handle of the axiom is set.
 *
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 18, 2011
 */
public abstract class OWLAxiomHGDB extends OWLObjectHGDB implements OWLAxiom, HGLink {
	
    private OWLAxiom nnf;

    //private final Set<OWLAnnotation> annotations;
    private Set<OWLAnnotation> annotations;
    
    /**
     * Switch indicating that the annotations shall be loaded from the graph on setAtomHandle(..).
     * (As opposed to being set in the constructor already.)
     * True by default, should be false after axiom get's added to an ontology (to prevent loading known annotations) or 
     * after the axiom was created by HG and the annotations were loaded.
     */
    private boolean loadAnnotations = true;
    
    public OWLAxiomHGDB() {
    	this(Collections.<OWLAnnotation>emptySet());
    }
    
    public OWLAxiomHGDB(Collection<? extends OWLAnnotation> annotations) {
        if (!annotations.isEmpty()) {
            this.annotations = CollectionFactory.getCopyOnRequestSet(new TreeSet<OWLAnnotation>(annotations));
        }
        else {
            this.annotations = Collections.emptySet();
        }
    }    		

	/**
	 * @return the loadAnnotations
	 */
	public boolean isLoadAnnotations() {
		return loadAnnotations;
	}

	/**
	 * @param loadAnnotations false, if loading annotations from graph should not take place.
	 */
	public void setLoadAnnotations(boolean loadAnnotations) {
		this.loadAnnotations = loadAnnotations;
	}

	public boolean isAnnotated() {
        return !annotations.isEmpty();
    }

	public Set<OWLAnnotation> getAnnotations() {
        return annotations;
    }
    
    /**
     * This axiom might have been created by our datafactory or loaded from the graph.
     * If this axiom was loaded from the graph it will have an atomhandle set and we will try to load the annotations from the graph once and 
     * switch annotationsLoaded to true.
     * The only drawback here is, that this axiom might be created by DF, then stored in the graph.
     * In this sequence, we take a performance penalty as we already know the annotations but query the graph unnecessarily anyways.
     */
    protected void loadAnnotationsFromGraph() {
    	if (getAtomHandle() == null) throw new IllegalStateException("Atomhandle null.");
    	if (getHyperGraph() == null) throw new IllegalStateException("Hypergraph null.");
    	HGHandle atomHandle = getAtomHandle(); 
		annotations = new TreeSet<OWLAnnotation>();
		annotations.addAll((Collection<? extends OWLAnnotation>) hg.<OWLAnnotation>getAll(getHyperGraph(), 
				hg.apply(hg.targetAt(getHyperGraph(), 1), //1 .. Annotation, 0 .. Axiom for AxiomAnnotatedBy
				hg.and(hg.type(AxiomAnnotatedBy.class),
				hg.incident(atomHandle)))
				)); //apply / getAll   			
		annotations = CollectionFactory.getCopyOnRequestSet(annotations);
    }

    /* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.OWLObjectHGDB#setAtomHandle(org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setAtomHandle(HGHandle handle) {
		if (getHyperGraph() == null) throw new IllegalStateException("Hypergraph null.");
		super.setAtomHandle(handle);
		if (loadAnnotations) {
			loadAnnotationsFromGraph();
			loadAnnotations = false;
		}
	}

	public Set<OWLAnnotation> getAnnotations(OWLAnnotationProperty annotationProperty) {
        if (annotations.isEmpty()) {
            return annotations;
        }
        else {
            Set<OWLAnnotation> result = new HashSet<OWLAnnotation>();
            for (OWLAnnotation anno : annotations) {
                if (anno.getProperty().equals(annotationProperty)) {
                    result.add(anno);
                }
            }
            return result;
        }
    }

    /**
     * Determines if another axiom is equal to this axiom not taking into consideration the annotations on the axiom
     * @param axiom The axiom to test if equal
     * @return <code>true</code> if <code>axiom</code> without annotations is equal to this axiom without annotations
     *         otherwise <code>false</code>.
     */
    public boolean equalsIgnoreAnnotations(OWLAxiom axiom) {
        return this.getAxiomWithoutAnnotations().equals(axiom.getAxiomWithoutAnnotations());
    }

    /**
     * Determines if this axiom is one of the specified types
     * @param axiomTypes The axiom types to check for
     * @return <code>true</code> if this axiom is one of the specified types, otherwise <code>false</code>
     * @since 3.0
     */
    public boolean isOfType(AxiomType<?>... axiomTypes) {
        for (AxiomType<?> type : axiomTypes) {
            if (getAxiomType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if this axiom is one of the specified types
     * @param types The axiom types to check for
     * @return <code>true</code> if this axioms is one of the specified types, otherwise <code>false</code>
     * @since 3.0
     */
    public boolean isOfType(Set<AxiomType<?>> types) {
        return types.contains(getAxiomType());
    }

    /**
     * A convenience method for implementation that returns a set containing the annotations on this axiom plus the
     * annoations in the specified set.
     * @param annos The annotations to add to the annotations on this axiom
     * @return The annotations
     */
    protected Set<OWLAnnotation> mergeAnnos(Set<OWLAnnotation> annos) {
        Set<OWLAnnotation> merged = new HashSet<OWLAnnotation>(annos);
        merged.addAll(getAnnotations());
        return merged;
    }

    @Override
	public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OWLAxiom)) {
            return false;
        }
        OWLAxiom other = (OWLAxiom) obj;
        return annotations.equals(other.getAnnotations());
    }

    public Set<OWLEntity> getReferencedEntities() {
    	return getSignature();
    }

    public OWLAxiom getNNF() {
        if (nnf == null) {
            NNF con = new NNF(getOWLDataFactory());
            nnf = accept(con);
        }
        return nnf;
    }
}
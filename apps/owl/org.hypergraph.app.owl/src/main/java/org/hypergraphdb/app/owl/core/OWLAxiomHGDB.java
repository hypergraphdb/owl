package org.hypergraphdb.app.owl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
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
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 18, 2011
 */
public abstract class OWLAxiomHGDB extends OWLObjectHGDB implements OWLAxiom
{
    private OWLAxiom nnf;

    //private final Set<OWLAnnotation> annotations;
    private Set<OWLAnnotation> annotations;
    
    /**
     * Switch indicating that we (lazily)loaded the annotations from the graph once.
     */
    private boolean annotationsLoaded = false;
    
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
    				
    public boolean isAnnotated() {
        return !annotations.isEmpty();
    }

    /**
     * This axiom might have been created by our datafactory or loaded from the graph.
     * If we were loaded from the graph we will have an atomhandle set and we will try to load the annotations from the graph once and 
     * switch annotationsLoaded to true.
     * The only drawback here is, that we might be created by DF, then stored in the graph and then asked for annotations.
     * In this sequence, we take a performance penalty as we already know the annotations but query the graph unnecessarily anyways.
     *
     */
    @SuppressWarnings("unchecked")
	public Set<OWLAnnotation> getAnnotations() {
    	if (!annotationsLoaded && getAtomHandle() != null) {
        	HGHandle atomHandle = getAtomHandle(); 
    		annotationsLoaded = true;
    		annotations = new TreeSet<OWLAnnotation>();
   			annotations.addAll((Collection<? extends OWLAnnotation>) hg.getAll(getHyperGraph(), 
   					hg.and(hg.type(AxiomAnnotatedBy.class),
   					hg.incident(atomHandle))));   			
   			annotations = CollectionFactory.getCopyOnRequestSet(annotations);
    	} // else keep annotations. which might be set by the datafactory.
        return annotations;
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

package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * OWLDeclarationAxiomHGDB.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 28, 2011
 */
public class OWLDeclarationAxiomHGDB extends OWLAxiomHGDB implements OWLDeclarationAxiom, HGLink {

    private HGHandle owlEntityHandle;


    public OWLDeclarationAxiomHGDB(HGHandle...args)
    {
    	this(args[0], Collections.<OWLAnnotation>emptySet());
    }
    
    public OWLDeclarationAxiomHGDB(HGHandle owlEntityHandle, Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
        //let's avoid late disaster, if entity is not a handle for a OWLEntity subclass object.
        //We can;t do this here, because we have no graph yet!!!!
        //if (!TypeUtils.isClassAssignableFromAtomHandleType(OWLEntity.class, owlEntityHandle, getHyperGraph())) {
        //	throw new IllegalArgumentException("OwlEntityHandle atom not usable as OWLEntity.");
        //}
        notifyTargetHandleUpdate(0, owlEntityHandle);
    }

    public boolean isLogicalAxiom() {
        return false;
    }

    public boolean isAnnotationAxiom() {
        return false;
    }

    public OWLDeclarationAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLDeclarationAxiom(getEntity());
    }

    public OWLDeclarationAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLDeclarationAxiom(getEntity(), mergeAnnos(annotations));
    }

    public OWLEntity getEntity() {
        return getHyperGraph().get(owlEntityHandle);
    }


    public Set<OWLAnnotationAssertionAxiom> getEntityAnnotations(OWLOntology ontology) {
        return ontology.getAnnotationAssertionAxioms(getEntity().getIRI());
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (obj instanceof OWLDeclarationAxiom) {
                return ((OWLDeclarationAxiom) obj).getEntity().equals(getEntity());
            }
        }
        return false;
    }


    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }


    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.DECLARATION;
    }


    @Override
	protected int compareObjectOfSameType(OWLObject object) {
    	return getEntity().compareTo(((OWLDeclarationAxiom) object).getEntity());
    }


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 1;
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i != 0) throw new HGException("Index i must be 0");
		return owlEntityHandle;
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0) throw new HGException("Index i must be 0");
		owlEntityHandle = handle;		
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0) throw new HGException("Index i must be 0");		
		owlEntityHandle = null;
	}
}

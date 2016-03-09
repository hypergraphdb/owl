package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLAnnotationPropertyRangeAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 14, 2011
 */
public class OWLAnnotationPropertyRangeAxiomHGDB extends OWLAxiomHGDB implements HGLink, OWLAnnotationPropertyRangeAxiom {
	private static final long serialVersionUID = 1L;
	private HGHandle propertyHandle; // index 0
    protected HGHandle rangeHandle;  // index 1
    // private OWLAnnotationProperty property;
    // protected IRI range;

    public OWLAnnotationPropertyRangeAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    }

    public OWLAnnotationPropertyRangeAxiomHGDB(HGHandle property, HGHandle range, Collection<? extends OWLAnnotation> annotations) {
    	//OWLAnnotationProperty property, IRI range, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        propertyHandle = property;
        rangeHandle = range;
    }

    public OWLAnnotationPropertyRangeAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLAnnotationPropertyRangeAxiom(getProperty(), getRange());
    }

    public OWLAnnotationPropertyRangeAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLAnnotationPropertyRangeAxiom(getProperty(), getRange(), mergeAnnos(annotations));
    }

    public OWLAnnotationProperty getProperty() {
        return getHyperGraph().get(propertyHandle);
    }

    public IRI getRange() {
        return getHyperGraph().get(rangeHandle);
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.ANNOTATION_PROPERTY_RANGE;
    }

    public boolean isLogicalAxiom() {
        return false;
    }

    public boolean isAnnotationAxiom() {
        return true;
    }

    @Override
    protected int compareObjectOfSameType(OWLObject object) {
        OWLAnnotationPropertyRangeAxiom other = (OWLAnnotationPropertyRangeAxiom) object;
        int diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        return getRange().compareTo(other.getRange());
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLAnnotationPropertyRangeAxiom)) {
            return false;
        }
        OWLAnnotationPropertyRangeAxiom other = (OWLAnnotationPropertyRangeAxiom) obj;
        return getProperty().equals(other.getProperty()) && getRange().equals(other.getRange()) && getAnnotations().equals(other.getAnnotations());
    }
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		return (i == 0)? propertyHandle: rangeHandle;  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			propertyHandle = handle;
		} else {
			rangeHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (i == 0) {
			propertyHandle = getHyperGraph().getHandleFactory().nullHandle();
		} else {
			rangeHandle = getHyperGraph().getHandleFactory().nullHandle();
		}
	}
}
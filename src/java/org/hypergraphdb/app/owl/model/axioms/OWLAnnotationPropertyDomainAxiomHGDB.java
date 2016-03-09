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
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLAnnotationPropertyDomainAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 14, 2011
 */
public class OWLAnnotationPropertyDomainAxiomHGDB extends OWLAxiomHGDB implements HGLink, OWLAnnotationPropertyDomainAxiom {
	private static final long serialVersionUID = 1L;
	private HGHandle propertyHandle;
    private HGHandle domainHandle;
    // private OWLAnnotationProperty property;
    // private IRI domain;

    public OWLAnnotationPropertyDomainAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    }
    
    public OWLAnnotationPropertyDomainAxiomHGDB(HGHandle property, HGHandle domain, Collection<? extends OWLAnnotation> annotations) {
    	//OWLDataFactory dataFactory, OWLAnnotationProperty property, IRI domain, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        domainHandle = domain;
        propertyHandle = property;
    }

    public OWLAnnotationPropertyDomainAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLAnnotationPropertyDomainAxiom(getProperty(), getDomain());
    }

    public OWLAnnotationPropertyDomainAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLAnnotationPropertyDomainAxiom(getProperty(), getDomain(), mergeAnnos(annotations));
    }

    public IRI getDomain() {
        return getHyperGraph().get(domainHandle);
    }

    public OWLAnnotationProperty getProperty() {
        return getHyperGraph().get(propertyHandle);
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.ANNOTATION_PROPERTY_DOMAIN;
    }

    public boolean isLogicalAxiom() {
        return false;
    }

    public boolean isAnnotationAxiom() {
        return true;
    }

    @Override
    protected int compareObjectOfSameType(OWLObject object) {
        OWLAnnotationPropertyDomainAxiom other = (OWLAnnotationPropertyDomainAxiom) object;
        int diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        return getDomain().compareTo(other.getDomain());
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
        if (!(obj instanceof OWLAnnotationPropertyDomainAxiom)) {
            return false;
        }
        OWLAnnotationPropertyDomainAxiom other = (OWLAnnotationPropertyDomainAxiom) obj;
        return getProperty().equals(other.getProperty()) && getDomain().equals(other.getDomain()) && getAnnotations().equals(other.getAnnotations());
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
		return (i == 0)? propertyHandle: domainHandle;  
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
			domainHandle = handle;
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
			domainHandle = getHyperGraph().getHandleFactory().nullHandle();
		}
	}    
}
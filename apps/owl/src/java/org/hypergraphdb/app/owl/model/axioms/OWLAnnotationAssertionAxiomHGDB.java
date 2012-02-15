package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLAnnotationAssertionAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 14, 2011
 */
public class OWLAnnotationAssertionAxiomHGDB extends OWLAxiomHGDB implements HGLink, OWLAnnotationAssertionAxiom {
    
	private HGHandle subjectHandle;  //index 0
    private HGHandle propertyHandle; //index 1
    private HGHandle valueHandle;    //index 2
//	private OWLAnnotationSubject subject;
//  private OWLAnnotationProperty property;
//  private OWLAnnotationValue value;

    public OWLAnnotationAssertionAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], args[2], Collections.<OWLAnnotation>emptySet());
    }

    public OWLAnnotationAssertionAxiomHGDB(HGHandle subject, HGHandle property, HGHandle value, Collection<? extends OWLAnnotation> annotations) {
        //OWLAnnotationSubject subject, OWLAnnotationProperty property, OWLAnnotationValue value, Collection<? extends OWLAnnotation> annotations
    	super(annotations);
        subjectHandle = subject;
        propertyHandle = property;
        valueHandle = value;
    }

    public OWLAnnotationAssertionAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLAnnotationAssertionAxiom(getProperty(), getSubject(), getValue());
    }

    /**
     * Determines if this annotation assertion deprecates the IRI that is the subject of the annotation.
     * @return <code>true</code> if this annotation assertion deprecates the subject IRI of the assertion, otherwise
     *         <code>false</code>.
     * @see {@link org.semanticweb.owlapi.model.OWLAnnotation#isDeprecatedIRIAnnotation()}
     */
    public boolean isDeprecatedIRIAssertion() {
        return getProperty().isDeprecated() && getAnnotation().isDeprecatedIRIAnnotation();
    }

    public OWLAnnotationAssertionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLAnnotationAssertionAxiom(getProperty(), getSubject(), getValue(), mergeAnnos(annotations));
    }

    public OWLAnnotationValue getValue() {
        return getHyperGraph().get(valueHandle);
    }

    public OWLAnnotationSubject getSubject() {
        return getHyperGraph().get(subjectHandle);
    }

    public OWLAnnotationProperty getProperty() {
        return getHyperGraph().get(propertyHandle);
    }

    public OWLAnnotation getAnnotation() {
        return getOWLDataFactory().getOWLAnnotation(getProperty(), getValue());
    }

    public boolean isLogicalAxiom() {
        return false;
    }

    public boolean isAnnotationAxiom() {
        return true;
    }

    @Override
    protected int compareObjectOfSameType(OWLObject object) {
        OWLAnnotationAssertionAxiom other = (OWLAnnotationAssertionAxiom) object;
        int diff = 0;
        diff = getSubject().compareTo(other.getSubject());
        if (diff != 0) {
            return diff;
        }
        diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        return getValue().compareTo(other.getValue());
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.ANNOTATION_ASSERTION;
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLAnnotationAssertionAxiom)) {
            return false;
        }
        OWLAnnotationAssertionAxiom other = (OWLAnnotationAssertionAxiom) obj;
        return getSubject().equals(other.getSubject()) && getProperty().equals(other.getProperty()) && getValue().equals(other.getValue()) && getAnnotations().equals(other.getAnnotations());
    }
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */    
	@Override
	public int getArity() {
		return 3;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		return (i == 0)? subjectHandle: (i == 1)? propertyHandle: valueHandle;  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			subjectHandle = handle;
		} else if (i == 1){
			propertyHandle = handle;
		} else { //2 if arity 3
			valueHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			subjectHandle = null;
		} else if (i == 1){
			propertyHandle = null;
		} else { //2 if arity 3
			valueHandle = null;
		}
	}        
}
package org.hypergraphdb.app.owl.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.HGChangeableLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLAnnotationHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 14, 2011
 */
public class OWLAnnotationHGDB extends OWLObjectHGDB implements HGChangeableLink, OWLAnnotation {
	private static final long serialVersionUID = 1L;
	private HGHandle propertyHandle;           // index   0
    private HGHandle valueHandle;              // index   1 
    private List<HGHandle> annotationsHandles; // indices 2..
    // private OWLAnnotationProperty property;
    // private OWLAnnotationValue value;
    // private Set<OWLAnnotation> annotations;

    public OWLAnnotationHGDB(HGHandle...args) {    
        //TODO assert 
    	// args[0] type OWLAnnotationProperty, 
    	// args[1] type OWLAnnotationValue
    	// args[2...length-1] type OWLAnnotation
    	assert (args.length >= 2);
    	Set<HGHandle> annotationsFromArgs = new HashSet<HGHandle>();
    	for(int i = 2; i < args.length; i++) {
    		annotationsFromArgs.add(args[i]);
    	}
        propertyHandle = args[0];
        valueHandle = args[1];
        annotationsHandles = new ArrayList<HGHandle>(annotationsFromArgs);   
        assert(annotationsFromArgs.size() == annotationsHandles.size());
    }

    public OWLAnnotationHGDB(HGHandle property, HGHandle value, Set<HGHandle> annotations) {
    	//OWLAnnotationProperty property, OWLAnnotationValue value, Set<? extends OWLAnnotation> annotations
        propertyHandle = property;
        valueHandle = value;
        annotationsHandles = new ArrayList<HGHandle>(annotations);
        assert(annotations.size() == annotationsHandles.size());
        //annotationsHandles = CollectionFactory.getCopyOnRequestSet(new TreeSet<OWLAnnotation>(annotations));
    }

    public Set<OWLAnnotation> getAnnotations() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLAnnotation> s = new TreeSet<OWLAnnotation>();
    	for (HGHandle h : annotationsHandles) {
    		s.add((OWLAnnotation) g.get(h));    		
    	}
    	return s;
        //return annotations;
    }

    public OWLAnnotationProperty getProperty() {
        return getHyperGraph().get(propertyHandle);
    }

    public OWLAnnotationValue getValue() {
        return getHyperGraph().get(valueHandle);
    }

    public OWLAnnotation getAnnotatedAnnotation(Set<OWLAnnotation> annotationsToAdd) {
        if(annotationsToAdd.isEmpty()) {
            return this;
        }
        Set<OWLAnnotation> merged = new HashSet<OWLAnnotation>(this.getAnnotations());
        merged.addAll(annotationsToAdd);
        return getOWLDataFactory().getOWLAnnotation(getProperty(), getValue(), merged);
        //TODO return new OWLAnnotationImpl(getOWLDataFactory(), property, value, merged);
    }

    public boolean isComment() {
        return getProperty().isComment();
    }

    public boolean isLabel() {
        return getProperty().isLabel();
    }

    /**
     * Determines if this annotation is an annotation used to deprecate an IRI.  This is the case if the annotation
     * property has an IRI of <code>owl:deprecated</code> and the value of the annotation is <code>"true"^^xsd:boolean</code>
     * @return <code>true</code> if this annotation is an annotation that can be used to deprecate an IRI, otherwise
     *         <code>false</code>.
     */
    public boolean isDeprecatedIRIAnnotation() {
        return getProperty().isDeprecated() && getValue() instanceof OWLLiteral && ((OWLLiteral) getValue()).isBoolean() && ((OWLLiteral) getValue()).parseBoolean();
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (obj instanceof OWLAnnotation) {
                OWLAnnotation other = (OWLAnnotation) obj;
                return other.getProperty().equals(getProperty()) && other.getValue().equals(getValue()) && other.getAnnotations().equals(getAnnotations());
            }
        }
        return false;
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLAnnotation other = (OWLAnnotation) object;
        int diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        else {
            return getValue().compareTo(other.getValue());
        }
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLAnnotationObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAnnotationObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    /* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 2 + annotationsHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (i == 0) {
			return propertyHandle;
		} else if (i == 1) {
			return valueHandle;
		} else {
			return annotationsHandles.get(i - 2);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			propertyHandle = handle;
		} else if (i == 1) {
			valueHandle = handle;
		} else { //> 1 and < arity
			annotationsHandles.set(i - 2, handle);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			propertyHandle = getHyperGraph().getHandleFactory().nullHandle();
		} else if (i == 1) {
			valueHandle = getHyperGraph().getHandleFactory().nullHandle();
		} else { //> 1 and < arity
			annotationsHandles.remove(i - 2);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.HGChangeableLink#setTargetAt(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setTargetAt(int i, HGHandle handle) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			propertyHandle = handle;
		} else if (i == 1) {
			valueHandle = handle;
		} else { //> 1 and < arity
			annotationsHandles.set(i - 2, handle);
		}
	}    
}
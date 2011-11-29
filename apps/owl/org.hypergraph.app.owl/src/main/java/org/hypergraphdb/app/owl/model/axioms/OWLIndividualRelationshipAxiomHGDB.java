package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.HGChangeableLink;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;


/**
 * OWLIndividualRelationshipAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public abstract class OWLIndividualRelationshipAxiomHGDB<P extends OWLPropertyExpression<?,?>, O extends OWLPropertyAssertionObject> extends OWLLogicalAxiomHGDB implements HGChangeableLink, OWLPropertyAssertionAxiom<P, O>  {

//    private OWLIndividual subject;
//    private P property;
//    private O object;
    private HGHandle subjectHandle;  // 0
    private HGHandle propertyHandle; // 1
    private HGHandle objectHandle;   // 2

    public OWLIndividualRelationshipAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], args[2], Collections.<OWLAnnotation>emptySet());
    	if (args.length != 3) throw new IllegalArgumentException("Args needs 3 handles. Was " + args.length);
    }
    
    public OWLIndividualRelationshipAxiomHGDB(HGHandle subject, HGHandle property, HGHandle object, Collection<? extends OWLAnnotation> annotations) {
    	//OWLIndividual subject, P property, O object, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        subjectHandle = subject;
        propertyHandle = property;
        objectHandle = object;
    }

    public OWLIndividual getSubject() {
        return getHyperGraph().get(subjectHandle);
    }

    public P getProperty() {
        return getHyperGraph().get(propertyHandle);
    }

    public O getObject() {
        return getHyperGraph().get(objectHandle);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLPropertyAssertionAxiom)) {
                return false;
            }
            OWLPropertyAssertionAxiom<?,?> other = (OWLPropertyAssertionAxiom<?,?>) obj;
            return other.getSubject().equals(getSubject()) && other.getProperty().equals(getProperty()) && other.getObject().equals(getObject());
        }
        return false;
    }

    @Override
	final protected int compareObjectOfSameType(OWLObject o) {
        OWLPropertyAssertionAxiom<?,?> other = (OWLPropertyAssertionAxiom<?,?>) o;
        int diff = getSubject().compareTo(other.getSubject());
        if (diff != 0) {
            return diff;
        }
        diff = getProperty().compareTo(other.getProperty());
        if (diff != 0) {
            return diff;
        }
        //TODO hilpold 2011.11.08 THIS IS WRONG??? :
        //TODO should be getObject()
        return o.compareTo(other.getObject());
    }
    
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.HGChangeableLink#setTargetAt(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setTargetAt(int i, HGHandle handle) {
		if (handle == null) throw new IllegalArgumentException();
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (i == 0) {
			subjectHandle = handle;   		
		} else if (i == 1) {
			propertyHandle = handle; 
		} else if (i == 2) {
			objectHandle = handle;			
		} else {
			throw new IllegalStateException("i out of bounds" + i);
		}
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
		return (i == 0)? subjectHandle: (i == 1)? propertyHandle: objectHandle;  
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
		}  else if (i == 2) {
			objectHandle = handle;			
		} else {
			throw new IllegalStateException("i out of bounds" + i);
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
			objectHandle = null;
		}
	}    
}
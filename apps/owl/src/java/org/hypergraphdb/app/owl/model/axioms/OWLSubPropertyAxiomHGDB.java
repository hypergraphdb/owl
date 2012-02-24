package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;

/**
 * OWLSubPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 7, 2011
 */
public abstract class OWLSubPropertyAxiomHGDB<P extends OWLPropertyExpression<?,?>> extends OWLPropertyAxiomHGDB implements OWLSubPropertyAxiom<P>, HGLink {

    private HGHandle subPropertyHandle; //0

    private HGHandle superPropertyHandle; //1

    public OWLSubPropertyAxiomHGDB(HGHandle...args)
    {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    }

    public OWLSubPropertyAxiomHGDB(HGHandle subProperty, HGHandle superProperty, Collection<? extends OWLAnnotation> annotations) {
    	//assert type of HGHandle OWLPropertyExpression
    	super(annotations);
        this.notifyTargetHandleUpdate(0, subProperty);
        this.notifyTargetHandleUpdate(1, superProperty);
    }


    public P getSubProperty() {
        return (P)getHyperGraph().get(subPropertyHandle);
    }


    public P getSuperProperty() {
        return (P)getHyperGraph().get(superPropertyHandle);
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLSubPropertyAxiom)) {
                return false;
            }
            OWLSubPropertyAxiom<?> other = (OWLSubPropertyAxiom<?>) obj;
            return other.getSubProperty().equals(getSubProperty()) && other.getSuperProperty().equals(getSuperProperty());
            //return other.getSubProperty().equals(subProperty) && other.getSuperProperty().equals(superProperty);
        }
        return false;
    }


    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLSubPropertyAxiom<?> other = (OWLSubPropertyAxiom<?>) object;
        int diff = getSubProperty().compareTo(other.getSubProperty());
        if (diff != 0) {
            return diff;
        }
        return getSuperProperty().compareTo(other.getSuperProperty());
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
		return (i == 0)? subPropertyHandle: superPropertyHandle;  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			subPropertyHandle = handle;
		} else {
			superPropertyHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (i == 0) {
			subPropertyHandle = null;
		} else {
			superPropertyHandle = null;
		}
	}


}

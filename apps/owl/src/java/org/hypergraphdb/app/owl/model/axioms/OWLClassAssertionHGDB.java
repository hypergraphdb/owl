package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.HGChangeableLink;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLClassAssertionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public class OWLClassAssertionHGDB extends OWLIndividualAxiomHGDB implements HGChangeableLink, OWLClassAssertionAxiom {

    //private OWLIndividual individual;

    //private OWLClassExpression classExpression;
	
	private HGHandle individualHandle; // index 0

    private HGHandle classExpressionHandle; // index 1

    public OWLClassAssertionHGDB(HGHandle...args) {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    }

    public OWLClassAssertionHGDB(HGHandle individual, HGHandle classExpression, Collection<? extends OWLAnnotation> annotations) {
    	//OWLIndividual individual, OWLClassExpression classExpression, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        individualHandle = individual;
        classExpressionHandle = classExpression;
    }

    public OWLClassAssertionAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLClassAssertionAxiom(getClassExpression(), getIndividual());
    }

    public OWLClassAssertionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLClassAssertionAxiom(getClassExpression(), getIndividual(), mergeAnnos(annotations));
    }

    public OWLClassExpression getClassExpression() {
        return getHyperGraph().get(classExpressionHandle);
    }

    public OWLIndividual getIndividual() {
        return getHyperGraph().get(individualHandle);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLClassAssertionAxiom)) {
                return false;
            }
            OWLClassAssertionAxiom other = (OWLClassAssertionAxiom) obj;
            return other.getIndividual().equals(getIndividual()) && other.getClassExpression().equals(getClassExpression());
        }
        return false;
    }

    public OWLSubClassOfAxiom asOWLSubClassOfAxiom() {
        return getOWLDataFactory().getOWLSubClassOfAxiom(getOWLDataFactory().getOWLObjectOneOf(getIndividual()), getClassExpression());
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
        return AxiomType.CLASS_ASSERTION;
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLClassAssertionAxiom otherAx = (OWLClassAssertionAxiom) object;
        int diff = getIndividual().compareTo(otherAx.getIndividual());
        if (diff != 0) {
            return diff;
        }
        else {
            return getClassExpression().compareTo(otherAx.getClassExpression());
        }
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
		return (i == 0)? individualHandle: classExpressionHandle;  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			individualHandle = handle;
		} else {
			classExpressionHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (i == 0) {
			individualHandle = getHyperGraph().getHandleFactory().nullHandle();
		} else {
			classExpressionHandle = getHyperGraph().getHandleFactory().nullHandle();
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.core.HGChangeableLink#setTargetAt(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setTargetAt(int i, HGHandle handle) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (i == 0) {
			individualHandle = handle;
		} else {
			classExpressionHandle = handle;
		}
	}
}
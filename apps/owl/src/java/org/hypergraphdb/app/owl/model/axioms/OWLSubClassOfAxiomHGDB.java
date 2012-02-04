package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLSubClassOfAxiomHGDB is a link of artiy two connecting two OWLClassExpressions.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 5, 2011
 */
public class OWLSubClassOfAxiomHGDB extends OWLClassAxiomHGDB implements OWLSubClassOfAxiom, HGLink  {

    private HGHandle subClassHandle;   //0 OWLClassExpression
    private HGHandle superClassHandle; //1 OWLClassExpression
    // private OWLClassExpression subClass;
    // private OWLClassExpression superClass;

    public OWLSubClassOfAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    }

    public OWLSubClassOfAxiomHGDB(HGHandle subClass, HGHandle superClass, Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
        //TODO check for type OWLClassExpression
        this.notifyTargetHandleUpdate(0, subClass);
        this.notifyTargetHandleUpdate(1, superClass);
    }

    public Set<OWLClassExpression> getClassExpressions() {
        Set<OWLClassExpression> classExpressions = new HashSet<OWLClassExpression>(3);
        classExpressions.add(getSubClass());
        classExpressions.add(getSuperClass());
        //classExpressions.add(subClass);
        //classExpressions.add(superClass);
        return classExpressions;
    }

    public Set<OWLClassExpression> getClassExpressionsMinus(OWLClassExpression... desc) {
        Set<OWLClassExpression> classExpressions = getClassExpressions();
        for (OWLClassExpression ce : desc) {
            classExpressions.remove(ce);
        }
        return classExpressions;
    }

    public OWLSubClassOfAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLSubClassOfAxiom(getSubClass(), getSuperClass(), mergeAnnos(annotations));
        //return getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass, mergeAnnos(annotations));
    }

    public OWLSubClassOfAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLSubClassOfAxiom(getSubClass(), getSuperClass());
        //return getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
    }

    public boolean contains(OWLClassExpression ce) {
        return getSubClass().equals(ce) || getSuperClass().equals(ce);
        //return subClass.equals(ce) || superClass.equals(ce);
    }

    public OWLClassExpression getSubClass() {
        return getHyperGraph().get(subClassHandle);
    }

    public OWLClassExpression getSuperClass() {
        return getHyperGraph().get(superClassHandle);
    }

    public boolean isGCI() {
        return getSubClass().isAnonymous();
        //return subClass.isAnonymous();
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLSubClassOfAxiom)) {
            return false;
        }
        OWLSubClassOfAxiom other = (OWLSubClassOfAxiom) obj;
        //return other.getSubClass().equals(subClass) && other.getSuperClass().equals(superClass);
        return other.getSubClass().equals(getSubClass()) && other.getSuperClass().equals(getSuperClass());
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
        return AxiomType.SUBCLASS_OF;
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLSubClassOfAxiom other = (OWLSubClassOfAxiom) object;
        int diff = getSubClass().compareTo(other.getSubClass());
        if (diff != 0) {
            return diff;
        }
        return getSuperClass().compareTo(other.getSuperClass());
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
		return (i == 0)? subClassHandle: superClassHandle;  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			subClassHandle = handle;
		} else {
			superClassHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (i == 0) {
			subClassHandle = null; //getHyperGraph().getHandleFactory().nullHandle();
		} else {
			superClassHandle = null; //getHyperGraph().getHandleFactory().nullHandle();;
		}
	}	
}
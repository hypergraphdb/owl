package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDisjointUnionAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 13, 2011
 */
public class OWLDisjointUnionAxiomHGDB extends OWLClassAxiomHGDB implements OWLDisjointUnionAxiom, HGLink {
    
	private HGHandle owlClassHandle; //OWLClass

	private List<HGHandle> classExpressionsHandles;
    //private Set<OWLClassExpression> classExpressions; //owlClassExpressions

    public OWLDisjointUnionAxiomHGDB(HGHandle...args) {    
        //TODO assert arg[0] type OWLClass, args[1...length-1] type OWLClassExpression
    	super(Collections.<OWLAnnotation>emptySet());    	
    	assert (args.length >= 2);
    	Set<HGHandle> classExpressionsFromArgs = new HashSet<HGHandle>();
    	for(int i = 1; i < args.length; i++) {
    		classExpressionsFromArgs.add(args[i]);
    	}
        this.owlClassHandle = args[0];
        this.classExpressionsHandles = new ArrayList<HGHandle>(classExpressionsFromArgs);    	
    }
	
    public OWLDisjointUnionAxiomHGDB(HGHandle owlClass, Set<? extends HGHandle> classExpressions, Set<? extends OWLAnnotation> annotations) {
        super(annotations);
        //TODO assert owlClass type OWLClass, classExpressions type OWLClassExpression
        this.owlClassHandle = owlClass;
        this.classExpressionsHandles = new ArrayList<HGHandle>(classExpressions);
    }

    public Set<OWLClassExpression> getClassExpressions() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLClassExpression> s = new TreeSet<OWLClassExpression>();
    	for (HGHandle h : classExpressionsHandles) {
    		s.add((OWLClassExpression) g.get(h));    		
    	}
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(classExpressions);
    }

    public OWLDisjointUnionAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLDisjointUnionAxiom(getOWLClass(), getClassExpressions());
    }

    public OWLDisjointUnionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLDisjointUnionAxiom(getOWLClass(), getClassExpressions(), mergeAnnos(annotations));
    }

    public OWLClass getOWLClass() {
        return getHyperGraph().get(owlClassHandle);
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLDisjointUnionAxiom)) {
                return false;
            }
            return ((OWLDisjointUnionAxiom) obj).getOWLClass().equals(getOWLClass());
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
        return AxiomType.DISJOINT_UNION;
    }

    public OWLEquivalentClassesAxiom getOWLEquivalentClassesAxiom() {
        return getOWLDataFactory().getOWLEquivalentClassesAxiom(getOWLClass(), getOWLDataFactory().getOWLObjectUnionOf(getClassExpressions()));
    }

    public OWLDisjointClassesAxiom getOWLDisjointClassesAxiom() {
        return getOWLDataFactory().getOWLDisjointClassesAxiom(getClassExpressions());
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLDisjointUnionAxiom other = (OWLDisjointUnionAxiom) object;
        int diff = getOWLClass().compareTo(other.getOWLClass());
        if (diff != 0) {
            return diff;
        }
        return compareSets(getClassExpressions(), other.getClassExpressions());
    }

    /* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		//2011.10.24  
		return 1 + classExpressionsHandles.size();
		//old return classExpressionsHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (i == 0) {
			return owlClassHandle;
		} else {
			return classExpressionsHandles.get(i - 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			owlClassHandle = handle;
		} else {
			classExpressionsHandles.set(i - 1, handle);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			owlClassHandle = null;
		} else {
			classExpressionsHandles.set(i - 1, null);
		}
	}
}
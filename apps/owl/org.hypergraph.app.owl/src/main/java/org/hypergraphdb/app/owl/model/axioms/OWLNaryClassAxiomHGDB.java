package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNaryClassAxiom;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * OWLNaryClassAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 13, 2011
 */
public abstract class OWLNaryClassAxiomHGDB extends OWLClassAxiomHGDB implements OWLNaryClassAxiom, HGLink {

    private List<HGHandle> classExpressionsHandles;
    
    public OWLNaryClassAxiomHGDB(HGHandle...args)
    {
    	super(Collections.<OWLAnnotation>emptySet());
    	classExpressionsHandles = new ArrayList<HGHandle>(Arrays.asList(args));
    }

    public OWLNaryClassAxiomHGDB(Set<? extends HGHandle> classExpressions, Collection<? extends OWLAnnotation> annotations) {
        super(annotations);
        //TODO ensure Type OWLClassExpression in collection
        classExpressionsHandles = new ArrayList<HGHandle>(classExpressions);        
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

    public List<OWLClassExpression> getClassExpressionsAsList() {
        return new ArrayList<OWLClassExpression>(getClassExpressions());
    	//return new ArrayList<OWLClassExpression>(classExpressions);
    }

    public boolean contains(OWLClassExpression ce) {
        return getClassExpressions().contains(ce);
    }


    public Set<OWLClassExpression> getClassExpressionsMinus(OWLClassExpression... descs) {
    	Set<OWLClassExpression> result = new HashSet<OWLClassExpression>(getClassExpressions());
        //Set<OWLClassExpression> result = new HashSet<OWLClassExpression>(classExpressions);
        for (OWLClassExpression desc : descs) {
            result.remove(desc);
        }
        return result;
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLNaryClassAxiom)) {
                return false;
            }
            return ((OWLNaryClassAxiom) obj).getClassExpressions().equals(getClassExpressions());
            //return ((OWLNaryClassAxiom) obj).getClassExpressions().equals(classExpressions);
            }
        return false;
    }


    @Override
	protected int compareObjectOfSameType(OWLObject object) {
    	return compareSets(getClassExpressions(), ((OWLNaryClassAxiom) object).getClassExpressions());
    	//return compareSets(classExpressions, ((OWLNaryClassAxiom) object).getClassExpressions());
    }
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return classExpressionsHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		return classExpressionsHandles.get(i);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		classExpressionsHandles.set(i, handle);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		classExpressionsHandles.remove(i);  
	}
}
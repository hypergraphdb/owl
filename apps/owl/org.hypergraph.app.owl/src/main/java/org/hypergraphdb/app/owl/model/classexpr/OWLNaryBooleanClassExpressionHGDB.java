package org.hypergraphdb.app.owl.model.classexpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNaryBooleanClassExpression;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * OWLNaryBooleanClassExpressionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public abstract class OWLNaryBooleanClassExpressionHGDB extends OWLAnonymousClassExpressionHGDB implements
		OWLNaryBooleanClassExpression, HGLink {
    private List<HGHandle> operandHandles; //Set OWLClassExpression


    public OWLNaryBooleanClassExpressionHGDB(HGHandle...args) {
    	// no duplicates allowed
    	assert(new HashSet<HGHandle>(Arrays.asList(args)).size() == args.length);
    	operandHandles = new ArrayList<HGHandle>(Arrays.asList(args));
    }

    public OWLNaryBooleanClassExpressionHGDB(Set<? extends HGHandle> operands) {   
    	//
    	this.operandHandles = new ArrayList<HGHandle>(operands);
        //this.operandHandles = new TreeSet<OWLClassExpression>(operands);
    }

    public List<OWLClassExpression> getOperandsAsList() {
    	return new ArrayList<OWLClassExpression>(getOperands());
        //return new ArrayList<OWLClassExpression>(operands);
    }

    public Set<OWLClassExpression> getOperands() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLClassExpression> s = new TreeSet<OWLClassExpression>();
    	for (HGHandle h : operandHandles) {
    		s.add((OWLClassExpression) g.get(h));    		
    	}
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(operands);
    }

    public boolean isClassExpressionLiteral() {
        return false;
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLNaryBooleanClassExpression)) {
                return false;
            }
            return ((OWLNaryBooleanClassExpression) obj).getOperands().equals(getOperands());
            //return ((OWLNaryBooleanClassExpression) obj).getOperands().equals(operands);
        }
        return false;
    }


    @Override
	final protected int compareObjectOfSameType(OWLObject object) {
    	return compareSets(getOperands(), ((OWLNaryBooleanClassExpression) object).getOperands());
    	//return compareSets(operands, ((OWLNaryBooleanClassExpression) object).getOperands());
    }

    /* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return operandHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		return operandHandles.get(i);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		operandHandles.set(i, handle);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		operandHandles.remove(i);  
	}

}

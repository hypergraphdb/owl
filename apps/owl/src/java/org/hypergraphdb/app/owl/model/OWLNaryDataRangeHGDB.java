package org.hypergraphdb.app.owl.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLNaryDataRange;
import org.semanticweb.owlapi.model.OWLRuntimeException;

/**
 * OWLNaryDataRangeHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 31, 2011
 */
public abstract class OWLNaryDataRangeHGDB extends OWLObjectHGDB implements HGLink, OWLNaryDataRange {

	private List<HGHandle> operandHandles;
    //private Set<OWLDataRange> operands;

    public OWLNaryDataRangeHGDB(HGHandle...args) {
    	// no duplicates allowed
    	assert(new TreeSet<HGHandle>(Arrays.asList(args)).size() == args.length);
    	operandHandles = Arrays.asList(args);
    }

    public OWLNaryDataRangeHGDB(Set<? extends HGHandle> operands) {   
    	//Set<? extends OWLDataRange> operands
    	this.operandHandles = new ArrayList<HGHandle>(operands);
        //this.operands = new TreeSet<OWLDataRange>(operands);
    }

    public Set<OWLDataRange> getOperands() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLDataRange> s = new TreeSet<OWLDataRange>();
    	for (HGHandle h : operandHandles) {
    		s.add((OWLDataRange) g.get(h));    		
    	}
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(operands);
    }

    public boolean isTopDatatype() {
        return false;
    }

    public boolean isDatatype() {
        return false;
    }

    public OWLDatatype asOWLDatatype() {
        throw new OWLRuntimeException("Not a datatype");
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
		operandHandles.set(i, null);  
	}

}

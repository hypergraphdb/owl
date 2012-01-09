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
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLRuntimeException;


/**
 * OWLDataOneOfHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 1, 2011
 */
public class OWLDataOneOfHGDB extends OWLObjectHGDB implements HGLink, OWLDataOneOf {

	private List<HGHandle> valuesHandles;
	//private Set<OWLLiteral> values;

    public OWLDataOneOfHGDB(HGHandle...args) {
    	// no duplicates allowed
    	assert(new TreeSet<HGHandle>(Arrays.asList(args)).size() == args.length);
    	valuesHandles = new ArrayList<HGHandle>(Arrays.asList(args));
    }

    public OWLDataOneOfHGDB(Set<? extends HGHandle> values) {
    	//TODO check Set<? extends OWLLiteral> values
    	valuesHandles =  new ArrayList<HGHandle>(values);
        //this.values = new TreeSet<OWLLiteral>(values);
    }

    public DataRangeType getDataRangeType() {
        return DataRangeType.DATA_ONE_OF;
    }

    public Set<OWLLiteral> getValues() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLLiteral> s = new TreeSet<OWLLiteral>();
    	for (HGHandle h : valuesHandles) {
    		s.add((OWLLiteral) g.get(h));    		
    	}
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(values);
    }


    public boolean isDatatype() {
        return false;
    }


    public boolean isTopDatatype() {
        return false;
    }


    public OWLDatatype asOWLDatatype() {
        throw new OWLRuntimeException("Not a data type!");
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLDataOneOf)) {
                return false;
            }
            return ((OWLDataOneOf) obj).getValues().equals(getValues());
        }
        return false;
    }


    public void accept(OWLDataVisitor visitor) {
        visitor.visit(this);
    }


    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLDataVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLDataRangeVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLDataRangeVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        return compareSets(getValues(), ((OWLDataOneOf) object).getValues());
    }
    /* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return valuesHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		return valuesHandles.get(i);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		valuesHandles.set(i, handle);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		valuesHandles.set(i, null);  
	}

}

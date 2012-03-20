package org.hypergraphdb.app.owl.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataRangeVisitor;
import org.semanticweb.owlapi.model.OWLDataRangeVisitorEx;
import org.semanticweb.owlapi.model.OWLDataVisitor;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLRuntimeException;


/**
 * OWLDatatypeRestrictionHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 1, 2011
 */
public class OWLDatatypeRestrictionHGDB extends OWLObjectHGDB implements HGLink, OWLDatatypeRestriction {
	
	private HGHandle datatypeHandle;
    //private OWLDatatype datatype;

	private List<HGHandle> facetRestrictionsHandles;
    //private Set<OWLFacetRestriction> facetRestrictions;

    public OWLDatatypeRestrictionHGDB(HGHandle...args) {    
        //TODO assert arg[0] type OWLClass, args[1...length-1] type OWLFacetRestriction
    	assert (args.length >= 2);
    	Set<HGHandle> facetRestrictionsHandlesSet = new HashSet<HGHandle>();
    	for(int i = 1; i < args.length; i++) {
    		facetRestrictionsHandlesSet.add(args[i]);
    	}
    	datatypeHandle = args[0];
        facetRestrictionsHandles = new ArrayList<HGHandle>(facetRestrictionsHandlesSet);    	
    }

    public OWLDatatypeRestrictionHGDB(HGHandle datatype, Set<HGHandle> facetRestrictions) {
        //OWLDatatype datatype, Set<OWLFacetRestriction> facetRestrictions
    	datatypeHandle = datatype;
    	facetRestrictionsHandles = new ArrayList<HGHandle>(facetRestrictions);
    	//this.datatype = datatype;
        //this.facetRestrictions = new HashSet<OWLFacetRestriction>(facetRestrictions);
    }

    public DataRangeType getDataRangeType() {
        return DataRangeType.DATATYPE_RESTRICTION;
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


    public OWLDatatype getDatatype() {
        return getHyperGraph().get(datatypeHandle);
    }


    /**
     * Gets the facet restrictions on this data range
     * @return A <code>Set</code> of facet restrictions that apply to
     *         this data range
     */
    public Set<OWLFacetRestriction> getFacetRestrictions() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLFacetRestriction> s = new TreeSet<OWLFacetRestriction>();
    	for (HGHandle h : facetRestrictionsHandles) {
    		s.add((OWLFacetRestriction) g.get(h));    		
    	}
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(facetRestrictions);
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLDatatypeRestriction)) {
                return false;
            }
            OWLDatatypeRestriction other = (OWLDatatypeRestriction) obj;
            return other.getDatatype().equals(getDatatype()) && other.getFacetRestrictions().equals(getFacetRestrictions());
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
        OWLDatatypeRestriction other = (OWLDatatypeRestriction) object;
        int diff = getDatatype().compareTo(other.getDatatype());
        if (diff != 0) {
            return diff;
        }
        return compareSets(getFacetRestrictions(), other.getFacetRestrictions());
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 1 + facetRestrictionsHandles.size();
		//old return classExpressionsHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (i == 0) {
			return datatypeHandle;
		} else {
			return facetRestrictionsHandles.get(i - 1);
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
			datatypeHandle = handle;
		} else {
			facetRestrictionsHandles.set(i - 1, handle);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			datatypeHandle = getHyperGraph().getHandleFactory().nullHandle();
		} else {
			facetRestrictionsHandles.remove(i - 1);
		}
	}
}

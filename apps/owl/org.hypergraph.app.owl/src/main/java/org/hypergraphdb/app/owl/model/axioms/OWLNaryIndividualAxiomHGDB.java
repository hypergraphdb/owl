package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNaryIndividualAxiom;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * OWLNaryIndividualAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public abstract class OWLNaryIndividualAxiomHGDB extends OWLIndividualAxiomHGDB implements HGLink, OWLNaryIndividualAxiom {

    //private Set<OWLIndividual> individuals;
	private List<HGHandle> individualsHandles;

	
    public OWLNaryIndividualAxiomHGDB(HGHandle...args) {
    	super(Collections.<OWLAnnotation>emptySet());
    	individualsHandles = Arrays.asList(args);
    }

    public OWLNaryIndividualAxiomHGDB(Set<? extends HGHandle> individuals, Collection<? extends OWLAnnotation> annotations) {
    	//Set<? extends OWLIndividual> individuals, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        individualsHandles = new ArrayList<HGHandle>(individuals);
        //individuals = new TreeSet<OWLIndividual>(individuals);
    }

    public Set<OWLIndividual> getIndividuals() {
    	HyperGraph g = getHyperGraph();
    	Set<OWLIndividual> s = new TreeSet<OWLIndividual>();
    	//int i = 0;
    	for (HGHandle h : individualsHandles) {
    		//induced error hilpold if (i % 2 == 0) { //For unit test check
    			s.add((OWLIndividual) g.get(h));
    		//}
    		//i++;
    	}
    	assert(s.size() == individualsHandles.size());
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(individuals);
    }

    public List<OWLIndividual> getIndividualsAsList() {
        return new ArrayList<OWLIndividual>(getIndividuals());
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLNaryIndividualAxiom)) {
                return false;
            }
            return ((OWLNaryIndividualAxiom) obj).getIndividuals().equals(getIndividuals());
        }
        return false;
    }


    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        return compareSets(getIndividuals(), ((OWLNaryIndividualAxiom) object).getIndividuals());
    }
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return individualsHandles.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		return individualsHandles.get(i);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		individualsHandles.set(i, handle);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		individualsHandles.set(i, null);  
	}
}
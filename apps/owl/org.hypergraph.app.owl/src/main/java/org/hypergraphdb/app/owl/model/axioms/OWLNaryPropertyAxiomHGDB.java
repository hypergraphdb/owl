package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLNaryPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

/**
 * OWLNaryPropertyAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public abstract class OWLNaryPropertyAxiomHGDB <P extends OWLPropertyExpression<?,?>> extends OWLPropertyAxiomHGDB implements HGLink, OWLNaryPropertyAxiom<P> {
	
	private List<HGHandle> propertiesHandles;
	//private Set<P> properties;

	public OWLNaryPropertyAxiomHGDB(Set<? extends HGHandle> properties, Collection<? extends OWLAnnotation> annotations) {
    	//Set<? extends P> properties, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        propertiesHandles = new ArrayList<HGHandle>(properties);
    }

	public OWLNaryPropertyAxiomHGDB(List<HGHandle> properties, Collection<? extends OWLAnnotation> annotations) {
    	//Set<? extends P> properties, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        propertiesHandles = new ArrayList<HGHandle>(properties);
    }

    @SuppressWarnings("unchecked")
	public Set<P> getProperties() {
    	HyperGraph g = getHyperGraph();
    	Set<P> s = new TreeSet<P>();
    	for (HGHandle h : propertiesHandles) {
    		s.add((P) g.get(h));    		
    	}
    	return s;
        //return CollectionFactory.getCopyOnRequestSet(properties);
    }

    public Set<P> getPropertiesMinus(P property) {
    	Set<P> props = getProperties();
        //Set<P> props = new TreeSet<P>(properties);
        props.remove(property);
        return props;
    }

    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLNaryPropertyAxiom)) {
                return false;
            }
            return ((OWLNaryPropertyAxiom<?>) obj).getProperties().equals(getProperties());
        }
        return false;
    }

    @Override
	final protected int compareObjectOfSameType(OWLObject object) {
        return compareSets(getProperties(), ((OWLNaryPropertyAxiom<?>) object).getProperties());
    }
    
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return propertiesHandles.size();
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		return propertiesHandles.get(i);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		assert(getHyperGraph().get(handle) instanceof OWLClassExpression);
		
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		propertiesHandles.set(i, handle);  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		propertiesHandles.set(i, null);  
	}
}
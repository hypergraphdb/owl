package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;


/**
 * OWLSubPropertyChainAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLSubPropertyChainAxiomHGDB extends OWLPropertyAxiomHGDB implements HGLink, OWLSubPropertyChainOfAxiom {
    
	private List<HGHandle> propertyHandlesChain;
	//private List<OWLObjectPropertyExpression> propertyChain;

    private HGHandle superPropertyHandle;
    //private OWLObjectPropertyExpression superProperty;

    public OWLSubPropertyChainAxiomHGDB(HGHandle...args) {    
        //TODO assert arg[0...length-1] type OWLObjectPropertyExpression
    	super(Collections.<OWLAnnotation>emptySet());    	
    	assert (args.length >= 2);
    	Set<HGHandle> propertyHandlesChainFromArgs = new HashSet<HGHandle>();
    	for(int i = 1; i < args.length; i++) {
    		propertyHandlesChainFromArgs.add(args[i]);
    	}
        this.superPropertyHandle = args[0];
        this.propertyHandlesChain = new ArrayList<HGHandle>(propertyHandlesChainFromArgs);    	
    }

    public OWLSubPropertyChainAxiomHGDB(List<HGHandle> propertyChain, HGHandle superProperty, Collection<? extends OWLAnnotation> annotations) {
        //List<? extends OWLObjectPropertyExpression> propertyChain, OWLObjectPropertyExpression superProperty, Collection<? extends OWLAnnotation> annotations
    	super(annotations);
        propertyHandlesChain = propertyChain; // new ArrayList<OWLObjectPropertyExpression>(propertyChain);
        superPropertyHandle = superProperty; //index 0
    }

    public OWLSubPropertyChainOfAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLSubPropertyChainOfAxiom(getPropertyChain(), getSuperProperty(), mergeAnnos(annotations));
    }

    public OWLSubPropertyChainOfAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLSubPropertyChainOfAxiom(getPropertyChain(), getSuperProperty());
    }

    public List<OWLObjectPropertyExpression> getPropertyChain() {
    	HyperGraph g = getHyperGraph();
    	List<OWLObjectPropertyExpression> s = new ArrayList<OWLObjectPropertyExpression>();
    	for (HGHandle h : propertyHandlesChain) {
    		s.add((OWLObjectPropertyExpression) g.get(h));    		
    	}
    	return s;
        //return new ArrayList<OWLObjectPropertyExpression>(propertyChain);
    }

    public OWLObjectPropertyExpression getSuperProperty() {
        return getHyperGraph().get(superPropertyHandle);
    }

    public boolean isEncodingOfTransitiveProperty() {
    	OWLObjectPropertyExpression a,b;
        if (propertyHandlesChain.size() == 2) {
        	a = getHyperGraph().get(propertyHandlesChain.get(0));
        	b = getHyperGraph().get(propertyHandlesChain.get(1));
        	return getSuperProperty().equals(a) && getSuperProperty().equals(b);
            //return superProperty.equals(propertyChain.get(0)) && superProperty.equals(propertyChain.get(1));
       }
        else {
            return false;
        }
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }


    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
	public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OWLSubPropertyChainOfAxiom)) {
            return false;
        }
        OWLSubPropertyChainOfAxiom other = (OWLSubPropertyChainOfAxiom) obj;
        return other.getPropertyChain().equals(getPropertyChain()) && other.getSuperProperty().equals(getSuperProperty());
    }

    public AxiomType<?> getAxiomType() {
        return AxiomType.SUB_PROPERTY_CHAIN_OF;
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLSubPropertyChainOfAxiom other = (OWLSubPropertyChainOfAxiom) object;
        OWLObjectPropertyExpression superProperty = getSuperProperty();
        List<OWLObjectPropertyExpression> propertyChain = getPropertyChain(); 
        for (int i = 0; i < propertyHandlesChain.size() && i < other.getPropertyChain().size(); i++) {
            int diff = propertyChain.get(i).compareTo(other.getPropertyChain().get(i));
            if (diff != 0) {
                return diff;
            }
            i++;
        }
        int diff = propertyChain.size() - other.getPropertyChain().size();
        if (diff != 0) {
            return diff;
        }
        return superProperty.compareTo(other.getSuperProperty());
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 1 + propertyHandlesChain.size();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity());
		if (i == 0) {
			return superPropertyHandle;
		} else {
			return propertyHandlesChain.get(i - 1);
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
			superPropertyHandle = handle;
		} else {
			propertyHandlesChain.set(i - 1, handle);
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (!(i >= 0 && i < getArity())) throw new IllegalArgumentException("Index has to be 0 and less than " + getArity()); 
		if (i == 0) {
			superPropertyHandle = null;
		} else {
			propertyHandlesChain.set(i - 1, null);
		}
	}
}

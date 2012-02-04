package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDatatypeDefinitionAxiomHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public class OWLDatatypeDefinitionAxiomHGDB extends OWLAxiomHGDB implements HGLink, OWLDatatypeDefinitionAxiom {

    //private OWLDatatype datatype;
    //private OWLDataRange dataRange;

    private HGHandle datatypeHandle; // index 0 
    private HGHandle dataRangeHandle; // index 1

    public OWLDatatypeDefinitionAxiomHGDB(HGHandle...args) {
    	this(args[0], args[1], Collections.<OWLAnnotation>emptySet());
    }

    public OWLDatatypeDefinitionAxiomHGDB(HGHandle datatype, HGHandle dataRange, Collection<? extends OWLAnnotation> annotations) {
    	//OWLDatatype datatype, OWLDataRange dataRange, Collection<? extends OWLAnnotation> annotations
        super(annotations);
        datatypeHandle = datatype;
        dataRangeHandle = dataRange;
    }

    public OWLAxiom getAxiomWithoutAnnotations() {
        if (!isAnnotated()) {
            return this;
        }
        return getOWLDataFactory().getOWLDatatypeDefinitionAxiom(getDatatype(), getDataRange());
    }

    public OWLDatatypeDefinitionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations) {
        return getOWLDataFactory().getOWLDatatypeDefinitionAxiom(getDatatype(), getDataRange(), mergeAnnos(annotations));
    }

    public OWLDatatype getDatatype() {
        return getHyperGraph().get(datatypeHandle);
    }

    public OWLDataRange getDataRange() {
        return getHyperGraph().get(dataRangeHandle);
    }

    public void accept(OWLAxiomVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAxiomVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public boolean isLogicalAxiom() {
        return true;
    }

    public boolean isAnnotationAxiom() {
        return false;
    }
    public AxiomType<?> getAxiomType() {
        return AxiomType.DATATYPE_DEFINITION;
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLDatatypeDefinitionAxiom other = (OWLDatatypeDefinitionAxiom) object;
        int diff = getDatatype().compareTo(other.getDatatype());
        if (diff != 0) {
            return diff;
        }
        return getDataRange().compareTo(other.getDataRange());
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLDatatypeDefinitionAxiom)) {
            return false;
        }
        OWLDatatypeDefinitionAxiom other = (OWLDatatypeDefinitionAxiom) obj;
        return getDatatype().equals(other.getDatatype()) && getDataRange().equals(other.getDataRange());
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
		return (i == 0)? datatypeHandle: dataRangeHandle;  
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (handle == null) throw new IllegalArgumentException("handle null"); 
		if (i == 0) {
			datatypeHandle = handle;
		} else {
			dataRangeHandle = handle;
		}
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0 && i != 1) throw new IllegalArgumentException("Index has to be 0 or 1"); 
		if (i == 0) {
			datatypeHandle = null;
		} else {
			dataRangeHandle = null;
		}
	}
}
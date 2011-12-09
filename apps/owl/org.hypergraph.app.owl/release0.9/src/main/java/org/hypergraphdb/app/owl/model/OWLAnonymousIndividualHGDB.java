package org.hypergraphdb.app.owl.model;

import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAnnotationSubjectVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationSubjectVisitorEx;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitor;
import org.semanticweb.owlapi.model.OWLAnnotationValueVisitorEx;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitor;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLRuntimeException;

/**
 * OWLAnonymousIndividualHGDB.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 28, 2011
 */
public class OWLAnonymousIndividualHGDB extends OWLIndividualHGDB implements OWLAnonymousIndividual {

    //private NodeID nodeId;
	private String nodeIdString;

	/**
	 * Default constructor to be used for Bean persistence only.
	 */
	public OWLAnonymousIndividualHGDB() {
		
	}

	public OWLAnonymousIndividualHGDB(NodeID nodeID) {
        nodeIdString = nodeID.getID();
    }

	/**
	 * Default getter to be used for Bean persistence only.
	 */
	public String getNodeIdString() {
		return nodeIdString;
	}

	/**
	 * Default setter to be used for Bean persistence only.
	 */
	public void setNodeIdString(String nodeIdString) {
		this.nodeIdString = nodeIdString;
	}


    public NodeID getID() {
        return NodeID.getNodeID(nodeIdString);
    }

    /**
     * Returns a string representation that can be used as the ID of this individual.  This is the toString
     * representation of the node ID of this individual
     * @return A string representing the toString of the node ID of this entity.
     */
    public String toStringID() {
        return nodeIdString;
    }

    public boolean isNamed() {
        return false;
    }

    public boolean isAnonymous() {
        return true;
    }

    public OWLAnonymousIndividual asOWLAnonymousIndividual() {
        return this;
    }

    public OWLNamedIndividual asOWLNamedIndividual() {
        throw new OWLRuntimeException("Not a named individual! This method should only be called on named individuals");
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLAnonymousIndividual other = (OWLAnonymousIndividual) object;
        //return getID().compareTo(other.getID());
        return nodeIdString.compareTo(other.getID().getID());
    }

    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public void accept(OWLIndividualVisitor visitor) {
        visitor.visit(this);
    }


    public <O> O accept(OWLIndividualVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLAnnotationValueVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLAnnotationValueVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    public void accept(OWLAnnotationSubjectVisitor visitor) {
        visitor.visit(this);
    }

    public <E> E accept(OWLAnnotationSubjectVisitorEx<E> visitor) {
        return visitor.visit(this);
    }

    @Override
	public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof OWLAnonymousIndividual)) {
            return false;
        }
        return nodeIdString.equals(((OWLAnonymousIndividual) obj).getID().getID());
        //return nodeId.equals(((OWLAnonymousIndividual) obj).getID());
    }
    @Override
    public int hashCode() {
    	//return nodeId.hashCode();
    	return nodeIdString.hashCode();
    }

}

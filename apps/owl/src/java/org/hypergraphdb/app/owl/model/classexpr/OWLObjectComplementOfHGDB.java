package org.hypergraphdb.app.owl.model.classexpr;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectComplementOfHGDB.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public class OWLObjectComplementOfHGDB extends OWLAnonymousClassExpressionHGDB implements OWLObjectComplementOf, HGLink {

	//private OWLClassExpression operand;
	private HGHandle operandHandle;
    
	public OWLObjectComplementOfHGDB(HGHandle...args)
    {
    	this(args[0]);
    }
    
    public OWLObjectComplementOfHGDB(HGHandle operand) {
        //let's avoid late disaster, if entity is not a handle for a OWLEntity subclass object.
        //We can;t do this here, because we have no graph yet!!!!
        //if (!TypeUtils.isClassAssignableFromAtomHandleType(OWLEntity.class, owlEntityHandle, getHyperGraph())) {
        //	throw new IllegalArgumentException("OwlEntityHandle atom not usable as OWLEntity.");
        //}
        notifyTargetHandleUpdate(0, operand);
    }


//    public OWLObjectComplementOfHGDB(OWLDataFactory dataFactory, OWLClassExpression operand) {
//        super(dataFactory);
//        this.operand = operand;
//    }

    /**
     * Gets the class expression type for this class expression
     * @return The class expression type
     */
    public ClassExpressionType getClassExpressionType() {
        return ClassExpressionType.OBJECT_COMPLEMENT_OF;
    }

    public boolean isClassExpressionLiteral() {
    	return !getOperand().isAnonymous();
        //return !operand.isAnonymous();
    }


    public OWLClassExpression getOperand() {
        return getHyperGraph().get(operandHandle);
    }


    @Override
	public boolean equals(Object obj) {
        if (super.equals(obj)) {
            if (!(obj instanceof OWLObjectComplementOf)) {
                return false;
            }
            return ((OWLObjectComplementOf) obj).getOperand().equals(getOperand());
            //return ((OWLObjectComplementOf) obj).getOperand().equals(operand);
        }
        return false;
    }


    public void accept(OWLClassExpressionVisitor visitor) {
        visitor.visit(this);
    }


    public void accept(OWLObjectVisitor visitor) {
        visitor.visit(this);
    }

    public <O> O accept(OWLClassExpressionVisitorEx<O> visitor) {
        return visitor.visit(this);
    }


    public <O> O accept(OWLObjectVisitorEx<O> visitor) {
        return visitor.visit(this);
    }

    @Override
	protected int compareObjectOfSameType(OWLObject object) {
        OWLObjectComplementOf other = (OWLObjectComplementOf) object;
        return getOperand().compareTo(other.getOperand());
        //return operand.compareTo(other.getOperand());
    }

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 1;
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		if (i != 0) throw new HGException("Index i must be 0");
		return operandHandle;
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		if (i != 0) throw new HGException("Index i must be 0");
		operandHandle = handle;		
	}


	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		if (i != 0) throw new HGException("Index i must be 0");		
		operandHandle = null;
	}

}

package org.hypergraphdb.app.owl.model;

import java.util.Set;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitor;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;

/**
 * OWLObjectInverseOfHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 24, 2011
 */
public class OWLObjectInverseOfHGDB extends OWLObjectPropertyExpressionHGDB implements HGLink, OWLObjectInverseOf
{
	// private OWLObjectPropertyExpression inverseProperty;
	private HGHandle inversePropertyHandle;

	public OWLObjectInverseOfHGDB(HGHandle... args)
	{
		this(args[0]);
	}

	public OWLObjectInverseOfHGDB(HGHandle inverseProperty)
	{
		// TODO ensure type: OWLObjectPropertyExpression inverseProperty;
		inversePropertyHandle = inverseProperty;
	}

	public OWLObjectPropertyExpression getInverse()
	{
		return getHyperGraph().get(inversePropertyHandle);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			if (!(obj instanceof OWLObjectInverseOf))
			{
				return false;
			}
			return ((OWLObjectInverseOf) obj).getInverse().equals(getInverse());
		}
		return false;
	}

	@Override
	protected Set<? extends OWLSubPropertyAxiom<OWLObjectPropertyExpression>> getSubPropertyAxiomsForRHS(OWLOntology ont)
	{
		return ont.getObjectSubPropertyAxiomsForSuperProperty(this);
	}

	public void accept(OWLPropertyExpressionVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLPropertyExpressionVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public boolean isAnonymous()
	{
		return true;
	}

	public OWLObjectProperty asOWLObjectProperty()
	{
		throw new OWLRuntimeException(
				"Property is not a named property.  Check using the isAnonymous method before calling this method!");
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		return getInverse().compareTo(((OWLObjectInverseOf) object).getInverse());
	}

	/**
	 * Determines if this is the owl:topObjectProperty
	 * 
	 * @return <code>true</code> if this property is the owl:topObjectProperty
	 *         otherwise <code>false</code>
	 */
	public boolean isOWLTopObjectProperty()
	{
		return false;
	}

	/**
	 * Determines if this is the owl:bottomObjectProperty
	 * 
	 * @return <code>true</code> if this property is the
	 *         owl:bottomObjectProperty otherwise <code>false</code>
	 */
	public boolean isOWLBottomObjectProperty()
	{
		return false;
	}

	/**
	 * Determines if this is the owl:topDataProperty
	 * 
	 * @return <code>true</code> if this property is the owl:topDataProperty
	 *         otherwise <code>false</code>
	 */
	public boolean isOWLTopDataProperty()
	{
		return false;
	}

	/**
	 * Determines if this is the owl:bottomDataProperty
	 * 
	 * @return <code>true</code> if this property is the owl:bottomDataProperty
	 *         otherwise <code>false</code>
	 */
	public boolean isOWLBottomDataProperty()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		return inversePropertyHandle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		inversePropertyHandle = handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		inversePropertyHandle = getHyperGraph().getHandleFactory().nullHandle();
	}
}

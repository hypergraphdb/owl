package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.owl.core.HGChangeableLink;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDataHasValueHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLObjectHasValueHGDB extends OWLValueRestrictionHGDB<OWLClassExpression, OWLObjectPropertyExpression, OWLIndividual>
		implements OWLObjectHasValue, HGChangeableLink
{
	/**
	 * @param args
	 *            [0]...property, [1]...OWLIndividual value
	 * 
	 */
	public OWLObjectHasValueHGDB(HGHandle... args)
	{
		super(args[0], args[1]);
		if (args.length != 2)
			throw new IllegalArgumentException("Must be exactly 2 handles.");
	}

	public OWLObjectHasValueHGDB(HGHandle property, HGHandle value)
	{
		// TODO check types: OWLObjectPropertyExpression property, OWLIndividual
		// value
		// value
		super(property, value);
	}

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType()
	{
		return ClassExpressionType.OBJECT_HAS_VALUE;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLObjectHasValue;
		}
		return false;
	}

	public boolean isObjectRestriction()
	{
		return true;
	}

	public boolean isDataRestriction()
	{
		return false;
	}

	public OWLClassExpression asSomeValuesFrom()
	{
		return getOWLDataFactory().getOWLObjectSomeValuesFrom(getProperty(), getOWLDataFactory().getOWLObjectOneOf(getValue()));
	}

	public void accept(OWLClassExpressionVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLClassExpressionVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	/**
	 * Sets the target. Only setting the valueHandle at position 1 is allowed.
	 * 
	 * @param i
	 *            must be 1
	 * @param handle
	 *            a non null handle to an OWLIndividual.
	 */
	@Override
	public void setTargetAt(int i, HGHandle handle)
	{
		if (i == 1)
			updateValueHandle(handle);
		else
		{
			throw new IllegalArgumentException("i was <> 1; only target valueHandle can be set.");
		}
	}

}

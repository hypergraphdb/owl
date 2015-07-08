package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDataSomeValuesFromHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLDataSomeValuesFromHGDB extends OWLQuantifiedDataRestrictionHGDB
		implements OWLDataSomeValuesFrom
{
	private static final long serialVersionUID = 1L;

	/**
	 * @param args
	 *            [0]...property, [1]...filler
	 */
	public OWLDataSomeValuesFromHGDB(HGHandle... args)
	{
		super(args[0], args[1]);
		if (args.length != 2)
			throw new IllegalArgumentException("Must be exactly 2 handles.");
	}

	public OWLDataSomeValuesFromHGDB(HGHandle property, HGHandle filler)
	{
		// TODO check types: OWLDataPropertyExpression property, OWLDataRange
		// filler
		super(property, filler);
	}

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType()
	{
		return ClassExpressionType.DATA_SOME_VALUES_FROM;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLDataSomeValuesFrom;
		}
		return false;
	}

	public boolean isObjectRestriction()
	{
		return false;
	}

	public boolean isDataRestriction()
	{
		return true;
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
}

package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDataAllValuesFromHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLDataAllValuesFromHGDB extends OWLQuantifiedDataRestrictionHGDB implements OWLDataAllValuesFrom
{
	/**
	 * @param args
	 *            [0]...property, [1]...filler
	 */
	public OWLDataAllValuesFromHGDB(HGHandle... args)
	{
		super(args[0], args[1]);
		if (args.length != 2)
			throw new IllegalArgumentException("Must be exactly 2 handles.");
	}

	public OWLDataAllValuesFromHGDB(HGHandle property, int cardinality, HGHandle filler)
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
		return ClassExpressionType.DATA_ALL_VALUES_FROM;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLDataAllValuesFrom;
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

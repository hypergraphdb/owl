package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectHasSelfHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLObjectHasSelfHGDB extends
		OWLRestrictionHGDB<OWLClassExpression, OWLObjectPropertyExpression, OWLObjectPropertyExpression> implements
		OWLObjectHasSelf
{
	public OWLObjectHasSelfHGDB(HGHandle... args)
	{
		super(args[0]);
		if (args.length != 1)
			throw new IllegalArgumentException();
	}

	public OWLObjectHasSelfHGDB(HGHandle property)
	{
		// TODO check property type: OWLObjectPropertyExpression
		super(property);
	}

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType()
	{
		return ClassExpressionType.OBJECT_HAS_SELF;
	}

	public boolean isObjectRestriction()
	{
		return true;
	}

	public boolean isDataRestriction()
	{
		return false;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLObjectHasSelf;
		}
		return false;
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

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		return getProperty().compareTo(((OWLObjectHasSelf) object).getProperty());
	}
}

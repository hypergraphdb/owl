package org.hypergraphdb.app.owl.model.classexpr;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectIntersectionOfHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public class OWLObjectIntersectionOfHGDB extends OWLNaryBooleanClassExpressionHGDB implements OWLObjectIntersectionOf
{
	public OWLObjectIntersectionOfHGDB(HGHandle... args)
	{
		super(args);
	}

	public OWLObjectIntersectionOfHGDB(Set<? extends HGHandle> operands)
	{
		super(operands);
	}

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType()
	{
		return ClassExpressionType.OBJECT_INTERSECTION_OF;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLObjectIntersectionOf;
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

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLClassExpressionVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	@Override
	public Set<OWLClassExpression> asConjunctSet()
	{
		Set<OWLClassExpression> conjuncts = new HashSet<OWLClassExpression>();
		for (OWLClassExpression op : getOperands())
		{
			conjuncts.addAll(op.asConjunctSet());
		}
		return conjuncts;
	}

	@Override
	public boolean containsConjunct(OWLClassExpression ce)
	{
		if (ce.equals(this))
		{
			return true;
		}
		for (OWLClassExpression op : getOperands())
		{
			if (op.containsConjunct(ce))
			{
				return true;
			}
		}
		return false;
	}

}

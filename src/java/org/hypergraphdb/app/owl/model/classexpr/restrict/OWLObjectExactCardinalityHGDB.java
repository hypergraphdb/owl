package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLObjectExactCardinalityHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public class OWLObjectExactCardinalityHGDB extends OWLObjectCardinalityRestrictionHGDB implements OWLObjectExactCardinality
{
	/**
	 * @param args
	 *            [0]...property, [1]...filler
	 */
	public OWLObjectExactCardinalityHGDB(HGHandle... args)
	{
		super(args[0], 0, args[1]);
		// TODO we call with 0 cardinality here, test that HG sets it later.
		if (args.length != 2)
			throw new IllegalArgumentException("Must be exactly 2 handles.");
	}

	public OWLObjectExactCardinalityHGDB(HGHandle property, int cardinality, HGHandle filler)
	{
		// TODO check types: OWLObjectPropertyExpression property,
		// OWLClassExpression filler
		super(property, cardinality, filler);
	}

	/**
	 * Gets the class expression type for this class expression
	 * 
	 * @return The class expression type
	 */
	public ClassExpressionType getClassExpressionType()
	{
		return ClassExpressionType.OBJECT_EXACT_CARDINALITY;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLObjectExactCardinality;
		}
		return false;
	}

	public OWLClassExpression asIntersectionOfMinMax()
	{
		OWLDataFactory df = getOWLDataFactory();
		return df.getOWLObjectIntersectionOf(df.getOWLObjectMinCardinality(getCardinality(), getProperty(), getFiller()),
				df.getOWLObjectMaxCardinality(getCardinality(), getProperty(), getFiller()));
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

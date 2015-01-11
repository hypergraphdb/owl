package org.hypergraphdb.app.owl.model.classexpr.restrict;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLQuantifiedRestriction;

/**
 * OWLQuantifiedDataRestrictionHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 19, 2011
 */
public abstract class OWLQuantifiedDataRestrictionHGDB extends
		OWLQuantifiedRestrictionHGDB<OWLDataRange, OWLDataPropertyExpression, OWLDataRange>
{

	/**
	 * @param args
	 *            [0]...property, [1]...filler
	 */
	public OWLQuantifiedDataRestrictionHGDB(HGHandle... args)
	{
		super(args[0], args[1]);
		if (args.length != 2)
			throw new IllegalArgumentException("Must be exactly 2 handles.");
	}

	public OWLQuantifiedDataRestrictionHGDB(HGHandle property, int cardinality, HGHandle filler)
	{
		// TODO check types: OWLDataPropertyExpression property, OWLDataRange
		// filler
		super(property, filler);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		@SuppressWarnings("unchecked")
		OWLQuantifiedRestriction<OWLDataRange, OWLDataPropertyExpression, OWLDataRange> other = (OWLQuantifiedRestriction<OWLDataRange, OWLDataPropertyExpression, OWLDataRange>) object;
		OWLDataPropertyExpression p1 = this.getProperty();
		OWLDataPropertyExpression p2 = other.getProperty();
		int diff = p1.compareTo(p2);
		if (diff != 0)
		{
			return diff;
		}
		return getFiller().compareTo(other.getFiller());
	}
}

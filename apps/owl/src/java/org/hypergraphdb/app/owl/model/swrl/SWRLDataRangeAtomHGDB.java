package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataRangeAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

/**
 * SWRLDataRangeAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLDataRangeAtomHGDB extends SWRLUnaryAtomHGDB<SWRLDArgument>
		implements SWRLDataRangeAtom
{
	
	public SWRLDataRangeAtomHGDB(HGHandle...args) {
		super(args);
	}

	public SWRLDataRangeAtomHGDB(OWLDataRange predicate, SWRLDArgument arg)
	{
		throw new UnsupportedOperationException();	
	}
	
	public SWRLDataRangeAtomHGDB(HGHandle predicate, HGHandle arg)
	{
		super(predicate, arg);
	}

	public OWLDataRange getPredicate()
	{
		return (OWLDataRange) super.getPredicate();
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(SWRLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(SWRLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof SWRLDataRangeAtom))
		{
			return false;
		}
		SWRLDataRangeAtom other = (SWRLDataRangeAtom) obj;
		return other.getArgument().equals(getArgument())
				&& other.getPredicate().equals(getPredicate());
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	protected int compareObjectOfSameType(OWLObject object)
	{

		SWRLDataRangeAtom other = (SWRLDataRangeAtom) object;
		int diff = getPredicate().compareTo(other.getPredicate());
		if (diff != 0)
		{
			return diff;
		}
		return getArgument().compareTo(other.getArgument());
	}
}

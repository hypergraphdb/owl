package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

/**
 * SWRLClassAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLClassAtomHGDB extends SWRLUnaryAtomHGDB<SWRLIArgument>	implements SWRLClassAtom
{
	public SWRLClassAtomHGDB(HGHandle...args) {
		super(args);
	}

	public SWRLClassAtomHGDB(OWLClassExpression predicate, SWRLIArgument arg)
	{
		throw new UnsupportedOperationException();
	}
	
	public SWRLClassAtomHGDB(HGHandle predicate, HGHandle arg)
	{
		super(predicate, arg);
	}

	public OWLClassExpression getPredicate()
	{
		return (OWLClassExpression) super.getPredicate();
	}

	public void accept(SWRLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(SWRLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof SWRLClassAtom))
		{
			return false;
		}
		SWRLClassAtom other = (SWRLClassAtom) obj;
		return other.getArgument().equals(getArgument())
				&& other.getPredicate().equals(getPredicate());
	}

	protected int compareObjectOfSameType(OWLObject object)
	{
		SWRLClassAtom other = (SWRLClassAtom) object;
		int diff = getPredicate().compareTo(other.getPredicate());
		if (diff != 0)
		{
			return diff;
		}
		return getArgument().compareTo(other.getArgument());
	}
}
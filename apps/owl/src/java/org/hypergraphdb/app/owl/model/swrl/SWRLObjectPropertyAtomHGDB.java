package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

/**
 * SWRLObjectPropertyAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLObjectPropertyAtomHGDB extends	SWRLBinaryAtomHGDB<SWRLIArgument, SWRLIArgument> implements	SWRLObjectPropertyAtom
{
	public SWRLObjectPropertyAtomHGDB(HGHandle... args)
	{
		super(args);
	}

	public SWRLObjectPropertyAtomHGDB(OWLObjectPropertyExpression predicate, SWRLIArgument arg0, SWRLIArgument arg1)
	{
		throw new UnsupportedOperationException();
	}
	
	public SWRLObjectPropertyAtomHGDB(HGHandle predicate, HGHandle arg0, HGHandle arg1)
	{
		super(predicate, arg0, arg1);
	}

	public OWLObjectPropertyExpression getPredicate()
	{
		return (OWLObjectPropertyExpression) super.getPredicate();
	}

	/**
	 * Gets a simplified form of this atom. This basically creates and returns a
	 * new atom where the predicate is not an inverse object property. If the
	 * atom is of the form P(x, y) then P(x, y) is returned. If the atom is of
	 * the form inverseOf(P)(x, y) then P(y, x) is returned.
	 * 
	 * @return This atom in a simplified form
	 */
	public SWRLObjectPropertyAtom getSimplified()
	{
		OWLObjectPropertyExpression prop = getPredicate().getSimplified();
		if (prop.equals(getPredicate()))
		{
			return this;
		} else if (prop.isAnonymous())
		{
			// Flip
			return getOWLDataFactory().getSWRLObjectPropertyAtom(
					prop.getInverseProperty().getSimplified(),
					getSecondArgument(), getFirstArgument());
		} else
		{
			// No need to flip
			return getOWLDataFactory().getSWRLObjectPropertyAtom(prop,
					getFirstArgument(), getSecondArgument());
		}
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
		if (!(obj instanceof SWRLObjectPropertyAtom))
		{
			return false;
		}
		SWRLObjectPropertyAtom other = (SWRLObjectPropertyAtom) obj;
		return other.getPredicate().equals(getPredicate())
				&& other.getFirstArgument().equals(getFirstArgument())
				&& other.getSecondArgument().equals(getSecondArgument());
	}
}
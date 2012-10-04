package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

/**
 * SWRLDataPropertyAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold(CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLDataPropertyAtomHGDB extends SWRLBinaryAtomHGDB<SWRLIArgument, SWRLDArgument> implements SWRLDataPropertyAtom
{
	public SWRLDataPropertyAtomHGDB(HGHandle... args)
	{
		super(args);
	}

	//public SWRLDataPropertyAtomHGDB(OWLDataPropertyExpression predicate, SWRLIArgument arg0, SWRLDArgument arg1)
	public SWRLDataPropertyAtomHGDB(HGHandle predicate, HGHandle arg0, HGHandle arg1)
	{
		super(predicate, arg0, arg1);
	}

	public OWLDataPropertyExpression getPredicate()
	{
		return (OWLDataPropertyExpression) super.getPredicate();
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
		if (!(obj instanceof SWRLDataPropertyAtom))
		{
			return false;
		}
		SWRLDataPropertyAtom other = (SWRLDataPropertyAtom) obj;
		return other.getPredicate().equals(getPredicate())
				&& other.getFirstArgument().equals(getFirstArgument())
				&& other.getSecondArgument().equals(getSecondArgument());
	}
}
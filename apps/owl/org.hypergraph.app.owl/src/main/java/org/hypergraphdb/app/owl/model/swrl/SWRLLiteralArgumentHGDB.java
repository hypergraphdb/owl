package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLLiteralHGDB;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

/**
 * SWRLLiteralArgumentHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLLiteralArgumentHGDB extends OWLObjectHGDB implements SWRLLiteralArgument
{
	private OWLLiteralHGDB literal;

	public SWRLLiteralArgumentHGDB() {}; 
	public SWRLLiteralArgumentHGDB(OWLLiteralHGDB literal) { 
		this.literal = literal; 
	}
	
	public void setLiteral(OWLLiteralHGDB literal)
	{
		this.literal = literal;
	}

	public OWLLiteralHGDB getLiteral()
	{
		return literal;
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
		if (!(obj instanceof SWRLLiteralArgumentHGDB))
		{
			return false;
		}
		SWRLLiteralArgument other = (SWRLLiteralArgument) obj;
		return other.getLiteral().equals(getLiteral());
	}

	protected int compareObjectOfSameType(OWLObject object)
	{
		return literal.compareTo(((SWRLLiteralArgument) object).getLiteral());
	}
}
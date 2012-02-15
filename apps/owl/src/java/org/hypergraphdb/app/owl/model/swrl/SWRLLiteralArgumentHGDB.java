package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.OWLLiteral;
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
	private OWLLiteral literal;

	public SWRLLiteralArgumentHGDB() {}; 
	public SWRLLiteralArgumentHGDB(OWLLiteral literal) { 
		this.literal = literal; 
	}
	
	public void setLiteral(OWLLiteral literal)
	{
		this.literal = literal;
	}

	public OWLLiteral getLiteral()
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
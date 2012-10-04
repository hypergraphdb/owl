package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
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
public class SWRLLiteralArgumentHGDB extends OWLObjectHGDB implements SWRLLiteralArgument, HGLink
{
	//private OWLLiteral literal;
	private HGHandle literal;

	public SWRLLiteralArgumentHGDB(HGHandle... args) { 
		this.literal = args[0]; 
	}

	//public SWRLLiteralArgumentHGDB(OWLLiteral literal) { 
	public SWRLLiteralArgumentHGDB(HGHandle literal) { 
		if (literal == null) throw new IllegalArgumentException();
		this.literal = literal; 
	}
	
	public OWLLiteral getLiteral()
	{
		return getHyperGraph().get(literal);
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
		return getLiteral().compareTo(((SWRLLiteralArgument) object).getLiteral());
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		// TODO Auto-generated method stub
		return literal == null? 0: 1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		return literal;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		literal = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		literal = null;
	}
}
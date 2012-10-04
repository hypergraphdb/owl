package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLVariable;

/**
 * SWRLVariableHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLVariableHGDB extends OWLObjectHGDB implements SWRLVariable, HGLink
{
	private HGHandle iri;

	//public SWRLVariableHGDB(IRI iri) 
	public SWRLVariableHGDB(HGHandle... args) {
		if (args[0] == null) throw new IllegalArgumentException();
		this.iri = args[0]; 
	}

	public SWRLVariableHGDB(HGHandle iri) {
		if (iri == null) throw new IllegalArgumentException();
		this.iri = iri; 
	}
	
	public IRI getIRI()
	{
		return getHyperGraph().get(iri);
	}

	public void accept(SWRLObjectVisitor paramSWRLObjectVisitor)
	{
		paramSWRLObjectVisitor.visit(this);
	}

	public <O> O accept(SWRLObjectVisitorEx<O> paramSWRLObjectVisitorEx)
	{
		return paramSWRLObjectVisitorEx.visit(this);
	}

	public void accept(OWLObjectVisitor paramOWLObjectVisitor)
	{
		paramOWLObjectVisitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> paramOWLObjectVisitorEx)
	{
		return paramOWLObjectVisitorEx.visit(this);
	}

	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof SWRLVariable))
		{
			return false;
		}
		SWRLVariable other = (SWRLVariable) obj;
		return other.getIRI().equals(this.getIRI());
	}

	final protected int compareObjectOfSameType(OWLObject object)
	{
		return getIRI().compareTo(((SWRLVariable) object).getIRI());
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i) {
		return iri;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int, org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle) {
		iri = handle;
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i) {
		iri = getHyperGraph().getHandleFactory().nullHandle();
	}
}
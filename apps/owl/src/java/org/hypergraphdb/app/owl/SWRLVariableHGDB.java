package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLVariable;

public class SWRLVariableHGDB extends OWLObjectHGDB implements SWRLVariable
{
	private IRI iri = null;

	final protected int compareObjectOfSameType(OWLObject object)
	{
		return iri.compareTo(((SWRLVariable) object).getIRI());
	}

	public void setIRI(IRI iri)
	{
		this.iri = iri;
	}

	public IRI getIRI()
	{
		return iri;
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
}
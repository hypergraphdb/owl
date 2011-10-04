package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;

public class SWRLIndividualArgumentHGDB extends OWLObjectHGDB implements
		SWRLIndividualArgument
{
	private OWLIndividual individual;
	
	public SWRLIndividualArgumentHGDB() { }
	public SWRLIndividualArgumentHGDB(OWLIndividual individual) { this.individual = individual; }
	
	public void setIndividual(OWLIndividual individual)
	{
		this.individual = individual;
	}
	
	public OWLIndividual getIndividual()
	{
		return individual;
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
		if (!(obj instanceof SWRLIndividualArgument))
		{
			return false;
		}
		SWRLIndividualArgument other = (SWRLIndividualArgument) obj;
		return other.getIndividual().equals(getIndividual());
	}

	protected int compareObjectOfSameType(OWLObject object)
	{
		return individual.compareTo(((SWRLIndividualArgument) object)
				.getIndividual());
	}
}
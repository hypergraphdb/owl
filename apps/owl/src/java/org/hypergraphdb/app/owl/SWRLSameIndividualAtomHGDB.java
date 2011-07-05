package org.hypergraphdb.app.owl;

import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

public class SWRLSameIndividualAtomHGDB extends
		SWRLBinaryAtomHGDB<SWRLIArgument, SWRLIArgument> implements
		SWRLSameIndividualAtom
{
	public SWRLSameIndividualAtomHGDB()
	{		
	}
	
	public SWRLSameIndividualAtomHGDB(SWRLIArgument arg0, SWRLIArgument arg1)
	{
		super(new OWLObjectPropertyHGDB(OWLRDFVocabulary.OWL_SAME_AS.getIRI()),
				arg0, arg1);
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
		if (!(obj instanceof SWRLSameIndividualAtom))
		{
			return false;
		}
		SWRLSameIndividualAtom other = (SWRLSameIndividualAtom) obj;
		return other.getAllArguments().equals(getAllArguments());
	}
}

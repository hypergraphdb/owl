package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * SWRLDifferentIndividualsAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLDifferentIndividualsAtomHGDB extends SWRLBinaryAtomHGDB<SWRLIArgument, SWRLIArgument> implements SWRLDifferentIndividualsAtom
{
	public SWRLDifferentIndividualsAtomHGDB()
	{		
	}
	
	public SWRLDifferentIndividualsAtomHGDB(SWRLIArgument arg0, SWRLIArgument arg1)
	{
		super(OWLDataFactoryHGDB.getInstance()
				.getOWLObjectProperty(OWLRDFVocabulary.OWL_DIFFERENT_FROM
						.getIRI()), arg0, arg1);
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
		if (!(obj instanceof SWRLDifferentIndividualsAtom))
		{
			return false;
		}
		SWRLDifferentIndividualsAtom other = (SWRLDifferentIndividualsAtom) obj;
		return other.getAllArguments().equals(getAllArguments());
	}
}
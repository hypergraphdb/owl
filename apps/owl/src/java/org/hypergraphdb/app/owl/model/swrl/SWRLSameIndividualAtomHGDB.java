package org.hypergraphdb.app.owl.model.swrl;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLObjectVisitor;
import org.semanticweb.owlapi.model.SWRLObjectVisitorEx;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;

/**
 * SWRLSameIndividualAtomHGDB.
 * @author Boris Iordanov (CIAO/Miami-Dade County)
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 9, 2011
 */
public class SWRLSameIndividualAtomHGDB extends
		SWRLBinaryAtomHGDB<SWRLIArgument, SWRLIArgument> implements
		SWRLSameIndividualAtom
{

	public SWRLSameIndividualAtomHGDB(HGHandle... args)
	{
		super(args);
	}

	//public SWRLSameIndividualAtomHGDB(SWRLIArgument arg0, SWRLIArgument arg1)
	public SWRLSameIndividualAtomHGDB(HGHandle sameAsH, HGHandle arg0, HGHandle arg1)
	{
//		super(OWLDataFactoryHGDB.getInstance()
//				.getOWLObjectProperty(OWLRDFVocabulary.OWL_SAME_AS.getIRI()),
//				arg0, arg1);
		super(sameAsH, arg0, arg1);
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
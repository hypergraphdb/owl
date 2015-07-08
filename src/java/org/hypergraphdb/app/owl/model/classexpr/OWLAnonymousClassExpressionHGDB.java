package org.hypergraphdb.app.owl.model.classexpr;

import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.app.owl.model.OWLClassExpressionHGDB;
import org.semanticweb.owlapi.model.OWLAnonymousClassExpression;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.util.NNF;

/**
 * OWLAnonymousClassExpressionHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 18, 2011
 */
public abstract class OWLAnonymousClassExpressionHGDB extends OWLClassExpressionHGDB implements OWLAnonymousClassExpression
{
	public boolean isAnonymous()
	{
		return true;
	}

	public boolean isOWLThing()
	{
		return false;
	}

	public boolean isOWLNothing()
	{
		return false;
	}

	public OWLClassExpression getNNF()
	{
		NNF nnf = new NNF(getOWLDataFactory());
		return accept(nnf);
	}

	public OWLClassExpression getComplementNNF()
	{
		NNF nnf = new NNF(getOWLDataFactory());
		return getOWLDataFactory().getOWLObjectComplementOf(this).accept(nnf);
	}

	/**
	 * Gets the object complement of this class expression.
	 * 
	 * @return A class expression that is the complement of this class
	 *         expression.
	 */
	public OWLClassExpression getObjectComplementOf()
	{
		return getOWLDataFactory().getOWLObjectComplementOf(this);
	}

	public OWLClass asOWLClass()
	{
		throw new OWLRuntimeException(
				"Not an OWLClass.  This method should only be called if the isAnonymous method returns false!");
	}

	public Set<OWLClassExpression> asConjunctSet()
	{
		return Collections.singleton((OWLClassExpression) this);
	}

	public boolean containsConjunct(OWLClassExpression ce)
	{
		return ce.equals(this);
	}

	public Set<OWLClassExpression> asDisjunctSet()
	{
		return Collections.singleton((OWLClassExpression) this);
	}
}

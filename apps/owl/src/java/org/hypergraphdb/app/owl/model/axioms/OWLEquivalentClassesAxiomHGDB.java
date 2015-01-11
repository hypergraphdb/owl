package org.hypergraphdb.app.owl.model.axioms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.CollectionFactory;

/**
 * OWLEquivalentClassesAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 13, 2011
 */
public class OWLEquivalentClassesAxiomHGDB extends OWLNaryClassAxiomHGDB implements OWLEquivalentClassesAxiom
{

	private Set<OWLClass> namedClasses;

	public OWLEquivalentClassesAxiomHGDB(HGHandle... args)
	{
		super(args);
		namedClasses = null;
	}

	public OWLEquivalentClassesAxiomHGDB(Set<? extends HGHandle> classExpressions, Collection<? extends OWLAnnotation> annotations)
	{
		// TODO assert HGHandle atom type extends OWLClassExpression
		super(classExpressions, annotations);
		namedClasses = null;
	}

	public OWLEquivalentClassesAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLEquivalentClassesAxiom(getClassExpressions());
	}

	public OWLEquivalentClassesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLEquivalentClassesAxiom(getClassExpressions(), mergeAnnos(annotations));
	}

	public Set<OWLEquivalentClassesAxiom> asPairwiseAxioms()
	{
		List<OWLClassExpression> classExpressions = new ArrayList<OWLClassExpression>(getClassExpressions());
		Set<OWLEquivalentClassesAxiom> result = new HashSet<OWLEquivalentClassesAxiom>();
		for (int i = 0; i < classExpressions.size() - 1; i++)
		{
			OWLClassExpression ceI = classExpressions.get(i);
			OWLClassExpression ceJ = classExpressions.get(i + 1);
			result.add(getOWLDataFactory().getOWLEquivalentClassesAxiom(ceI, ceJ));
		}
		return result;
	}

	public boolean containsNamedEquivalentClass()
	{
		return !getNamedClasses().isEmpty();
	}

	public boolean containsOWLNothing()
	{
		for (OWLClassExpression desc : getClassExpressions())
		{
			if (desc.isOWLNothing())
			{
				return true;
			}
		}
		return false;
	}

	public boolean containsOWLThing()
	{
		for (OWLClassExpression desc : getClassExpressions())
		{
			if (desc.isOWLThing())
			{
				return true;
			}
		}
		return false;
	}

	public Set<OWLClass> getNamedClasses()
	{
		if (namedClasses == null)
		{
			Set<OWLClass> clses = new HashSet<OWLClass>(1);
			for (OWLClassExpression desc : getClassExpressions())
			{
				if (!desc.isAnonymous() && !desc.isOWLNothing() && !desc.isOWLThing())
				{
					clses.add(desc.asOWLClass());
				}
			}
			namedClasses = Collections.unmodifiableSet(clses);
		}
		return CollectionFactory.getCopyOnRequestSet(namedClasses);
	}

	public Set<OWLSubClassOfAxiom> asOWLSubClassOfAxioms()
	{
		Set<OWLSubClassOfAxiom> result = new HashSet<OWLSubClassOfAxiom>();
		for (OWLClassExpression descA : getClassExpressions())
		{
			for (OWLClassExpression descB : getClassExpressions())
			{
				if (!descA.equals(descB))
				{
					result.add(getOWLDataFactory().getOWLSubClassOfAxiom(descA, descB));
				}
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLEquivalentClassesAxiom;
		}
		return false;
	}

	public void accept(OWLAxiomVisitor visitor)
	{
		visitor.visit(this);
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLAxiomVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public AxiomType<?> getAxiomType()
	{
		return AxiomType.EQUIVALENT_CLASSES;
	}

}

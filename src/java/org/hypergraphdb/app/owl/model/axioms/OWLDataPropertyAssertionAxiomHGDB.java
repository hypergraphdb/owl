package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLDataPropertyAssertionAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public class OWLDataPropertyAssertionAxiomHGDB extends OWLIndividualRelationshipAxiomHGDB<OWLDataPropertyExpression, OWLLiteral>
		implements OWLDataPropertyAssertionAxiom
{
	public OWLDataPropertyAssertionAxiomHGDB(HGHandle... args)
	{
		super(args);
	}

	public OWLDataPropertyAssertionAxiomHGDB(HGHandle subject, HGHandle property, HGHandle value,
			Collection<? extends OWLAnnotation> annotations)
	{
		// OWLIndividual subject, OWLDataPropertyExpression property, OWLLiteral
		// value, Set<? extends OWLAnnotation> annotations
		super(subject, property, value, annotations);
	}

	public OWLSubClassOfAxiom asOWLSubClassOfAxiom()
	{
		return getOWLDataFactory().getOWLSubClassOfAxiom(getOWLDataFactory().getOWLObjectOneOf(getSubject()),
				getOWLDataFactory().getOWLDataHasValue(getProperty(), getObject()));
	}

	public OWLDataPropertyAssertionAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLDataPropertyAssertionAxiom(getProperty(), getSubject(), getObject());
	}

	public OWLDataPropertyAssertionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLDataPropertyAssertionAxiom(getProperty(), getSubject(), getObject(),
				mergeAnnos(annotations));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLDataPropertyAssertionAxiom;
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
		return AxiomType.DATA_PROPERTY_ASSERTION;
	}
}

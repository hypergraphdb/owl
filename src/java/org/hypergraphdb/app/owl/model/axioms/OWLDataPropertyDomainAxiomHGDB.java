package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLDataPropertyDomainAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 7, 2011
 */
public class OWLDataPropertyDomainAxiomHGDB extends OWLPropertyDomainAxiomHGDB<OWLDataPropertyExpression> implements
		OWLDataPropertyDomainAxiom
{

	public OWLDataPropertyDomainAxiomHGDB(HGHandle... args)
	{
		// TODO assert arg[0] type OWLDataPropertyExpression, args[1] type
		// OWLClassExpression
		super(args[0], args[1], Collections.<OWLAnnotation> emptySet());
		if (args.length != 2)
			throw new IllegalArgumentException("args.length must be 2. Was " + args.length);
	}

	public OWLDataPropertyDomainAxiomHGDB(HGHandle property, HGHandle domain, Set<? extends OWLAnnotation> annotations)
	{
		// OWLDataPropertyExpression property, OWLClassExpression domain, Set<?
		// extends OWLAnnotation> annotations
		super(property, domain, annotations);
	}

	public OWLDataPropertyDomainAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLDataPropertyDomainAxiom(getProperty(), getDomain());
	}

	public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLDataPropertyDomainAxiom(getProperty(), getDomain(), mergeAnnos(annotations));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLDataPropertyDomainAxiom;
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
		return AxiomType.DATA_PROPERTY_DOMAIN;
	}

	public OWLSubClassOfAxiom asOWLSubClassOfAxiom()
	{
		OWLDataFactory df = getOWLDataFactory();
		OWLClassExpression sub = df.getOWLDataSomeValuesFrom(getProperty(), df.getTopDatatype());
		return df.getOWLSubClassOfAxiom(sub, getDomain());
	}
}
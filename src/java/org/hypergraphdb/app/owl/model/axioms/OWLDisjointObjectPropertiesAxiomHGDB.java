package org.hypergraphdb.app.owl.model.axioms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * OWLDisjointObjectPropertiesAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public class OWLDisjointObjectPropertiesAxiomHGDB extends OWLNaryPropertyAxiomHGDB<OWLObjectPropertyExpression> implements
		OWLDisjointObjectPropertiesAxiom
{
	public OWLDisjointObjectPropertiesAxiomHGDB(HGHandle... args)
	{
		this(new HashSet<HGHandle>(Arrays.asList(args)), Collections.<OWLAnnotation> emptySet());
		if (new HashSet<HGHandle>(Arrays.asList(args)).size() != args.length)
			throw new IllegalArgumentException("Duplicates in args not allowed. " + args);
	}

	public OWLDisjointObjectPropertiesAxiomHGDB(Set<HGHandle> properties, Collection<? extends OWLAnnotation> annotations)
	{
		// Set<? extends OWLObjectPropertyExpression> properties, Collection<?
		// extends OWLAnnotation> annotations
		super(properties, annotations);
	}

	public OWLDisjointObjectPropertiesAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(getProperties());
	}

	public OWLDisjointObjectPropertiesAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLDisjointObjectPropertiesAxiom(getProperties(), mergeAnnos(annotations));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLDisjointObjectPropertiesAxiom;
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
		return AxiomType.DISJOINT_OBJECT_PROPERTIES;
	}
}
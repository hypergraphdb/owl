package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

/**
 * OWLSubObjectPropertyOfAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 7, 2011
 */
public class OWLSubObjectPropertyOfAxiomHGDB extends OWLSubPropertyAxiomHGDB<OWLObjectPropertyExpression> implements
		OWLSubObjectPropertyOfAxiom
{
	public OWLSubObjectPropertyOfAxiomHGDB(HGHandle... args)
	{
		this(args[0], args[1], Collections.<OWLAnnotation> emptySet());
	}

	public OWLSubObjectPropertyOfAxiomHGDB(HGHandle subProperty, HGHandle superProperty,
			Collection<? extends OWLAnnotation> annotations)
	{
		// TODO assert type of HGHandle OWLObjectPropertyExpression
		super(subProperty, superProperty, annotations);
	}

	public OWLSubObjectPropertyOfAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(getSubProperty(), getSuperProperty());
	}

	public OWLSubObjectPropertyOfAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLSubObjectPropertyOfAxiom(getSubProperty(), getSuperProperty(), mergeAnnos(annotations));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLSubObjectPropertyOfAxiom;
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
		return AxiomType.SUB_OBJECT_PROPERTY;
	}

}

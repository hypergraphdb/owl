package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLNegativeObjectPropertyAssertionAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 8, 2011
 */
public class OWLNegativeObjectPropertyAssertionAxiomHGDB extends
		OWLIndividualRelationshipAxiomHGDB<OWLObjectPropertyExpression, OWLIndividual> implements
		OWLNegativeObjectPropertyAssertionAxiom
{
	public OWLNegativeObjectPropertyAssertionAxiomHGDB(HGHandle... args)
	{
		super(args);
	}

	public OWLNegativeObjectPropertyAssertionAxiomHGDB(HGHandle subject, HGHandle property, HGHandle object,
			Collection<? extends OWLAnnotation> annotations)
	{
		// OWLIndividual subject, OWLObjectPropertyExpression property,
		// OWLIndividual object, Set<? extends OWLAnnotation> annotations
		super(subject, property, object, annotations);
	}

	public OWLNegativeObjectPropertyAssertionAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(getProperty(), getSubject(), getObject());
	}

	public OWLNegativeObjectPropertyAssertionAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLNegativeObjectPropertyAssertionAxiom(getProperty(), getSubject(), getObject(),
				mergeAnnos(annotations));
	}

	public OWLSubClassOfAxiom asOWLSubClassOfAxiom()
	{
		OWLDataFactory df = getOWLDataFactory();
		return df.getOWLSubClassOfAxiom(df.getOWLObjectOneOf(getSubject()),
				df.getOWLObjectComplementOf(df.getOWLObjectHasValue(getProperty(), getObject())));
	}

	/**
	 * Determines whether this axiom contains anonymous individuals. Anonymous
	 * individuals are not allowed in negative object property assertions.
	 * 
	 * @return <code>true</code> if this axioms contains anonymous individual
	 *         axioms
	 */
	public boolean containsAnonymousIndividuals()
	{
		return getSubject().isAnonymous() || getObject().isAnonymous();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLNegativeObjectPropertyAssertionAxiom;
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
		return AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION;
	}
}

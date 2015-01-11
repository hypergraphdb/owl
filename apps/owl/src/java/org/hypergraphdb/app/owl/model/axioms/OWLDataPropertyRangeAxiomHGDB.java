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
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * OWLDataPropertyRangeAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 7, 2011
 */
public class OWLDataPropertyRangeAxiomHGDB extends OWLPropertyRangeAxiomHGDB<OWLDataPropertyExpression, OWLDataRange> implements
		OWLDataPropertyRangeAxiom
{
	public OWLDataPropertyRangeAxiomHGDB(HGHandle... args)
	{
		// TODO assert arg[0] type OWLDataPropertyExpression, args[1] type
		// OWLDataRange
		super(args[0], args[1], Collections.<OWLAnnotation> emptySet());
		if (args.length != 2)
			throw new IllegalArgumentException("args.length must be 2. Was " + args.length);
	}

	public OWLDataPropertyRangeAxiomHGDB(HGHandle property, HGHandle range, Set<? extends OWLAnnotation> annotations)
	{
		// OWLDataPropertyExpression property, OWLDataRange range, Set<? extends
		// OWLAnnotation> annotations
		super(property, range, annotations);
	}

	public OWLDataPropertyRangeAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLDataPropertyRangeAxiom(getProperty(), getRange());
	}

	public OWLAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory().getOWLDataPropertyRangeAxiom(getProperty(), getRange(), mergeAnnos(annotations));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			return obj instanceof OWLDataPropertyRangeAxiom;
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
		return AxiomType.DATA_PROPERTY_RANGE;
	}

	public OWLSubClassOfAxiom asOWLSubClassOfAxiom()
	{
		OWLDataFactory df = getOWLDataFactory();
		OWLClassExpression sup = df.getOWLDataAllValuesFrom(getProperty(), getRange());
		return df.getOWLSubClassOfAxiom(df.getOWLThing(), sup);
	}
}
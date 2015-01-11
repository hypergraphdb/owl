package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;

/**
 * OWLSubAnnotationPropertyOfAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 12, 2011
 */
public class OWLSubAnnotationPropertyOfAxiomHGDB extends OWLAxiomHGDB implements OWLSubAnnotationPropertyOfAxiom, HGLink
{
	private HGHandle subPropertyHandle;
	private HGHandle superPropertyHandle;

	public OWLSubAnnotationPropertyOfAxiomHGDB(HGHandle... args)
	{
		this(args[0], args[1], Collections.<OWLAnnotation> emptySet());
	}

	public OWLSubAnnotationPropertyOfAxiomHGDB(HGHandle subProperty, HGHandle superProperty,
			Collection<? extends OWLAnnotation> annotations)
	{
		super(annotations);
		// TODO ensure type OWLAnnotationProperty
		this.notifyTargetHandleUpdate(0, subProperty);
		this.notifyTargetHandleUpdate(1, superProperty);
	}

	public OWLSubAnnotationPropertyOfAxiom getAnnotatedAxiom(Set<OWLAnnotation> annotations)
	{
		return getOWLDataFactory()
				.getOWLSubAnnotationPropertyOfAxiom(getSubProperty(), getSuperProperty(), mergeAnnos(annotations));
	}

	public OWLSubAnnotationPropertyOfAxiom getAxiomWithoutAnnotations()
	{
		if (!isAnnotated())
		{
			return this;
		}
		return getOWLDataFactory().getOWLSubAnnotationPropertyOfAxiom(getSubProperty(), getSuperProperty());
	}

	public OWLAnnotationProperty getSubProperty()
	{
		return getHyperGraph().get(subPropertyHandle);
	}

	public OWLAnnotationProperty getSuperProperty()
	{
		return getHyperGraph().get(superPropertyHandle);
	}

	public void accept(OWLAxiomVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLAxiomVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	public boolean isLogicalAxiom()
	{
		return false;
	}

	public boolean isAnnotationAxiom()
	{
		return true;
	}

	public AxiomType<?> getAxiomType()
	{
		return AxiomType.SUB_ANNOTATION_PROPERTY_OF;
	}

	public void accept(OWLObjectVisitor visitor)
	{
		visitor.visit(this);
	}

	public <O> O accept(OWLObjectVisitorEx<O> visitor)
	{
		return visitor.visit(this);
	}

	@Override
	protected int compareObjectOfSameType(OWLObject object)
	{
		OWLSubAnnotationPropertyOfAxiom other = (OWLSubAnnotationPropertyOfAxiom) object;
		int diff = getSubProperty().compareTo(other.getSubProperty());
		if (diff != 0)
		{
			return diff;
		}
		return getSuperProperty().compareTo(other.getSuperProperty());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof OWLSubAnnotationPropertyOfAxiom))
		{
			return false;
		}
		OWLSubAnnotationPropertyOfAxiom other = (OWLSubAnnotationPropertyOfAxiom) obj;
		return getSubProperty().equals(other.getSubProperty()) && getSuperProperty().equals(other.getSuperProperty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i != 0 && i != 1)
			throw new IllegalArgumentException("Index has to be 0 or 1");
		return (i == 0) ? subPropertyHandle : superPropertyHandle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetHandleUpdate(int,
	 * org.hypergraphdb.HGHandle)
	 */
	@Override
	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		if (i != 0 && i != 1)
			throw new IllegalArgumentException("Index has to be 0 or 1");
		if (handle == null)
			throw new IllegalArgumentException("handle null");
		if (i == 0)
		{
			subPropertyHandle = handle;
		}
		else
		{
			superPropertyHandle = handle;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i != 0 && i != 1)
			throw new IllegalArgumentException("Index has to be 0 or 1");
		if (i == 0)
		{
			subPropertyHandle = getHyperGraph().getHandleFactory().nullHandle();
		}
		else
		{
			superPropertyHandle = getHyperGraph().getHandleFactory().nullHandle();
		}
	}

}

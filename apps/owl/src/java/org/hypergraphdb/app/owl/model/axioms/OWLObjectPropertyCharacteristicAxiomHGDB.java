package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

/**
 * OWLObjectPropertyCharacteristicAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 4, 2011
 */
public abstract class OWLObjectPropertyCharacteristicAxiomHGDB extends OWLPropertyAxiomHGDB implements HGLink,
		OWLObjectPropertyCharacteristicAxiom
{
	private HGHandle propertyHandle;

	// private OWLObjectPropertyExpression property;

	public OWLObjectPropertyCharacteristicAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations)
	{
		// OWLObjectPropertyExpression property, Collection<? extends
		// OWLAnnotation> annotations
		super(annotations);
		propertyHandle = property;
		if (property == null)
			throw new IllegalArgumentException("property was null");
	}

	public OWLObjectPropertyExpression getProperty()
	{
		return getHyperGraph().get(propertyHandle);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			if (!(obj instanceof OWLObjectPropertyCharacteristicAxiom))
			{
				return false;
			}
			return ((OWLObjectPropertyCharacteristicAxiom) obj).getProperty().equals(getProperty());
		}
		return false;
	}

	@Override
	final protected int compareObjectOfSameType(OWLObject object)
	{
		return getProperty().compareTo(((OWLObjectPropertyCharacteristicAxiom) object).getProperty());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getArity()
	 */
	@Override
	public int getArity()
	{
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#getTargetAt(int)
	 */
	@Override
	public HGHandle getTargetAt(int i)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		return propertyHandle;
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
		if (i != 0)
			throw new HGException("Index i must be 0");
		propertyHandle = handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGLink#notifyTargetRemoved(int)
	 */
	@Override
	public void notifyTargetRemoved(int i)
	{
		if (i != 0)
			throw new HGException("Index i must be 0");
		propertyHandle = getHyperGraph().getHandleFactory().nullHandle();
	}
}
package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataPropertyCharacteristicAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;

/**
 * OWLDataPropertyCharacteristicAxiomHGDB.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 7, 2011
 */
public abstract class OWLDataPropertyCharacteristicAxiomHGDB extends OWLPropertyAxiomHGDB implements HGLink,
		OWLDataPropertyCharacteristicAxiom
{
	private HGHandle propertyHandle;

	public OWLDataPropertyCharacteristicAxiomHGDB(HGHandle property, Collection<? extends OWLAnnotation> annotations)
	{
		// OWLDataPropertyExpression property, Collection<? extends
		// OWLAnnotation> annotations
		super(annotations);
		this.propertyHandle = property;
		if (property == null)
			throw new IllegalArgumentException("property was null");
	}

	public OWLDataPropertyExpression getProperty()
	{
		return getHyperGraph().get(propertyHandle);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (super.equals(obj))
		{
			if (!(obj instanceof OWLDataPropertyCharacteristicAxiom))
			{
				return false;
			}
			return ((OWLDataPropertyCharacteristicAxiom) obj).getProperty().equals(getProperty());
		}
		return false;
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
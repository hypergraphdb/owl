package org.hypergraphdb.app.owl;

import java.util.Collections;

import java.util.HashSet;
import java.util.Set;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HyperGraph;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.util.HashCode;
import org.semanticweb.owlapi.util.OWLClassExpressionCollector;
import org.semanticweb.owlapi.util.OWLEntityCollector;
import org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public abstract class OWLObjectHGDB implements OWLObject, HGGraphHolder
{
	private HyperGraph graph;
	private int hashCode = 0;
	private Set<OWLEntity> signature;

	protected OWLDataFactory getOWLDataFactory()
	{
		return OWLDataFactoryImpl.getInstance();
	}
	
	
	public HyperGraph getHyperGraph()
	{
		return graph;
	}


	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}


	public Set<OWLClass> getClassesInSignature()
	{
		Set<OWLClass> result = new HashSet<OWLClass>();
		for (OWLEntity ent : getSignature())
		{
			if (ent.isOWLClass())
			{
				result.add(ent.asOWLClass());
			}
		}
		return result;
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature()
	{
		Set<OWLDataProperty> result = new HashSet<OWLDataProperty>();
		for (OWLEntity ent : getSignature())
		{
			if (ent.isOWLDataProperty())
			{
				result.add(ent.asOWLDataProperty());
			}
		}
		return result;
	}

	public Set<OWLDatatype> getDatatypesInSignature()
	{
		Set<OWLDatatype> result = new HashSet<OWLDatatype>();
		for (OWLEntity ent : getSignature())
		{
			if (ent.isOWLDatatype())
			{
				result.add(ent.asOWLDatatype());
			}
		}
		return result;
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature()
	{
		Set<OWLNamedIndividual> result = new HashSet<OWLNamedIndividual>();
		for (OWLEntity ent : getSignature())
		{
			if (ent.isOWLNamedIndividual())
			{
				result.add(ent.asOWLNamedIndividual());
			}
		}
		return result;
	}

	public Set<OWLClassExpression> getNestedClassExpressions()
	{
		OWLClassExpressionCollector collector = new OWLClassExpressionCollector();
		return this.accept(collector);
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature()
	{
		Set<OWLObjectProperty> result = new HashSet<OWLObjectProperty>();
		for (OWLEntity ent : getSignature())
		{
			if (ent.isOWLObjectProperty())
			{
				result.add(ent.asOWLObjectProperty());
			}
		}
		return result;
	}

	public Set<OWLEntity> getSignature()
	{
		if (signature == null)
		{
			OWLEntityCollector collector = new OWLEntityCollector();
			accept(collector);
			signature = Collections.unmodifiableSet(collector.getObjects());
		}
		return signature;
	}

	public boolean isBottomEntity()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isTopEntity()
	{
		// TODO Auto-generated method stub
		return false;
	}

	protected abstract int compareObjectOfSameType(OWLObject object);

	public int compareTo(OWLObject o)
	{
		OWLObjectTypeIndexProvider typeIndexProvider = new OWLObjectTypeIndexProvider();
		int thisTypeIndex = typeIndexProvider.getTypeIndex(this);
		int otherTypeIndex = typeIndexProvider.getTypeIndex(o);
		int diff = thisTypeIndex - otherTypeIndex;
		if (diff == 0)
		{
			// Objects are the same type
			return compareObjectOfSameType(o);
		} else
		{
			return diff;
		}
	}

	public boolean equals(Object obj)
	{
		return obj == this || obj != null && obj instanceof OWLObject;
	}

	public int hashCode()
	{
		if (hashCode == 0)
		{
			hashCode = HashCode.hashCode(this);
		}
		return hashCode;
	}
}
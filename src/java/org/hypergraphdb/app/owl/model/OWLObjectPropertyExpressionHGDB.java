package org.hypergraphdb.app.owl.model;

import java.util.Set;
import java.util.TreeSet;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLNaryPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyAxiom;
import org.semanticweb.owlapi.util.ObjectPropertySimplifier;

/**
 * OWLObjectPropertyExpressionHGDB.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 3, 2011
 */
public abstract class OWLObjectPropertyExpressionHGDB
		extends
		OWLPropertyExpressionHGDB<OWLClassExpression, OWLObjectPropertyExpression>
		implements OWLObjectPropertyExpression
{
	private static final long serialVersionUID = 1L;

	private OWLObjectPropertyExpression simplestForm;

	private OWLObjectPropertyExpression inverse;

	@Override
	protected Set<? extends OWLPropertyDomainAxiom<?>> getDomainAxioms(
			OWLOntology ontology)
	{
		return ontology.getObjectPropertyDomainAxioms(this);
	}

	public boolean isObjectPropertyExpression()
	{
		return true;
	}

	public boolean isDataPropertyExpression()
	{
		return false;
	}

	public boolean isFunctional(OWLOntology ontology)
	{
		return ontology.getFunctionalObjectPropertyAxioms(this).size() > 0;
	}

	public boolean isFunctional(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isFunctional(ont))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isInverseFunctional(OWLOntology ontology)
	{
		return !ontology.getInverseFunctionalObjectPropertyAxioms(this)
				.isEmpty();
	}

	public boolean isInverseFunctional(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isInverseFunctional(ont))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isSymmetric(OWLOntology ontology)
	{
		return !ontology.getSymmetricObjectPropertyAxioms(this).isEmpty();
	}

	public boolean isSymmetric(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isSymmetric(ont))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isAsymmetric(OWLOntology ontology)
	{
		return !ontology.getAsymmetricObjectPropertyAxioms(this).isEmpty();
	}

	public boolean isAsymmetric(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isAsymmetric(ont))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isReflexive(OWLOntology ontology)
	{
		return !ontology.getReflexiveObjectPropertyAxioms(this).isEmpty();
	}

	public boolean isReflexive(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isReflexive(ont))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isIrreflexive(OWLOntology ontology)
	{
		return !ontology.getIrreflexiveObjectPropertyAxioms(this).isEmpty();
	}

	public boolean isIrreflexive(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isIrreflexive(ont))
			{
				return true;
			}
		}
		return false;
	}

	public boolean isTransitive(OWLOntology ontology)
	{
		return !ontology.getTransitiveObjectPropertyAxioms(this).isEmpty();
	}

	public boolean isTransitive(Set<OWLOntology> ontologies)
	{
		for (OWLOntology ont : ontologies)
		{
			if (isTransitive(ont))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected Set<? extends OWLPropertyRangeAxiom<OWLObjectPropertyExpression, OWLClassExpression>> getRangeAxioms(
			OWLOntology ontology)
	{
		return ontology.getObjectPropertyRangeAxioms(this);
	}

	@Override
	protected Set<? extends OWLSubPropertyAxiom<OWLObjectPropertyExpression>> getSubPropertyAxioms(
			OWLOntology ontology)
	{
		return ontology.getObjectSubPropertyAxiomsForSubProperty(this);
	}

	@Override
	protected Set<? extends OWLNaryPropertyAxiom<OWLObjectPropertyExpression>> getEquivalentPropertiesAxioms(
			OWLOntology ontology)
	{
		return ontology.getEquivalentObjectPropertiesAxioms(this);
	}

	@Override
	protected Set<? extends OWLNaryPropertyAxiom<OWLObjectPropertyExpression>> getDisjointPropertiesAxioms(
			OWLOntology ontology)
	{
		return ontology.getDisjointObjectPropertiesAxioms(this);
	}

	public Set<OWLObjectPropertyExpression> getInverses(OWLOntology ontology)
	{
		Set<OWLObjectPropertyExpression> result = new TreeSet<OWLObjectPropertyExpression>();
		for (OWLInverseObjectPropertiesAxiom ax : ontology.getInverseObjectPropertyAxioms(this))
		{
			if (ax.getFirstProperty().equals(this))
			{
				result.add(ax.getSecondProperty());
			}
			else
			{
				result.add(ax.getFirstProperty());
			}
		}
		return result;
	}

	public Set<OWLObjectPropertyExpression> getInverses(Set<OWLOntology> ontologies)
	{
		Set<OWLObjectPropertyExpression> result = new TreeSet<OWLObjectPropertyExpression>();
		for (OWLOntology ont : ontologies)
		{
			result.addAll(getInverses(ont));
		}
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		return super.equals(obj) && obj instanceof OWLObjectPropertyExpression;
	}

	public OWLObjectPropertyExpression getSimplified()
	{
		if (simplestForm == null)
		{
			ObjectPropertySimplifier simplifier = new ObjectPropertySimplifier(
					getOWLDataFactory());
			simplestForm = simplifier.getSimplified(this);
		}
		return simplestForm;
	}

	public OWLObjectPropertyExpression getInverseProperty()
	{
		// We have to check that the inverse expression is still in the DB because
		// it could have been GC-ed. This is a side-effect of the way GC has been 
		// designed and it needs to be fixed, see issue: https://github.com/hypergraphdb/owl/issues/4
		if (inverse == null || getHyperGraph().getHandle(inverse) == null) 
		{
			inverse = getOWLDataFactory().getOWLObjectInverseOf(this);
		}
		return inverse;
	}

	public OWLObjectProperty getNamedProperty()
	{
		OWLObjectPropertyExpression simp = getSimplified();
		if (simp.isAnonymous())
		{
			return ((OWLObjectInverseOf) simp).getInverse().asOWLObjectProperty();
		}
		else
		{
			return simp.asOWLObjectProperty();
		}
	}

}

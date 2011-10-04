package org.hypergraphdb.app.owl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.query.HGQueryCondition;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationSubject;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;

import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLAnnotationPropertyImpl;

public class HGDBOntology implements OWLOntology, HGGraphHolder, HGHandleHolder
{
	private HGHandle handle;
	private HGDBOntologyManager manager;
	
	protected Set<OWLOntology> getOntologies (final boolean includeImportsClosure)
	{
		return includeImportsClosure ? this.getImportsClosure () : Collections.singleton ((OWLOntology) this);
	}
	
	protected boolean containsObject(OWLObject instance)
	{
		return findHandle(instance) != null;
	}

	protected HGHandle findHandle(OWLObject instance)
	{
		HGHandle h = manager.getHyperGraph().getHandle(instance);
		if (h != null)
			return h;
		else
			return hg.findOne(manager.getHyperGraph(), hg.guessUniquenessCondition(manager.getHyperGraph(), instance));
	}

	public HGHandle getAtomHandle()
	{
		return handle;
	}

	public void setAtomHandle(HGHandle handle)
	{
		this.handle = handle;
	}

	public void setHyperGraph(HyperGraph graph)
	{
		manager = hg.getOne(graph, hg.type(HGDBOntologyManager.class));
	}

	public boolean containsAnnotationPropertyInSignature(IRI iri)
	{		
		return iri == null ? false : containsObject(manager.getOWLDataFactory().getOWLAnnotationProperty(iri));	
	}

	public boolean containsAnnotationPropertyInSignature(IRI propIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsAnnotationPropertyInSignature (propIRI))
				return true;
		}
		return false;
	}

	public boolean containsAxiom(OWLAxiom axiom)
	{
		return hg.findOne(manager.getHyperGraph(), 
						  hg.guessUniquenessCondition(manager.getHyperGraph(), axiom)) != null;
	}

	public boolean containsAxiom(OWLAxiom axiom, boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: getOntologies(includeImportsClosure))
		{
			if (owlOntology.containsAxiom (axiom))
				return true;
		}
		return false;
	}

	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom)
	{
		if (axiom == null)
			return false;

		HGQueryCondition cond = hg.guessUniquenessCondition(manager.getHyperGraph(), axiom);
		return hg.findOne(manager.getHyperGraph(), hg.and(hg.not(hg.typePlus(OWLAnnotationAxiom.class)), cond)) != null;
	}

	public boolean containsAxiomIgnoreAnnotations(OWLAxiom axiom, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsAxiomIgnoreAnnotations (axiom))
				return true;
		}
		return false;
	}

	public boolean containsClassInSignature(IRI iri)
	{
		return iri == null ? false : containsObject(manager.getOWLDataFactory().getOWLClass(iri));
	}

	public boolean containsClassInSignature(IRI owlClassIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsClassInSignature (owlClassIRI))
				return true;
		}
		return false;
	}

	public boolean containsDataPropertyInSignature(IRI iri)
	{
		return iri == null ? false : containsObject(manager.getOWLDataFactory().getOWLDataProperty(iri));
	}

	public boolean containsDataPropertyInSignature(IRI propIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsDataPropertyInSignature (propIRI))
				return true;
		}
		return false;
	}

	public boolean containsDatatypeInSignature(IRI iri)
	{
		return iri == null ? false : containsObject(manager.getOWLDataFactory().getOWLDatatype(iri));
	}

	public boolean containsDatatypeInSignature(IRI datatypeIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: getOntologies(includeImportsClosure))
		{
			if (owlOntology.containsDatatypeInSignature (datatypeIRI))
				return true;
		}
		return false;
	}

	public boolean containsEntityInSignature(OWLEntity entity)
	{
		return containsObject(entity);
	}

	public boolean containsEntityInSignature(IRI iri)
	{
		// this is a TODO: we need appropriate complex type for the iri to be a RW property.
		//return hg.findOne(manager.getHyperGraph(), hg.and(hg.typePlus(OWLEntity.class), hg.eq("iri", iri)));
		return false;
	}

	public boolean containsEntityInSignature(OWLEntity owlEntity, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: this.getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsEntityInSignature (owlEntity))
				return true;
		}
		return false;
	}

	public boolean containsEntityInSignature(IRI entityIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: this.getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsEntityInSignature (entityIRI))
				return true;
		}
		return false;
	}

	public boolean containsIndividualInSignature(final IRI individualIRI)
	{
		return individualIRI == null ? false : containsObject(manager.getOWLDataFactory().getOWLNamedIndividual (individualIRI));
	}

	public boolean containsIndividualInSignature(final IRI individualIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: this.getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsIndividualInSignature (individualIRI))
				return true;
		}
		return false;
	}

	public boolean containsObjectPropertyInSignature(final IRI propIRI)
	{
		return propIRI == null ? false : containsObject(manager.getOWLDataFactory().getOWLObjectProperty (propIRI));
	}

	public boolean containsObjectPropertyInSignature(final IRI propIRI, final boolean includeImportsClosure)
	{
		for (final OWLOntology owlOntology: this.getOntologies (includeImportsClosure))
		{
			if (owlOntology.containsObjectPropertyInSignature (propIRI))
				return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	public Set<OWLAnnotationAssertionAxiom> getAnnotationAssertionAxioms(OWLAnnotationSubject entity)
	{
		HashSet<OWLAnnotationAssertionAxiom> S = new HashSet<OWLAnnotationAssertionAxiom>();
		HGHandle entityHandle = findHandle(entity);
		if (entityHandle != null)
			S.addAll((List<OWLAnnotationAssertionAxiom>)(List<?>)hg.getAll(manager.getHyperGraph(), 
					hg.and(hg.type(OWLAnnotationAssertionAxiomImpl.class),
					hg.orderedLink(hg.anyHandle(), entityHandle, hg.anyHandle()))));	
		return S;
	}

	public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature()
	{
		HashSet<OWLAnnotationProperty> S = new HashSet<OWLAnnotationProperty>();
		S.addAll((List<OWLAnnotationProperty>)
				(List<?>)hg.getAll(manager.getHyperGraph(), hg.type(OWLAnnotationPropertyImpl.class)));
		return S;
	}

	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(
			OWLAnnotationProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(
			OWLAnnotationProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAnnotation> getAnnotations()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAsymmetricObjectPropertyAxiom> getAsymmetricObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getAxiomCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> arg0)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> arg0,
			boolean arg1)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<OWLAxiom> getAxioms()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLClassAxiom> getAxioms(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLObjectPropertyAxiom> getAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDataPropertyAxiom> getAxioms(OWLDataProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLIndividualAxiom> getAxioms(OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAnnotationAxiom> getAxioms(OWLAnnotationProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDatatypeDefinitionAxiom> getAxioms(OWLDatatype arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(OWLAxiom arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLClassAssertionAxiom> getClassAssertionAxioms(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLClass> getClassesInSignature()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLClass> getClassesInSignature(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDataProperty> getDataPropertiesInSignature(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDataPropertyAssertionAxiom> getDataPropertyAssertionAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDataPropertyDomainAxiom> getDataPropertyDomainAxioms(
			OWLDataProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDataPropertyRangeAxiom> getDataPropertyRangeAxioms(
			OWLDataProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSubProperty(
			OWLDataProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubDataPropertyOfAxiom> getDataSubPropertyAxiomsForSuperProperty(
			OWLDataPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(
			OWLDatatype arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDatatype> getDatatypesInSignature()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDatatype> getDatatypesInSignature(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDifferentIndividualsAxiom> getDifferentIndividualAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getDirectImports()
			throws UnknownOWLOntologyException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<IRI> getDirectImportsDocuments()
			throws UnknownOWLOntologyException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDisjointClassesAxiom> getDisjointClassesAxioms(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDisjointDataPropertiesAxiom> getDisjointDataPropertiesAxioms(
			OWLDataProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDisjointObjectPropertiesAxiom> getDisjointObjectPropertiesAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLDisjointUnionAxiom> getDisjointUnionAxioms(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEntity> getEntitiesInSignature(IRI arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEntity> getEntitiesInSignature(IRI arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEquivalentClassesAxiom> getEquivalentClassesAxioms(
			OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEquivalentDataPropertiesAxiom> getEquivalentDataPropertiesAxioms(
			OWLDataProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEquivalentObjectPropertiesAxiom> getEquivalentObjectPropertiesAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLFunctionalDataPropertyAxiom> getFunctionalDataPropertyAxioms(
			OWLDataPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLFunctionalObjectPropertyAxiom> getFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLClassAxiom> getGeneralClassAxioms()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLHasKeyAxiom> getHasKeyAxioms(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getImports() throws UnknownOWLOntologyException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLOntology> getImportsClosure() throws UnknownOWLOntologyException
	{
		return manager.getImportsClosure(this);
	}

	public Set<OWLImportsDeclaration> getImportsDeclarations()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLNamedIndividual> getIndividualsInSignature(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLInverseFunctionalObjectPropertyAxiom> getInverseFunctionalObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLInverseObjectPropertiesAxiom> getInverseObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLIrreflexiveObjectPropertyAxiom> getIrreflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int getLogicalAxiomCount()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLNegativeDataPropertyAssertionAxiom> getNegativeDataPropertyAssertionAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLNegativeObjectPropertyAssertionAxiom> getNegativeObjectPropertyAssertionAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntologyManager getOWLOntologyManager()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLObjectProperty> getObjectPropertiesInSignature(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLObjectPropertyAssertionAxiom> getObjectPropertyAssertionAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLObjectPropertyDomainAxiom> getObjectPropertyDomainAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLObjectPropertyRangeAxiom> getObjectPropertyRangeAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSubProperty(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubObjectPropertyOfAxiom> getObjectSubPropertyAxiomsForSuperProperty(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public OWLOntologyID getOntologyID()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAnonymousIndividual> getReferencedAnonymousIndividuals()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLEntity arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLEntity arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLReflexiveObjectPropertyAxiom> getReflexiveObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSameIndividualAxiom> getSameIndividualAxioms(
			OWLIndividual arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEntity> getSignature()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLEntity> getSignature(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(
			OWLAnnotationProperty arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSubClass(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSubClassOfAxiom> getSubClassAxiomsForSuperClass(OWLClass arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLSymmetricObjectPropertyAxiom> getSymmetricObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Set<OWLTransitiveObjectPropertyAxiom> getTransitiveObjectPropertyAxioms(
			OWLObjectPropertyExpression arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isAnonymous()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDeclared(OWLEntity arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDeclared(OWLEntity arg0, boolean arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void accept(OWLObjectVisitor arg0)
	{
		// TODO Auto-generated method stub

	}

	public <O> O accept(OWLObjectVisitorEx<O> arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(OWLObject o)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<OWLClassExpression> getNestedClassExpressions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBottomEntity()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTopEntity()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
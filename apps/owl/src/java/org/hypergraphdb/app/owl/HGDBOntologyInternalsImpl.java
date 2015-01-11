package org.hypergraphdb.app.owl;

import static org.semanticweb.owlapi.util.CollectionFactory.createSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.algorithms.HGBreadthFirstTraversal;
import org.hypergraphdb.app.owl.core.AbstractInternalsHGDB;
import org.hypergraphdb.app.owl.core.AxiomTypeToHGDBMap;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.core.PrefixHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLAnonymousIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDeclarationAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDisjointClassesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLEquivalentClassesAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubClassOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.swrl.SWRLConjuction;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.hypergraphdb.app.owl.util.ImplUtils;
import org.hypergraphdb.app.owl.util.IncidenceSetALGenerator;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.indexing.HGIndexer;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.query.Or;
import org.hypergraphdb.query.SubgraphMemberCondition;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.SWRLObject;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * HGDBOntologyInternalsImpl.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyInternalsImpl extends AbstractInternalsHGDB
{

	/**
	 * Switches LOG string creation on or off. This will slow down performance
	 * tremendously, as graph stats are calculated often.
	 */
	public static boolean DBG = false;

	public static boolean USE_CONTAINS_AXIOM_BY_IS = false;
	public static boolean USE_CONTAINS_AXIOM_BY_HASHCODE = true;

	/**
	 * Max directly Acceptable axiom signature size for findAxiom lookup (to
	 * avoid finding minimum size.)
	 */
	public static final int PERFORMANCE_INCIDENCE_SET_SIZE = 2;
	public static int PERFCOUNTER_FIND_AXIOM = 0;
	public static int PERFCOUNTER_FIND_BY_MEMBERSHIP = 0;
	public static int PERFCOUNTER_FIND_BY_SIGNATURE = 0;
	public static int PERFCOUNTER_CONTAINS_DECLARATION = 0;
	public static int PERFCOUNTER_FIND_EQUALS = 0;
	public static int PERFCOUNTER_FIND_BY_SIGNATURE_EQUALS = 0;
	public static int PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS = 0;
	public static int PERFCOUNTER_FIND_BY_HASHCODE_EQUALS = 0;

	/**
	 * recursion level/depth for getReferencingAxioms.
	 */
	private int recLevel = 0;

	protected Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	static
	{
		boolean assertsEnabled = false;
		assert assertsEnabled = true; // force assertions.
		if (!assertsEnabled)
		{
			Logger.getLogger(HGDBOntologyInternalsImpl.class.getCanonicalName()).info(
					"Asserts are disabled for HGDBOntologyInternalsImpl");
			// throw new
			// RuntimeException("We need Asserts to be enabled. Use: java -ea:org.hypergraphdb.app.owl...");
		}
	}

	// hilpold protected Set<OWLImportsDeclaration> importsDeclarations;
	// 2011.11.17 protected Set<OWLAnnotation> ontologyAnnotations; //
	// recursive??
	// protected Map<AxiomType<?>, Set<OWLAxiom>> axiomsByType; removed
	// 2011.10.06
	// 2011.11.17 protected Map<OWLAxiom, Set<OWLAxiom>>
	// logicalAxiom2AnnotatedAxiomMap;
	// 2011.11.21 protected Set<OWLClassAxiom> generalClassAxioms;
	// 2011.11.07 protected Set<OWLSubPropertyChainOfAxiom>
	// propertyChainSubPropertyAxioms;

	// protected Map<OWLClass, Set<OWLAxiom>> owlClassReferences;
	// protected Map<OWLObjectProperty, Set<OWLAxiom>>
	// owlObjectPropertyReferences;
	// protected Map<OWLDataProperty, Set<OWLAxiom>> owlDataPropertyReferences;
	// protected Map<OWLNamedIndividual, Set<OWLAxiom>> owlIndividualReferences;
	// 2011.10.26 protected Map<OWLAnonymousIndividual, Set<OWLAxiom>>
	// owlAnonymousIndividualReferences;

	// protected Map<OWLDatatype, Set<OWLAxiom>> owlDatatypeReferences;
	// protected Map<OWLAnnotationProperty, Set<OWLAxiom>>
	// owlAnnotationPropertyReferences;

	// hilpold 2011.09.27 eliminating protected Map<OWLEntity,
	// Set<OWLDeclarationAxiom>> declarationsByEntity;

	public HGDBOntologyInternalsImpl()
	{
		// initMaps();
	}

	// protected void initMaps() {
	// this.importsDeclarations = createSet();
	// this.ontologyAnnotations = createSet();
	// this.axiomsByType = createMap();
	// this.logicalAxiom2AnnotatedAxiomMap = createMap();
	// this.generalClassAxioms = createSet();
	// this.propertyChainSubPropertyAxioms = createSet();
	// this.owlClassReferences = createMap();
	// this.owlObjectPropertyReferences = createMap();
	// this.owlDataPropertyReferences = createMap();
	// this.owlIndividualReferences = createMap();
	// this.owlAnonymousIndividualReferences = createMap();
	// this.owlDatatypeReferences = createMap();
	// this.owlAnnotationPropertyReferences = createMap();
	// this.declarationsByEntity = createMap();
	// }

	// BORIS
	// public Set<OWLAxiom> getAxiomsByType(AxiomType t)
	// {
	// HGHandle hgType = axiomTypeToHGType.get(t);
	// HashSet<OWLAxiom> S= new HashSet();
	// S.addAll(hg.getAll(graph, hg.type(hgType)));
	// return S;
	// }

	/**
	 * Entity of OWLDeclarationAxiom declared? iff we have Entity with
	 * incidenceset count > 0. We should remove such entities. hilpold
	 */
	public boolean isDeclared(OWLDeclarationAxiom ax)
	{
		return containsAxiom(ax);
		// old return declarationsByEntity.containsKey(ax.getEntity());
	}

	public boolean isEmpty()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				boolean noAxioms = ontology.count(hg.typePlus(OWLAxiomHGDB.class)) == 0;
				return noAxioms && getOntologyAnnotations().isEmpty();
			}
		}, HGTransactionConfig.READONLY);
		// Don't do this: ontology.isEmpty(); because Onto is considered empty
		// despite imports.
		// for (Set<OWLAxiom> axiomSet : axiomsByType.values()) {
		// if (!axiomSet.isEmpty()) {
		// return false;
		// }
		// }
		// return ontologyAnnotations.isEmpty();
	}

	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(final OWLDatatype datatype)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLDatatypeDefinitionAxiom>>()
		{
			public Set<OWLDatatypeDefinitionAxiom> call()
			{
				Set<OWLDatatypeDefinitionAxiom> result = createSet();
				Set<OWLDatatypeDefinitionAxiom> axioms = getAxiomsInternal(AxiomType.DATATYPE_DEFINITION);
				for (OWLDatatypeDefinitionAxiom ax : axioms)
				{
					if (ax.getDatatype().equals(datatype))
					{
						result.add(ax);
					}
				}
				return result;
			}
		}, HGTransactionConfig.READONLY);
	}

	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(final OWLAnnotationProperty subProperty)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLSubAnnotationPropertyOfAxiom>>()
		{
			public Set<OWLSubAnnotationPropertyOfAxiom> call()
			{
				Set<OWLSubAnnotationPropertyOfAxiom> result = createSet();
				for (OWLSubAnnotationPropertyOfAxiom ax : getAxiomsInternal(AxiomType.SUB_ANNOTATION_PROPERTY_OF))
				{
					if (ax.getSubProperty().equals(subProperty))
					{
						result.add(ax);
					}
				}
				return result;
			}
		}, HGTransactionConfig.READONLY);
	}

	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(final OWLAnnotationProperty property)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAnnotationPropertyDomainAxiom>>()
		{
			public Set<OWLAnnotationPropertyDomainAxiom> call()
			{
				Set<OWLAnnotationPropertyDomainAxiom> result = createSet();
				for (OWLAnnotationPropertyDomainAxiom ax : getAxiomsInternal(AxiomType.ANNOTATION_PROPERTY_DOMAIN))
				{
					if (ax.getProperty().equals(property))
					{
						result.add(ax);
					}
				}
				return result;
			}
		}, HGTransactionConfig.READONLY);
	}

	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(final OWLAnnotationProperty property)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAnnotationPropertyRangeAxiom>>()
		{
			public Set<OWLAnnotationPropertyRangeAxiom> call()
			{
				Set<OWLAnnotationPropertyRangeAxiom> result = createSet();
				for (OWLAnnotationPropertyRangeAxiom ax : getAxiomsInternal(AxiomType.ANNOTATION_PROPERTY_RANGE))
				{
					if (ax.getProperty().equals(property))
					{
						result.add(ax);
					}
				}
				return result;
			}
		}, HGTransactionConfig.READONLY);
	}

	@Override
	protected <T extends OWLAxiom> Set<T> getAxiomsInternal(final AxiomType<T> axiomType)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<T>>()
		{
			public Set<T> call()
			{
				List<T> axiomsOneTypeList;
				Class<? extends OWLAxiomHGDB> hgdbAxiomClass = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiomType);
				axiomsOneTypeList = ontology.getAll(hg.type(hgdbAxiomClass));
				Set<T> s = getReturnSet(axiomsOneTypeList);
				if (s.size() != axiomsOneTypeList.size())
					throw new IllegalStateException("Set contract broken.");
				return s;
			}
		}, HGTransactionConfig.READONLY);
		// return (Set<T>) getAxioms(axiomType, axiomsByType, false);
	}

	/**
	 * Anonymousindividuals have an ID that must be unique in the context of an
	 * ontology. DataFactoryHGDB cannot prevent creation of n AnonI with the
	 * same ID.
	 * 
	 */
	public Set<OWLAxiom> getReferencingAxioms(final OWLAnonymousIndividual individual)
	{
		// TODO we know here, that this might not be in our onto; still an equal
		// object might.
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAxiom>>()
		{
			public Set<OWLAxiom> call()
			{
				HGHandle h = findAnonymousIndividual(individual);
				if (h == null || !ontology.isMember(h))
					return getReturnSet(null);
				else
				{
					Set<OWLAxiom> s = new HashSet<OWLAxiom>();
					collectOntologyAxiomsRecursive(h, s);
					return s;
				}
			}
		}, HGTransactionConfig.READONLY);
		// return getReturnSet(getAxioms(individual,
		// owlAnonymousIndividualReferences, false));
	}

	public HGHandle findAnonymousIndividual(OWLAnonymousIndividual individual)
	{
		HGHandle h = ontology.findOne(hg.and(hg.type(OWLAnonymousIndividualHGDB.class), hg.eq(individual)));
		return h;
	}

	/**
	 * 2011.10.13: return only axioms in Ontology!
	 */
	public Set<OWLAxiom> getReferencingAxioms(final OWLEntity owlEntity)
	{
		// TODO create method to HGHandle getFindEntity(OWLEntity owlEntity);
		// TODO shall we ensure that entity is in ontology?
		// TODO this get;s called with null by one or more protege views!!
		if (owlEntity == null)
		{
			log.warning("BAD ? getReferencingAx(null) called");
			return Collections.emptySet();
		}
		String className = owlEntity.getClass().getCanonicalName();
		if (className.startsWith("uk.ac"))
		{
			log.warning("BAD ! OWLENTITY TYPE IS : " + owlEntity.getClass().getSimpleName());
			log.warning("BAD ! Object IS : " + owlEntity);
			log.warning("BAD ! IRI IS : " + owlEntity.getIRI());
			// 2010.10.06 not acceptable anymore. HGApp adds BUILTIN types.;
			// return Collections.emptySet();
			throw new IllegalStateException("getReferencingAxioms was called with a uk.ac entity.");
		}
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAxiom>>()
		{
			public Set<OWLAxiom> call()
			{
				Set<OWLAxiom> s;
				HGHandle owlEntityHandle = graph.getHandle(owlEntity);
				if (owlEntityHandle == null)
				{
					// TODO might not find what we need, because
					// owlEntity.getClass must match our
					// *HGDB implementation types.
					owlEntityHandle = hg.findOne(graph, hg.and(hg.type(owlEntity.getClass()), hg.eq("IRI", owlEntity.getIRI())));
				}
				if (owlEntityHandle == null)
				{
					s = Collections.emptySet();
				}
				else
				{
					s = new HashSet<OWLAxiom>();
					// 2010.10.20 we traverse incidenceset until we hit an empty
					// set or an axiom
					// that is a member of our Ontology.
					if (DBG)
						System.out.print("Collecting axioms: ");
					collectOntologyAxiomsRecursive(owlEntityHandle, s);
					if (DBG)
						System.out.println();
					if (DBG)
						System.out.println("Found : " + s.size());
					// old l =
					// ontology.getAll(hg.and(hg.typePlus(OWLAxiom.class),
					// hg.incident(owlEntityHandle)));
					// older l = hg.getAll(graph,
					// hg.and(hg.typePlus(OWLAxiom.class),
					// hg.incident(owlEntityHandle)));
				}
				return s;
			}

		}, HGTransactionConfig.READONLY);
		// if (owlEntity instanceof OWLClass) {
		// axioms = getAxioms(owlEntity.asOWLClass(), owlClassReferences,
		// false);
		// } else if (owlEntity instanceof OWLObjectProperty) {
		// axioms = getAxioms(owlEntity.asOWLObjectProperty(),
		// owlObjectPropertyReferences, false);
		// } else if (owlEntity instanceof OWLDataProperty) {
		// axioms = getAxioms(owlEntity.asOWLDataProperty(),
		// owlDataPropertyReferences, false);
		// } else if (owlEntity instanceof OWLNamedIndividual) {
		// axioms = getAxioms(owlEntity.asOWLNamedIndividual(),
		// owlIndividualReferences, false);
		// } else if (owlEntity instanceof OWLDatatype) {
		// axioms = getAxioms(owlEntity.asOWLDatatype(), owlDatatypeReferences,
		// false);
		// } else if (owlEntity instanceof OWLAnnotationProperty) {
		// axioms = getAxioms(owlEntity.asOWLAnnotationProperty(),
		// owlAnnotationPropertyReferences, false);
		// } else {
		// axioms = Collections.emptySet();
		// }
	}

	/**
	 * Initial call: Handle should be entity. Recursive calls will ascend only
	 * on OWLClassExpression, OWLObjectPropertyExpression, OWLDataRange,
	 * OWLLiteral and OWLFacetRestriction. Must be called within transaction.
	 * 
	 * @param h
	 *            Handle (Entity intially, ClassExpressionHandle,
	 *            OWLObjectPropertyExpression handle (InverseOf) or DataRange on
	 *            recursion)
	 * @param l
	 *            the list for the axioms. Postcondition: List has no equal
	 *            axioms.
	 * @throws IllegalStateException
	 *             , if an incidenceset contains something else than Axioms or
	 *             ClassExpressions.
	 */
	private void collectOntologyAxiomsRecursive(HGHandle atomHandle, Set<OWLAxiom> axiomList)
	{
		if (DBG)
			System.out.print("*" + recLevel);
		if (atomHandle == null)
		{
			return;
		}
		else
		{
			HGRandomAccessResult<HGHandle> iSetRAR = graph.getIncidenceSet(atomHandle).getSearchResult();
			while (iSetRAR.hasNext())
			{
				HGHandle incidentAtomHandle = iSetRAR.next();
				Object o = graph.get(incidentAtomHandle);
				if (o != null)
				{
					if (o instanceof OWLAxiom)
					{
						if (ontology.isMember(incidentAtomHandle))
						{
							axiomList.add((OWLAxiom) o);
						} // else ignore axiom not part of our onto.
					}
					else
					{
						// we have no cycles up incidence sets starting
						// on an entity.
						if (!(o instanceof VOWLChange))
						{
							if (!(o instanceof OWLClassExpression || o instanceof OWLObjectPropertyExpression
									|| o instanceof OWLDataRange || o instanceof OWLLiteral || o instanceof OWLFacetRestriction
									|| o instanceof OWLAnnotation || o instanceof SWRLObject || o instanceof SWRLConjuction))
							{
								throw new IllegalStateException("We encountered an unexpected object in an incidenceset:" + o);
							}
							recLevel++;
							collectOntologyAxiomsRecursive(incidentAtomHandle, axiomList);
							recLevel--;
						}
					} // else we don't recurse on changes.
				} // else o == null do nothing
			} // for
			iSetRAR.close();
		}
	}

	/**
	 * Tests, if for a given Entity, ClassExpression, ObjectPropExopression or
	 * Datarange at least one axiom can be found, that is a member of the
	 * ontology. Must be called within transaction.
	 * 
	 * @param atomHandle
	 *            non null.
	 * @return true as soon as one axiom in the ontology is found.
	 */
	private boolean hasOntologyAxiomsRecursive(HGHandle atomHandle)
	{
		if (DBG)
			System.out.print("*" + recLevel);
		HGRandomAccessResult<HGHandle> iSetRAR = graph.getIncidenceSet(atomHandle).getSearchResult();
		while (iSetRAR.hasNext())
		{
			HGHandle incidentAtomHandle = iSetRAR.next();
			Object o = graph.get(incidentAtomHandle);
			if (o != null)
			{
				if (o instanceof OWLAxiom)
				{
					if (ontology.isMember(incidentAtomHandle))
					{
						if (DBG)
							System.out.println("\r\nFound axiom recursive: " + o);
						return true;
					} // else ignore axiom not part of our onto.
				}
				else
				{
					// we have no cycles up incidence sets starting
					// on an entity.
					if (!(o instanceof VOWLChange))
					{
						if (!(o instanceof OWLClassExpression || o instanceof OWLObjectPropertyExpression
								|| o instanceof OWLDataRange || o instanceof OWLLiteral || o instanceof OWLFacetRestriction
								|| o instanceof OWLAnnotation || o instanceof SWRLObject || o instanceof SWRLConjuction))
						{
							throw new IllegalStateException("We encountered an unexpected object: " + o + "  in incidenceset of "
									+ graph.get(atomHandle));
						}
						recLevel++;
						if (hasOntologyAxiomsRecursive(incidentAtomHandle))
						{
							recLevel--;
							return true;
						}
						recLevel--;
					} // else we don't recurse on changes.
				}
			} // else o == null do nothing
		} // for
		iSetRAR.close();
		return false;
	}

	// hilpold
	public Set<OWLDeclarationAxiom> getDeclarationAxioms(final OWLEntity entity)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLDeclarationAxiom>>()
		{
			public Set<OWLDeclarationAxiom> call()
			{
				// is entity in graph, fail if not?
				final HGHandle entityHandle = graph.getHandle(entity);
				// All links of type OWLDeclarationAxiom in the incidence set of
				// OWLEntity.
				List<OWLDeclarationAxiom> l = ontology.<OWLDeclarationAxiom> getAll(hg.and(hg.type(OWLDeclarationAxiomHGDB.class),
						hg.incident(entityHandle)));
				Set<OWLDeclarationAxiom> s = getReturnSet(l);
				if (l.size() != s.size())
					throw new IllegalStateException("Set contract broken.");
				return s;
			}
		}, HGTransactionConfig.READONLY);
	}

	public Set<OWLImportsDeclaration> getImportsDeclarations()
	{
		// get link by name and link(handle)
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLImportsDeclaration>>()
		{
			public Set<OWLImportsDeclaration> call()
			{
				// 2012.01.25 hilpold New import declaration handling; need GC
				// to collect zero incidence set atoms.
				List<OWLImportsDeclaration> l = ontology.<OWLImportsDeclaration> getAll(hg.type(OWLImportsDeclarationImpl.class)); // BUGFIX
				// 2012.03.01 hilpold BUGFIX old did not find any ever??:
				// hg.typePlus(OWLImportsDeclaration.class));
				Set<OWLImportsDeclaration> s = getReturnSet(l);
				if (l.size() != s.size())
					throw new IllegalStateException("Set contract broken.");
				return s;
			};
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * CALL INSIDE TRANSACTION.
	 * 
	 * @param importDeclaration
	 * @return
	 */
	protected HGHandle findEqualImportsDeclaration(OWLImportsDeclaration importDeclaration)
	{
		List<HGHandle> sr = ontology.findAll(hg.and(hg.type(OWLImportsDeclarationImpl.class), hg.eq(importDeclaration)));
		HGHandle returnValue;
		if (sr.isEmpty())
		{
			returnValue = null;
		}
		else
		{
			returnValue = sr.get(0);
		}
		return returnValue;
	}

	boolean containsImportDeclaration(OWLImportsDeclaration importDeclaration)
	{
		return findEqualImportsDeclaration(importDeclaration) != null;
	}

	public boolean addImportsDeclaration(final OWLImportsDeclaration importDeclaration)
	{
		boolean success = false;
		if (DBG)
			ontology.printGraphStats("Before AddImp");
		success = graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				if (containsImportDeclaration(importDeclaration))
					return false;
				else
				{
					// 2012.01.25 hilpold new import declaration handling
					// without links
					// might already be in graph
					HGHandle importDeclarationHandle = graph.getHandle(importDeclaration);
					if (importDeclarationHandle == null)
					{
						importDeclarationHandle = graph.add(importDeclaration);
					}
					// ImportDeclarationLink link = new
					// ImportDeclarationLink(ontoHandle,
					// importDeclarationHandle);
					// HGHandle linkHandle = graph.add(link);
					ontology.add(importDeclarationHandle);
					// ontology.add(linkHandle);
					return true;
				}
			}
		});
		// ontology.printGraphStats("After  AddImp");
		return success;
	}

	/**
	 * Removes both, the importDeclaration and the link connecting it to
	 * internals from hypergraph.
	 */
	public boolean removeImportsDeclaration(final OWLImportsDeclaration importDeclaration)
	{
		boolean success = graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				boolean success;
				HGHandle importDeclarationHandle;
				importDeclarationHandle = findEqualImportsDeclaration(importDeclaration);
				if (importDeclarationHandle == null)
					return false;
				// if (!containsImportDeclaration(importDeclaration)) {
				// return false;
				// }
				// importDeclarationHandle = graph.getHandle(importDeclaration);
				// if (importDeclarationHandle == null) {
				// throw new
				// IllegalStateException("Contains said fine, but can't get handle.");
				// }
				// if (!ontology.isMember(importDeclarationHandle)) {
				// //try find it's equal and remove it
				//
				// }
				// // 2012.01.25 hilpold New import declaration handling;
				// // need GC to collect zero incidence set atoms.
				// // no more link usage
				success = ontology.remove(importDeclarationHandle);
				return success;
			}
		});
		if (DBG)
			ontology.printGraphStats("After  Remove Import");
		return success;
	}

	public Set<OWLAnnotation> getOntologyAnnotations()
	{
		// return ontologyAnnotationsQuery.findInSet();
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAnnotation>>()
		{
			public Set<OWLAnnotation> call()
			{
				List<OWLAnnotation> l;
				l = ontology.getAll(hg.type(OWLAnnotationHGDB.class));
				Set<OWLAnnotation> s;
				s = getReturnSet(l);
				if (s.size() != l.size())
					throw new IllegalStateException("Set contract broken.");
				return s;
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * CALL INSIDE TRANSACTION.
	 * 
	 * @param ann
	 * @return
	 */
	protected HGHandle findEqualOntologyAnnotation(OWLAnnotation ann)
	{
		List<HGHandle> sr = ontology.findAll(hg.and(hg.type(OWLAnnotationHGDB.class), hg.eq(ann)));
		HGHandle returnValue;
		if (sr.isEmpty())
		{
			returnValue = null;
		}
		else
		{
			returnValue = sr.get(0);
		}
		return returnValue;
	}

	public boolean containsOntologyAnnotation(OWLAnnotation ann)
	{
		return findEqualOntologyAnnotation(ann) != null;
	}

	public boolean addOntologyAnnotation(final OWLAnnotation ann)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				HGHandle annotationHandle = graph.getHandle(ann);
				if (annotationHandle == null)
					throw new IllegalStateException("annotationHandle null");
				boolean contains = containsOntologyAnnotation(ann);
				if (!contains)
				{
					ontology.add(annotationHandle);
				}
				return !contains;
			}
		});
	}

	public boolean removeOntologyAnnotation(final OWLAnnotation ann)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				if (DBG)
					System.out.println("Annotations before remove: " + getOntologyAnnotations().size());
				HGHandle annotationHandle = findEqualOntologyAnnotation(ann);
				if (annotationHandle == null)
				{
					return false;
				}
				// HGHandle annotationHandle = graph.getHandle(ann);
				// if (annotationHandle == null) throw new
				// IllegalStateException("annotationHandle null");
				// boolean doRemove = containsOntologyAnnotation(ann);
				// if (doRemove) {
				boolean success = ontology.remove(annotationHandle);
				// }
				if (DBG)
					System.out.println("Annotations after remove: " + getOntologyAnnotations().size());
				return success;
			}
		});
	}

	public boolean containsAxiom(final OWLAxiom axiom)
	{
		PERFCOUNTER_FIND_AXIOM++;
		if (axiom == null)
			throw new NullPointerException("axiom");
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				HGHandle h = graph.getHandle(axiom);
				if (h != null && ontology.isMember(h))
				{
					PERFCOUNTER_FIND_BY_MEMBERSHIP++;
					return true;
				}
				else
				{
					return findEqualAxiom(axiom, false) != null;
				}
			}
		}, HGTransactionConfig.READONLY);
		// //TODO will not work 2011.10.13; must rely on equals code in axiom
		// HGHandle axiomHandle = graph.getHandle(axiom);
		// // true iff found in graph and in ontology
		// return (axiomHandle == null) ? false : ontology.get(axiomHandle) !=
		// null;
		// // old Set<OWLAxiom> axioms = axiomsByType.get(axiom.getAxiomType());
		// // return axioms != null && axioms.contains(axiom);
	}

	protected OWLAxiom findEqualAxiom(final OWLAxiom axiom, boolean ignoreAnnotations)
	{
		// 2012.04.04 only use hashcode, if ignoreAnnotations is not set.
		// we'll need to implement index and store 2 hashcodes per axiom if we
		// want to
		// be able to ignore annotations and use a hashcode indexer.
		if (USE_CONTAINS_AXIOM_BY_HASHCODE && !ignoreAnnotations)
		{
			return findEqualAxiomByHashCode(axiom, ignoreAnnotations);
		}
		else
		{
			return findEqualAxiomOptimized(axiom, ignoreAnnotations);
		}
	}

	/**
	 * Exploits index on hashCode for axioms. See OWLObjectHGDB OWLAxiomHGDB.
	 * Will not work if ignoreannotations is set.
	 * 
	 * @param axiom
	 * @param ignoreAnnotations
	 * @return
	 */
	protected OWLAxiom findEqualAxiomByHashCode(final OWLAxiom axiom, final boolean ignoreAnnotations)
	{
		if (axiom == null)
			throw new NullPointerException("axiom");
		if (ignoreAnnotations)
			throw new IllegalStateException("Current Hash lookup Implementation fails, ignoreAnnotations is set.");
		return graph.getTransactionManager().ensureTransaction(new Callable<OWLAxiomHGDB>()
		{
			@SuppressWarnings("rawtypes")
			public OWLAxiomHGDB call()
			{
				int findHashCode = axiom.hashCode();
				HGIndexer axiomByHashCodeIndexer = ImplUtils.getAxiomByHashCodeIndexer(graph);
				HGIndex<Integer, HGHandle> index = graph.getIndexManager().getIndex(axiomByHashCodeIndexer);
				// 2012.02.06 hilpold query was much too slow: therefore using
				// index.find
				// List<HGHandle> axiomCandidatesByHash =
				// ontology.findAll(hg.and(
				// hg.typePlus(OWLAxiomHGDB.class),
				// hg.eq("hashCode", findHashCode)));
				HGRandomAccessResult<HGHandle> axiomCandidatesByHash = index.find(findHashCode);
				while (axiomCandidatesByHash.hasNext())
				{
					HGHandle candidateHandle = axiomCandidatesByHash.next();
					if (ontology.isMember(candidateHandle))
					{
						OWLAxiomHGDB candidate = graph.get(candidateHandle);
						PERFCOUNTER_FIND_BY_HASHCODE_EQUALS++;
						if (ignoreAnnotations)
						{
							if (candidate.equalsIgnoreAnnotations(axiom))
							{
								return candidate;
							}
						}
						else
						{
							if (candidate.equals(axiom))
							{
								return candidate;
							}
						}
					}
				}
				// none found
				return null;
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * Finds an equal axiom in the graph. See the equals methods implementation
	 * in Axiom. The method does not test for self.
	 * 
	 * ALLWAYS CALLED WITHIN TRANSACTION.
	 * 
	 * @param axiom
	 *            an axiom object that might be equal to one in the graph.
	 * @return an axiom object that is guaranteed to be in the graph and equal
	 *         to the given axiom.
	 */
	protected OWLAxiom findEqualAxiomOptimized(final OWLAxiom axiom, boolean ignoreAnnotations)
	{
		if (axiom == null)
			throw new NullPointerException("axiom");
		if (axiom.getAxiomType() == AxiomType.DECLARATION)
		{
			// optimized find
			return findEqualDeclarationAxiom((OWLDeclarationAxiom) axiom, ignoreAnnotations);
		}
		else
		{
			if (USE_CONTAINS_AXIOM_BY_IS)
			{
				Set<OWLEntity> signature = axiom.getSignature();
				if (!signature.isEmpty())
				{
					PERFCOUNTER_FIND_BY_SIGNATURE++;
					// Check incidence set of one signature entity!
					return findEqualAxiomBySignature(axiom, signature, ignoreAnnotations);
				}
			}
			PERFCOUNTER_FIND_EQUALS++;
			// signature empty possible?
			// expensive and slow O(N) equals search
			OWLAxiom foundAxiom = null;
			Class<?> hgdbType = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiom.getAxiomType());
			List<OWLAxiomHGDB> axiomsOneTypeInOnto = ontology.getAll(hg.type(hgdbType));
			// expensive scan
			if (ignoreAnnotations)
			{
				// Find by axiom.equalsIgnoreAnnotations (expensive)
				Iterator<OWLAxiomHGDB> i = axiomsOneTypeInOnto.iterator();
				while (foundAxiom == null && i.hasNext())
				{
					OWLAxiomHGDB curAxiom = i.next();
					if (axiom.equalsIgnoreAnnotations(curAxiom))
					{
						foundAxiom = curAxiom;
					}
				}
			}
			else
			{
				// Find by axiom.equal (expensive)
				int i = axiomsOneTypeInOnto.indexOf(axiom);
				if (i != -1)
				{
					foundAxiom = axiomsOneTypeInOnto.get(i);
				}
			}
			return foundAxiom;
		}
	}

	/**
	 * Finds an equal axiom efficiently by using a two step process: <br>
	 * 1. Find an entity with a small incidence set in the signature. 2. use
	 * this entity to BFS for the wanted axiom checking: A) ontology membership,
	 * B) get and is OWLAxiomHGDB instance, C) matching AxiomType, and D) Full
	 * Axiom equals.
	 * 
	 * 
	 * @param axiom
	 * @param signature
	 * @return
	 */
	private OWLAxiomHGDB findEqualAxiomBySignature(OWLAxiom axiom, Set<OWLEntity> signature, boolean ignoreAnnotations)
	{
		if (signature.isEmpty())
			throw new IllegalArgumentException("Find Axiom by signature, signature empty not allowed.");
		// Find signature object with small incidence set.
		IncidenceSet curIS, lookupEntityIS = null;
		HGHandle lookupEntity = null;
		for (OWLEntity e : signature)
		{
			HGHandle eHandle = graph.getHandle(e);
			curIS = graph.getIncidenceSet(eHandle);
			// TODO is.SIZE IS A SLOW OPERATION. CHECK IF ITERATE PERF_SIZE
			// ITERATIONS IS MUCH FASTER.
			if (curIS.size() < PERFORMANCE_INCIDENCE_SET_SIZE)
			{
				lookupEntityIS = curIS;
				lookupEntity = eHandle;
				break;
			}
			else
			{
				// we might have to go through all entities to find min size.
				if (lookupEntityIS == null)
				{
					lookupEntityIS = curIS;
					lookupEntity = eHandle;
					// TODO is.SIZE IS A SLOW OPERATION. CHECK IF ITERATE
					// PERF_SIZE ITERATIONS IS MUCH FASTER.
				}
				else if (lookupEntityIS.size() > curIS.size())
				{
					lookupEntityIS = curIS;
					lookupEntity = eHandle;
				}
			}
		}
		// Find axiom in Ontology and by type BFS => fast, if axiom is not too
		// deep.
		IncidenceSetALGenerator isALG = new IncidenceSetALGenerator(graph);
		HGBreadthFirstTraversal bfsIS = new HGBreadthFirstTraversal(lookupEntity, isALG);
		OWLAxiomHGDB foundAxiom = null;
		while (foundAxiom == null && bfsIS.hasNext())
		{
			Pair<HGHandle, HGHandle> cur = bfsIS.next();
			HGHandle curHandle = cur.getSecond();
			Object candidate = graph.get(curHandle);
			if (candidate instanceof OWLAxiomHGDB)
			{
				OWLAxiomHGDB axiomCandidate = (OWLAxiomHGDB) candidate;
				if (axiomCandidate.getAxiomType().equals(axiom.getAxiomType()))
				{
					// types match, do expensive equals compare, loading all
					// dependent objects.
					PERFCOUNTER_FIND_BY_SIGNATURE_EQUALS++;
					if (ignoreAnnotations)
					{
						// compare excluding annotations
						if (axiom.equalsIgnoreAnnotations(axiomCandidate))
						{
							PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS++;
							if (ontology.isMember(curHandle))
							{
								foundAxiom = axiomCandidate;
							}
						} // else equalsIgnoreAnno false, sth else did not match
					}
					else
					{
						// compare including annotations
						if (axiom.equals(axiomCandidate))
						{
							PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS++;
							if (ontology.isMember(curHandle))
							{
								foundAxiom = axiomCandidate;
							}
						} // else equals false, maybe annotations or sth else
							// did not match
					}
				} // else different types
			} // else no axiomHGDB
		} // while
		return foundAxiom;
	}

	// 2012.02.06 hilpold following is version used for profiling before 11:10
	// AM.
	//
	// /**
	// * Finds an equal axiom efficiently by using a two step process: <br>
	// * 1. Find an entity with a small incidence set in the signature.
	// * 2. use this entity to BFS for the wanted axiom checking:
	// * A) ontology membership,
	// * B) get and is OWLAxiomHGDB instance,
	// * C) matching AxiomType, and
	// * D) Full Axiom equals.
	// *
	// *
	// * @param axiom
	// * @param signature
	// * @return
	// */
	// private OWLAxiomHGDB findEqualAxiomBySignature(OWLAxiom axiom,
	// Set<OWLEntity> signature, boolean ignoreAnnotations) {
	// if (signature.isEmpty()) throw new
	// IllegalArgumentException("Find Axiom by signature, signature empty not allowed.");
	// //Find signature object with small incidence set.
	// IncidenceSet curIS, lookupEntityIS = null;
	// HGHandle lookupEntity = null;
	// for (OWLEntity e : signature) {
	// HGHandle eHandle = graph.getHandle(e);
	// curIS = graph.getIncidenceSet(eHandle);
	// if (curIS.size() < PERFORMANCE_INCIDENCE_SET_SIZE) {
	// lookupEntityIS = curIS;
	// lookupEntity = eHandle;
	// break;
	// } else {
	// //we might have to go through all entities to find min size.
	// if (lookupEntityIS == null) {
	// lookupEntityIS = curIS;
	// lookupEntity = eHandle;
	// } else if (lookupEntityIS.size() > curIS.size()) {
	// lookupEntityIS = curIS;
	// lookupEntity = eHandle;
	// }
	// }
	// }
	// //Find axiom in Ontology and by type BFS => fast, if axiom is not too
	// deep.
	// IncidenceSetALGenerator isALG = new IncidenceSetALGenerator(graph);
	// HGBreadthFirstTraversal bfsIS = new HGBreadthFirstTraversal(lookupEntity,
	// isALG);
	// OWLAxiomHGDB foundAxiom = null;
	// while (foundAxiom == null && bfsIS.hasNext()) {
	// Pair<HGHandle, HGHandle> cur = bfsIS.next();
	// HGHandle curHandle = cur.getSecond();
	// PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS ++;
	// if (ontology.isMember(curHandle)) {
	// //Axioms are members in ontologies. We have a candidate, get it:
	// Object candidate = graph.get(curHandle);
	// if (candidate instanceof OWLAxiomHGDB) {
	// OWLAxiomHGDB axiomCandidate = (OWLAxiomHGDB) candidate;
	// if (axiomCandidate.getAxiomType().equals(axiom.getAxiomType())) {
	// //types match, do expensive equals compare, loading all dependent
	// objects.
	// PERFCOUNTER_FIND_BY_SIGNATURE_EQUALS ++;
	// if (ignoreAnnotations) {
	// // compare excluding annotations
	// if (axiom.equalsIgnoreAnnotations(axiomCandidate)) {
	// foundAxiom = axiomCandidate;
	// } //else equalsIgnoreAnno false, sth else did not match
	// } else {
	// // compare including annotations
	// if (axiom.equals(axiomCandidate)) {
	// foundAxiom = axiomCandidate;
	// } //else equals false, maybe annotations or sth else did not match
	// }
	// } //else different types
	// } //else no axiomHGDB
	// } //else not our member
	// } //while
	// return foundAxiom;
	// }

	/**
	 * ALWAYS CALLED WITHIN TRANSACTION.
	 * 
	 * @param axiom
	 * @return
	 */
	private OWLDeclarationAxiomHGDB findEqualDeclarationAxiom(OWLDeclarationAxiom axiom, boolean ignoreAnnotations)
	{
		// Strategy: get/find Entity by IRI (indexed), find DeclarationAxiom in
		// (direct) incidence set.
		final OWLEntity owlEntity = axiom.getEntity();
		HGHandle owlEntityHandle;
		owlEntityHandle = graph.getHandle(owlEntity);
		if (owlEntityHandle == null)
		{
			owlEntityHandle = hg.findOne(graph, hg.and(hg.type(owlEntity.getClass()), hg.eq("IRI", owlEntity.getIRI())));
		}
		if (owlEntityHandle != null)
		{
			HGRandomAccessResult<HGHandle> iSetRAR = graph.getIncidenceSet(owlEntityHandle).getSearchResult();
			while (iSetRAR.hasNext())
			{
				HGHandle incidentAtomHandle = iSetRAR.next();
				if (ontology.isMember(incidentAtomHandle))
				{
					Object o = graph.get(incidentAtomHandle);
					if (o != null)
					{
						if (o instanceof OWLDeclarationAxiom)
						{
							// TODO 2011.12.08 BUGFIX contains ignores
							// Annotations
							OWLDeclarationAxiomHGDB axO = (OWLDeclarationAxiomHGDB) o;
							if (ignoreAnnotations)
							{
								if (axiom.equalsIgnoreAnnotations(axO))
								{
									return (OWLDeclarationAxiomHGDB) o;
								} // else
							}
							else
							{
								if (axiom.equals(axO))
								{
									return (OWLDeclarationAxiomHGDB) o;
								} // else almost, maybe annotations did not
									// match.
							}
						} // other class
					} // else incidentAtomHandle not in cache!
				}// else not this ontology.
			}
			iSetRAR.close();
		}// else no entity found
		return null;
	}

	public int getAxiomCount()
	{
		long count = ontology.count(hg.typePlus(OWLAxiom.class));
		if (count > Integer.MAX_VALUE)
		{
			throw new ArithmeticException("Got long value to big for int");
		}
		return (int) count;
		// old int count = 0;
		// for (AxiomType<?> type : AXIOM_TYPES) {
		// Set<OWLAxiom> axiomSet = axiomsByType.get(type);
		// if (axiomSet != null) {
		// count += axiomSet.size();
		// }
		// }
		// return count;
	}

	public Set<OWLAxiom> getAxioms()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAxiom>>()
		{
			public Set<OWLAxiom> call()
			{
				List<OWLAxiom> allAxiomsList = ontology.getAll(hg.typePlus(OWLAxiom.class));
				Set<OWLAxiom> s = getReturnSet(allAxiomsList);
				if (s.size() != allAxiomsList.size())
					throw new IllegalStateException("Set contract broken.");
				return s;
			}
		}, HGTransactionConfig.READONLY);
		// Set<OWLAxiom> axioms = createSet();
		// for (AxiomType<?> type : AXIOM_TYPES) {
		// Set<OWLAxiom> owlAxiomSet = axiomsByType.get(type);
		// if (owlAxiomSet != null) {
		// axioms.addAll(owlAxiomSet);
		// }
		// }
		// return axioms;
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType)
	{
		return getAxiomsInternal(axiomType);
	}

	/**
	 * Gets the axioms which are of the specified type, possibly from the
	 * imports closure of this ontology
	 * 
	 * @param axiomType
	 *            The type of axioms to be retrieved.
	 * @param includeImportsClosure
	 *            if <code>true</code> then axioms of the specified type will
	 *            also be retrieved from the imports closure of this ontology,
	 *            if <code>false</code> then axioms of the specified type will
	 *            only be retrieved from this ontology.
	 * @return A set containing the axioms which are of the specified type. The
	 *         set that is returned is a copy of the axioms in the ontology (and
	 *         its imports closure) - it will not be updated if the ontology
	 *         changes.
	 */
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, Collection<OWLOntology> importsClosure)
	{
		if (importsClosure == null || importsClosure.size() == 0)
		{
			return getAxioms(axiomType);
		}
		Set<T> result = createSet();
		for (OWLOntology ont : importsClosure)
		{
			result.addAll(ont.getAxioms(axiomType));
		}
		return result;
	}

	public <T extends OWLAxiom> int getAxiomCount(final AxiomType<T> axiomType)
	{
		long axiomsOneTypeCount = 0;
		Class<? extends OWLAxiomHGDB> hgdbAxiomClass = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiomType);
		axiomsOneTypeCount = ontology.count(hg.type(hgdbAxiomClass));
		if (axiomsOneTypeCount > Integer.MAX_VALUE)
			throw new ArithmeticException("long Count > int Max");
		return (int) axiomsOneTypeCount;
		// Set<OWLAxiom> axioms = axiomsByType.get(axiomType);
		// if (axioms == null) {
		// return 0;
		// }
		// return axioms.size();
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms()
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLLogicalAxiom>>()
		{
			public Set<OWLLogicalAxiom> call()
			{
				List<OWLLogicalAxiom> l = ontology.getAll(getLogicalAxiomQuery());
				Set<OWLLogicalAxiom> s = getReturnSet(l);
				if (s.size() != l.size())
					throw new IllegalStateException("Set contract broken.");
				return s;
			}
		}, HGTransactionConfig.READONLY);
		// for (AxiomType<?> type : AXIOM_TYPES) {
		// if (type.isLogical()) {
		// Set<OWLAxiom> axiomSet = axiomsByType.get(type);
		// if (axiomSet != null) {
		// for (OWLAxiom ax : axiomSet) {
		// axioms.add((OWLLogicalAxiom) ax);
		// }
		// }
		// }
		// }
	}

	public int getLogicalAxiomCount()
	{
		long count = ontology.count(getLogicalAxiomQuery());
		if (count > Integer.MAX_VALUE)
			throw new ArithmeticException("count > int max");
		return (int) count;
		// int count = 0;
		// for (AxiomType<?> type : AXIOM_TYPES) {
		// if (type.isLogical()) {
		// Set<OWLAxiom> axiomSet = axiomsByType.get(type);
		// if (axiomSet != null) {
		// count += axiomSet.size();
		// }
		// }
		// }
		// return count;
	}

	protected HGQueryCondition getLogicalAxiomQuery()
	{
		Or logicalAxQuery = new Or();
		Set<Class<? extends OWLAxiomHGDB>> classes = AxiomTypeToHGDBMap.getLogicalAxiomTypesHGDB();
		for (Class<? extends OWLAxiomHGDB> c : classes)
		{
			logicalAxQuery.add(new AtomTypeCondition(c));
		}
		return logicalAxQuery;
	}

	public void addAxiomsByType(final AxiomType<?> type, final OWLAxiom axiom)
	{
		if (DBG)
		{
			log.info("ADD Axiom: " + axiom.getClass().getSimpleName() + "Type: " + type + " Hash: " + axiom.hashCode() + " Ax: "
					+ axiom);
		}
		if (AxiomTypeToHGDBMap.getAxiomClassHGDB(type) != null)
		{
			graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
			{
				public Boolean call()
				{
					OWLAxiomHGDB axiomHGDB = (OWLAxiomHGDB) axiom;
					// 2012.02.06 hilpold hashCode indexed
					// ensure hashCode Calculated before storage
					axiomHGDB.hashCode();
					axiomHGDB.setLoadAnnotations(false);
					if (DBG)
						ontology.printGraphStats("Before AddAxiom");
					// 2011.10.06 hilpold adding to graph here instead of
					// previously in Datafactory
					// 2012.01.05 hilpold copy axioms operation from onto 1 to
					// onto 2 will have axiom in graph already:
					// Therefore we need to check, if it's already in graph?
					HGHandle axiomHandle = graph.getHandle(axiom);
					if (axiomHandle == null)
					{
						// TODO here we could look up an existing axiom in the
						// whole graph (e.g. by hashcode), if we want shared
						// axioms between ontologies.
						axiomHandle = graph.add(axiom);
					}
					ontology.add(axiomHandle);
					//
					// OWLAnnotation handling (== AxiomAnnotatedBy links
					// added to graph)
					//
					Set<OWLAnnotation> annos = axiom.getAnnotations();
					for (OWLAnnotation anno : annos)
					{
						HGHandle annoHandle = graph.getHandle(anno);
						if (annoHandle == null)
							throw new IllegalStateException("AnnotationHandle null.");
						if (hg.findOne(graph, hg.and(hg.type(AxiomAnnotatedBy.class), hg.orderedLink(axiomHandle, annoHandle))) != null)
						{
							// link exists between the given
							// axiom and current annotation.
							// 2012.01.05 that's ok, might be an undo anyways.
							// throw new IllegalStateException(
							// "Added axiom with existing AxiomAnnotatedLink to annotation "
							// + anno);
						}
						else
						{
							// Link does not exist -> create
							AxiomAnnotatedBy link = new AxiomAnnotatedBy(axiomHandle, annoHandle);
							graph.add(link);
						}
					}
					if (DBG)
						ontology.printGraphStats("After AddAxiom");
					return true;
				}
			});
		}
	}

	public void removeAxiomsByType(final AxiomType<?> type, final OWLAxiom axiom)
	{
		if (DBG)
		{
			log.info("REMOVE Axiom: " + axiom.getClass().getSimpleName() + " Type: " + type);
		}
		// if (AxiomTypeToHGDBMap.getAxiomClassHGDB(type) != null) {
		graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				boolean removedSuccess = false;
				if (DBG)
					ontology.printGraphStats("Before RemoveAxiom");
				// get the axiom handle or find an equal axiom in the
				// ontology.
				HGHandle axiomHandle = graph.getHandle(axiom);
				if (axiomHandle == null || ontology.get(axiomHandle) == null)
				{
					// Axiom null or not in ontology, try find an equal one
					// in ontology
					axiomHandle = graph.getHandle(findEqualAxiom(axiom, false));
				}
				if (axiomHandle != null)
				{
					// //
					// // 1. remove AxiomAnnotatedBy links from graph
					// //
					// List<HGHandle> annoLinkHandles = hg.findAll(graph,
					// hg.and(hg.type(AxiomAnnotatedBy.class),
					// hg.incident(axiomHandle)));
					// for (HGHandle annoLinkHandle : annoLinkHandles) {
					// graph.remove(annoLinkHandle);
					// }
					// //
					// // 2. Remove Axiom from Ontology and graph.
					// //
					// ontology.remove(axiomHandle);
					// removedSuccess = graph.remove(axiomHandle);
					removedSuccess = ontology.remove(axiomHandle);
					// 2012.01.05 hilpold we now keep annolinks and the axiom in
					// graph for GC later
					// Axiom annotations belong to the axiom and not to the
					// ontology.
					// If we would remove them here, we could not do an undo.
					if (DBG)
						ontology.printGraphStats("After  RemoveAxiom");
					// if it pointed to an entity, entity incidence is -1
				}
				return removedSuccess;
			}
		});
		// } else {
		// throw new IllegalStateException("Unknown axiom : " + axiom);
		// // log.warning("NOT YET IMPLEMENTED: " +
		// axiom.getClass().getSimpleName());
		// // removeAxiomFromSet(type, axiomsByType, axiom, true);
		// }
	}

	// 2011.11.17 public Map<OWLAxiom, Set<OWLAxiom>>
	// getLogicalAxiom2AnnotatedAxiomMap() {
	// return new HashMap<OWLAxiom,
	// Set<OWLAxiom>>(this.logicalAxiom2AnnotatedAxiomMap);
	// }
	//
	// public Set<OWLAxiom> getLogicalAxiom2AnnotatedAxiom(OWLAxiom ax) {
	// //2011.11.17 return
	// getReturnSet(logicalAxiom2AnnotatedAxiomMap.get(ax.getAxiomWithoutAnnotations()));
	// Set<OWLAxiom> s = new HashSet<OWLAxiom>();
	// return s;
	// }

	// public void addLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
	// //2011.11.17 addToIndexedSet(ax.getAxiomWithoutAnnotations(),
	// logicalAxiom2AnnotatedAxiomMap, ax);
	// }

	// public void removeLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
	// //2011.11.17 removeAxiomFromSet(ax.getAxiomWithoutAnnotations(),
	// logicalAxiom2AnnotatedAxiomMap, ax, true);
	// }

	//
	// public boolean containsLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
	// //2011.11.17 return
	// logicalAxiom2AnnotatedAxiomMap.containsKey(ax.getAxiomWithoutAnnotations());
	// return false;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#containsAxiomIgnoreAnnotations
	 * (org.semanticweb.owlapi.model.OWLAxiom)
	 */
	@Override
	public boolean containsAxiomIgnoreAnnotations(final OWLAxiom axiom)
	{
		if (axiom == null)
			throw new NullPointerException("axiom");
		PERFCOUNTER_FIND_AXIOM++;
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				HGHandle h = graph.getHandle(axiom);
				if (h != null && ontology.isMember(h))
				{
					PERFCOUNTER_FIND_BY_MEMBERSHIP++;
					return true;
				}
				else
				{
					return findEqualAxiom(axiom, true) != null;
				}
				// return h != null ? ontology.isMember(h) ||
				// findEqualAxiom(axiom, true) != null : findEqualAxiom(axiom,
				// true) != null;
			}
		}, HGTransactionConfig.READONLY);
		// 2011.12.29
		// // TODO this is expensive !! Maybe implement a complex search
		// condition
		// // based on equals in each axiom type.
		// return graph.getTransactionManager().ensureTransaction(new
		// Callable<Boolean>() {
		// public Boolean call() {
		// boolean foundAxiom = false;
		// Class<?> hg dbType =
		// AxiomTypeToHGDBMap.getAxiomClassHGDB(axiom.getAxiomType());
		// List<OWLAxiomHGDB> axiomsOneTypeInOnto =
		// ontology.getAll(hg.type(hgdbType));
		// // Find by axiom.equal (expensive)
		// Iterator<OWLAxiomHGDB> i = axiomsOneTypeInOnto.iterator();
		// while (!foundAxiom && i.hasNext()) {
		// foundAxiom = axiom.equalsIgnoreAnnotations(i.next());
		// }
		// return foundAxiom;
		// }}, HGTransactionConfig.READONLY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#getAxiomsIgnoreAnnotations
	 * (org.semanticweb.owlapi.model.OWLAxiom)
	 */
	@Override
	public Set<OWLAxiom> getAxiomsIgnoreAnnotations(final OWLAxiom axiom)
	{
		if (axiom == null)
			throw new NullPointerException("axiom");
		// TODO this is expensive !! Maybe implement a complex search condition
		// based on equals in each axiom type.
		return graph.getTransactionManager().ensureTransaction(new Callable<Set<OWLAxiom>>()
		{
			public Set<OWLAxiom> call()
			{
				Set<OWLAxiom> foundAxioms = new HashSet<OWLAxiom>();
				Class<?> hgdbType = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiom.getAxiomType());
				List<OWLAxiomHGDB> axiomsOneTypeInOnto = ontology.getAll(hg.type(hgdbType));
				// Find by axiom.equal (expensive)
				Iterator<OWLAxiomHGDB> i = axiomsOneTypeInOnto.iterator();
				while (i.hasNext())
				{
					OWLAxiomHGDB curAxiom = i.next();
					if (axiom.equalsIgnoreAnnotations(curAxiom))
					{
						foundAxioms.add(curAxiom);
					}
				}
				return foundAxioms;
			}
		}, HGTransactionConfig.READONLY);
	}

	/**
	 * THIS IS IN THERE (Based on old ChangeAxiomVisitor behaviour): <br>
	 * A)OWLSubClassOfAxiom, where axiom.getSubClass().isAnonymous() <br>
	 * B)OWLDisjointClassesAxiom, where all axiom.getClassExpressions() are
	 * anonymous. <br>
	 * C) OWLEquivalentClassesAxiom, where all axiom.getClassExpressions() are
	 * anonymous.
	 */
	public Set<OWLClassAxiom> getGeneralClassAxioms()
	{
		List<OWLClassAxiom> s = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLClassAxiom>>()
		{
			public List<OWLClassAxiom> call()
			{
				List<OWLClassAxiom> generalClassAxioms = new LinkedList<OWLClassAxiom>();
				List<OWLSubClassOfAxiomHGDB> subclassAxioms;
				List<OWLDisjointClassesAxiomHGDB> disjointClassesAxioms;
				List<OWLEquivalentClassesAxiomHGDB> equivalentClassesAxioms;
				subclassAxioms = ontology.getAll(hg.type(OWLSubClassOfAxiomHGDB.class));
				disjointClassesAxioms = ontology.getAll(hg.type(OWLDisjointClassesAxiomHGDB.class));
				equivalentClassesAxioms = ontology.getAll(hg.type(OWLEquivalentClassesAxiomHGDB.class));
				for (OWLSubClassOfAxiomHGDB axiom : subclassAxioms)
				{
					if (axiom.getSubClass().isAnonymous())
						generalClassAxioms.add(axiom);
				}
				for (OWLDisjointClassesAxiomHGDB axiom : disjointClassesAxioms)
				{
					if (allAnonymous(axiom.getClassExpressions()))
						generalClassAxioms.add(axiom);
				}
				for (OWLEquivalentClassesAxiomHGDB axiom : equivalentClassesAxioms)
				{
					if (allAnonymous(axiom.getClassExpressions()))
						generalClassAxioms.add(axiom);
				}
				return generalClassAxioms;
			};

			public boolean allAnonymous(Set<OWLClassExpression> classExpressions)
			{
				boolean anon = true;
				Iterator<OWLClassExpression> i = classExpressions.iterator();
				while (anon && i.hasNext())
				{
					anon = i.next().isAnonymous();
				}
				return anon;
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(s);
		// return getReturnSet(this.generalClassAxioms);
	}

	// public void addGeneralClassAxioms(OWLClassAxiom ax) {
	// this.generalClassAxioms.add(ax);
	// }
	//
	// public void removeGeneralClassAxioms(OWLClassAxiom ax) {
	// this.generalClassAxioms.remove(ax);
	// }

	public Set<OWLSubPropertyChainOfAxiom> getPropertyChainSubPropertyAxioms()
	{
		return getAxiomsInternal(AxiomType.SUB_PROPERTY_CHAIN_OF);
		// return getReturnSet(this.propertyChainSubPropertyAxioms);
	}

	// public void addPropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom
	// ax) {
	// this.propertyChainSubPropertyAxi oms.add(ax);
	// }

	// public void
	// removePropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax) {
	// this.propertyChainSubPropertyAxioms.remove(ax);
	// }

	// public Map<OWLClass, Set<OWLAxiom>> getOwlClassReferences() {
	// return new HashMap<OWLClass, Set<OWLAxiom>>(this.owlClassReferences);
	// }

	// public void removeOwlClassReferences(OWLClass c, OWLAxiom ax) {
	// removeAxiomFromSet(c, owlClassReferences, ax, true);
	// }
	//
	// public void addOwlClassReferences(OWLClass c, OWLAxiom ax) {
	// addToIndexedSet(c, owlClassReferences, ax);
	// }

	// public boolean containsOwlClassReferences(OWLClass c) {
	// // return this.owlClassReferences.containsKey(c);
	// }

	// public Map<OWLObjectProperty, Set<OWLAxiom>>
	// getOwlObjectPropertyReferences() {
	// return new HashMap<OWLObjectProperty,
	// Set<OWLAxiom>>(this.owlObjectPropertyReferences);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlClass(org.
	 * semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public boolean containsOwlClass(final OWLClass c)
	{
		return containsOWLEntityOntology(c.getIRI(), OWLClassHGDB.class);
	}

	// public void removeOwlObjectPropertyReferences(OWLObjectProperty p,
	// OWLAxiom ax) {
	// removeAxiomFromSet(p, owlObjectPropertyReferences, ax, true);
	// }
	//
	// public void addOwlObjectPropertyReferences(OWLObjectProperty p, OWLAxiom
	// ax) {
	// addToIndexedSet(p, owlObjectPropertyReferences, ax);
	// }

	// public boolean containsOwlObjectPropertyReferences(OWLObjectProperty c) {
	// // return this.owlObjectPropertyReferences.containsKey(c);
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlObjectProperty
	 * (org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public boolean containsOwlObjectProperty(final OWLObjectProperty c)
	{
		return containsOWLEntityOntology(c.getIRI(), OWLObjectPropertyHGDB.class);
	}

	// public Map<OWLDataProperty, Set<OWLAxiom>> getOwlDataPropertyReferences()
	// {
	// return new HashMap<OWLDataProperty,
	// Set<OWLAxiom>>(this.owlDataPropertyReferences);
	// }

	// public void removeOwlDataPropertyReferences(OWLDataProperty c, OWLAxiom
	// ax) {
	// removeAxiomFromSet(c, owlDataPropertyReferences, ax, true);
	// }
	//
	//
	// public void addOwlDataPropertyReferences(OWLDataProperty c, OWLAxiom ax)
	// {
	// addToIndexedSet(c, owlDataPropertyReferences, ax);
	// }

	// public boolean containsOwlDataPropertyReferences(OWLDataProperty c) {
	// // return this.owlDataPropertyReferences.containsKey(c);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlDataProperty
	 * (org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public boolean containsOwlDataProperty(final OWLDataProperty c)
	{
		return containsOWLEntityOntology(c.getIRI(), OWLDataPropertyHGDB.class);
	}

	/**
	 * Contains by IRI and exact type (HGDB class).
	 * 
	 * @param iri
	 *            an IRI of the Entity
	 * @param hgdbType
	 *            an exact storage (HGDB) class type.
	 * @return
	 */
	boolean containsOWLEntityOntology(final IRI iri, final Class<?> hgdbType)
	{
		if (!OWLObjectHGDB.class.isAssignableFrom(hgdbType))
			throw new IllegalArgumentException("Only subclasses of OWLObjectHGDB allowed! Was:" + hgdbType);
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				return hg.findOne(graph, hg.and(hg.type(hgdbType), hg.eq("IRI", iri), new SubgraphMemberCondition(ontoHandle))) != null;
			}
		}, HGTransactionConfig.READONLY);
	}

	// public Map<OWLNamedIndividual, Set<OWLAxiom>>
	// getOwlIndividualReferences() {
	// return this.owlIndividualReferences;
	// }

	// public void removeOwlIndividualReferences(OWLNamedIndividual c, OWLAxiom
	// ax) {
	// removeAxiomFromSet(c, owlIndividualReferences, ax, true);
	// }

	// public void addOwlIndividualReferences(OWLNamedIndividual c, OWLAxiom ax)
	// {
	// addToIndexedSet(c, owlIndividualReferences, ax);
	// }

	// hilpold public boolean containsOwlIndividualReferences(OWLNamedIndividual
	// c) {
	// return this.owlIndividualReferences.containsKey(c);
	// }

	public boolean containsOwlNamedIndividual(final IRI individualIRI)
	{
		return containsOWLEntityOntology(individualIRI, OWLNamedIndividualHGDB.class);
	}

	// ------------------------------------------------------------------------------------
	// OWL_ENTITY BASIC QUERIES
	//

	public Set<OWLAnnotationProperty> getOwlAnnotationProperties()
	{
		List<OWLAnnotationProperty> l;
		l = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLAnnotationProperty>>()
		{
			public List<OWLAnnotationProperty> call()
			{
				return hg.getAll(graph, hg.and(hg.type(OWLAnnotationPropertyHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLClass> getOwlClasses()
	{
		List<OWLClass> l;
		l = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLClass>>()
		{
			public List<OWLClass> call()
			{
				return hg.getAll(graph, hg.and(hg.type(OWLClassHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLDatatype> getOwlDatatypes()
	{
		List<OWLDatatype> l;
		l = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLDatatype>>()
		{
			public List<OWLDatatype> call()
			{
				return hg.getAll(graph, hg.and(hg.type(OWLDatatypeHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getOwlIndividuals()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Set<OWLNamedIndividual> getOwlNamedIndividuals()
	{
		// List<OWLNamedIndividual> l;
		// l = graph.getTransactionManager().ensureTransaction(new
		// Callable<List<OWLNamedIndividual>>() {
		// public List<OWLNamedIndividual> call() {
		// return hg.getAll(graph,
		// hg.and(hg.type(OWLNamedIndividualHGDB.class), new
		// SubgraphMemberCondition(ontoHandle)));
		// }
		// }, HGTransactionConfig.READONLY);
		HGQueryCondition cond = OWLDataFactoryHGDB.get(graph).ignoreOntologyScope() ? hg.type(OWLNamedIndividualHGDB.class) : hg
				.and(hg.type(OWLNamedIndividualHGDB.class), hg.memberOf(ontoHandle));
		return getReturnSet((List<OWLNamedIndividual>) (List<?>) hg.getAll(graph, cond));
	}

	public Set<OWLDataProperty> getOwlDataProperties()
	{
		List<OWLDataProperty> l;
		l = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLDataProperty>>()
		{
			public List<OWLDataProperty> call()
			{
				return hg.getAll(graph, hg.and(hg.type(OWLDataPropertyHGDB.class), hg.memberOf(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLObjectProperty> getOwlObjectProperties()
	{
		List<OWLObjectProperty> l;
		l = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLObjectProperty>>()
		{
			public List<OWLObjectProperty> call()
			{
				return hg.getAll(graph, hg.and(hg.type(OWLObjectPropertyHGDB.class), hg.memberOf(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	//
	// END OWL_ENTITY BASIC QUERIES
	// ------------------------------------------------------------------------------------

	// public Map<OWLAnonymousIndividual, Set<OWLAxiom>>
	// getOwlAnonymousIndividualReferences() {
	// return new HashMap<OWLAnonymousIndividual,
	// Set<OWLAxiom>>(this.owlAnonymousIndividualReferences);
	// }

	// public void removeOwlAnonymousIndividualReferences(OWLAnonymousIndividual
	// c, OWLAxiom ax) {
	// removeAxiomFromSet(c, owlAnonymousIndividualReferences, ax, true);
	// }
	//
	// public void addOwlAnonymousIndividualReferences(OWLAnonymousIndividual c,
	// OWLAxiom ax) {
	// addToIndexedSet(c, owlAnonymousIndividualReferences, ax);
	// }
	//
	// public boolean
	// containsOwlAnonymousIndividualReferences(OWLAnonymousIndividual c) {
	// return this.owlAnonymousIndividualReferences.containsKey(c);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#getOwlAnonymousIndividuals
	 * ()
	 */
	@Override
	public Set<OWLAnonymousIndividual> getOwlAnonymousIndividuals()
	{
		List<OWLAnonymousIndividual> l;
		l = graph.getTransactionManager().ensureTransaction(new Callable<List<OWLAnonymousIndividual>>()
		{
			public List<OWLAnonymousIndividual> call()
			{
				return hg.getAll(graph, hg.and(hg.type(OWLAnonymousIndividualHGDB.class), hg.memberOf(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlAnonymousIndividual
	 * (org.semanticweb.owlapi.model.OWLAnonymousIndividual)
	 */
	@Override
	public boolean containsOwlAnonymousIndividual(final OWLAnonymousIndividual c)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				return hg.findOne(graph, hg.and(hg.type(OWLAnonymousIndividualHGDB.class),
				// equals is defined as equal id strings.
						hg.eq(c), new SubgraphMemberCondition(ontoHandle))) != null;
			}
		}, HGTransactionConfig.READONLY);
	}

	// public Map<OWLDatatype, Set<OWLAxiom>> getOwlDatatypeReferences() {
	// return new HashMap<OWLDatatype,
	// Set<OWLAxiom>>(this.owlDatatypeReferences);
	// }

	// public void removeOwlDatatypeReferences(OWLDatatype c, OWLAxiom ax) {
	// removeAxiomFromSet(c, owlDatatypeReferences, ax, true);
	// }
	//
	// public void addOwlDatatypeReferences(OWLDatatype c, OWLAxiom ax) {
	// addToIndexedSet(c, owlDatatypeReferences, ax);
	// }

	// public boolean containsOwlDatatypeReferences(OWLDatatype c) {
	// return this.owlDatatypeReferences.containsKey(c);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlDatatype(org
	 * .semanticweb.owlapi.model.OWLDatatype)
	 */
	@Override
	public boolean containsOwlDatatype(OWLDatatype c)
	{
		return containsOWLEntityOntology(c.getIRI(), OWLDatatypeHGDB.class);
	}

	// public Map<OWLAnnotationProperty, Set<OWLAxiom>>
	// getOwlAnnotationPropertyReferences() {
	// return new HashMap<OWLAnnotationProperty, Set<OWLAxiom>>(
	// this.owlAnnotationPropertyReferences);
	// }

	// public void removeOwlAnnotationPropertyReferences(OWLAnnotationProperty
	// c, OWLAxiom ax) {
	// removeAxiomFromSet(c, owlAnnotationPropertyReferences, ax, true);
	// }
	//
	// public void addOwlAnnotationPropertyReferences(OWLAnnotationProperty c,
	// OWLAxiom ax) {
	// addToIndexedSet(c, owlAnnotationPropertyReferences, ax);
	// }

	// public boolean
	// containsOwlAnnotationPropertyReferences(OWLAnnotationProperty c) {
	// return this.owlAnnotationPropertyReferences.containsKey(c);
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlAnnotationProperty
	 * (org.semanticweb.owlapi.model.OWLAnnotationProperty)
	 */
	@Override
	public boolean containsOwlAnnotationProperty(OWLAnnotationProperty c)
	{
		return containsOWLEntityOntology(c.getIRI(), OWLAnnotationPropertyHGDB.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#hasReferencingAxioms(org
	 * .semanticweb.owlapi.model.OWLEntity)
	 */
	@Override
	public boolean hasReferencingAxioms(OWLEntity entity)
	{
		final HGHandle h = graph.getHandle(entity);
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				return hasOntologyAxiomsRecursive(h);
			}
		}, HGTransactionConfig.READONLY);
	}

	@Override
	public boolean hasReferencingAxioms(final HGHandle entity)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<Boolean>()
		{
			public Boolean call()
			{
				return hasOntologyAxiomsRecursive(entity);
			}
		}, HGTransactionConfig.READONLY);
	}

	// /**
	// * This is an expensive operation, because the hashmap has to be created.
	// * Maybe the hashmap should be lazy and backed by HG? (Protege never calls
	// * this.)
	// */
	// public Map<OWLEntity, Set<OWLDeclarationAxiom>> getDeclarationsByEntity()
	// {
	// // return new HashMap<OWLEntity, Set<OWLDeclarationAxiom>>(
	// // this.declarationsByEntity);
	// // hilpold - for now.
	// return null;
	// }

	// public void removeDeclarationsByEntity(OWLEntity c, OWLDeclarationAxiom
	// ax) {
	// removeAxiomFromSet(c, declarationsByEntity, ax, true);
	// }

	// public void addDeclarationsByEntity(OWLEntity c, OWLDeclarationAxiom ax)
	// {
	// throw new
	// IllegalArgumentException("Operation no longer supported; Interface will be changed.");
	// //addToIndexedSet(c, declarationsByEntity, ax);
	// }

	// public boolean containsDeclarationsByEntity(OWLEntity c) {
	// // return this.declarationsByEntity.containsKey(c);
	// return false;
	// }

	public static String toStringPerfCounters()
	{
		return "---- Performance counters -----" + "\n Find axiom calls total         : " + PERFCOUNTER_FIND_AXIOM
				+ "\n   axiom was a member           : " + PERFCOUNTER_FIND_BY_MEMBERSHIP + "\n   used by signature            : "
				+ PERFCOUNTER_FIND_BY_SIGNATURE + "\n   had to use slow equals scan  : " + PERFCOUNTER_FIND_EQUALS
				+ "\n By Hashcode test equals        : " + PERFCOUNTER_FIND_BY_HASHCODE_EQUALS
				+ "\n By Signature test onto member  : " + PERFCOUNTER_FIND_BY_SIGNATURE_ONTOLOGY_MEMBERS
				+ "\n By Signature test slow equals  : " + PERFCOUNTER_FIND_BY_SIGNATURE_EQUALS
				+ "\n ---------------------------------\n";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getPrefixes()
	 */
	@Override
	public Map<String, String> getPrefixes()
	{
		Map<String, String> prefixMap = new HashMap<String, String>(9);
		List<PrefixHGDB> prefixes = ontology.getAll(hg.type(PrefixHGDB.class));
		for (PrefixHGDB prefix : prefixes)
		{
			if (prefixMap.put(prefix.getPrefixName(), prefix.getNamespace()) != null)
			{
				throw new IllegalStateException("Key Set contract broken");
			}
		}
		return prefixMap;
	}

	public void setPrefixesFrom(final Map<String, String> prefixMap)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				final Set<String> oldPrefixes = getPrefixes().keySet();
				for (String oldPrefix : oldPrefixes)
				{
					removePrefix(oldPrefix);
				}
				for (Map.Entry<String, String> newPrefix : prefixMap.entrySet())
				{
					setPrefix(newPrefix.getKey(), newPrefix.getValue());
				}
				return null;
			};
		}, HGTransactionConfig.DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#getPrefix(java.lang.String
	 * )
	 */
	@Override
	public String getPrefix(final String prefixName)
	{
		PrefixHGDB prefix = ontology.getOne(hg.and(hg.eq("prefixName", prefixName), hg.type(PrefixHGDB.class)));
		return prefix == null ? null : prefix.getNamespace();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#setPrefix(java.lang.String
	 * , java.lang.String)
	 */
	@Override
	public String setPrefix(final String prefixName, final String namespace)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				boolean needsAdd = true;
				HGHandle oldPrefixHandle = ontology.findOne(hg.and(hg.eq("prefixName", prefixName), hg.type(PrefixHGDB.class)));
				String oldNamespace = null;
				if (oldPrefixHandle != null)
				{
					oldNamespace = graph.<PrefixHGDB> get(oldPrefixHandle).getNamespace();
					if (!oldNamespace.equals(namespace))
					{
						ontology.removeGlobally(oldPrefixHandle);
					}
					else
					{
						needsAdd = false;
					}
				}
				if (needsAdd)
				{
					PrefixHGDB prefix = new PrefixHGDB(prefixName, namespace);
					ontology.add(graph.add(prefix));
				}
				return oldNamespace;
			};
		}, HGTransactionConfig.DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.app.owl.HGDBOntologyInternals#removePrefix(java.lang
	 * .String)
	 */
	@Override
	public String removePrefix(final String prefixName)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<String>()
		{
			public String call()
			{
				HGHandle oldPrefixHandle = ontology.findOne(hg.and(hg.eq("prefixName", prefixName), hg.type(PrefixHGDB.class)));
				String oldNamespace = null;
				if (oldPrefixHandle != null)
				{
					oldNamespace = graph.<PrefixHGDB> get(oldPrefixHandle).getNamespace();
					ontology.removeGlobally(oldPrefixHandle);
				}
				return oldNamespace;
			};
		}, HGTransactionConfig.DEFAULT);
	}
}

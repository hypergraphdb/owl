package org.hypergraphdb.app.owl;

import static org.semanticweb.owlapi.util.CollectionFactory.createSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.app.owl.core.AbstractInternalsHGDB;
import org.hypergraphdb.app.owl.core.AxiomTypeToHGDBMap;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLDeclarationAxiomHGDB;
import org.hypergraphdb.app.owl.type.link.ImportDeclarationLink;
import org.hypergraphdb.query.AtomTypeCondition;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.query.Or;
import org.hypergraphdb.query.SubgraphMemberCondition;
import org.hypergraphdb.transaction.HGTransactionConfig;
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
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

/**
 * HGDBOntologyInternalsImpl.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyInternalsImpl extends AbstractInternalsHGDB {

	public static boolean DBG = true; // Switches LOG string creation on or off.

	private int recLevel = 0; //recursion leve/depth for getReferencingAxioms.

	protected Logger log = Logger.getLogger(this.getClass().getCanonicalName());

	static {
		// TODO Disable force assertions before release.
		boolean assertsEnabled = false;
		assert assertsEnabled = true; // force assertions.
		if (!assertsEnabled) {
			Logger.getLogger(HGDBOntologyInternalsImpl.class.getCanonicalName()).severe(
					"Asserts disabled for HGDBOntologyInternalsImpl");
			// throw new
			// RuntimeException("We need Asserts to be enabled. Use: java -ea:org.hypergraphdb.app.owl...");
		}
	}

	// hilpold protected Set<OWLImportsDeclaration> importsDeclarations;
	protected Set<OWLAnnotation> ontologyAnnotations; // recursive??
	// protected Map<AxiomType<?>, Set<OWLAxiom>> axiomsByType; removed
	// 2011.10.06
	protected Map<OWLAxiom, Set<OWLAxiom>> logicalAxiom2AnnotatedAxiomMap;
	protected Set<OWLClassAxiom> generalClassAxioms;
	protected Set<OWLSubPropertyChainOfAxiom> propertyChainSubPropertyAxioms;
	// protected Map<OWLClass, Set<OWLAxiom>> owlClassReferences;
	// protected Map<OWLObjectProperty, Set<OWLAxiom>>
	// owlObjectPropertyReferences;
	// protected Map<OWLDataProperty, Set<OWLAxiom>> owlDataPropertyReferences;
	// protected Map<OWLNamedIndividual, Set<OWLAxiom>> owlIndividualReferences;
	protected Map<OWLAnonymousIndividual, Set<OWLAxiom>> owlAnonymousIndividualReferences;

	// protected Map<OWLDatatype, Set<OWLAxiom>> owlDatatypeReferences;
	// protected Map<OWLAnnotationProperty, Set<OWLAxiom>>
	// owlAnnotationPropertyReferences;

	// hilpold 2011.09.27 eliminating protected Map<OWLEntity,
	// Set<OWLDeclarationAxiom>> declarationsByEntity;

	public HGDBOntologyInternalsImpl() {
		initMaps();
	}

	protected void initMaps() {
		// hilpold this.importsDeclarations = createSet();
		this.ontologyAnnotations = createSet();
		// this.axiomsByType = createMap();
		this.logicalAxiom2AnnotatedAxiomMap = createMap();
		this.generalClassAxioms = createSet();
		this.propertyChainSubPropertyAxioms = createSet();
		// this.owlClassReferences = createMap();
		// this.owlObjectPropertyReferences = createMap();
		// this.owlDataPropertyReferences = createMap();
		// this.owlIndividualReferences = createMap();
		this.owlAnonymousIndividualReferences = createMap();
		// this.owlDatatypeReferences = createMap();
		// this.owlAnnotationPropertyReferences = createMap();
		// this.declarationsByEntity = createMap();
	}

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
	public boolean isDeclared(OWLDeclarationAxiom ax) {
		return containsAxiom(ax);
		// HGHandle entityHandle = graph.getHandle(ax.getEntity());
		// if (entityHandle != null) {
		// return graph.getIncidenceSet(entityHandle).size() > 0;
		// } else {
		// return (graph.getHandle(ax) != null);
		// }
		// old return declarationsByEntity.containsKey(ax.getEntity());
	}

	public boolean isEmpty() {
		boolean noAxioms = ontology.count(hg.typePlus(OWLAxiom.class)) == 0;
		return noAxioms && ontologyAnnotations.isEmpty();

		// Don't do this: ontology.isEmpty(); because Onto is considered empty
		// despite imports.
		// for (Set<OWLAxiom> axiomSet : axiomsByType.values()) {
		// if (!axiomSet.isEmpty()) {
		// return false;
		// }
		// }
		// return ontologyAnnotations.isEmpty();
	}

	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype datatype) {
		Set<OWLDatatypeDefinitionAxiom> result = createSet();
		Set<OWLDatatypeDefinitionAxiom> axioms = getAxiomsInternal(AxiomType.DATATYPE_DEFINITION);
		for (OWLDatatypeDefinitionAxiom ax : axioms) {
			if (ax.getDatatype().equals(datatype)) {
				result.add(ax);
			}
		}
		return result;
	}

	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(OWLAnnotationProperty subProperty) {
		Set<OWLSubAnnotationPropertyOfAxiom> result = createSet();
		for (OWLSubAnnotationPropertyOfAxiom ax : getAxiomsInternal(AxiomType.SUB_ANNOTATION_PROPERTY_OF)) {
			if (ax.getSubProperty().equals(subProperty)) {
				result.add(ax);
			}
		}
		return result;
	}

	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(OWLAnnotationProperty property) {
		Set<OWLAnnotationPropertyDomainAxiom> result = createSet();
		for (OWLAnnotationPropertyDomainAxiom ax : getAxiomsInternal(AxiomType.ANNOTATION_PROPERTY_DOMAIN)) {
			if (ax.getProperty().equals(property)) {
				result.add(ax);
			}
		}
		return result;
	}

	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(OWLAnnotationProperty property) {
		Set<OWLAnnotationPropertyRangeAxiom> result = createSet();
		for (OWLAnnotationPropertyRangeAxiom ax : getAxiomsInternal(AxiomType.ANNOTATION_PROPERTY_RANGE)) {
			if (ax.getProperty().equals(property)) {
				result.add(ax);
			}
		}
		return result;
	}

	@Override
	protected <T extends OWLAxiom> Set<T> getAxiomsInternal(AxiomType<T> axiomType) {
		List<T> axiomsOneType = null;
		Class<? extends OWLAxiomHGDB> hgdbAxiomClass = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiomType);
		if (hgdbAxiomClass == null) {
			log.warning("getAxiomsInternal Not yet implemented: " + axiomType);
		} else {
			axiomsOneType = ontology.getAll(hg.type(hgdbAxiomClass));
		}
		return getReturnSet(axiomsOneType);
		// return (Set<T>) getAxioms(axiomType, axiomsByType, false);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual individual) {
		return getReturnSet(getAxioms(individual, owlAnonymousIndividualReferences, false));
	}

	/**
	 * 2011.10.13: return only axioms in Ontology!
	 */
	public Set<OWLAxiom> getReferencingAxioms(final OWLEntity owlEntity) {
		// TODO use static type map instead of OWLENTITY.class ->
		// owlEntity.getClass() works.
		// TODO create method to HGHandle getFindEntity(OWLEntity owlEntity);
		// TODO shall we ensure that entity is in ontology?
		// TODO this get;s called with null by one or more protege views!!
		if (owlEntity == null) {
			log.warning("BAD ? getReferencingAx(null) called");
			return Collections.emptySet();
		}
		String className = owlEntity.getClass().getCanonicalName();
		if (className.startsWith("uk.ac")) {
			log.warning("BAD ! OWLENTITY TYPE IS : " + owlEntity.getClass().getSimpleName());
			log.warning("BAD ! Object IS : " + owlEntity);
			log.warning("BAD ! IRI IS : " + owlEntity.getIRI());
			// 2010.10.06 not acceptable anymore. HGApp adds BUILTIN types.
			// return Collections.emptySet();
			throw new IllegalStateException("We were called with a uk.ac entity.");
		}
		List<OWLAxiom> axioms;
		axioms = graph.getTransactionManager().transact(new Callable<List<OWLAxiom>>() {
			public List<OWLAxiom> call() {
				List<OWLAxiom> l;
				HGHandle owlEntityHandle = graph.getHandle(owlEntity);
				if (owlEntityHandle == null) {
					// TODO might not find what we need, because
					// owlEntity.getClass must match our
					// *HGDB implementation types.
					owlEntityHandle = hg.findOne(graph,
							hg.and(hg.type(owlEntity.getClass()), hg.eq("IRI", owlEntity.getIRI())));
				}
				if (owlEntityHandle == null) {
					l = Collections.emptyList();
				} else {
					l = new ArrayList<OWLAxiom>();
					// 2010.10.20 we traverse incidenceset until we hit an empty
					// set or an axiom
					// that is a member of our Ontology.
					System.out.print("Collecting axioms: ");
					collectOntologyAxiomsRecursive(owlEntityHandle, l);
					System.out.println();
					System.out.println("Found : " + l.size());
					// old l =
					// ontology.getAll(hg.and(hg.typePlus(OWLAxiom.class),
					// hg.incident(owlEntityHandle)));
					// older l = hg.getAll(graph,
					// hg.and(hg.typePlus(OWLAxiom.class),
					// hg.incident(owlEntityHandle)));
				}
				return l;
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
		return getReturnSet(axioms);
	}

	/**
	 * Initial call: Handle should be entity. Recursive calls will only
	 * be on ClassExpressions.
	 * 
	 * @param h
	 *            Handle (Entity intially, ClassExpressionHandle, OWLObjectPropertyExpression handle (InverseOf) or DataRange on
	 *            recursion)
	 * @param l
	 *            the list for the axioms. Postcondition: List has no
	 *            equal axioms.
	 * @throws IllegalStateException
	 *             , if an incidenceset contains something else than
	 *             Axioms or ClassExpressions.
	 */
	private void collectOntologyAxiomsRecursive(HGHandle atomHandle, List<OWLAxiom> axiomList) {
		if (DBG) System.out.print("*" + recLevel);
		if (atomHandle == null) {
			return;
		} else {
			IncidenceSet iSet = graph.getIncidenceSet(atomHandle);
			for (HGHandle incidentAtomHandle : iSet) {
				Object o = graph.get(incidentAtomHandle);
				if (o != null) {
					if (o instanceof OWLAxiom) {
						if (ontology.isMember(incidentAtomHandle)) {
							axiomList.add((OWLAxiom) o);
						} // else ignore axiom not part of our onto.
					} else {
						// we have no cycles up incidence sets starting
						// on an entity.
						if (!(o instanceof OWLClassExpression 
							  || o instanceof OWLObjectPropertyExpression
							  || o instanceof OWLDataRange

							 )) {
							throw new IllegalStateException(
									"We encountered an unexpected object in an incidenceset:" + o);
						}
						recLevel ++;
						collectOntologyAxiomsRecursive(incidentAtomHandle, axiomList);
						recLevel --;
					}
				} // else o == null do nothing
			} // for
		}
	}

	/**
	 * Tests, if for a given Entity, ClassExpression, ObjectPropExopression or Datarange one or more axioms can be found, 
	 * that is a member of the ontology. 
	 * @param atomHandle non null.
	 * @return true as soon as one axiom in the ontology is found.
	 */
	private boolean hasOntologyAxiomsRecursive(HGHandle atomHandle) {
		if (DBG) System.out.print("*" + recLevel);
		IncidenceSet iSet = graph.getIncidenceSet(atomHandle);
		for (HGHandle incidentAtomHandle : iSet) {
			Object o = graph.get(incidentAtomHandle);
			if (o != null) {
				if (o instanceof OWLAxiom) {
					if (ontology.isMember(incidentAtomHandle)) {
						if (DBG) System.out.println("Found axiom recursive: " + o);
						return true;
					} // else ignore axiom not part of our onto.
				} else {
					// we have no cycles up incidence sets starting
					// on an entity.
					if (!(o instanceof OWLClassExpression 
							|| o instanceof OWLObjectPropertyExpression
							|| o instanceof OWLDataRange
					)) {
						throw new IllegalStateException(
								"We encountered an unexpected object in an incidenceset:" + o);
					}
					recLevel ++;
					if (hasOntologyAxiomsRecursive(incidentAtomHandle)) {
						recLevel --;
						return true;
					} 
					recLevel --;
				}
			} // else o == null do nothing
		} // for
		return false;
	}

	// hilpold
	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity entity) {
		// is entity in graph, fail if not?
		final HGHandle entityHandle = graph.getHandle(entity);
		List<OWLDeclarationAxiom> l;
		// All links of type OWLDeclarationAxiom in the incidence set of
		// OWLEntity.
		l = graph.getTransactionManager().transact(new Callable<List<OWLDeclarationAxiom>>() {
			public List<OWLDeclarationAxiom> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLDeclarationAxiomHGDB.class), hg.incident(entityHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	// l = hg.getAll(graph,
	// hg.and(
	// hg.apply(hg.targetAt(graph, 0)
	// ,hg.and(hg.orderedLink(hg.anyHandle(), entityHandle),
	// hg.type(AxiomToEntityLink.class))
	// )
	// )
	// );
	// return getReturnSet(getAxioms(entity, declarationsByEntity, false));

	public Set<OWLImportsDeclaration> getImportsDeclarations() {
		// get link by name and link(handle)
		List<OWLImportsDeclaration> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLImportsDeclaration>>() {
			@SuppressWarnings("deprecation")
			public List<OWLImportsDeclaration> call() {
				return hg.getAll(
						graph,
						hg.apply(hg.targetAt(graph, 1), hg.and(hg.type(ImportDeclarationLink.class),
								hg.orderedLink(ontoHandle, hg.anyHandle()), new SubgraphMemberCondition(ontoHandle))));
			}
		}, HGTransactionConfig.READONLY);

		// List<IRI> imports = hg.getAll(getHyperGraph(),
		// hg.apply(hg.targetAt(getHyperGraph(), 1),
		// hg.and(hg.eq("importsLink"), hg.orderedLink(
		// this.getHyperGraph().getHandle(this), hg.anyHandle()))));
		// result.addAll(imports);
		return new HashSet<OWLImportsDeclaration>(l);
	}

	boolean containsImportDeclaration(OWLImportsDeclaration importDeclaration) {
		return getImportsDeclarations().contains(importDeclaration);
	}

	public boolean addImportsDeclaration(final OWLImportsDeclaration importDeclaration) {
		boolean success = false;
		ontology.printGraphStats("Before AddImp");
		success = graph.getTransactionManager().transact(new Callable<Boolean>() {
			public Boolean call() {
				if (containsImportDeclaration(importDeclaration))
					return false;
				else {
					HGHandle importDeclarationHandle = graph.add(importDeclaration);
					ImportDeclarationLink link = new ImportDeclarationLink(ontoHandle, importDeclarationHandle);
					HGHandle linkHandle = graph.add(link);
					ontology.add(importDeclarationHandle);
					ontology.add(linkHandle);
					return true;
				}
			}
		});
		ontology.printGraphStats("After  AddImp");
		assert (ontology.findOne(hg.eq(importDeclaration)) != null);
		assert (!ontology.findAll(hg.type(ImportDeclarationLink.class)).isEmpty());
		return success;
	}

	/**
	 * Removes both, the importDeclaration and the link connecting it to
	 * internals from hypergraph.
	 */
	public boolean removeImportsDeclaration(final OWLImportsDeclaration importDeclaration) {
		ontology.printGraphStats("Before RemImp");
		boolean success = graph.getTransactionManager().transact(new Callable<Boolean>() {
			public Boolean call() {
				boolean success;
				HGHandle importDeclarationHandle;
				HGHandle link;
				// graph.getTransactionManager().beginTransaction();
				if (!containsImportDeclaration(importDeclaration)) {
					return false;
				}
				importDeclarationHandle = graph.getHandle(importDeclaration);
				if (importDeclarationHandle == null) {
					throw new IllegalStateException("Contains said fine, but can't get handle.");
				}
				link = hg.findOne(graph, hg.and(hg.type(ImportDeclarationLink.class),
						hg.orderedLink(ontoHandle, importDeclarationHandle), new SubgraphMemberCondition(ontoHandle)));
				if (link == null) {
					throw new IllegalStateException(
							"Found importDeclaration, but no link. Each Importdeclaration must have exactly one link.");
				}
				success = ontology.remove(link) && ontology.remove(importDeclarationHandle) && graph.remove(link)
						&& graph.remove(importDeclarationHandle);
				return success;
			}
		});
		ontology.printGraphStats("After  Remove Import");
		return success;
	}

	public Set<OWLAnnotation> getOntologyAnnotations() {
		return this.getReturnSet(this.ontologyAnnotations);
	}

	public boolean addOntologyAnnotation(OWLAnnotation ann) {
		return ontologyAnnotations.add(ann);
	}

	public boolean removeOntologyAnnotation(OWLAnnotation ann) {
		return ontologyAnnotations.remove(ann);
	}

	public boolean containsAxiom(final OWLAxiom axiom) {
		if (axiom == null)
			throw new NullPointerException("axiom");
		HGHandle h = graph.getHandle(axiom);
		return h != null ? ontology.isMember(h) : findEqualAxiom(axiom) != null;
		// //TODO will not work 2011.10.13; must rely on equals code in axiom
		// HGHandle axiomHandle = graph.getHandle(axiom);
		// // true iff found in graph and in ontology
		// return (axiomHandle == null) ? false : ontology.get(axiomHandle) !=
		// null;
		// // old Set<OWLAxiom> axioms = axiomsByType.get(axiom.getAxiomType());
		// // return axioms != null && axioms.contains(axiom);
	}

	/**
	 * Finds an equal axiom in the graph. See the equals methods implementation
	 * in Axiom. The method does not test for self.
	 * 
	 * @param axiom
	 *            an axiom object that might be equal to one in the graph.
	 * @return an axiom object that is guaranteed to be in the graph and equal
	 *         to the given axiom.
	 */
	protected OWLAxiom findEqualAxiom(OWLAxiom axiom) {
		if (axiom == null)
			throw new NullPointerException("axiom");
		// TODO this is expensive !! Maybe implement a complex search condition
		// based on equals in each axiom type.
		// Boris had ideas about bottom up search and parallel search.
		// Called by OWLCellrenderer true will render an entity in bold font.
		OWLAxiom foundAxiom = null;
		Class<?> hgdbType = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiom.getAxiomType());
		List<OWLAxiom> axiomsOneTypeInOnto = ontology.getAll(hg.type(hgdbType));
		// Find by axiom.equal (expensive)
		int i = axiomsOneTypeInOnto.indexOf(axiom);
		if (i != -1) {
			foundAxiom = axiomsOneTypeInOnto.get(i);
		}
		return foundAxiom;
	}

	public int getAxiomCount() {
		long count = ontology.count(hg.typePlus(OWLAxiom.class));
		if (count > Integer.MAX_VALUE) {
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

	public Set<OWLAxiom> getAxioms() {
		List<HGHandle> allHandles = ontology.findAll(hg.typePlus(OWLAxiom.class));
		Set<OWLAxiom> axioms = createSet();
		for (HGHandle h : allHandles) {
			axioms.add((OWLAxiom) graph.get(h));
		}
		return axioms;
		// Set<OWLAxiom> axioms = createSet();
		// for (AxiomType<?> type : AXIOM_TYPES) {
		// Set<OWLAxiom> owlAxiomSet = axiomsByType.get(type);
		// if (owlAxiomSet != null) {
		// axioms.addAll(owlAxiomSet);
		// }
		// }
		// return axioms;
	}

	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
		return getAxiomsInternal(axiomType);
		// WHY DIDN'T they refer to internal? return (Set<T>)
		// getAxioms(axiomType, axiomsByType, false);
	}

	/**
	 * Gets the axioms which are of the specified type, possibly from the
	 * imports closure of this ontology
	 * 
	 * @param axiomType
	 *            The type of axioms to be retrived.
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
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType, Collection<OWLOntology> importsClosure) {
		if (importsClosure == null || importsClosure.size() == 0) {
			return getAxioms(axiomType);
		}
		Set<T> result = createSet();
		for (OWLOntology ont : importsClosure) {
			result.addAll(ont.getAxioms(axiomType));
		}
		return result;
	}

	public <T extends OWLAxiom> int getAxiomCount(final AxiomType<T> axiomType) {
		long axiomsOneTypeCount = 0;
		Class<? extends OWLAxiomHGDB> hgdbAxiomClass = AxiomTypeToHGDBMap.getAxiomClassHGDB(axiomType);
		if (hgdbAxiomClass == null) {
			log.warning("getAxiomCount: Not yet implemented for HG: " + axiomType);
		} else {
			axiomsOneTypeCount = ontology.count(hg.type(hgdbAxiomClass));
		}
		if (axiomsOneTypeCount > Integer.MAX_VALUE)
			throw new ArithmeticException("long Count > int Max");
		return (int) axiomsOneTypeCount;
		// Set<OWLAxiom> axioms = axiomsByType.get(axiomType);
		// if (axioms == null) {
		// return 0;
		// }
		// return axioms.size();
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		List<OWLLogicalAxiom> axioms = ontology.getAll(getLogicalAxiomQuery());
		return getReturnSet(axioms);
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

	public int getLogicalAxiomCount() {
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

	protected HGQueryCondition getLogicalAxiomQuery() {
		Or logicalAxQuery = new Or();
		Set<Class<? extends OWLAxiomHGDB>> classes = AxiomTypeToHGDBMap.getLogicalAxiomTypesHGDB();
		for (Class<? extends OWLAxiomHGDB> c : classes) {
			logicalAxQuery.add(new AtomTypeCondition(c));
		}
		return logicalAxQuery;
	}

	public void addAxiomsByType(final AxiomType<?> type, final OWLAxiom axiom) {
		// TODO implement more axiom types and remove check when done
		if (DBG) {
			log.info("ADD Axiom: " + axiom.getClass().getSimpleName() + "Type: " + type);
		}
		if (containsAxiom(axiom)) {
			log.severe("DUPLICATE AXIOM WILL NOT BE ADDED TO ONTOLOGY");
			// throw new
			// IllegalStateException("Tried to add axiom already in ontology.");
			// A graph may contain duplicates, an ontology not.
		} else {
			if (AxiomTypeToHGDBMap.getAxiomClassHGDB(type) != null) {
				graph.getTransactionManager().ensureTransaction(new Callable<Boolean>() {
					public Boolean call() {
						if (DBG)
							ontology.printGraphStats("Before AddAxiom");
						// hyper hyper
						// hilpold 2011.10.06 adding to graph here instead of
						// previously in Datafactory
						HGHandle h = graph.add(axiom);
						// TODO REMOVE 2nd add (just to see if HG complains and
						// what
						// handle we'd get ?)
						// HGHandle h2 = graph.add(axiom); this leads to getting
						// a
						// second handle ???
						// HGHandle h = graph.getHandle(axiom);
						ontology.add(h);
						if (DBG)
							ontology.printGraphStats("After AddAxiom");
						return true;
					}
				});
			} else {
				log.warning("NOT YET IMPLEMENTED: " + axiom.getClass().getSimpleName());
				// addToIndexedSet(type, axiomsByType, axiom);
			}
		}
	}

	public void removeAxiomsByType(final AxiomType<?> type, final OWLAxiom axiom) {
		// TODO implement more axiom types and remove check when done
		if (DBG) {
			log.info("REMOVE Axiom: " + axiom.getClass().getSimpleName() + " Type: " + type);
		}
		if (AxiomTypeToHGDBMap.getAxiomClassHGDB(type) != null) {
			graph.getTransactionManager().transact(new Callable<Boolean>() {
				public Boolean call() {
					boolean removedSuccess = false;
					if (DBG)
						ontology.printGraphStats("Before RemoveAxiom");
					// get the axiom handle or find an equal axiom in the
					// ontology.
					HGHandle h = graph.getHandle(axiom);
					if (h == null || ontology.get(h) == null) {
						// Axiom null or not in ontology, try find an equal one
						// in ontology
						h = graph.getHandle(findEqualAxiom(axiom));
					}
					if (h != null) {
						ontology.remove(h);
						removedSuccess = graph.remove(h);
						if (DBG)
							ontology.printGraphStats("After  RemoveAxiom");
						// if it pointed to an entity, entity incidence is -1
					}
					return removedSuccess;
				}
			});
		} else {
			log.warning("NOT YET IMPLEMENTED: " + axiom.getClass().getSimpleName());
			// removeAxiomFromSet(type, axiomsByType, axiom, true);
		}
	}

	public Map<OWLAxiom, Set<OWLAxiom>> getLogicalAxiom2AnnotatedAxiomMap() {
		return new HashMap<OWLAxiom, Set<OWLAxiom>>(this.logicalAxiom2AnnotatedAxiomMap);
	}

	public Set<OWLAxiom> getLogicalAxiom2AnnotatedAxiom(OWLAxiom ax) {
		return getReturnSet(logicalAxiom2AnnotatedAxiomMap.get(ax.getAxiomWithoutAnnotations()));
	}

	public void addLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
		addToIndexedSet(ax.getAxiomWithoutAnnotations(), logicalAxiom2AnnotatedAxiomMap, ax);
	}

	public void removeLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
		removeAxiomFromSet(ax.getAxiomWithoutAnnotations(), logicalAxiom2AnnotatedAxiomMap, ax, true);
	}

	public boolean containsLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
		return logicalAxiom2AnnotatedAxiomMap.containsKey(ax.getAxiomWithoutAnnotations());
	}

	public Set<OWLClassAxiom> getGeneralClassAxioms() {
		return getReturnSet(this.generalClassAxioms);
	}

	public void addGeneralClassAxioms(OWLClassAxiom ax) {
		this.generalClassAxioms.add(ax);
	}

	public void removeGeneralClassAxioms(OWLClassAxiom ax) {
		this.generalClassAxioms.remove(ax);
	}

	public Set<OWLSubPropertyChainOfAxiom> getPropertyChainSubPropertyAxioms() {
		return getReturnSet(this.propertyChainSubPropertyAxioms);
	}

	public void addPropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax) {
		this.propertyChainSubPropertyAxioms.add(ax);
	}

	public void removePropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax) {
		this.propertyChainSubPropertyAxioms.remove(ax);
	}

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
	public boolean containsOwlClass(final OWLClass c) {
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
	public boolean containsOwlObjectProperty(final OWLObjectProperty c) {
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
	public boolean containsOwlDataProperty(final OWLDataProperty c) {
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
	boolean containsOWLEntityOntology(final IRI iri, final Class<?> hgdbType) {
		if (!OWLObjectHGDB.class.isAssignableFrom(hgdbType))
			throw new IllegalArgumentException("Only subclasses of OWLObjectHGDB allowed! Was:" + hgdbType);
		return graph.getTransactionManager().transact(new Callable<Boolean>() {
			public Boolean call() {
				return hg.findOne(graph,
						hg.and(hg.type(hgdbType), hg.eq("IRI", iri), new SubgraphMemberCondition(ontoHandle))) != null;
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

	public boolean containsOwlNamedIndividual(final IRI individualIRI) {
		return containsOWLEntityOntology(individualIRI, OWLNamedIndividualHGDB.class);
	}

	// ------------------------------------------------------------------------------------
	// OWL_ENTITY BASIC QUERIES
	//

	public Set<OWLAnnotationProperty> getOwlAnnotationProperties() {
		List<OWLAnnotationProperty> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLAnnotationProperty>>() {
			public List<OWLAnnotationProperty> call() {
				return hg.getAll(graph,
						hg.and(hg.type(OWLAnnotationPropertyHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLClass> getOwlClasses() {
		List<OWLClass> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLClass>>() {
			public List<OWLClass> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLClassHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLDatatype> getOwlDatatypes() {
		List<OWLDatatype> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLDatatype>>() {
			public List<OWLDatatype> call() {
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
	@Override
	public Set<OWLNamedIndividual> getOwlNamedIndividuals() {
		List<OWLNamedIndividual> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLNamedIndividual>>() {
			public List<OWLNamedIndividual> call() {
				return hg.getAll(graph,
						hg.and(hg.type(OWLNamedIndividualHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLDataProperty> getOwlDataProperties() {
		List<OWLDataProperty> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLDataProperty>>() {
			public List<OWLDataProperty> call() {
				return hg.getAll(graph,
						hg.and(hg.type(OWLDataPropertyHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLObjectProperty> getOwlObjectProperties() {
		List<OWLObjectProperty> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLObjectProperty>>() {
			public List<OWLObjectProperty> call() {
				return hg.getAll(graph,
						hg.and(hg.type(OWLObjectPropertyHGDB.class), new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	//
	// END OWL_ENTITY BASIC QUERIES
	// ------------------------------------------------------------------------------------

	public Map<OWLAnonymousIndividual, Set<OWLAxiom>> getOwlAnonymousIndividualReferences() {
		return new HashMap<OWLAnonymousIndividual, Set<OWLAxiom>>(this.owlAnonymousIndividualReferences);
	}

	public void removeOwlAnonymousIndividualReferences(OWLAnonymousIndividual c, OWLAxiom ax) {
		removeAxiomFromSet(c, owlAnonymousIndividualReferences, ax, true);
	}

	public void addOwlAnonymousIndividualReferences(OWLAnonymousIndividual c, OWLAxiom ax) {
		addToIndexedSet(c, owlAnonymousIndividualReferences, ax);
	}

	public boolean containsOwlAnonymousIndividualReferences(OWLAnonymousIndividual c) {
		return this.owlAnonymousIndividualReferences.containsKey(c);
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
	public boolean containsOwlDatatype(OWLDatatype c) {
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
	public boolean containsOwlAnnotationProperty(OWLAnnotationProperty c) {
		return containsOWLEntityOntology(c.getIRI(), OWLAnnotationPropertyHGDB.class);
	}

	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#hasReferencingAxioms(org.semanticweb.owlapi.model.OWLEntity)
	 */
	@Override
	public boolean hasReferencingAxioms(OWLEntity entity) {
		HGHandle h= graph.getHandle(entity);
		return hasOntologyAxiomsRecursive(h);
	}

	@Override
	public boolean hasReferencingAxioms(HGHandle entity) {
		return hasOntologyAxiomsRecursive(entity);
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

}

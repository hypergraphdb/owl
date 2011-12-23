package org.hypergraphdb.app.owl.gc;

/**
 * GarbageCollectorStatistics.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 */
public class GarbageCollectorStatistics {

	private int totalAtoms = 0;
	
	private int ontologies = 0;
		
	private int axioms = 0;

	/**
	 * A counter for cases, when gc encounters an axiom that was found as a member of one deleted ontology, 
	 * but not removable, because it's incidence set contained one or more other ontologies.
	 * e.g. if an axiom is part of 3 ontologies to be deleted and no other, this counter will be 2 
	 * for this axiom and the axiom will be deleted on removal of the last ontology.
	 */
	private int axiomNotRemovableCases = 0;

	private int entities = 0;

	/**
	 * includes:
	 * - OWLClassExpressionHGDB (not CN, named Class)
	 * - (I) OWLDataRange (not R, named data prop)
	 * - OwlFacetRestrictionHGDB
	 * - OWLLiteralHGBD
	 * - OWLObjectPropertyExpression (not PN, OWLObjectPropery)
	 * - SWRLAtomHGDB
	 * - SWRLIndividualArgument
	 * - SWRLLiteralArgument
	 * - SWRLVariable
	 */
	private int otherObjects = 0;

	/**
	 * @return the totalAtoms
	 */
	public int getTotalAtoms() {
		return totalAtoms;
	}

	/**
	 * @param totalAtoms the totalAtoms to set
	 */
	public void setTotalAtoms(int totalAtoms) {
		this.totalAtoms = totalAtoms;
	}

	public void increaseTotalAtoms() {
		totalAtoms++;
	}
	
	/**
	 * @return the ontologies
	 */
	public int getOntologies() {
		return ontologies;
	}

	/**
	 * @param ontologies the ontologies to set
	 */
	public void setOntologies(int ontologies) {
		this.ontologies = ontologies;
	}
	
	public void increaseOntologies() {
		ontologies++;
	}
	
	/**
	 * @return the axioms
	 */
	public int getAxioms() {
		return axioms;
	}

	/**
	 * @param axioms the axioms to set
	 */
	public void setAxioms(int axioms) {
		this.axioms = axioms;
	}

	public void increaseAxioms() {
		axioms ++;
	}
	
	/**
	 * @return the axiomNotRemovableCases
	 */
	public int getAxiomNotRemovableCases() {
		return axiomNotRemovableCases;
	}

	/**
	 * @param axiomNotRemovableCases the axiomNotRemovableCases to set
	 */
	public void setAxiomNotRemovableCases(int axiomNotRemovableCases) {
		this.axiomNotRemovableCases = axiomNotRemovableCases;
	}

	public void increaseAxiomNotRemovableCases() {
		axiomNotRemovableCases ++;
	}
	
	/**
	 * @return the entities
	 */
	public int getEntities() {
		return entities;
	}

	/**
	 * @param entities the entities to set
	 */
	public void setEntities(int entities) {
		this.entities = entities;
	}

	public void increaseEntities() {
		entities ++;
	}

	/**
	 * @return the otherObjects
	 */
	public int getOtherObjects() {
		return otherObjects;
	}

	/**
	 * @param otherObjects the otherObjects to set
	 */
	public void setOtherObjects(int otherObjects) {
		this.otherObjects = otherObjects;
	}

	public void increaseOtherObjects() {
		otherObjects ++;
	}
	
	public String toString() {
		return "Total: " + totalAtoms + ", axioms: " + axioms +  ", entities: " + entities 
		+ " other: " + otherObjects + "\n  NRAxioms: " + axiomNotRemovableCases;
	}
	
}

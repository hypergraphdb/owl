package org.hypergraphdb.app.owl.versioning.change;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.model.RemoveOntologyAnnotation;
import org.semanticweb.owlapi.model.SetOntologyID;

/**
 * OWLChangeConflictDetector.
 * Checks, if this change would not cause a modification to the given Ontology.
 * If not, we consider the change a conflicting change, which might be a
 * double addition, double removal, set from the same old to the same new value.
 * Even such changes would be noops if applied forwards, they must not be reverted as this 
 * breaks the logical relationship of the workingset with the history. 
 * (E.g. [c1(Add A), c2(Add A), revert(c2(Add A)] would remove A from workingset, 
 * despite c1 implying that A exists) 
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Sep 18, 2012
 */
public class OWLChangeConflictDetector implements OWLOntologyChangeVisitorEx<Boolean> {

	private OWLOntology ontology;
	
	public OWLChangeConflictDetector(OWLOntology o) {
		if (o == null) throw new IllegalArgumentException();
		ontology = o;
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.AddAxiom)
	 */
	@Override
	public Boolean visit(AddAxiom change) {
		return (ontology.containsAxiom(change.getAxiom()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.RemoveAxiom)
	 */
	@Override
	public Boolean visit(RemoveAxiom change) {
		return !(ontology.containsAxiom(change.getAxiom()));
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.SetOntologyID)
	 */
	@Override
	public Boolean visit(SetOntologyID change) {
		return ontology.getOntologyID().equals(change.getNewOntologyID());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.AddImport)
	 */
	@Override
	public Boolean visit(AddImport change) {
		return ontology.getImports().contains(change.getImportDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.RemoveImport)
	 */
	@Override
	public Boolean visit(RemoveImport change) {
		return !ontology.getImports().contains(change.getImportDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.AddOntologyAnnotation)
	 */
	@Override
	public Boolean visit(AddOntologyAnnotation change) {
		return ontology.getAnnotations().contains(change.getAnnotation());
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx#visit(org.semanticweb.owlapi.model.RemoveOntologyAnnotation)
	 */
	@Override
	public Boolean visit(RemoveOntologyAnnotation change) {
		return !ontology.getAnnotations().contains(change.getAnnotation());
	}
}

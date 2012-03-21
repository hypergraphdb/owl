package org.hypergraphdb.app.owl.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * OntologyComparator.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 21, 2012
 */
public class OntologyComparator {

		
		public ComparatorDelta compare(OWLOntology from, OWLOntology to) {
			ComparatorDelta result = new ComparatorDelta();
			Set<OWLAxiom> fromAxioms = from.getAxioms();
			Set<OWLAxiom> toAxioms = to.getAxioms();
			Iterator<OWLAxiom> fromIt = fromAxioms.iterator();
			while (fromIt.hasNext()) {
				OWLAxiom fromA = fromIt.next();		
				if (!toAxioms.contains(fromA)) {
					result.removed(fromA);
				} else {
					//To will contain only added after this loop
					//To = To - FROM
					toAxioms.remove(fromA);
				}
			}
			for (OWLAxiom toA : toAxioms) {
				result.added(toA);
			}
			return result;
		}
		
		public static class ComparatorDelta {
			private List<OWLAxiom> addedAxioms = new ArrayList<OWLAxiom>();
			private List<OWLAxiom> removedAxioms = new ArrayList<OWLAxiom>();
			private List<OWLAnnotation> addedOntologyAnnotations = new ArrayList<OWLAnnotation>();
			private List<OWLAnnotation> removedOntologyAnnotations = new ArrayList<OWLAnnotation>();
			private List<OWLImportsDeclaration> addedImportDeclarations = new ArrayList<OWLImportsDeclaration>();
			private List<OWLImportsDeclaration> removedImportDeclarations = new ArrayList<OWLImportsDeclaration>();
			private List<OWLOntologyID> changedNewOntologyIDs = new ArrayList<OWLOntologyID>();

			public void added(OWLAxiom o) {
				addedAxioms.add(o);
			}

			public void added(OWLAnnotation o) {
				addedOntologyAnnotations.add(o);
			}
			
			public void added(OWLImportsDeclaration o) {
				addedImportDeclarations.add(o);
			}
			
			public void removed(OWLAxiom o) {
				removedAxioms.add(o);
			}
			
			public void removed(OWLAnnotation o) {
				removedOntologyAnnotations.add(o);
			}
			
			public void removed(OWLImportsDeclaration o) {
				removedImportDeclarations.add(o);
			}
			
			public void changed(OWLOntologyID newId) {
				if (!changedNewOntologyIDs.isEmpty()) throw new IllegalStateException("Only one changeId allowed.");
				changedNewOntologyIDs.add(newId);
			}
			
			public boolean isEmpty() {
				return addedAxioms.isEmpty() 
				&& removedAxioms.isEmpty()
				&& addedImportDeclarations.isEmpty()
				&& removedImportDeclarations.isEmpty()
				&& addedOntologyAnnotations.isEmpty()
				&& removedOntologyAnnotations.isEmpty()
				&& changedNewOntologyIDs.isEmpty();
			}
			
			public int nrOfChanges() {
				return addedAxioms.size() 
				+ removedAxioms.size()
				+ addedImportDeclarations.size()
				+ removedImportDeclarations.size()
				+ addedOntologyAnnotations.size()
				+ removedOntologyAnnotations.size()
				+ changedNewOntologyIDs.size();
			}

			/**
			 * @return the addedAxioms
			 */
			public List<OWLAxiom> getAddedAxioms() {
				return addedAxioms;
			}

			/**
			 * @return the removedAxioms
			 */
			public List<OWLAxiom> getRemovedAxioms() {
				return removedAxioms;
			}

			/**
			 * @return the addedOntologyAnnotations
			 */
			public List<OWLAnnotation> getAddedOntologyAnnotations() {
				return addedOntologyAnnotations;
			}

			/**
			 * @return the removedOntologyAnnotations
			 */
			public List<OWLAnnotation> getRemovedOntologyAnnotations() {
				return removedOntologyAnnotations;
			}

			/**
			 * @return the addedImportDeclarations
			 */
			public List<OWLImportsDeclaration> getAddedImportDeclarations() {
				return addedImportDeclarations;
			}

			/**
			 * @return the removedImportDeclarations
			 */
			public List<OWLImportsDeclaration> getRemovedImportDeclarations() {
				return removedImportDeclarations;
			}

			/**
			 * @return the changedNewOntologyIDs
			 */
			public List<OWLOntologyID> getChangedNewOntologyIDs() {
				return changedNewOntologyIDs;
			}
			
		}
}

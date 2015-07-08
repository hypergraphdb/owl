package org.hypergraphdb.app.owl.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * OntologyComparator.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Mar 21, 2012
 */
public class OntologyComparator {

		public ComparatorDelta compare(OWLOntology from, OWLOntology to) {
			ComparatorDelta result = new ComparatorDelta();
			// Compare Axioms
			Set<OWLAxiom> fromAxioms = from.getAxioms();
			Set<OWLAxiom> toAxioms = to.getAxioms();
			result.setTotalAxiomsFrom(fromAxioms.size());
			result.setTotalAxiomsTo(toAxioms.size());
			// Determine REMOVED toAxioms = toAxiom - fromAxioms
			for (OWLAxiom fromA : fromAxioms) {
				if (!toAxioms.contains(fromA)) {
					result.removed(fromA);
				} else {
					//To will contain only added after this loop
					//To = To - FROM
					toAxioms.remove(fromA);
					result.equal(fromA);
				}
			}
			//Determine ADDED
			for (OWLAxiom toA : toAxioms) {
				result.added(toA);
			}
			//
			// Compare Ontology Annotations
			//
			Set<OWLAnnotation> fromAnnotations = from.getAnnotations();
			Set<OWLAnnotation> toAnnotations = to.getAnnotations();
			// Determine REMOVED toAxioms = toAxiom - fromAxioms
			for (OWLAnnotation fromA : fromAnnotations) {
				if (!toAnnotations.contains(fromA)) {
					result.removed(fromA);
				} else {
					//To will contain only added after this loop
					//To = To - FROM
					toAnnotations.remove(fromA);
				}
			}
			//Determine ADDED
			for (OWLAnnotation toA : toAnnotations) {
				result.added(toA);
			}
			//
			// Compare Import Declarations
			//
			Set<OWLImportsDeclaration> fromImportDeclarations = from.getImportsDeclarations();
			Set<OWLImportsDeclaration> toImportDeclarations = to.getImportsDeclarations();
			// Determine REMOVED toAxioms = toAxiom - fromAxioms
			for (OWLImportsDeclaration fromI : fromImportDeclarations) {
				if (!toImportDeclarations.contains(fromI)) {
					result.removed(fromI);
				} else {
					//To will contain only added after this loop
					//To = To - FROM
					toImportDeclarations.remove(fromI);
				}
			}
			//Determine ADDED
			for (OWLImportsDeclaration toI : toImportDeclarations) {
				result.added(toI);
			}
			//
			// Compare Ontology ID
			//
			if (!to.getOntologyID().equals(from.getOntologyID())) {
				result.changed(from.getOntologyID());
			}
			return result;
		}
		
		public static class ComparatorDelta {
			private List<OWLAxiom> addedAxioms = new ArrayList<OWLAxiom>();
			private List<OWLAxiom> equalAxioms = new ArrayList<OWLAxiom>();
			private List<OWLAxiom> removedAxioms = new ArrayList<OWLAxiom>();
			private List<OWLAnnotation> addedOntologyAnnotations = new ArrayList<OWLAnnotation>();
			private List<OWLAnnotation> removedOntologyAnnotations = new ArrayList<OWLAnnotation>();
			private List<OWLImportsDeclaration> addedImportDeclarations = new ArrayList<OWLImportsDeclaration>();
			private List<OWLImportsDeclaration> removedImportDeclarations = new ArrayList<OWLImportsDeclaration>();
			private List<OWLOntologyID> changedNewOntologyIDs = new ArrayList<OWLOntologyID>();
			private int totalAxiomsFrom = -1;
			private int totalAxiomsTo = -1;


			public int getTotalAxiomsFrom() {
				return totalAxiomsFrom;
			}

			public void setTotalAxiomsFrom(int totalAxiomsFrom) {
				this.totalAxiomsFrom = totalAxiomsFrom;
			}

			public int getTotalAxiomsTo() {
				return totalAxiomsTo;
			}

			public void setTotalAxiomsTo(int totalAxiomsTo) {
				this.totalAxiomsTo = totalAxiomsTo;
			}

			public void added(OWLAxiom o) {
				addedAxioms.add(o);
			}

			public void equal(OWLAxiom o) {
				equalAxioms.add(o);
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
			
			/**
			 * True, if there is an addition or removal. Ignores equal axiom list.
			 * @return
			 */
			public boolean hasChanges() {
				return !(addedAxioms.isEmpty() 
				&& removedAxioms.isEmpty()
				&& addedImportDeclarations.isEmpty()
				&& removedImportDeclarations.isEmpty()
				&& addedOntologyAnnotations.isEmpty()
				&& removedOntologyAnnotations.isEmpty()
				&& changedNewOntologyIDs.isEmpty());
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
			 * @return the equalAxioms
			 */
			public List<OWLAxiom> getEqualAxioms() {
				return equalAxioms;
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
			
			public void sortAxioms() {
				Collections.sort(addedAxioms, new AxiomComparator());
				Collections.sort(removedAxioms, new AxiomComparator());
			}
			
			public void print(PrintWriter p) {
				p.println("## COMPARATOR DELTA START");
				p.println("## TOTAL FROM ONTOLOGY AXIOMS: " + getTotalAxiomsFrom());
				p.println("## TOTAL TO ONTOLOGY AXIOMS  : " + getTotalAxiomsTo());
				p.println("## REMOVED AXIOMS: " + getRemovedAxioms().size());
				for (OWLAxiom ax : getRemovedAxioms()) {
					p.println(ax.toString());
				}
				if (getRemovedAxioms().isEmpty()) p.println("None.");
				p.println("## ADDED AXIOMS: " + getAddedAxioms().size());
				for (OWLAxiom ax : getAddedAxioms()) {
					p.println(ax.toString());
				}
				p.println("## EQUAL AXIOMS: " + getEqualAxioms().size());
				if (getAddedAxioms().isEmpty()) p.println("None.");
				p.println("## REMOVED ONTOLOGY ANNOTATIONS");
				for (OWLAnnotation an : getRemovedOntologyAnnotations()) {
					p.println(an.toString());
				}
				if (getRemovedOntologyAnnotations().isEmpty()) p.println("None.");
				p.println("## ADDED ONTOLOGY ANNOTATIONS");
				for (OWLAnnotation an : getAddedOntologyAnnotations()) {
					p.println(an.toString());
				}
				if (getAddedOntologyAnnotations().isEmpty()) p.println("None.");
				p.println("## REMOVED IMPORTDECLARATIONS");
				for (OWLImportsDeclaration im : getRemovedImportDeclarations()) {
					p.println(im.toString());
				}
				if (getRemovedImportDeclarations().isEmpty()) p.println("None.");
				p.println("## ADDED IMPORTDECLARATIONS");
				for (OWLImportsDeclaration im : getAddedImportDeclarations()) {
					p.println(im.toString());
				}
				if (getAddedImportDeclarations().isEmpty()) p.println("None.");
				p.println("## MODIFIED IDS ");
				for (OWLOntologyID id : getChangedNewOntologyIDs()) {
					p.println(id.toString());
				}				
				if (getChangedNewOntologyIDs().isEmpty()) p.println("None.");
				p.println("## END COMPARATOR DELTA ");
				p.flush();
			}
		}
		
		@SuppressWarnings("deprecation")
		public static void main(String[] argv) {
			if (argv == null || argv.length != 2) {
				printHelp();
				System.exit(0);
			}
			File fromFile = new File (argv[0]);
			File toFile = new File (argv[1]);
			if (!fromFile.exists()) {
				System.err.println("File " + argv[0] + " does not exist.");
			}
			if (!toFile.exists()) {
				System.err.println("File " + argv[1] + " does not exist.");
			}
			if (!fromFile.exists() || !toFile.exists()) {
				System.exit(-1);
			}
			OWLOntologyManager fromManager = OWLManager.createOWLOntologyManager();
			OWLOntologyManager toManager = OWLManager.createOWLOntologyManager();
			fromManager.setSilentMissingImportsHandling(true);
			toManager.setSilentMissingImportsHandling(true);
			try {
				System.out.println(" ONTOLOGY COMPARATOR ");
				System.out.println("Loading from Ontology : " + fromFile.toURI());
				OWLOntology from = fromManager.loadOntologyFromOntologyDocument(fromFile);
				System.out.println("Loading to Ontology : " + toFile.toURI());
				OWLOntology to = toManager.loadOntologyFromOntologyDocument(toFile);
				System.out.println("Comparing from -> to ");
				OntologyComparator comp = new OntologyComparator();
				ComparatorDelta delta = comp.compare(from, to);
				delta.sortAxioms();
				saveAxioms(new File(toFile.getAbsolutePath() + "_added.owl"), delta.getAddedAxioms());
				saveAxioms(new File(toFile.getAbsolutePath() + "_remove.owl"), delta.getRemovedAxioms());
				saveAxioms(new File(toFile.getAbsolutePath() + "_equal.owl"), delta.getEqualAxioms());
				delta.print(new PrintWriter(System.out));
				System.out.println(" END COMPARATOR ");
			} catch (Exception e) {
				System.err.println(e);
				System.exit(-1);
			}
		}
		
		public static void saveAxioms(File f, List<OWLAxiom> axioms) throws IOException{
			PrintWriter p = new PrintWriter(f.getAbsolutePath(), "UTF-8");
			for (OWLAxiom ax : axioms) {
				p.println(ax);
			}
			p.close();
		}
		
		public static void printHelp() {
			System.out.println("Compares two ontologies From-to and prints added/removed axioms, annotations and import declararions in functional format.");
			System.out.println("OntologyComparator ontologyFileFrom ontologyFileTwo ");
			System.out.println();
			System.out.println(" This is particularly useful if you need to find the changes between two revisions of one ontology and wish to apply them to another ontology. ");
		}
		
}
 
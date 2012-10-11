package org.hypergraphdb.app.owl.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.hypergraphdb.app.owl.util.OntologyComparator.ComparatorDelta;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * Syed.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 5, 2012
 */
public class Syed {
	
	/**
	 * Ontology1, Ontology2, String namedIndividualIRI
	 * @param argv
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] argv) {
		if (argv == null || argv.length != 3) {
			System.exit(0);
		}
		String individual = argv[2];
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
			OntologyComparator.saveAxioms(new File(toFile.getAbsolutePath() + "_added.owl"), delta.getAddedAxioms());
			OntologyComparator.saveAxioms(new File(toFile.getAbsolutePath() + "_remove.owl"), delta.getRemovedAxioms());
			OntologyComparator.saveAxioms(new File(toFile.getAbsolutePath() + "_equal.owl"), delta.getEqualAxioms());
			delta.print(new PrintWriter(System.out));
			System.out.println(" END COMPARATOR ");
			OWLNamedIndividual ind = fromManager.getOWLDataFactory().getOWLNamedIndividual(IRI.create(individual)); 
			OntologyGraphSearch gs = new OntologyGraphSearch();
			OWLOntology addedOnto = toManager.createOntology(new HashSet<OWLAxiom>(delta.getAddedAxioms()));
			List<OWLAxiom> found = gs.findAllConnectedDFS(addedOnto, ind);
			List<OWLAxiom> addedMinusFound = new ArrayList<OWLAxiom>(delta.getAddedAxioms());
			addedMinusFound.removeAll(found);
			System.out.println(" ADDED AXIOMS THAT ARE RELATED TO " + ind + " :" + found.size());
			System.out.println(" ADDED AXIOMS THAT ARE NOT RELATED TO " + ind + " :" + addedMinusFound.size());
			OntologyComparator.saveAxioms(new File(toFile.getAbsolutePath() + "_added_related_DFS.owl"), found);
			OntologyComparator.saveAxioms(new File(toFile.getAbsolutePath() + "_added_unrelated_sorted.owl"), addedMinusFound);
			Collections.sort(found, new AxiomComparator());
			OntologyComparator.saveAxioms(new File(toFile.getAbsolutePath() + "_added_related_sorted.owl"), found);
		} catch (Exception e) {
			System.err.println(e);
			System.exit(-1);
		}
	}}

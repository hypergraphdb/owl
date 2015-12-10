package org.hypergraphdb.app.owl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * OntologyGraphSearch finds all axioms connected to a non-builtin entity (e.g.
 * an SR) by depth first search. No reasoner involved.
 * 
 * Connected means starting from the given non-builtin entity, all axioms are
 * returned that can be reached by DFS. Builtin Entities such as OWLDatatypes
 * are not explored!
 * 
 * You can subclass and overwrite shouldExplore in case you need more specific
 * traversal semantics.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 5, 2012
 */
public class OntologyGraphSearch
{

	Map<OWLEntity, Set<OWLAxiom>> entityToAxioms;

	/**
	 * Stops at Datatypes.
	 * 
	 * @param onto
	 * @param i
	 * @return a list with unique axioms that are connected to e. (builtin
	 *         entities ignored)
	 */
	public List<OWLAxiom> findAllConnectedDFS(OWLOntology onto, OWLEntity e)
	{
		prepareGraph(onto);
		Set<OWLAxiom> exploredA = new HashSet<OWLAxiom>(entityToAxioms.size());
		Set<OWLEntity> exploredE = new HashSet<OWLEntity>(entityToAxioms.size());
		List<OWLAxiom> orderedResult = new ArrayList<OWLAxiom>(entityToAxioms.size() / 2);
		findAllConnectedDFS(e, exploredA, exploredE, orderedResult);
		return orderedResult;
	}

	protected void findAllConnectedDFS(OWLEntity e, Set<OWLAxiom> exploredA, Set<OWLEntity> exploredE, List<OWLAxiom> result)
	{
		exploredE.add(e);
		if (!shouldExplore(e))
			return;
		for (OWLAxiom ax : entityToAxioms.get(e))
		{
			if (exploredA.add(ax))
			{
				result.add(ax);
				for (OWLEntity connectedE : ax.getSignature())
				{
					if (!exploredE.contains(connectedE))
					{
						findAllConnectedDFS(connectedE, exploredA, exploredE, result);
					}
				}
			}
		}
	}

	/**
	 * @param e
	 * @return
	 */
	public boolean shouldExplore(OWLEntity e)
	{
		return !e.isBuiltIn();
	}

	private void prepareGraph(OWLOntology onto)
	{
		Set<OWLAxiom> allAxioms = onto.getAxioms();
		entityToAxioms = new HashMap<OWLEntity, Set<OWLAxiom>>(allAxioms.size() * 2);
		for (OWLAxiom ax : allAxioms)
		{
			Set<OWLEntity> sig = ax.getSignature();
			for (OWLEntity e : sig)
			{
				Set<OWLAnnotationAssertionAxiom> annotations = e.getAnnotationAssertionAxioms(onto);
				Set<OWLAxiom> eAxioms = entityToAxioms.get(e);
				if (eAxioms == null)
				{
					eAxioms = new HashSet<OWLAxiom>();
					entityToAxioms.put(e, eAxioms);
				}
				eAxioms.add(ax);
				eAxioms.addAll(annotations);
			}
		}
	}
}

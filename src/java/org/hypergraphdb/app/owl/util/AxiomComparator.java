package org.hypergraphdb.app.owl.util;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * AxiomComparator compares axioms by toString.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jul 3, 2012
 */
public class AxiomComparator extends Object implements Comparator<OWLAxiom> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(OWLAxiom o1, OWLAxiom o2) {
		if (o1 != null) {
			if (o2 != null) {
				return o1.toString().compareTo(o2.toString());
			} else {
				return 1;
			}
		} else {
			if (o2 != null) {
				return -1;
			} else {
				return 0;
			}
		}
	}

}

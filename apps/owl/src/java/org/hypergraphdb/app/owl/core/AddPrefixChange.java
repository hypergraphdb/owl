package org.hypergraphdb.app.owl.core;

import org.hypergraphdb.app.owl.HGDBOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;
import org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx;

/**
 * AddPrefixChange.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 1, 2012
 */
public class AddPrefixChange extends PrefixChange {

	public AddPrefixChange(HGDBOntologyFormat format, String prefixName, String prefix) {
		super(format, prefixName, prefix);
	}

	public AddPrefixChange(OWLOntologyEx onto, String prefixName, String prefix) {
		super(onto, prefixName, prefix);
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChange#accept(org.semanticweb.owlapi.model.OWLOntologyChangeVisitor)
	 */
	@Override
	public void accept(OWLOntologyChangeVisitor visitor) {
		if (visitor instanceof HGDBOntologyChangeVisitor) {
			((HGDBOntologyChangeVisitor)visitor).visit(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLOntologyChange#accept(org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx)
	 */
	@Override
	public <O> O accept(OWLOntologyChangeVisitorEx<O> visitor) {
		return null;
	}
	
	public String toString() {
		return "Add " + super.toString();
	}

}

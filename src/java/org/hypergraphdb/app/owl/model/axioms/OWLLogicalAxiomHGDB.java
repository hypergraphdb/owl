package org.hypergraphdb.app.owl.model.axioms;

import java.util.Collection;

import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 * OWLLogicalAxiomHGDB.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 5, 2011
 */
public abstract class OWLLogicalAxiomHGDB extends OWLAxiomHGDB implements OWLLogicalAxiom
{
	private static final long serialVersionUID = 1L;

	protected OWLLogicalAxiomHGDB(
			Collection<? extends OWLAnnotation> annotations)
	{
		super(annotations);
	}

	public boolean isLogicalAxiom()
	{
		return true;
	}

	public boolean isAnnotationAxiom()
	{
		return false;
	}
}

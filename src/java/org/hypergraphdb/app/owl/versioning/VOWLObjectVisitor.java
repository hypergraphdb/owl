package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;
import org.hypergraphdb.app.owl.versioning.change.VPrefixChange;

/**
 * VOWLObjectVisitor.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public interface VOWLObjectVisitor
{
	public void visit(VAxiomChange change);

	public void visit(VImportChange change);

	public void visit(VOntologyAnnotationChange change);

	public void visit(VModifyOntologyIDChange change);

	public void visit(VPrefixChange change);

}

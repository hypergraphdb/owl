package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.app.owl.versioning.change.VAxiomChange;
import org.hypergraphdb.app.owl.versioning.change.VImportChange;
import org.hypergraphdb.app.owl.versioning.change.VModifyOntologyIDChange;
import org.hypergraphdb.app.owl.versioning.change.VOWLChange;
import org.hypergraphdb.app.owl.versioning.change.VOntologyAnnotationChange;

/**
 * VersioningObjectVisitor.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public interface VersioningObjectVisitor  {

	public void visit(VersionedOntology ontology);
	
	public void visit(Revision revision);

	public void visit(ChangeSet changeSet);

	public void visit(VOWLChange change);

	public void visit(VAxiomChange change);

	public void visit(VImportChange change);

	public void visit(VOntologyAnnotationChange change);
	
	public void visit(VModifyOntologyIDChange change);
	
}

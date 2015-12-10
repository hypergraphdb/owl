package org.hypergraphdb.app.owl.versioning.change;


/**
 * VOWLObjectVisitor.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public interface VOWLChangeVisitor
{
	public void visit(VAxiomChange change);

	public void visit(VImportChange change);

	public void visit(VOntologyAnnotationChange change);

	public void visit(VModifyOntologyIDChange change);

	public void visit(VPrefixChange change);

}

package org.hypergraphdb.app.owl.versioning;


public interface VisitableObject
{
	public void accept(VOWLObjectVisitor visitor);
	
}

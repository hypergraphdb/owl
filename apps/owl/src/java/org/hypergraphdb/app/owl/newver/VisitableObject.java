package org.hypergraphdb.app.owl.newver;

public interface VisitableObject
{
	public void accept(VOWLObjectVisitor visitor);
	
}

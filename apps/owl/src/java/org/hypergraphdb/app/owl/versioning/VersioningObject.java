package org.hypergraphdb.app.owl.versioning;


/**
 * VersioningObject.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Feb 24, 2012
 */
public interface VersioningObject {

    public void accept(VOWLObjectVisitor visitor);

}

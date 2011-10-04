package org.hypergraphdb.app.owl.type;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;

/**
 * TypeUtils.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 29, 2011
 */
public class TypeUtils {
	
    /**
     * Tests, if a given javaclass is a supertype or the same type than the typesystem registered javaclass for the atom.
     *  
     * @param clazz a non-null java class or interface.
     * @param atomHandle a non-null atomHandle.
     * @param hypergraph a non-null hypergraph.
     * @return true, iff clazz is a supertype of the java type for atomHandle in the HG typesystem. 
     */
    public static boolean isClassAssignableFromAtomHandleType(Class<?> clazz, HGHandle atomHandle, HyperGraph g) {
    	HGHandle atomHandleType = g.getType(atomHandle);
    	HGTypeSystem ts = g.getTypeSystem();
    	//clazz is same or supertype of atomHandleType?
    	return clazz.isAssignableFrom(ts.getClassForType(atomHandleType));
    }
}

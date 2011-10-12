package org.hypergraphdb.app.owl.type;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGTypeSystem;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGSubsumes;
import org.hypergraphdb.type.HGAtomType;
import org.semanticweb.owlapi.model.OWLEntity;

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
    
    public static List<HGAtomType> getSubTypes(HyperGraph graph, Class<?> clazz) {
	    HGHandle th = graph.getTypeSystem().getTypeHandle(clazz);
	    if (th == null) { 
	    	throw new IllegalArgumentException("Hypergraph found no type for " + clazz.getSimpleName());
	    }
        return hg.getAll(graph, hg.apply(hg.targetAt(graph, 1), hg.and(hg.type(HGSubsumes.class), hg.orderedLink(th, hg.anyHandle()))));
    }

    public static List<HGAtomType> getSuperTypes(HyperGraph graph, Class<?> clazz) {
	    HGHandle th = graph.getTypeSystem().getTypeHandle(clazz);
	    if (th == null) { 
	    	throw new IllegalArgumentException("Hypergraph found no type for " + clazz.getSimpleName());
	    }
        return hg.getAll(graph, hg.apply(hg.targetAt(graph, 0), hg.and(hg.type(HGSubsumes.class), hg.orderedLink(hg.anyHandle(), th))));
    }

    public static List<HGAtomType> getSuperTypes(HyperGraph graph, HGHandle typeHandle) {
        return hg.getAll(graph, hg.apply(hg.targetAt(graph, 0), hg.and(hg.type(HGSubsumes.class), hg.orderedLink(hg.anyHandle(), typeHandle))));
    }
    
    public static List<HGAtomType> getSubTypes(HyperGraph graph, HGHandle typeHandle) {
        return hg.getAll(graph, hg.apply(hg.targetAt(graph, 1), hg.and(hg.type(HGSubsumes.class), hg.orderedLink(typeHandle, hg.anyHandle()))));
    }

    static int supertypeLevel = 0;
    
    public static void printAllSupertypes(HyperGraph graph, HGAtomType type) {
    	HGHandle typeHandle = graph.getHandle(type);
    	for (int i = 0; i < supertypeLevel; i++) System.out.print("  ");
    	System.out.println("" + supertypeLevel + "| " + type.toString() + " : " + type.getClass().getSimpleName() + " (HGType)");
    	Set<URI> javaClasses = graph.getTypeSystem().getIdentifiersForHandle(typeHandle);
    	printURISet(javaClasses, supertypeLevel);
    	List<HGAtomType> superTypes = getSuperTypes(graph, typeHandle);
    	supertypeLevel ++;
    	for (HGAtomType superType : superTypes) {
    		printAllSupertypes(graph, superType);
    	}
    	supertypeLevel --;
    }    
    
    static void printURISet(Set<URI> uris, int level) {
    	for (URI u : uris) {
        	for (int i = 0; i < level; i++) System.out.print("  ");
    		System.out.println(" for " + u + " (Java)");
    	}
    }
    static int subtypeLevel = 0;
    
    public static void printAllSubtypes(HyperGraph graph, HGAtomType type) {
    	HGHandle typeHandle = graph.getHandle(type);
    	for (int i = 0; i < subtypeLevel; i++) System.out.print("  ");
    	System.out.print("" + subtypeLevel + "| " + type.toString() + " : " + type.getClass().getSimpleName() + " (HGType)");
    	System.out.print("\t Atoms: ");
    	printTypeCount(graph, type);
    	System.out.print("\t AtomsPlus: ");
    	printTypePlusCount(graph, type);    	
    	System.out.println();
    	Set<URI> javaClasses = graph.getTypeSystem().getIdentifiersForHandle(typeHandle);
    	printURISet(javaClasses, subtypeLevel);
    	List<HGAtomType> subTypes = getSubTypes(graph, typeHandle);
    	subtypeLevel ++;
    	for (HGAtomType subType : subTypes) {
    		printAllSubtypes(graph, subType);
    	}
    	subtypeLevel --;
    }
    
    public static int printTypeCount(HyperGraph graph, HGAtomType type) {
    	long count = hg.count(graph, hg.type(graph.getHandle(type)));
    	System.out.print(count);
    	return (int)count;
    }

    public static int printTypePlusCount(HyperGraph graph, HGAtomType type) {
    	long count = hg.count(graph, hg.typePlus(graph.getHandle(type)));
    	System.out.print(count);
    	return (int)count;
    	
    }

}

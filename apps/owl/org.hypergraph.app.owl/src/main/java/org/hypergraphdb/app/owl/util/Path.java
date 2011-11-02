package org.hypergraphdb.app.owl.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Path represents a graph path with basic query abilities, such as match sequence of classes against objects on the path.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 2, 2011
 */
public class Path {
	
	private List<Object> pathAtoms = new ArrayList<Object>(10);
	
	/**
	 * Appends an object to the end of this path.
	 * The path may contain duplicate atoms, circles, a.s.o.
	 * @param o 
	 */
	public void addAtom(Object a) {
		pathAtoms.add(a);
	}
	
	public ListIterator<Object> listIterator() {
		return pathAtoms.listIterator();
	}
	
	/**
	 * Removes the last added Object from the path.
	 */
	public void removeLast() {
		if (pathAtoms.isEmpty()) throw new IllegalStateException("Empty");
		pathAtoms.remove(size() - 1);
	}

	/**
	 * Uses a hashset to find duplicates.
	 * @return
	 */
	public boolean hasDuplicates() {
		HashSet<Object> s = new HashSet<Object>(size() / 2 + size());
		for (Object o : pathAtoms) {
			if (!s.add(o)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public boolean containsObjectOfClass(Class<?> clazz) {
		Iterator<Object> i = pathAtoms.iterator();
		boolean found = false;
		while (!found && i.hasNext()) {
			found = clazz.isAssignableFrom(i.next().getClass());
		}
		return found;
	}
	
	/**
	 * Counts each object on the path that is of the same or a subclass of clazz or implements the interface.
	 * 
	 * @param clazz
	 * @return
	 */
	public int countObjectsOfClass(Class<?> clazz) {
		int hits = 0;
		for (Object o: pathAtoms) {
			if (clazz.isAssignableFrom(o.getClass())) {
				hits++;
			}
		}
		return hits;		
	}
	
	/**
	 * Looks, if the n'th object on the path matches the n'th given class from 0..m, where m is the number of clazzes arguments.
	 * @param clazzes length must > 0 and smaller or equal to size().
	 * @return
	 */
	public boolean startsWithObjectsOfClasses(Class<?>... clazzes) {
		return startsWithObjectsOfClasses(0, clazzes);
	}
	
	public boolean startsWithObjectsOfClasses(int offset, Class<?>... clazzes) {
		if (clazzes.length == 0) throw new IllegalArgumentException("clazzes.length == 0");
		if (clazzes.length + offset > pathAtoms.size()) throw new IllegalArgumentException("offset + clazzes.length > size()");			
		int i = 0;
		boolean stillMatches = true;
		while (stillMatches && i < clazzes.length) {
			stillMatches = clazzes[i].isAssignableFrom(pathAtoms.get(i + offset).getClass());
			i++;
		}
		return stillMatches;
	}
	
	public boolean endsWithObjectsOfClasses(Class<?>... clazzes) {
		if (clazzes.length == 0) throw new IllegalArgumentException("clazzes.length == 0");
		int offset = size() - clazzes.length;
		if (offset >= 0) {
			return startsWithObjectsOfClasses(offset, clazzes);
		} else {
			return false;
		}
	}
	
	/**
	 * Slow and trivial indexOf implementation.
	 * @param clazzes length must > 0 and smaller or equal to size().
	 * @return
	 */
	public int indexOfObjectsOfClasses(Class<?>... clazzes) {
		if (clazzes.length == 0) throw new IllegalArgumentException("clazzes.length == 0");
		int offset = 0;
		boolean found = false;
		while (!found && clazzes.length + offset <= size()) {
			if (startsWithObjectsOfClasses(offset, clazzes)) {
				found = true;
			} else {
				offset ++;
			}
		}
		return found ? offset : -1;
	}
	
	public int size() {
		return pathAtoms.size();
	}
	
	public void clear() {
		pathAtoms.clear();
	}		
}

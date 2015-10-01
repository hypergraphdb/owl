package org.hypergraphdb.app.owl.versioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.owl.util.CoffmanGraham;
import org.hypergraphdb.app.owl.versioning.change.VChange;
import org.hypergraphdb.util.Pair;

/**
 * <p>
 * Utility methods related to version management.
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class versioning
{
	/**
	 * Return true if revision <code>preceeding</code> is an ancestor
	 * in the {@link ParentLink} DAG of <code>subsequent</code>.
	 */
	public static boolean isPrior(HyperGraph graph, HGHandle preceeding, HGHandle subsequent)
	{
		return hg.findAll(graph, hg.dfs(preceeding, hg.type(ParentLink.class), null, true, false))
				 .contains(subsequent);
	}
	
	public static <V extends Versioned<V>, C extends VChange<V>> 
	List<C> normalize(V versioned, List<C> L)
	{
		return normalize(versioned, L, true);
	}
	
	public static <V extends Versioned<V>, C extends VChange<V>> 
	List<C> normalize(V versioned, List<C> L, boolean removeIneffective)
	{
		Set<Integer> toremove = new HashSet<Integer>();		
		for (int i = 0; i < L.size(); i++)
		{
			if (toremove.contains(i))
				continue;
			VChange<V> c = L.get(i);
			if (removeIneffective && !c.isEffective(versioned))
				toremove.add(i);
			VChange<V> ic = c.inverse();			
			if (c.isIdempotent())
			{
				if (ic == null || ic.isIdempotent())
				{
					// We want to keep only the last change equal to 'c' or to 
					// its inverse 'ic' - all the initial ones will be overridden
					// by the last.
					int last = i;
					for (int j = i + 1; j < L.size(); j++)
					{
						VChange<V> next = L.get(j);
						if (next.equals(c) || next.equals(ic))
						{
							toremove.add(last);
							last = j;
						}
						else 
						{
							@SuppressWarnings("unchecked")
							C merged = (C)next.reduce(c);
							if (merged != null)
							{
								toremove.add(i);
								L.set(j, merged);
							}
						}						
					}
				}
				else // non-idempotent inverse 
				{
					for (int j = i + 1; j < L.size(); j++)
					{
						C next = L.get(j);
						if (next.equals(c))
						{
							toremove.add(i);
							break;
						}
						else if (next.equals(ic))
						{
							toremove.add(i);
							toremove.add(j);
							break;
						}
						else 
						{
							@SuppressWarnings("unchecked")
							C merged = (C)next.reduce(c);
							if (merged != null)
							{
								toremove.add(i);
								L.set(j, merged);
							}
						}
					}					
				}
			}
			else if (ic != null)
			{
				for (int j = i + 1; j < L.size(); j++)
				{
					VChange<V> next = L.get(j);
					if (next.equals(ic))
					{
						toremove.add(i);
						toremove.add(j);
						break;
					}
					else 
					{
						@SuppressWarnings("unchecked")
						C merged = (C)next.reduce(c);
						if (merged != null)
						{
							toremove.add(i);
							L.set(j, merged);
						}
					}					
				}									
			}
			// else it's non-idempotent and it has no inverse, just try to merge with something else
			else for (int j = i + 1; j < L.size(); j++)
			{
				VChange<V> next = L.get(j);
				@SuppressWarnings("unchecked")
				C merged = (C)next.reduce(c);
				if (merged != null)
				{
					toremove.add(i);
					L.set(j, merged);
				}
			}				
		}
		List<C> normal = new ArrayList<C>();		
		for (int i = 0; i < L.size(); i++)
		{
			if (toremove.contains(i))
				continue;
			C c = L.get(i);
			normal.add(c);
		}		
		return normal;
	}
	
	public static <V extends Versioned<V>, C extends VChange<V>> 
	Set<Pair<C, C>> findConflicts(List<C> base, List<C> incoming)
	{
		Set<Pair<C, C>> conflicts = new HashSet<Pair<C, C>>();
		for (C in : incoming)
			for (C b : base)
				if (in.conflictsWith(b))
					conflicts.add(new Pair<C,C>(in, b));
		return conflicts;
	}

	private static void line(char[][] grid, int x1, int y1, int x2, int y2)
	{
		int dx = 0, dy = 0;
		if (x2 > x1) dx = 1; else if (x1 > x2) dx = -1;
		if (y2 > y1) dy = 1; else if (y1 > y2) dy = -1;
		for (int y = y1, x = x1; (y2 > y1 && y <= y2 || y2 < y1 && y >= y2); )
		{
			if (x == x2)
			{
				grid[y][x] = '|';
				y += dy;
				continue;
			}
			// slope of remaining line to draw
			float slope = ((float)y2 - (float)y)/(x2 - x);
			if (slope >= 0)
			{
				if (slope < 0.5)
				{
					grid[y][x] = '_';
					x += dx;
				}
				else
				{
					grid[y][x] = '\\';
					x += dx;
					y += dy;
				}
			}
			else
			{
				if (slope > -0.5)
				{
					grid[y][x] = '_';
					x += dx;
				}
				else
				{
					grid[y][x] = '/';
					x += dx;
					y += dy;					
				}
			}
		}
	}
	
	public static void printRevisionGraph(VersionedOntology versioned)
	{
		HyperGraph graph = versioned.graph();
		int width = 5;
		String hspace = "        ";
		int idlength = 8; // graph.getHandleFactory().nullHandle().toString().length();
		int vspace = 5;// graph.getHandleFactory().nullHandle().toString().length();
		CoffmanGraham algo = new CoffmanGraham(graph, versioned.getRootRevision());
		SortedMap<Integer, HGHandle[]> layers = algo.coffmanGrahamLayers(width);
		Map<HGHandle, Pair<Integer, Integer>> coordinates = new HashMap<HGHandle, Pair<Integer, Integer>>(); 
		int lineLength = width * (idlength + hspace.length());
		char [][] ascii = new char[layers.size() + (layers.size() - 1)*vspace][];
		
		for (int i = 0; i < layers.size(); i++)
		{
			int lineIndex = i*(vspace + 1);			
			ascii[lineIndex] = new char[lineLength];
			HGHandle [] data = layers.get(i + 1);
			String line = "";
			for (HGHandle current : data)
			{
				line += current.toString().substring(0, 8);
				Pair<Integer, Integer> C = new Pair<Integer, Integer>(line.length() - idlength / 2, lineIndex);
				coordinates.put(current, C);
				line += hspace;
			}
			System.arraycopy(line.toCharArray(), 0, ascii[lineIndex], 0, line.length());
			if (i < layers.size() - 1) for (int j = 1; j <= vspace; j++)				
			{
				ascii[lineIndex + j] = new char[lineLength];
				Arrays.fill(ascii[lineIndex + j], ' ');
			}
		}
		for (int i = 0; i < layers.size(); i++)
		{
			HGHandle [] data = layers.get(i + 1);
			for (HGHandle current : data)
			{
				List<HGHandle> parents = graph.findAll(hg.apply(
						hg.targetAt(graph, 1), 
						hg.and(hg.type(ParentLink.class), 
							   hg.orderedLink(current, hg.anyHandle()))));
				Pair<Integer, Integer> currentCoord = coordinates.get(current);
				for (HGHandle parent : parents)
				{
					Pair<Integer, Integer> parentCoord = coordinates.get(parent);
					if (parentCoord != null)
						line(ascii, parentCoord.getFirst(), 
									parentCoord.getSecond() - 1, 
									currentCoord.getFirst(), 
									currentCoord.getSecond() + 1);
				}
			}
		}
		System.out.println("Revision graph: " + versioned.ontology().getOntologyID().getOntologyIRI() 
				+ " @ " + graph.getLocation());
		System.out.println("----------------------------------------------------------------------------");
		for (char [] line : ascii)
			System.out.println(line);
		System.out.println("----------------------------------------------------------------------------");
	}
}
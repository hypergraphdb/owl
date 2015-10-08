package org.hypergraphdb.app.owl.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.versioning.ChangeLink;

public class CoffmanGraham
{
	private static final HGHandle [] EMPTY_HANDLE_ARRAY = new HGHandle[0];
	
	HyperGraph graph;
	HGHandle root;
	Map<HGHandle, int[]> parentSets = new HashMap<HGHandle, int[]>();
	Map<HGHandle, Integer> ordering = new HashMap<HGHandle, Integer>();

	public CoffmanGraham(HyperGraph graph, HGHandle root)
	{
		this.graph = graph;
		this.root = root;
	}

	// return true if any of the node in layerData is a child of
	// the 'node' parameter
	boolean hasChildren(int nodePosition, List<HGHandle> layerData)
	{
		for (HGHandle other : layerData)
		{
			int[] parentPositions = parentSets.get(other);
			if (parentPositions == null)
				continue;
			for (int parent : parentPositions)
				if (parent == nodePosition)
					return true;
		}
		return false;
	}

	int[] parentSet(HGHandle child)
	{
		int[] ps = parentSets.get(child);
		if (ps != null)
			return ps;
		List<ChangeLink> links = graph.getAll(hg.and(hg.type(ChangeLink.class), hg.orderedLink(hg.anyHandle(), hg.anyHandle(), child)));
		ps = new int[links.size()];
		for (int i = 0; i < ps.length; i++)
			ps[i] = ordering.get(links.get(i).parent());
		Arrays.sort(ps);
		parentSets.put(child, ps);
		return ps;
	}

	static boolean isSmaller(int set1[], int set2[])
	{
		if (set2 == null)
			return false;
		if (set1 == null)
			return true;
		final int smallerSize = Math.min(set1.length, set2.length);
		for (int x = 0; x < smallerSize; x++)
			if (set1[x] < set2[x])
				return true;

		return set1.length < set2.length;
	}

	// Topological ordering based on parents is done
	void orderNodes()
	{
		HashSet<HGHandle> candidates = new HashSet<HGHandle>();
		candidates.add(root);
		int position = 0;
		while (!candidates.isEmpty())
		{
			Iterator<HGHandle> candIter = candidates.iterator();
			HGHandle winner = candIter.next();
			int [] winnerParents = parentSet(winner);
			while (candIter.hasNext())
			{
				HGHandle candidate = candIter.next();
				int [] candidateParents = parentSet(candidate);
				if (isSmaller(candidateParents, winnerParents))
				{
					winner = candidate;
					winnerParents = candidateParents;
				}
			}
			ordering.put(winner, position++);
			candidates.remove(winner);
			// Add children of "winner" as new candidates
			candidates.addAll(graph.findAll(hg.apply(
					hg.targetAt(graph, 2), 
					hg.and(hg.type(ChangeLink.class), 
						   hg.orderedLink(winner, hg.anyHandle(), hg.anyHandle())))));
		}
	}

	public SortedMap<Integer, HGHandle[]> coffmanGrahamLayers(int width)
	{
		orderNodes();
		HGHandle[] available = new HGHandle[ordering.size()];
		for (Map.Entry<HGHandle, Integer> e : ordering.entrySet())
			available[e.getValue()] = e.getKey();

		SortedMap<Integer, HGHandle[]> layers = new TreeMap<Integer, HGHandle[]>();
		int layerIndex = 1;
		ArrayList<HGHandle> layerData = new ArrayList<HGHandle>();
		for (int position = available.length - 1; position >= 0; position--)
		{
			HGHandle current = available[position];
			if (layerData.size() == width || hasChildren(position, layerData))
			{
				layers.put(layerIndex, layerData.toArray(EMPTY_HANDLE_ARRAY));
				layerIndex++;
				layerData.clear();
			}
			layerData.add(current);
		}
		if (!layerData.isEmpty())
			layers.put(layerIndex, layerData.toArray(EMPTY_HANDLE_ARRAY));
		return layers;
	}
	
	public void minimizeCrossings(HGHandle [] top, HGHandle [] bottom)
	{
		
	}
}
package org.hypergraphdb.app.owl.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.hypergraphdb.HGEnvironment;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;

/**
 * GraphStats prints a sorted list of Classes in a given Graph and the instance
 * count of each.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Apr 5, 2012
 */
public class GraphStats
{

	private HashMap<Class<?>, Integer> classInstanceCounts = new HashMap<Class<?>, Integer>(250);

	public static void main(String[] argv)
	{
		if (argv.length < 1)
			die("Please specify the Hypergraph location directory as parameter.");
		GraphStats s = new GraphStats();
		HyperGraph graph = HGEnvironment.getExistingOnly(argv[0]);
		s.analyzeGraph(graph);
		s.printResults();
	}

	public static void die(String msg)
	{
		System.out.println(msg);
		System.exit(1);
	}

	public void analyzeGraph(HyperGraph graph)
	{
		List<HGHandle> allHandles = hg.findAll(graph, hg.all());
		System.out.println("Analyzing " + allHandles.size() + "  objects.");
		for (HGHandle cur : allHandles)
		{
			HGHandle typeHandle = graph.getType(cur);
			Class<?> c = graph.getTypeSystem().getClassForType(typeHandle);
			Integer counted = classInstanceCounts.get(c);
			if (counted == null)
				counted = 0;
			classInstanceCounts.put(c, 1 + counted);
		}
	}

	DecimalFormat df = new DecimalFormat("####000");

	public void printResults()
	{
		int i = 0;
		int accumulated = 0;
		System.out.println("Graph Analysis Results: ");
		ArrayList<Class<?>> keys = new ArrayList<Class<?>>(classInstanceCounts.keySet());
		Collections.sort(keys, new Comparator<Class<?>>()
		{
			@Override
			public int compare(Class<?> o1, Class<?> o2)
			{
				if (o1 == null)
				{
					if (o2 == null)
						return 0;
					else
						return 1;
				}
				else if (o2 == null)
					return -1;
				else
				{
					return o1.getSimpleName().compareTo(o2.getSimpleName());
				}
			}

		});
		for (Class<?> c : keys)
		{
			accumulated += classInstanceCounts.get(c);
			System.out.println(df.format(i) + " " + df.format(classInstanceCounts.get(c)) + " "
					+ (c == null ? "No Java Type" : c.getSimpleName()) + " \t \t Total: " + df.format(accumulated));
			i++;
		}
		System.out.println("Graph Analysis Results Done. Total objects: " + accumulated);
	}
}

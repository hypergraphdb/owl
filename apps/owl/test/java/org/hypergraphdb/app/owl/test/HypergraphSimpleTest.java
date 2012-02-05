package org.hypergraphdb.app.owl.test;



import java.util.Date;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * HypergraphSimpleTest.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 7, 2011
 */
public class HypergraphSimpleTest {

	StopWatch s = new StopWatch();
	
	//String prefix = "012345678901234567890";
	HyperGraph graph;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		HGDBOntologyRepository r = HGDBOntologyRepository.getInstance();
		graph = r.getHyperGraph();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		graph.close();
	}
	
	@Test
	public void simpleTest() {
		int n = 100;
		while (n < 1E8) {
			System.out.println("----------------------------------------");
			System.out.println("--" + new Date());
			System.out.println("Graph has: " + graph.count(hg.all()) + " Atoms ");
			long startT = System.nanoTime();		
			addSimpleIfNotFound(n);
			findRemoveAll(n);
			s.stop("-Add/Remove took: ", startT);
			n = 2 * n;
		}		
	}
	
	public void addSimpleIfNotFound(int n) {
		System.out.println("Adding n randoms, if not found. n = " + n);
		s.start();
		int found = 0;
		for (int i =0; i < n; i++) {
			Integer in = new Integer((int)(Math.random() * n));			
			if (graph.getOne(hg.eq(in)) == null) {
				graph.add(in);
			} else {
				found ++;
			}
		}
		System.out.println("Added: " + (n - found) + " Add Ratio: " + ((n - found) /(double)n));
		s.stop("-addSimpleIfNotFound : ");		
	}

	public void findRemoveAll(int n) {
		System.out.println("Removing each in range 0..n, if found.");
		s.start();
		int found = 0;
		for (int i =0; i < n; i++) {
			HGHandle inH = graph.findOne(hg.eq(new Integer(i)));
			if (inH != null) {
				found ++;
				graph.remove(inH);
			}
		}
		System.out.println("Removed: " + found + " Rem Ratio: " + (found/(double)n));
		s.stop("-findRemoveAll : ");
	}

}

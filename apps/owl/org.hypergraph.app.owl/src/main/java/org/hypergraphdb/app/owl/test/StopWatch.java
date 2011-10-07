package org.hypergraphdb.app.owl.test;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * StopWatch with nano precision printing secs on stopcall.
 * Stop also calls start(). 
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 7, 2011
 */
public class StopWatch {
	
	private long start;	
	NumberFormat secFormat = new DecimalFormat("####00.00");
	
	public void start() {
		start = System.nanoTime();
	}
	public double stop() {
		return stop(null);
	}

	public double stop(String s) {		
		return stop(s, start);
	}

	public double stop(String s, long starttime) {		
		double secs = (System.nanoTime() - starttime) / 1E9d;
		if (s != null) System.out.println(s + " " + secFormat.format(secs) + " secs");
		start();
		return secs;
	}

}

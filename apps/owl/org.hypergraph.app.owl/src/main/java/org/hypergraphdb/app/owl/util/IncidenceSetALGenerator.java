package org.hypergraphdb.app.owl.util;

import java.util.Iterator;

import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.algorithms.HGALGenerator;
import org.hypergraphdb.util.Pair;

/**
 * <p>
 * The <code>IncidenceSetALGenerator</code> produces all atoms in the incidence set of a given atom.
 * </p>
 *  
 * @author Thomas Hilpold, Borislav Iordanov
 */
public class IncidenceSetALGenerator implements HGALGenerator 
{
	private HyperGraph graph;
	
	private class IncidenceSetIterator implements HGSearchResult<Pair<HGHandle,HGHandle>>
	{
		HGHandle srcHandle;
		Iterator<HGHandle> srcIncidenceSetIterator;
		Pair<HGHandle,HGHandle> current;
				
		IncidenceSetIterator(HGHandle src)
		{
			this.srcHandle = src;
			srcIncidenceSetIterator = graph.getIncidenceSet(src).iterator();
		}
		
		public void remove() { throw new UnsupportedOperationException(); }
		
		public boolean hasNext()
		{
			return srcIncidenceSetIterator.hasNext();
		}
		
		public Pair<HGHandle,HGHandle> next()
		{
			HGHandle next = srcIncidenceSetIterator.next();
			current = new Pair<HGHandle,HGHandle>(srcHandle, next);	        
			return current;
		}

		public void close()
		{			
			//do nothing
		}

		public Pair<HGHandle,HGHandle> current()
		{
			return current;
		}

		public boolean isOrdered()
		{
			return false;
		}

		public boolean hasPrev() { 
			throw new UnsupportedOperationException();
		}
		
		public Pair<HGHandle,HGHandle> prev() { 
			throw new UnsupportedOperationException();
		}				
	}

	/**
	 * <p>
	 * Empty constructor - you will need to set the graph (see {@link setGraph}) before
	 * the instance becomes usable.
	 * </p>
	 */
	public IncidenceSetALGenerator()
	{		
	}
	
	/**
	 * <p>Construct a <code>TargetSetALGenerator</code> for the given HyperGraph instance.</p>
	 * 
	 * @param hg The HyperGraph instance.
	 */
	public IncidenceSetALGenerator(HyperGraph hg)
	{
		this.graph = hg;
	}
	
	public HGSearchResult<Pair<HGHandle,HGHandle>> generate(HGHandle h) 
	{
		return new IncidenceSetIterator(h);
	}
	
	
	public void setGraph(HyperGraph graph)
	{
		this.graph = graph;
		
	}
	
	public HyperGraph getGraph()
	{
		return this.graph;
	}
}
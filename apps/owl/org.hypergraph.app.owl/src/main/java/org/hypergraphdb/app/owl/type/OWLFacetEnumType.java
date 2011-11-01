package org.hypergraphdb.app.owl.type;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.type.HGProjection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.OWLFacet;

/**
 * OWLFacetEnumType represents OWLFacet in the graph.
 * The type stores only the symbolicForm field of an OWLFacet in the graph as this is sufficient to identify the enum.
 * It uses OWLFacet's static list to retrieve the appropriate enum instance by symbolicForm. 
 * All dimensions can be used for query, index, et.c.
 * 
 * We could optimize OWLFacetEnumType.make from O(n) to O(1) by using a Hashmap (shortForm, OWLFacet).
 * (OWLFacet.getFacetBySymbolicName(..) does a O(n) list search.)
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 1, 2011
 */
public class OWLFacetEnumType extends HGAtomTypeBase implements HGCompositeType {
	public static final String DIM_IRI = "IRI";
	public static final String DIM_SHORTNAME = "shortname";
	public static final String DIM_SYMBOLICFORM = "symbolicForm";
	public static final List<String> DIMENSIONS = Collections.unmodifiableList(Arrays.asList(DIM_IRI, DIM_SHORTNAME, DIM_SYMBOLICFORM));
	
	public Object make(HGPersistentHandle handle,
					   LazyRef<HGHandle[]> targetSet, 
					   IncidenceSetRef incidenceSet)
	{
		HGHandle [] layout = graph.getStore().getLink(handle);
		String  symbolicForm = graph.get(layout[0]);
		if (symbolicForm == null) throw new NullPointerException("symbolicForm missing for OWLFacetEnum at " + handle);
		//Get the facet by SymbolicForm
		//TODO use a map for faster access 
		return OWLFacet.getFacetBySymbolicName(symbolicForm);
	} 

	public void release(HGPersistentHandle handle)
	{
		graph.getStore().removeLink(handle);
	}

	public HGPersistentHandle store(Object instance)
	{
		OWLFacet facet = (OWLFacet)instance;
		HGHandle symbolicFormHandle = hg.assertAtom(graph, facet.getSymbolicForm());
		return graph.getStore().store(new HGPersistentHandle[]{symbolicFormHandle.getPersistent()}); 
	}

	//
	// HGCompositeType Interface
	//
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.type.HGCompositeType#getDimensionNames()
	 */
	@Override
	public Iterator<String> getDimensionNames() {		
		return DIMENSIONS.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.type.HGCompositeType#getProjection(java.lang.String)
	 */
	@Override
	public HGProjection getProjection(String dimensionName) {
		System.out.println("DIM:" + dimensionName);
		if (DIM_IRI.equals(dimensionName))
			return new HGProjection() {

				@Override
				public int[] getLayoutPath() {
					return null;
				}

				@Override
				public String getName() {
					return DIM_IRI;
				}

				@Override
				public HGHandle getType() {
					return graph.getTypeSystem().getTypeHandle(IRI.class);
				}

				@Override
				public void inject(Object atomValue, Object value) {
				}

				@Override
				public Object project(Object atomValue) {
					return ((OWLFacet) atomValue).getIRI();
				}

			};
			else if (DIM_SHORTNAME.equals(dimensionName))
			return new HGProjection() {

			@Override
			public int[] getLayoutPath() {
				return null;
			}

			@Override
			public String getName() {
				return DIM_SHORTNAME;
			}

			@Override
			public HGHandle getType() {
				return graph.getTypeSystem().getTypeHandle(String.class);
			}

			@Override
			public void inject(Object atomValue, Object value) {
			}

			@Override
			public Object project(Object atomValue) {
				return ((OWLFacet) atomValue).getShortName();
			}

		};
		else if (DIM_SYMBOLICFORM.equals(dimensionName))
			return new HGProjection() {

			@Override
			public int[] getLayoutPath() {
				return null;
			}

			@Override
			public String getName() {
				return DIM_SYMBOLICFORM;
			}

			@Override
			public HGHandle getType() {
				return graph.getTypeSystem().getTypeHandle(String.class);
			}

			@Override
			public void inject(Object atomValue, Object value) {
			}

			@Override
			public Object project(Object atomValue) {
				return ((OWLFacet) atomValue).getSymbolicForm();
			}

		};
		else
			throw new IllegalArgumentException();
	}
}

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
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * OntologyIDType, a Hypergraph composite type.
 *
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @author Borislav Iordanovic (GIC/Miami-Dade County)
 */
public class OntologyIDType extends HGAtomTypeBase implements HGCompositeType
{
	public static final String DIM_DEFAULT_DOCUMENT_IRI = "defaultDocumentIRI";
	public static final String DIM_ONTOLOGY_IRI = "ontologyIRI";
	public static final String DIM_VERSION_IRI = "versionIRI";
	public static final List<String> DIMENSIONS = Collections.unmodifiableList(Arrays.asList(DIM_DEFAULT_DOCUMENT_IRI, DIM_ONTOLOGY_IRI, DIM_VERSION_IRI));
	
	public Object make(HGPersistentHandle handle,
					   LazyRef<HGHandle[]> targetSet, 
					   IncidenceSetRef incidenceSet)
	{
		if (handle.equals(graph.getHandleFactory().anyHandle()))
			return new OWLOntologyID(); // anonymous
		HGHandle [] layout = graph.getStore().getLink(handle);
		IRI ontiri = graph.get(layout[0]);
		if (ontiri == null)
			throw new NullPointerException("IRI missing for OWLOntologyID at " + handle);
		IRI veriri = layout[1].equals(graph.getHandleFactory().nullHandle()) ? null :
					 (IRI)graph.get(layout[1]);
		return new OWLOntologyID(ontiri, veriri);
	} 

	public void release(HGPersistentHandle handle)
	{
		graph.getStore().removeLink(handle);
	}

	public HGPersistentHandle store(Object instance)
	{
		OWLOntologyID oid = (OWLOntologyID)instance;
		if (oid.isAnonymous())
			return graph.getHandleFactory().anyHandle();
		HGHandle onthandle = hg.assertAtom(graph, oid.getOntologyIRI());
		HGHandle verhandle = null;
		if (oid.getVersionIRI() != null)
			verhandle = hg.assertAtom(graph, oid.getVersionIRI());
		else
			verhandle = graph.getHandleFactory().nullHandle();			
		return graph.getStore().store(new HGPersistentHandle[]{onthandle.getPersistent(),
															   verhandle.getPersistent()}); 
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
	// TODO Auto-generated method stub
	@Override
	public HGProjection getProjection(String dimensionName) {
		System.out.println("DIM:" + dimensionName);
		if (DIM_DEFAULT_DOCUMENT_IRI.equals(dimensionName))
			return new HGProjection() {

				@Override
				public int[] getLayoutPath() {
					return null;
				}

				@Override
				public String getName() {
					return DIM_DEFAULT_DOCUMENT_IRI;
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
					return ((OWLOntologyID) atomValue).getDefaultDocumentIRI();
				}

			};
			else if (DIM_ONTOLOGY_IRI.equals(dimensionName))
			return new HGProjection() {

			@Override
			public int[] getLayoutPath() {
				return null;
			}

			@Override
			public String getName() {
				return DIM_ONTOLOGY_IRI;
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
				return ((OWLOntologyID) atomValue).getOntologyIRI();
			}

		};
		else if (DIM_VERSION_IRI.equals(dimensionName))
			return new HGProjection() {

			@Override
			public int[] getLayoutPath() {
				return null;
			}

			@Override
			public String getName() {
				return DIM_VERSION_IRI;
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
				return ((OWLOntologyID) atomValue).getVersionIRI();
			}

		};
		else
			throw new IllegalArgumentException();
	}
}
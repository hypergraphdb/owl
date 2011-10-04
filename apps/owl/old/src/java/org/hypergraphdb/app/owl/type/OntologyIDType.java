package org.hypergraphdb.app.owl.type;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

public class OntologyIDType extends HGAtomTypeBase
{
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
}
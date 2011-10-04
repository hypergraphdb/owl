package org.hypergraphdb.app.owl.type;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.semanticweb.owlapi.model.IRI;

public class IRIType extends HGAtomTypeBase
{
	public Object make(HGPersistentHandle handle,
			LazyRef<HGHandle[]> targetSet, IncidenceSetRef incidenceSet)
	{
		return IRI.create((String)graph.getTypeSystem().getAtomType(String.class).make(handle, targetSet, incidenceSet));
	}

	public void release(HGPersistentHandle handle)
	{
		graph.getTypeSystem().getAtomType(String.class).release(handle);
	}

	public HGPersistentHandle store(Object instance)
	{
		return graph.getTypeSystem().getAtomType(String.class).store(((IRI)instance).toString());
	}
}
package org.hypergraphdb.app.owl;

import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.owl.type.IRIType;
import org.hypergraphdb.type.HGAtomType;
import org.semanticweb.owlapi.model.IRI;

public class HGOWLAPP extends HGApplication
{
	private void createTypes(HyperGraph graph)
	{
		HGAtomType type = new IRIType();
		type.setHyperGraph(graph);;
		graph.getTypeSystem().addPredefinedType(graph.getHandleFactory().makeHandle(), type, IRI.class);
	}
	
	public void install(HyperGraph graph)
	{
		createTypes(graph);
	}

	public void reset(HyperGraph graph)
	{
		// TODO Auto-generated method stub
	}

	public void uninstall(HyperGraph graph)
	{
		// TODO Auto-generated method stub
	}

	public void update(HyperGraph graph)
	{
		// TODO Auto-generated method stub
	}
}
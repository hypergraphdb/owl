package org.hypergraphdb.app.owl;

import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.management.HGApplication;
import org.hypergraphdb.app.owl.type.IRIType;
import org.hypergraphdb.type.HGAtomType;
import org.semanticweb.owlapi.model.IRI;

public class HGOWLAPP extends HGApplication
{
	private void createTypes(HyperGraph graph)
	{
		HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
		HGAtomType type = new IRIType();
		type.setHyperGraph(graph);
		
		graph.getTypeSystem().addPredefinedType(typeHandle, 
												type, 
												IRI.class);
		try
		{
			graph.getTypeSystem().addPredefinedType(typeHandle, 
					type, 
					Class.forName("org.semanticweb.owlapi.model.IRI$IRIImpl"));
		}
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
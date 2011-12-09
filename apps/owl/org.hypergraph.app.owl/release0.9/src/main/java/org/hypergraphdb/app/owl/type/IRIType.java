package org.hypergraphdb.app.owl.type;

import java.util.Comparator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.storage.BAUtils;
import org.hypergraphdb.storage.ByteArrayConverter;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.semanticweb.owlapi.model.IRI;

/**
 * IRIType.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 30, 2011
 */
public class IRIType extends HGAtomTypeBase implements ByteArrayConverter<IRI>, Comparator<byte[]>
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

	/* (non-Javadoc)
	 * @see org.hypergraphdb.storage.ByteArrayConverter#toByteArray(java.lang.Object)
	 */
	@Override
	public byte[] toByteArray(IRI object) {		
		return object.toString().getBytes();
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.storage.ByteArrayConverter#fromByteArray(byte[])
	 */
	@Override
	public IRI fromByteArray(byte[] byteArray) {
		return IRI.create(new String(byteArray));
	}
	
	 public int compare(byte [] left, byte []right)
     {
		 int c = BAUtils.compare(left, 0, right, 0, Math.min(left.length, right.length));
		 if (c == 0)
		 {
			 if (left.length == right.length)
				 return 0;
			 else if  (left.length > right.length)
				 return -1;
			 else 
				 return 1;
		 }
		 else
			 return c;
     }	
}
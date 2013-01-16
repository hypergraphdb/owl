package org.hypergraphdb.app.owl.type;

import java.io.Serializable;
import java.util.Comparator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGOrderedSearchable;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGSortIndex;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.storage.BAUtils;
import org.hypergraphdb.storage.BAtoBA;
import org.hypergraphdb.storage.BAtoHandle;
import org.hypergraphdb.storage.ByteArrayConverter;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.hypergraphdb.type.HGPrimitiveType;
import org.hypergraphdb.type.javaprimitive.StringType;
import org.semanticweb.owlapi.model.IRI;

/**
 * IRIType is a type for IRIs that maps IRIS to Strings by exploiting StringType
 * functionality including the StringType's index.
 * 
 * @see org.hypergraphdb.type.javaprimitive.StringType
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Nov 30, 2011
 */
public class IRIType extends HGAtomTypeBase implements
        HGOrderedSearchable<IRI, HGPersistentHandle>, ByteArrayConverter<IRI>,
        HGPrimitiveType<IRI>
{

    // old Comparator<byte[]>, Serializable
    /**
     * This has to match the offset as defined in Stringtype, as we are using
     * the Stringtypes index.
     * 
     * @see org.hypergraphdb.type.javaprimitive.StringType.
     */
    private int dataOffset = 4; // Like Stringtype

    protected HGSortIndex<byte[], HGPersistentHandle> valueIndex = null;

    private static IRIComparator comparatorInstance;

    public Object make(HGPersistentHandle handle,
                       LazyRef<HGHandle[]> targetSet,
                       IncidenceSetRef incidenceSet)
    {
        return IRI.create((String) graph.getTypeSystem().getAtomType(
                String.class).make(handle, targetSet, incidenceSet));
    }

    public void release(HGPersistentHandle handle)
    {
        graph.getTypeSystem().getAtomType(String.class).release(handle);
    }

    public HGPersistentHandle store(Object instance)
    {
        return graph.getTypeSystem().getAtomType(String.class).store(
                ((IRI) instance).toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.hypergraphdb.storage.ByteArrayConverter#toByteArray(java.lang.Object)
     */
    @Override
    public byte[] toByteArray(IRI object)
    {
        return object.toString().getBytes();// Charset.forName("US-ASCII"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.storage.ByteArrayConverter#fromByteArray(byte[])
     */
    @Override
    public IRI fromByteArray(byte[] byteArray, int offset, int length)
    {
        return IRI.create(new String(byteArray, offset, length));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.HGSearchable#find(java.lang.Object)
     */
    @Override
    public HGSearchResult<HGPersistentHandle> find(IRI key)
    {
        return getIndex().find(objectAsBytes(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.HGOrderedSearchable#findLT(java.lang.Object)
     */
    @Override
    public HGSearchResult<HGPersistentHandle> findLT(IRI key)
    {
        return getIndex().findLT(objectAsBytes(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.HGOrderedSearchable#findGT(java.lang.Object)
     */
    @Override
    public HGSearchResult<HGPersistentHandle> findGT(IRI key)
    {
        return getIndex().findGT(objectAsBytes(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.HGOrderedSearchable#findLTE(java.lang.Object)
     */
    @Override
    public HGSearchResult<HGPersistentHandle> findLTE(IRI key)
    {
        return getIndex().findLTE(objectAsBytes(key));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.HGOrderedSearchable#findGTE(java.lang.Object)
     */
    @Override
    public HGSearchResult<HGPersistentHandle> findGTE(IRI key)
    {
        return getIndex().findGTE(objectAsBytes(key));
    }

    //
    // From hypergraph org.hypergraphdb.type.javaprimitive.StringType
    //
    /**
     * @see org.hypergraphdb.type.javaprimitive.StringType
     */
    protected final HGSortIndex<byte[], HGPersistentHandle> getIndex()
    {
        if (valueIndex == null)
        {
            StringType s = (StringType) graph.getTypeSystem().getAtomType(
                    String.class);
            Comparator<byte[]> comparator = s.getComparator();

            valueIndex = (HGSortIndex<byte[], HGPersistentHandle>) graph
                    .getStore().getIndex(StringType.INDEX_NAME,
                            BAtoBA.getInstance(),
                            BAtoHandle.getInstance(graph.getHandleFactory()),
                            comparator, true);
        }
        return valueIndex;
    }

    /**
     * @see org.hypergraphdb.type.javaprimitive.StringType
     */
    private byte[] objectAsBytes(IRI instance)
    {
        byte data[] = stringToBytes(instance.toString());
        byte full[] = new byte[dataOffset + data.length];
        System.arraycopy(data, 0, full, dataOffset, data.length);
        return full;
    }

    /**
     * @see org.hypergraphdb.type.javaprimitive.StringType
     */
    private byte[] stringToBytes(String s)
    {
        byte[] data;

        if (s == null)
        {
            data = new byte[1];
            data[0] = 0;
        }
        else if (s.length() == 0)
        {
            data = new byte[1];
            data[0] = 1;
        }
        else
        {
            byte[] asBytes = s.getBytes();
            data = new byte[1 + asBytes.length];
            data[0] = 2;
            System.arraycopy(asBytes, 0, data, 1, asBytes.length);
        }
        return data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hypergraphdb.type.HGPrimitiveType#getComparator()
     */
    @Override
    public Comparator<byte[]> getComparator()
    {
        if (comparatorInstance == null)
        {
            synchronized (this)
            {
                if (comparatorInstance == null)
                {
                    comparatorInstance = new IRIComparator();
                }
            }
        }
        return comparatorInstance;
    }

    public static class IRIComparator implements Comparator<byte[]>,
            Serializable
    {

        private static final long serialVersionUID = 1L;

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(byte[] left, byte[] right)
        {
            int c = BAUtils.compare(left, 0, right, 0, Math.min(left.length,
                    right.length));
            if (c == 0)
            {
                if (left.length == right.length)
                    return 0;
                else if (left.length > right.length)
                    return -1;
                else
                    return 1;
            }
            else
                return c;
        }
    }
}
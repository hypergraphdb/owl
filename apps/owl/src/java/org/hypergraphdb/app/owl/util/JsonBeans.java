package org.hypergraphdb.app.owl.util;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;

import org.hypergraphdb.HGException;

import mjson.Json;

/**
 * <p>
 * Implements serialization of Java beans to and from JSON. This is a prototype class
 * in the HGDB-OWL codebase, to be eventually promoted as a separate library/plugin
 * on top of MJSON.
 * </p>
 * 
 * <p>
 * Constructing a JSON from a bean needs to be paired with the reverse operation of 
 * re-building the bean from the JSON. This is accomplished by an the {@link #bean(Json)}
 * method.
 * </p>
 * 
 * <p>
 * The representation of the bean is as simply as a JSON object contains all 
 * the properties of the bean. The following remarks apply:
 * <ul>
 * <li>:type - this is a special property containing the fully qualified classname of the 
 * bean. This makes use of the fact that a Java identifier cannot contain a 
 * colon character.</li>
 * <li>array - array properties are naturally represented as JSON arrays. However, the type
 * of the arrays must be serialized as well. We put it in a colon-prefixed property with
 * the same name. For example if the bean has an array property <em>aliases</em> of type 
 * String[] the JSON representation will look like { ..., "aliases" : ["alias1", "alias2",...],
 * ":aliases":"java.lang.String", ...}. </li>
 * <li>Circular structures are not supported by assigning each object an ID, an integer
 * that is unique to the serialization taking place. Thus along with the special <em>:type</em>
 * property, each JSON object will have an <em>:id</em>. Then if that object is being referred
 * to in a circular fashion, instead of serializing it again, a JSON with a single <em>:id</em>
 * property is used, e.g. { ":id" : 10 }.</li>
 * <li></li> 
 * </ul>
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class JsonBeans extends Json.DefaultFactory 
{
    private static final JsonBeans instance = new JsonBeans();
    
    public static JsonBeans instance() { return instance; }
    
    private Object getProperty(Object bean, PropertyDescriptor prop, int index)
    {
        try
        {
            Method method = null;
            if (prop instanceof IndexedPropertyDescriptor)
                if (index < 0)
                    method = prop.getReadMethod();
                else
                    method = ((IndexedPropertyDescriptor) prop)
                            .getIndexedReadMethod();
            else if (index >= 0)
                throw new java.lang.UnsupportedOperationException("Property "
                        + prop.getName() + " of bean "
                        + bean.getClass().getName()
                        + " is not an indexed property.");
            else
                method = prop.getReadMethod();
            if (method == null)
                throw new java.lang.UnsupportedOperationException("Property "
                        + prop.getName() + " of bean "
                        + bean.getClass().getName() + " is not readable.");
            if (index < 0)
                return method.invoke(bean, (Object[]) null);
            else
                return method.invoke(bean, new Object[] { new Integer(index) });
        }
        catch (IllegalAccessException ex)
        {
            throw new HGException("Illegal access to property "
                    + prop.getName() + " of bean " + bean.getClass().getName()
                    + " it is probably a private property: " + ex.toString());
        }
        catch (InvocationTargetException ex)
        {
            throw new HGException("InvocationTargetException while accessing "
                    + "property " + prop.getName() + " of bean "
                    + bean.getClass().getName() + ex.toString()
                    + ", target exception is "
                    + ex.getTargetException().toString(), ex);
        }
    }
    
    private static void setProperty(Object bean, 
                                    PropertyDescriptor prop,
                                    int index, 
                                    Object newvalue)
    {
        try
        {
            Method method = null;
            if (prop instanceof IndexedPropertyDescriptor)
                if (index < 0)
                    method = prop.getWriteMethod();
                else
                    method = ((IndexedPropertyDescriptor) prop)
                            .getIndexedWriteMethod();
            else if (index >= 0)
                throw new java.lang.UnsupportedOperationException("Property "
                        + prop.getName() + " of bean "
                        + bean.getClass().getName()
                        + " is not an indexed property.");
            else
                method = prop.getWriteMethod();
            if (method == null)
                throw new java.lang.UnsupportedOperationException("Property "
                        + prop.getName() + " of bean "
                        + bean.getClass().getName()
                        + " is not mutable (writeable).");
            if (index < 0)
                method.invoke(bean, new Object[] { newvalue });
            else
                method.invoke(bean,
                        new Object[] { new Integer(index), newvalue });
        }
        catch (IllegalAccessException ex)
        {
            throw new HGException("Illegal access to property "
                    + prop.getName() + " of bean " + bean.getClass().getName()
                    + " it is probably a private property: " + ex.toString());
        }
        catch (InvocationTargetException ex)
        {
            throw new HGException("InvocationTargetException while accessing "
                    + "property " + prop.getName() + " of bean "
                    + bean.getClass().getName() + ex.toString()
                    + ", taget exception is "
                    + ex.getTargetException().toString(), ex);
        }
    }
    
    private void collectObjects(Json x, HashMap<Integer, Object> objects) throws Exception
    {
        if (x.has(":id") && x.has(":type"))
        {
            Class<?> cl = Class.forName(x.at(":type").asString());
            BeanInfo bean_info = Introspector.getBeanInfo(cl);
            Object bean = cl.newInstance();
            objects.put(x.at(":id").asInteger(), bean);
            // First pass instantiates
            for (String propname : x.asJsonMap().keySet())
            {
                if (propname.startsWith(":"))
                    continue;
                collectObjects(x.at(propname), objects);
            }            
            // second pass populates
            for (PropertyDescriptor desc : bean_info.getPropertyDescriptors())
            {
                if (!x.has(desc.getName()))
                    continue;
                Json p = x.at(desc.getName());
                if (p.isObject() && p.has(":id"))
                    setProperty(bean, desc, -1, objects.get(p.at(":id").asInteger()));
                else
                    setProperty(bean, desc, -1, p.getValue());                
            }
        }
    }
    
    /**
     * <p>
     * De-serialize a bean back from its JSON representation. The representation
     * is expected to be obtained from the {@link #make(Object)} method, but you can pass
     * any JSON element to this method and if it doesn't look like a bean, 
     * its value will be returned (<code>x.getValue()</code>).
     * </p>
     * 
     * @param x The JSON representation of the bean.
     * @return A newly instantiated bean populated from <code>x</code>.
     */
    public Object object(Json x)
    {
        if (!x.isObject() || !x.has(":id") || !x.has(":type"))
            return x.getValue();
        try
        {
            HashMap<Integer, Object> objects = new HashMap<Integer, Object>();
            collectObjects(x, objects);
            return objects.get(x.at(":id").asInteger());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    private Json internalMake(Object x, IdentityHashMap<Object, Integer> ids, int lastId)
    {
        Json result = super.make(x);
        if (result != null)
            return result;
        else
            result = Json.object();
        try
        {
            result.set(":type", x.getClass().getName());
            BeanInfo bean_info = Introspector.getBeanInfo(x.getClass());
            for (PropertyDescriptor desc : bean_info.getPropertyDescriptors())
            {
                Object value = getProperty(x, desc, -1);
                Integer id = ids.get(value);
                if (id == null)
                {
                    ++lastId;
                    ids.put(value, lastId);
                    result.set(desc.getName(), make(value).set(":id", lastId));
                }
                else
                    result.set(desc.getName(), Json.object(":id", id));                
            }
            return result;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public Json make(Object x)
    {
        return internalMake(x, new IdentityHashMap<Object, Integer>(), 0);
    }
}
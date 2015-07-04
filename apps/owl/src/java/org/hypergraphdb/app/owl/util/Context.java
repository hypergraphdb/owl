package org.hypergraphdb.app.owl.util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Associate a "Context" with arbitrary objects. Then each context has a bunch
 * of singletons that can be accessed, lazily created.
 * 
 * @author Borislav Iordanov
 *
 */
public class Context 
{
	static Map<Object, Context> contexts = new IdentityHashMap<Object, Context>();
	
	public static Context of(Object ref)
	{
		synchronized (contexts)
		{
			Context c = contexts.get(ref);
			if (c == null)
			{
				c = new Context();
				contexts.put(ref, c);
			}
			return c;
		}
	}
	
	public static void drop(Object ref)
	{
		contexts.remove(ref);
	}
	
	Map<Class<?>, Object> singletons = new HashMap<Class<?>, Object>();
	
	public <T> T singleton(Class<T> type, Callable<T> factory)
	{
		synchronized (singletons)
		{
			@SuppressWarnings("unchecked")
			T x = (T)singletons.get(type);
			if (x == null)
				try
				{
					x = factory.call();
					singletons.put(type, x);
				}
				catch (Exception ex)
				{
					throw new RuntimeException(ex);
				}
			return x;
		}		
	}
	
	public <T> T singleton(final Class<T> type)
	{
		return singleton(type, new Callable<T>() {
			public T call() throws Exception
			{
				return type.newInstance();
			}
		});
	}
}

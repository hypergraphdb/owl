package org.hypergraphdb.app.owl.util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class Context 
{
	static Map<Object, Context> contexts = new IdentityHashMap<Object, Context>();
	
	public static Context get(Object ref)
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
	
	Map<Class<?>, Object> singletons = new HashMap<Class<?>, Object>();
	
	public <T> T singleton(Class<T> type)
	{
		synchronized (singletons)
		{
			@SuppressWarnings("unchecked")
			T x = (T)singletons.get(type);
			if (x == null)
				try
				{
					x = type.newInstance();
					singletons.put(type, x);
				}
				catch (Exception ex)
				{
					throw new RuntimeException(ex);
				}
			return x;
		}
	}
}

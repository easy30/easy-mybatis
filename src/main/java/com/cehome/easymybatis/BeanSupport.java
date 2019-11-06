package com.cehome.easymybatis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class BeanSupport {

	final static Logger logger = LoggerFactory.getLogger(BeanSupport.class);
	private static final Map primitiveDefaults = new HashMap();
	protected static final int PROPERTY_NOT_FOUND = -1;

	static
	{
		primitiveDefaults.put(Integer.TYPE, new Integer(0));
		primitiveDefaults.put(Short.TYPE, new Short((short) 0));
		primitiveDefaults.put(Byte.TYPE, new Byte((byte) 0));
		primitiveDefaults.put(Float.TYPE, new Float(0));
		primitiveDefaults.put(Double.TYPE, new Double(0));
		primitiveDefaults.put(Long.TYPE, new Long(0));
		primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
		primitiveDefaults.put(Character.TYPE, new Character('\u0000'));
	}


	
	private static PropertyDescriptor[] propertyDescriptors(Class c) throws SQLException
	{
		// Introspector caches BeanInfo classes for better performance
		BeanInfo beanInfo = null;
		try
		{
			beanInfo = Introspector.getBeanInfo(c);

		}
		catch (IntrospectionException e)
		{
			throw new SQLException("Bean introspection failed: " + e.getMessage());
		}

		return beanInfo.getPropertyDescriptors();

	}



	
	private static Field getField0(Class c, String name){
		Field[] fs=c.getDeclaredFields();
		for(Field f:fs){
			if(f.getName().equals(name)) return f;
		}
		return null;
	}
	public static Field getField(Class clasz, String name){
		Field  f=getField0(clasz,name);
		if(f!=null) return f;
		 Class<?>[] interfaces =clasz.getInterfaces();
	        for (int i = 0; i < interfaces.length; i++) {
	            Class<?> c = interfaces[i];
	            f=getField(c,name);
	    		if(f!=null) return f;
	        }
	        // Direct superclass, recursively
	        if (!clasz.isInterface()) {
	            Class<?> c = clasz.getSuperclass();
	            if(c!=null) f=getField(c,name);
	    		if(f!=null) return f;
	        }
	        return null;
		
	}

	private static boolean arrayIndexOf(String[] array, String s) {
		if (array == null || array.length == 0)
			return false;
		if (s == null) {
			for (String a : array) {
				if (a == null)
					return true;
			}
		} else {
			for (String a : array) {
				if(s.equalsIgnoreCase(a))return true;
			}
		}
		return false;
	}
	
}

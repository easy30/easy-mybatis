package com.cehome.easymybatis.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class ObjectSupport {

	
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

	public static Class getGenericInterfaces(final Class clazz,final int interfaceIndex, final int index) {

		Type genType = clazz.getGenericInterfaces()[interfaceIndex];
		return getGenricType(genType,index);
	}

	public static Class getSuperClassGenricType(final Class clazz, final int index) {

		Type genType = clazz.getGenericSuperclass();
		return getGenricType(genType,index);
	}

	private static Class getGenricType(Type genType , final int index) {


		if (!(genType instanceof ParameterizedType)) {
			// logger.warn(clazz.getSimpleName() +
			// "'s superclass not ParameterizedType");
			return Object.class;
		}

		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

		if (index >= params.length || index < 0) {
			// logger.warn("Index: " + index + ", Size of " +
			// clazz.getSimpleName() + "'s Parameterized Type: " +
			// params.length);
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			// logger.warn(clazz.getSimpleName() +
			// " not set the actual class on superclass generic parameter");
			return Object.class;
		}

		return (Class) params[index];
	}

	public static void setFieldValue(Object object, String fieldName, Object value){
		try {
			Field field=object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(object,value);
		} catch (Exception e) {
		   throw new RuntimeException(e);
		}

	}

	public static <T>T getFieldValue(Class clazz, Object object, String fieldName) {
		try {
			Field field=clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T)field.get(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}

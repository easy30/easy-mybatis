package com.github.easy30.easymybatis.utils;


import org.springframework.cglib.proxy.Enhancer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;


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

	/**
	 *
	 * @param clazz
	 * @param object  for static field , object can be null
	 * @param fieldName
	 * @param <T>
	 * @return
	 */
	public static <T>T getFieldValue(Class clazz, Object object, String fieldName) {
		try {
			Field field=clazz.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T)field.get(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Method getMethod(Class clazz,String methodName){
		for (Method m : clazz.getMethods()) {
			if (m.getName().equals(methodName)) return m;
		}
		return null;
	}

	public static Method getMethod(Class clazz,String methodName, Class<?>... parameterTypes){
		try {
			return clazz.getMethod(methodName,parameterTypes);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static  <T extends Annotation> T getAnnotation(Class<T> annotationClass, Field field, Method method){
		T  t=null;
		if(field!=null) {
			t=field.getAnnotation(annotationClass);
		}
		if(t==null && method!=null)  {
			t=method.getAnnotation(annotationClass);
		}
		return t;
	}
	public static  <T extends Annotation> T getAnnotation(Class<T> annotationClass,Class clazz, String prop){
		Field field = ObjectSupport.getField(clazz, prop);
		T  t=null;
		if(field!=null) {
			t = field.getAnnotation(annotationClass);
		}
		if(t==null) {
			Method method=null;
			BeanInfo beanInfo = null;
			try {
				beanInfo = Introspector.getBeanInfo(clazz);
			} catch (IntrospectionException e) {
				throw new RuntimeException(e);
			}
			for(PropertyDescriptor pd:  beanInfo.getPropertyDescriptors()){
				String prop2=pd.getName();
				if (prop2.equals(prop)) {
					method=pd.getReadMethod();
					break;
				}

			}
			if(method!=null) {
				t = method.getAnnotation(annotationClass);
			}
		}
		return t;
	}
	public static  <T extends Annotation> T getAnnotation(Class<T> annotationClass,Class clazz, PropertyDescriptor pd){
		Field field = ObjectSupport.getField(clazz, pd.getName());
		T  t=null;
		if(field!=null) {
			t = field.getAnnotation(annotationClass);
		}
		if(t==null) {

			Method method=pd.getReadMethod();
			if(method!=null) {
				t = method.getAnnotation(annotationClass);
			}
		}
		return t;
	}

	public static Object getProperty(Object bean, String name){
		if (bean == null) {
			throw new IllegalArgumentException("Bean is null");
		}
		if (name == null || name.length() == 0) {
			throw new IllegalArgumentException("Property name is null or empty");
		}
		// 获取bean的class
		Class<?> clazz = bean.getClass();
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);
			for(PropertyDescriptor pd:  beanInfo.getPropertyDescriptors()){
				if (pd.getName().equals(name)) {
				   return  pd.getReadMethod().invoke(bean);
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException("can not find property: "+name);

	}
}

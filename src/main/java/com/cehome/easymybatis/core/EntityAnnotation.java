package com.cehome.easymybatis.core;


import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.annotation.ColumnInsertDefault;
import com.cehome.easymybatis.annotation.ColumnUpdateDefault;
import com.cehome.easymybatis.utils.ObjectSupport;
import com.cehome.easymybatis.utils.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 

import javax.persistence.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Entity Annotation Helper
 * 
 * @author ma
 * 
 */

/**
 * @author ma
 *
 */
public class EntityAnnotation
{
	private  static Logger logger= LoggerFactory.getLogger(EntityAnnotation.class);
	private static Map<Class, EntityAnnotation> beanMap =new  HashMap<Class, EntityAnnotation> ();;


	private String table = null;
	private String idName=null;

	private List<PropertyDescriptor>  idProperties=new ArrayList();
	private List<String> idPropertyNames=new ArrayList();
	private List<String> idColumnNames=new ArrayList();
	private Map<String, ColumnAnnotation> propertyColumnMap = new HashMap<String, ColumnAnnotation> ();
	private Set<String> transientColumnSet = new HashSet<String> ();
	private Set<String> lobColumnSet= new HashSet<String> ();
	//private PropertyDescriptor[] properties =null;
	private Map<String,PropertyDescriptor> propertyMap=new HashMap<String, PropertyDescriptor> ();
	private Class entityClass=null;
	private boolean dialectEntity;

	public static EntityAnnotation getInstance(Class entityClass)
	{
		// cblib child class to parent class
		//if(Enhancer.isEnhanced(entityClass)) entityClass=entityClass.getSuperclass();
		EntityAnnotation ba= beanMap.get(entityClass);
		if(ba==null)
		{
			synchronized(beanMap)
			{
				ba= beanMap.get(entityClass);
				if(ba==null)
				{
					ba=new EntityAnnotation(entityClass);
					beanMap.put(entityClass, ba);
				}
			}
		}

		return ba;
	}

	public static EntityAnnotation getInstanceByMapper(Class mapperClass){
		return getInstance(ObjectSupport.getGenericInterfaces(mapperClass,0,0));
	}


	public Set<String> getLobColumnSet() {
		return lobColumnSet;
	}

	public void setLobColumnSet(Set<String> lobColumnSet) {
		this.lobColumnSet = lobColumnSet;
	}

	/**
	 *
	 * @return
	 */
	public Set<String> getTransientColumnSet() {
		return transientColumnSet;
	}

	public void setTransientColumnSet(Set<String> transientColumnSet) {
		this.transientColumnSet = transientColumnSet;
	}

	/**
	 *  db field name for id
	 * @return
     */
	public String getIdName()
	{
		return idName;
	}

	/**
	 * 设置ID字段名称
	 * @param idName
	 */
	public void setIdName(String idName)
	{
		this.idName = idName;
	}



	/**
	 * 实体类的表名注解
	 * @return
	 */
	public String getTable()
	{
		return table;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	/**
	 * readMethod名跟数据库字段名映射表
	 * @return
	 */
	public Map<String, ColumnAnnotation> getPropertyColumnMap()
	{
		return propertyColumnMap;
	}

	public void setPropertyColumnMap(Map<String, ColumnAnnotation> propertyColumnMap)
	{
		this.propertyColumnMap = propertyColumnMap;
	}

   private  <T extends Annotation> T findFieldAnnotation(Class<T> clazz, String fieldName,Class<T> annotationClass ) {
       T annotation = null;
       try {
           Field field = clazz.getDeclaredField(fieldName);
           if (field != null) {
               annotation = field.getAnnotation(annotationClass);
           }
       } catch (SecurityException e) {
           //e.printStackTrace();
       } catch (NoSuchFieldException e) {
          // e.printStackTrace();
       }
       return annotation;
   }

   /**
    * 获取字段或方法上面的指定类型的注解；如果两者都有则报错。
    * @param <T>
    * @param c
    * @param method
    * @param fieldName
    * @param annotationClass
    * @return
    */
   private  <T extends Annotation> T getMethodorFieldAnn(Class<T> c,  Method method, String fieldName,Class<T> annotationClass )
   {

	   T t1 =method==null?null: method.getAnnotation(annotationClass);
	    T t2=findFieldAnnotation(c,fieldName ,annotationClass);
	    if(t1!=null && t2!=null) throw new RuntimeException("Can not define Annotation both on filed and method!");
		return t1==null?t2:t1;
   }

	public EntityAnnotation(Class c)
	{
		//if (Enhancer.isEnhanced(c)) c=c.getSuperclass();
		this.entityClass=c;
		dialectEntity= DialectEntity.class.isAssignableFrom(entityClass);
		Table t = (Table) c.getAnnotation(Table.class);
		if (t != null && t.name()!=null && t.name().length()>0) table = t.name();
		//ColumnUnderscore columnUnderscore=(ColumnUnderscore)c.getAnnotation(ColumnUnderscore.class);
		boolean columnUnderscoreSupport=true;//(t!=null && t.columnUnderscoreSupport()) || (columnUnderscore!=null ) ;
		//System.out.println(table);

		//Field[] fields=c.getDeclaredFields();

		PropertyDescriptor[] pds= propertyDescriptors(c);
		for (PropertyDescriptor pd : pds)
		{
			String name=pd.getName();
			if (name.equals("class")) continue;
			Method method = pd.getReadMethod();
			Field field = ObjectSupport.getField(c, name);
			if(field==null) {
				Transient trans=method.getAnnotation(Transient.class);
				if(trans==null)
				throw  new RuntimeException("can not find field: "+name);
				else continue;
			}
			propertyMap.put(name, pd);
			//String methodName = method.getName();
			//--@Column
			//Column column1 = method.getAnnotation(Column.class);
			//Column column2=findFieldAnnotation(c,pd.getName(),Column.class);
			//if(column1!=null && column2!=null) throw new RuntimeException("Can not define Annotation both on filed and method!");
			ColumnAnnotation ca=new ColumnAnnotation();
			propertyColumnMap.put(pd.getName(), ca);

			if(getMethodorFieldAnn(c,method, name  ,javax.persistence.Column.class)!=null)
			{
				throw new RuntimeException("please use 'websharp.persistence.Column' instead of 'javax.persistence.Column' for "+c);
			}

			Column column=getMethodorFieldAnn(c,method, name  ,Column.class);
			if (column != null)
			{

				//columnMap.put(methodName, name);
				ca.setName(column.name()==null||column.name().isEmpty()?
						(columnUnderscoreSupport?this.camelCaseToUnderscore(name): name):column.name());
				ca.setInsertable(column.insertable());
				ca.setUpdatable(column.updatable());
				ca.setNullable(column.nullable());
				ca.setLength(column.length());
				ca.setPrecision(column.precision());
				ca.setScale(column.scale());
			//	fa.setDefaultValue(column.defaultValue());
				ca.setColumnDefinition(column.columnDefinition());


				//System.out.println(name);
			}
			else ca.setName(columnUnderscoreSupport?this.camelCaseToUnderscore(name): name);

			ColumnInsertDefault columnInsertDefault=getMethodorFieldAnn(c,method, name  ,ColumnInsertDefault.class);
			if(columnInsertDefault!=null){

				ca.setColumnInsertDefault(columnInsertDefault.value());

			}

			ColumnUpdateDefault columnUpdateDefault=getMethodorFieldAnn(c,method, name  ,ColumnUpdateDefault.class);
			if(columnUpdateDefault!=null){

				ca.setColumnUpdateDefault(columnUpdateDefault.value());

			}


			//-- @Id注解
			Id id=field.getAnnotation(Id.class);
			if(id==null && method!=null) id= method.getAnnotation(Id.class);
			if(id!=null)
			{
				ca.setIdentitied(true);
				setIdName(ca.getName());

				idProperties.add(pd);
				idPropertyNames.add(pd.getName());
				idColumnNames.add(ca.getName());

			}

			//--@Transient
			Transient  trans=field.getAnnotation(Transient.class);
			if(trans==null)  trans=method.getAnnotation(Transient.class);
			if(trans!=null) ca.setTransient(true);        //transientColumnSet.add(methodName);

			//--@Lob
			Lob lob=field.getAnnotation(Lob.class);
			if(lob==null) lob=method.getAnnotation(Lob.class);
			if(lob!=null)
				{
					ca.setLob(true);//  this.lobColumnSet.add(methodName);
					if(pd.getPropertyType().equals(String.class)) ca.setClob(true);
					else ca.setBlob(true);
				}



		}

		//properties=propertyMap.values().toArray(new PropertyDescriptor[0]);


	}

	public Object getProperty(Object object,String property){
		PropertyDescriptor pd=propertyMap.get(property);
		if(pd==null) throw new RuntimeException("property '"+property+"' not found");
		try {
			return pd.getReadMethod().invoke(object);
		} catch ( Exception e) {
			throw new RuntimeException(e);
		}

	}

	//--  cameTo came_to
	private String camelCaseToUnderscore(String s){
		if(s==null)return s;
		StringBuilder sb=new StringBuilder(s.length());
		for(int i=0;i<s.length();i++){
			char c=s.charAt(i);
			if(Character.isUpperCase(c)){
				sb.append("_").append(Character.toLowerCase(c));
			}else{
				sb.append(c);
			}
		}
		return sb.toString();
	}

	/*
	public PropertyDescriptor[] getProperties()
	{
		return properties;
	}*/
	
	public Class getEntityClass()
	{
		return entityClass;
	}
	
	
	private PropertyDescriptor[] propertyDescriptors(Class c)  
	{
		// Introspector caches BeanInfo classes for better performance
		BeanInfo beanInfo = null;
		try
		{
			beanInfo = Introspector.getBeanInfo(c);


		}
		catch (IntrospectionException e)
		{
			throw new RuntimeException("Bean introspection failed: " + e.getMessage());
		}

		return beanInfo.getPropertyDescriptors();

	}
	 
	public String getColumnByProperty(String name)
	{
		ColumnAnnotation fa= propertyColumnMap.get(name);
		if(fa!=null) return fa.getName();
		logger.warn("no column mapping found for "+name);
		return name;
		 
		
	}

	public List<PropertyDescriptor> getIdProperties() {
		return idProperties;
	}

	public List<String> getIdPropertyNames() {
		return idPropertyNames;
	}

	public List<String> getIdColumnNames() {
		return idColumnNames;
	}

	public boolean isDialectEntity(){
   		return dialectEntity;
	}

	public Object getDialectProperty(Object entity,String property){
   		if(!isDialectEntity()) return null;
   		 Class c=entity.getClass();
   		 while(c!=DialectEntity.class) c=c.getSuperclass();
		Map<String, String> dialectMap=(Map<String, String>) ObjectSupport.getFieldValue(c,entity, Const.DIALECT_MAP);
		return dialectMap==null?null:dialectMap.get(property);
	}


}

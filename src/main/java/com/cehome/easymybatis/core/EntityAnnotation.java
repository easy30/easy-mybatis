package com.cehome.easymybatis.core;


import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.Generation;
import com.cehome.easymybatis.annotation.ColumnDefault;
import com.cehome.easymybatis.annotation.ColumnGeneration;
import com.cehome.easymybatis.annotation.EntitySelectKey;
import com.cehome.easymybatis.dialect.Dialect;
import com.cehome.easymybatis.utils.ObjectSupport;
import com.cehome.easymybatis.Const;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedCaseInsensitiveMap;


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
 */
public class EntityAnnotation {
    private static Logger logger = LoggerFactory.getLogger(EntityAnnotation.class);
    private static Map<Class, EntityAnnotation> beanMap = new HashMap<Class, EntityAnnotation>();
    ;

    private Set<String> transientColumnSet = new HashSet<String>();
    private Set<String> lobColumnSet = new HashSet<String>();

    private String table = null;


    //private List<PropertyDescriptor>  idProperties=new ArrayList();
    private List<String> idPropertyNames = new ArrayList();
    private List<String> idColumnNames = new ArrayList();
    private Map<String, ColumnAnnotation> propertyColumnMap = new HashMap<String, ColumnAnnotation>();
    private Map<String, ColumnAnnotation> columnMap = new LinkedCaseInsensitiveMap<ColumnAnnotation>();

    //private PropertyDescriptor[] properties =null;
    private Map<String, PropertyDescriptor> propertyDescriptorMap = new HashMap<String, PropertyDescriptor>();
    private Class entityClass = null;
    //private boolean dialectEntity;
    private EntitySelectKey entitySelectKey;
    private Dialect dialect;

    public static EntityAnnotation getInstance(Class entityClass) {
        // cblib child class to parent class
        //if(Enhancer.isEnhanced(entityClass)) entityClass=entityClass.getSuperclass();
        EntityAnnotation ba = beanMap.get(entityClass);
        if (ba == null) {
            synchronized (beanMap) {
                if (Map.class.isAssignableFrom(entityClass)) {
                    throw new RuntimeException("entity class can not be a Map");
                }
                ba = beanMap.get(entityClass);
                if (ba == null) {
                    ba = new EntityAnnotation(entityClass);
                    beanMap.put(entityClass, ba);
                }
            }
        }

        return ba;
    }

    public static EntityAnnotation getInstanceByMapper(Class mapperClass) {
        return getInstance(ObjectSupport.getGenericInterfaces(mapperClass, 0, 0));
    }


    public Set<String> getLobColumnSet() {
        return lobColumnSet;
    }

    public void setLobColumnSet(Set<String> lobColumnSet) {
        this.lobColumnSet = lobColumnSet;
    }

    /**
     * @return
     */
    public Set<String> getTransientColumnSet() {
        return transientColumnSet;
    }

    public void setTransientColumnSet(Set<String> transientColumnSet) {
        this.transientColumnSet = transientColumnSet;
    }


    /**
     * 实体类的表名注解
     *
     * @return
     */
    public String getTable() {
        return table;
    }

/*	public void setTable(String table)
	{
		this.table = table;
	}*/

    /**
     * prop,ColumnAnnotation  map
     * @return
     */
    public Map<String, ColumnAnnotation> getPropertyColumnMap() {
        return propertyColumnMap;
    }

    /**
     *  column,ColumnAnnotation  map
     * @return
     */
    public Map<String, ColumnAnnotation> getColumnMap() {
        return columnMap;
    }

    public Map<String, PropertyDescriptor> getPropertyDescriptorMap() {
        return propertyDescriptorMap;
    }

    private <T extends Annotation> T findFieldAnnotation(Class<T> clazz, String fieldName, Class<T> annotationClass) {
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


/*
   private  <T extends Annotation> T getMethodorFieldAnn(Class<T> c,  Method method, String fieldName,Class<T> annotationClass )
   {

	   T t1 =method==null?null: method.getAnnotation(annotationClass);
	    T t2=findFieldAnnotation(c,fieldName ,annotationClass);
	    if(t1!=null && t2!=null) throw new RuntimeException("Can not define Annotation both on filed and method!");
		return t1==null?t2:t1;
   }
*/

    public EntityAnnotation(Class clazz) {
        //if (Enhancer.isEnhanced(c)) c=c.getSuperclass();
        this.entityClass = clazz;
        //dialectEntity= DialectEntity.class.isAssignableFrom(entityClass);

        //ColumnUnderscore columnUnderscore=(ColumnUnderscore)c.getAnnotation(ColumnUnderscore.class);
        boolean columnUnderscoreSupport = true;//(t!=null && t.columnUnderscoreSupport()) || (columnUnderscore!=null ) ;
        //System.out.println(table);
        doWithTable();

        setEntitySelectKey((EntitySelectKey) entityClass.getAnnotation(EntitySelectKey.class));


        PropertyDescriptor[] pds = propertyDescriptors(clazz);
        for (PropertyDescriptor pd : pds) {

            String prop = pd.getName();
            if (prop.equals("class")) continue;
            try {

                if(prop.equalsIgnoreCase("showBargain")){
                    System.out.println("DDD");
                }
                Method method = pd.getReadMethod();
                Field field = ObjectSupport.getField(clazz, prop);
                if(method==null && field==null){
                    continue;
                }
			/*if(field==null) {
				Transient trans=method.getAnnotation(Transient.class);
				if(trans==null)
				throw  new RuntimeException("can not find field: "+prop);
				else continue;
			}*/

                propertyDescriptorMap.put(prop, pd);

                //--@Column
                ColumnAnnotation ca = new ColumnAnnotation();
                ca.setPropName(prop);
                propertyColumnMap.put(pd.getName(), ca);



                doWithColumn(field, method, ca, prop, columnUnderscoreSupport);
                doWithId(pd, field, method, ca);
                doWithColumnGenerator(field, method, ca);
                doWithColumnDefault(field, method, ca);
                columnMap.put(ca.getName(),ca);

                //--@Transient
                Transient trans = getAnnotation(Transient.class, field, method);
                if (trans != null) ca.setTransient(true);        //transientColumnSet.add(methodName);

                //--@Lob
                Lob lob = getAnnotation(Lob.class, field, method);
                if (lob != null) {
                    ca.setLob(true);//  this.lobColumnSet.add(methodName);
                    if (pd.getPropertyType().equals(String.class)) ca.setClob(true);
                    else ca.setBlob(true);
                }
            } catch (Exception e) {
                logger.error("do with property '" + prop + "' cause error", e);
                throw e;
            }


        }


    }


    private void doWithColumn(Field field, Method method, ColumnAnnotation ca, String prop, boolean columnUnderscoreSupport) {
        Column column = getAnnotation(Column.class, field, method);
        if (column != null) {

            ca.setName(column.name() == null || column.name().isEmpty() ?
                    (columnUnderscoreSupport ? this.camelCaseToUnderscore(prop) : prop) : column.name());
            ca.setInsertable(column.insertable());
            ca.setUpdatable(column.updatable());
            ca.setNullable(column.nullable());
            ca.setLength(column.length());
            ca.setPrecision(column.precision());
            ca.setScale(column.scale());
            //	fa.setDefaultValue(column.defaultValue());
            ca.setColumnDefinition(column.columnDefinition());


        } else ca.setName(columnUnderscoreSupport ? this.camelCaseToUnderscore(prop) : prop);


    }

    private void doWithColumnDefault(Field field, Method method, ColumnAnnotation ca) {
        ColumnDefault columnDefault = getAnnotation(ColumnDefault.class, field, method);
        if (columnDefault != null) {
            String value = StringUtils.isBlank(columnDefault.insertValue()) ? columnDefault.value() : columnDefault.insertValue();
            if (StringUtils.isNotBlank(value)) ca.setColumnInsertDefault(value);
            value = StringUtils.isBlank(columnDefault.updateValue()) ? columnDefault.value() : columnDefault.updateValue();
            if (StringUtils.isNotBlank(value)) ca.setColumnUpdateDefault(value);

        }
    }

    private void doWithTable() {
        Table t = (Table) entityClass.getAnnotation(Table.class);
        if (t != null && t.name() != null && t.name().length() > 0) table = t.name();
    }

    private void doWithId(PropertyDescriptor pd, Field field, Method method, ColumnAnnotation ca) {
        //-- @Id注解
        Id id = getAnnotation(Id.class, field, method);
        if (id != null) {
            ca.setIdentitied(true);
            //setIdName(ca.getName());

            //idProperties.add(pd);
            idPropertyNames.add(pd.getName());
            idColumnNames.add(ca.getName());

        }
    }

    /*private void doWithIdGenerator(){
        IdGeneration idGeneration =(IdGeneration)entityClass.getAnnotation(IdGeneration.class);
        if(idGeneration !=null){
            String generatorName= idGeneration.name();
            String generatorArg= idGeneration.arg();
            Generators generators=Generators.getInstance();

            if(StringUtils.isBlank(generatorName)){
                if(generators.getPrimary()==null){
                    throw new RuntimeException("no default Generator found for class "+entityClass
                            +". Please set generator name or keep only one primary Generator bean");

                }
                setIdGeneration(generators.getPrimary());

            }else{
                Generation generation =generators.get(generatorName);
                if(generation ==null){
                    throw new RuntimeException("Generator bean '"+ generatorName+"' not found for class "+entityClass);
                }
                setIdGeneration(generation);
            }
             setIdGeneratorArg(generatorArg);

        }
    }*/
    private void doWithColumnGenerator(Field field, Method method, ColumnAnnotation ca) {
        ColumnGeneration columnGeneration = getAnnotation(ColumnGeneration.class, field, method);
        if (columnGeneration != null) {
            String generatorName = columnGeneration.insertGeneration();
            String generatorArg = columnGeneration.insertArg();
            Generations generations = Generations.getInstance();


            Generation generation = generations.get(generatorName);
            if (generation == null) {
                throw new RuntimeException("Generator bean '" + generatorName + "' not found for class " + entityClass);
            }
            ca.setGeneration(generation);

            ca.setGeneratorArg(generatorArg);

        }
    }

    private <T extends Annotation> T getAnnotation(Class<T> annotationClass, Field field, Method method) {
        return ObjectSupport.getAnnotation(annotationClass, field, method);
    }

    public Object getProperty(Object object, String property) {
        PropertyDescriptor pd = propertyDescriptorMap.get(property);
        if (pd == null) throw new RuntimeException("property '" + property + "' not found");
        try {
            return pd.getReadMethod().invoke(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void setProperty(Object object, String property, Object value) {
        PropertyDescriptor pd = propertyDescriptorMap.get(property);
        if (pd == null) throw new RuntimeException("property '" + property + "' not found");
        try {
            pd.getWriteMethod().invoke(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    //--  cameTo came_to
    private String camelCaseToUnderscore(String s) {
        if (s == null) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
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

    public Class getEntityClass() {
        return entityClass;
    }


    private PropertyDescriptor[] propertyDescriptors(Class c) {
        // Introspector caches BeanInfo classes for better performance
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(c);


        } catch (IntrospectionException e) {
            throw new RuntimeException("Bean introspection failed: " + e.getMessage());
        }

        return beanInfo.getPropertyDescriptors();

    }
	 
	/*public String getColumnByProperty(String name)
	{
		ColumnAnnotation fa= propertyColumnMap.get(name);
		if(fa!=null) return fa.getName();
		logger.warn("no column mapping found for "+name);
		return name;
		 
		
	}*/

/*	public List<PropertyDescriptor> getIdProperties() {
		return idProperties;
	}*/

    public List<String> getIdPropertyNames() {
        return idPropertyNames;
    }

    public List<String> getIdColumnNames() {
        return idColumnNames;
    }

    public boolean isDialectEntity() {
        return DialectEntity.class.isAssignableFrom(entityClass);
    }

    private boolean isDialectEntity(Object entity) {
        return DialectEntity.class.isAssignableFrom(entity.getClass());
    }

    public Object getDialectValue(Object entity, String property) {
        if (!isDialectEntity(entity)) return null;
        Class c = entity.getClass();
        while (c != DialectEntity.class) c = c.getSuperclass();
        Map<String, String> dialectMap = (Map<String, String>) ObjectSupport.getFieldValue(c, entity, Const.VALUE_MAP);
        return dialectMap == null ? null : dialectMap.get(property);
    }

    public Object getDialectParam(Object entity, String property) {
        if (!isDialectEntity(entity)) return null;
        Class c = entity.getClass();
        while (c != DialectEntity.class) c = c.getSuperclass();
        Map<String, String> dialectMap = (Map<String, String>) ObjectSupport.getFieldValue(c, entity, Const.PARAM_MAP);
        return dialectMap == null ? null : dialectMap.get(property);
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void setLogger(Logger logger) {
        EntityAnnotation.logger = logger;
    }


    public EntitySelectKey getEntitySelectKey() {
        return entitySelectKey;
    }

    public void setEntitySelectKey(EntitySelectKey entitySelectKey) {
        this.entitySelectKey = entitySelectKey;
    }


    public String getColumnName(String prop) {
        ColumnAnnotation columnAnnotation = getPropertyColumnMap().get(prop);
        if (columnAnnotation == null) return null;
        return columnAnnotation.getName();
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }
}

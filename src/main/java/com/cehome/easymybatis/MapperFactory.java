package com.cehome.easymybatis;

import com.cehome.easymybatis.annotation.EntitySelectKey;
import com.cehome.easymybatis.core.*;
import com.cehome.easymybatis.core.DialectFactory;
import com.cehome.easymybatis.dialect.Dialect;
import com.cehome.easymybatis.utils.ObjectSupport;
import com.cehome.easymybatis.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * coolma 2019/10/25
 **/
public class MapperFactory implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {
    private static Boolean loaded = false;

   
    SqlSessionTemplate sqlSessionTemplate;
    SqlSessionFactory sqlSessionFactory;
    Configuration configuration;
    Set<String> methodNameSet = new HashSet<String>();
    //Map<String, AbstractMethodBuilder> methodBuilderMap = new HashMap<String, AbstractMethodBuilder>();
    private Dialect dialect;

    private String dialectName;


    private Class[] mapperInterfaces;

    public MapperFactory() {

    }


    public SqlSessionTemplate getSqlSessionTemplate() {
        return sqlSessionTemplate;
    }

    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public SqlSessionFactory getSqlSessionFactory() {
        return sqlSessionFactory;
    }

    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        if(sqlSessionFactory !=null) configuration= sqlSessionFactory.getConfiguration();
        else if(sqlSessionTemplate!=null) configuration=configuration;
        else throw new RuntimeException("SqlSessionTemplate or SqlSessionFactory not found");

        dialect=DialectFactory.createDialect(dialectName,configuration);

        //-- default config
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);



    }

    /*private void addBuilders(Class c) throws Exception {
        for (Method method : c.getMethods()) {
            methodNameSet.add(method.getName());
            MethodBuilder mb = method.getAnnotation(MethodBuilder.class);
            if (mb == null || mb.value() == null) {
                if (!methodBuilderMap.containsKey(method.getName())) {
                    throw new RuntimeException("@MethodBuilder need for " + c.getName() + "." + method.getName());
                }
            }
            AbstractMethodBuilder methodBuilder = mb.value().newInstance();
            if (!method.getName().equals(methodBuilder.getMethodName().equals(method.getName())))
                throw new RuntimeException("method name not match, need" + method.getName() + ", but found "
                        + methodBuilder.getMethodName() + " in " + methodBuilder.getClass().getName());
            methodBuilderMap.put(methodBuilder.getMethodName(), methodBuilder);

        }
    }*/

    public Class[] getMapperInterfaces() {
        return mapperInterfaces;
    }

    public void setMapperInterfaces(Class[] mapperInterfaces) {
        this.mapperInterfaces = mapperInterfaces;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        configuration.addInterceptor(new DefaultInterceptor(dialect));
        ApplicationContext context=event.getApplicationContext();

        initGenerators(context);

        Map<String, MapperFactoryBean> beanMap = context.getBeansOfType(MapperFactoryBean.class);

        //Map<String,Mapper> beanMap = event.getApplicationContext().getBeansOfType(Mapper.class);
        if (beanMap != null || beanMap.size() > 0) {
            for (Map.Entry<String, MapperFactoryBean> e : beanMap.entrySet()) {
                Class mapperClass = e.getValue().getObjectType();
                add(mapperClass);

            }
        }

    }

    private void initGenerators( ApplicationContext context){
        Generations generations = Generations.getInstance();
        Map<String, Generation> beans=context.getBeansOfType(Generation.class);
        generations.putAll(beans);

    }

    public void add(Class mapperClass) {
        String namespace = mapperClass.getName();
        Class entityClass = ObjectSupport.getGenericInterfaces(mapperClass, 0, 0);
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        entityAnnotation.setDialect(dialect);
        String resource = namespace.replace('.', '/') + ".java (best guess)";
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, resource);
        assistant.setCurrentNamespace(namespace);

        for (Method method : mapperClass.getMethods()) {
            //AbstractMethodBuilder methodBuilder= methodBuilderMap.get(method.getName());
            //if(methodBuilder!=null) methodBuilder.add(assistant,mapperClass,entityClass,entityAnnotation);
            if(method.isDefault() || Object.class.equals(method.getDeclaringClass())) continue;
            MappedStatement ms = configuration.getMappedStatement(namespace + "." + method.getName());
            if (ms != null) {
                if (ms.getSqlCommandType().equals(SqlCommandType.INSERT)) {
                    doKeyGenerator(mapperClass,entityClass,method,ms);

                }

            }


        }
    }

    private void doKeyGenerator(Class mapperClass,Class entityClass,Method method,MappedStatement ms){
        KeyGenerator keyGenerator=ms.getKeyGenerator();
        // SelectKey exists ,so do nothing
        if(keyGenerator!=null && keyGenerator instanceof SelectKeyGenerator) return;

       final EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        final EntitySelectKey entitySelectKey=entityAnnotation.getEntitySelectKey();

        List<String> idPropertyNames=entityAnnotation.getIdPropertyNames();
        List<String> idColumnNames=entityAnnotation.getIdColumnNames();
        if(idPropertyNames.size()==0) return;


        if (entitySelectKey !=null ) {

            //org.apache.ibatis.builder.annotation.MapperAnnotationBuilder.handleSelectKeyAnnotation
            SelectKey selectKey=new SelectKey(){

                @Override
                public Class<? extends Annotation> annotationType() {
                    return SelectKey.class;
                }

                @Override
                public String[] statement() {
                    return entitySelectKey.statement();
                }

                @Override
                public String keyProperty() {
                    if(!StringUtils.isBlank(entitySelectKey.keyProperty())) return entitySelectKey.keyProperty();
                    if(entityAnnotation.getIdPropertyNames().size()>1 ){

                            throw new RuntimeException("keyProperty can not be empty for multiple columns key");

                    }
                    return Utils.toString(entityAnnotation.getIdPropertyNames(),",",null);
                }

                @Override
                public String keyColumn() {
                    if(!StringUtils.isBlank(entitySelectKey.keyColumn()))  return entitySelectKey.keyColumn();
                    if(!StringUtils.isBlank(entitySelectKey.keyProperty())){
                        String kc="";
                        Map<String, ColumnAnnotation> propertyColumnMap=entityAnnotation.getPropertyColumnMap();
                        for(String p: entitySelectKey.keyProperty().split(",")){
                            if(kc.length()>0) kc+=",";
                            kc+=propertyColumnMap.get(p).getName();
                        }
                        return kc;
                    }
                    return Utils.toString(entityAnnotation.getIdColumnNames(),",",null);


                }

                @Override
                public boolean before() {
                    return entitySelectKey.before();
                }

                @Override
                public Class<?> resultType() {
                    return entitySelectKey.resultType();
                }

                @Override
                public StatementType statementType() {
                    return entitySelectKey.statementType();
                }
            };



            MapperAnnotationBuilder  builder=new MapperAnnotationBuilder(configuration,mapperClass);

            //invok private method : handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver)

            Method handleSelectKeyAnnotationMethod=ObjectSupport.getMethod(  MapperAnnotationBuilder.class,"handleSelectKeyAnnotation");

            Method getParameterTypeMethod=ObjectSupport.getMethod(  MapperAnnotationBuilder.class,"getParameterType");
            getParameterTypeMethod.setAccessible(true);

            handleSelectKeyAnnotationMethod.setAccessible(true);
            try {
                keyGenerator=(KeyGenerator)handleSelectKeyAnnotationMethod.invoke(builder,selectKey,ms.getId(),
                        getParameterTypeMethod.invoke(builder,method),ms.getLang());
            } catch ( Exception e) {
                throw new RuntimeException(e);
            }


        }

        if(entitySelectKey==null) keyGenerator=Jdbc3KeyGenerator.INSTANCE;
        // set private field
        ObjectSupport.setFieldValue(ms, "keyProperties",idPropertyNames.toArray(new String[0]));
        ObjectSupport.setFieldValue(ms, "keyColumns", idColumnNames.toArray(new String[0]));
        ObjectSupport.setFieldValue(ms, "keyGenerator", keyGenerator);


    }

    public String getDialectName() {
        return dialectName;
    }

    public void setDialectName(String dialectName) {
        this.dialectName = dialectName;
    }
}

package com.cehome.easymybatis;

import com.cehome.easymybatis.builder.MethodBuilder;
import com.cehome.easymybatis.builder.AbstractMethodBuilder;
import com.cehome.easymybatis.core.*;
import com.cehome.easymybatis.utils.ObjectSupport;
import com.cehome.easymybatis.utils.Utils;
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
    Map<String, AbstractMethodBuilder> methodBuilderMap = new HashMap<String, AbstractMethodBuilder>();


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

        //addBuilders(Mapper.class);

        // custom mapper interfaces
       /* if (mapperInterfaces != null) {
            for (Class c : mapperInterfaces) {
                //addBuilders(c);
            }
        }*/


    }

    private void addBuilders(Class c) throws Exception {
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
    }

    public Class[] getMapperInterfaces() {
        return mapperInterfaces;
    }

    public void setMapperInterfaces(Class[] mapperInterfaces) {
        this.mapperInterfaces = mapperInterfaces;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        configuration.addInterceptor(new DefaultInterceptor());
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
        Generators generators= Generators.getInstance();
        Map<String, Generation> beans=context.getBeansOfType(Generation.class);
        generators.putAll(beans);
        try{
            Generation bean=  context.getBean(Generation.class);//IdGenerator
            generators.setPrimary(bean);
        }catch (Exception e){

        }

    }

    public void add(Class mapperClass) {
        String namespace = mapperClass.getName();
        Class entityClass = ObjectSupport.getGenericInterfaces(mapperClass, 0, 0);
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        String resource = namespace.replace('.', '/') + ".java (best guess)";
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(configuration, resource);
        assistant.setCurrentNamespace(namespace);
        configuration.setMapUnderscoreToCamelCase(true);
        for (Method method : mapperClass.getMethods()) {
            //AbstractMethodBuilder methodBuilder= methodBuilderMap.get(method.getName());
            //if(methodBuilder!=null) methodBuilder.add(assistant,mapperClass,entityClass,entityAnnotation);

            MappedStatement ms = configuration.getMappedStatement(namespace + "." + method.getName());
            if (ms != null) {
                if (ms.getSqlCommandType().equals(SqlCommandType.INSERT)) {
                    selectKey(mapperClass,entityClass,method,ms);

                }

            }


        }
    }

    private void selectKey(Class mapperClass,Class entityClass,Method method,MappedStatement ms){
        KeyGenerator keyGenerator=ms.getKeyGenerator();
        // SelectKey exists ,so do nothing
        if(keyGenerator!=null && keyGenerator instanceof SelectKeyGenerator) return;

       final EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        Generation generation =entityAnnotation.getIdGeneration();
        if (generation !=null && generation.getType().equals(Generation.Type.SELECT_KEY_SQL)) {
            //org.apache.ibatis.builder.annotation.MapperAnnotationBuilder.handleSelectKeyAnnotation

            SelectKey selectKey=new SelectKey(){

                @Override
                public Class<? extends Annotation> annotationType() {
                    return SelectKey.class;
                }

                @Override
                public String[] statement() {
                    return new String[0];
                }

                @Override
                public String keyProperty() {
                    return Utils.toString(entityAnnotation.getIdPropertyNames(),",",null);
                }

                @Override
                public String keyColumn() {
                    return Utils.toString(entityAnnotation.getIdColumnNames(),",",null);
                }

                @Override
                public boolean before() {
                    return true;
                }

                @Override
                public Class<?> resultType() {
                    return null;
                }

                @Override
                public StatementType statementType() {
                    return StatementType.PREPARED;
                }
            };

            String value = (String) generation.generate(null, entityAnnotation.getTable(), null,
                    entityAnnotation.getIdGeneratorArg());

            MapperAnnotationBuilder  builder=new MapperAnnotationBuilder(configuration,mapperClass);

            //-- handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver)

            Method handleSelectKeyAnnotationMethod=ObjectSupport.getMethod(  MapperAnnotationBuilder.class,"handleSelectKeyAnnotation");

            Method getParameterTypeMethod=ObjectSupport.getMethod(  MapperAnnotationBuilder.class,"getParameterType");
            getParameterTypeMethod.setAccessible(true);
          ;


            handleSelectKeyAnnotationMethod.setAccessible(true);
            try {
                keyGenerator=(KeyGenerator)handleSelectKeyAnnotationMethod.invoke(builder,selectKey,ms.getId(),
                        getParameterTypeMethod.invoke(builder,method),ms.getLang());
            } catch ( Exception e) {
                throw new RuntimeException(e);
            }


        }

        if(keyGenerator==null) keyGenerator=Jdbc3KeyGenerator.INSTANCE;
        ObjectSupport.setFieldValue(ms, "keyProperties", entityAnnotation.getIdPropertyNames().toArray(new String[0]));
        ObjectSupport.setFieldValue(ms, "keyColumns", entityAnnotation.getIdColumnNames().toArray(new String[0]));
        ObjectSupport.setFieldValue(ms, "keyGenerator", keyGenerator);


    }


}

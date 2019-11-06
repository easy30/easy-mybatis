package com.cehome.easymybatis;

import com.cehome.easymybatis.annotation.MethodBuilder;
import com.cehome.easymybatis.builder.AbstractMethodBuilder;
import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.*;

/**
 * coolma 2019/10/25
 **/
public class MapperFactory implements InitializingBean, ApplicationListener<ContextRefreshedEvent> {
    private static Boolean loaded = false;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;
    Set<String> methodNameSet=new HashSet<String>();
    Map<String, AbstractMethodBuilder> methodBuilderMap =new HashMap<String, AbstractMethodBuilder>();



    private Class[] mapperInterfaces;

    public MapperFactory(){

    }


    @Override
    public void afterPropertiesSet() throws Exception {


        //addBuilders(Mapper.class);

        // custom mapper interfaces
        if(mapperInterfaces!=null){
            for(Class c :mapperInterfaces){
                //addBuilders(c);
            }
        }


    }
    private void addBuilders(Class c) throws Exception {
        for(Method method:c.getMethods()){
            methodNameSet.add(method.getName());
            MethodBuilder mb= method.getAnnotation(MethodBuilder.class);
            if(mb==null || mb.value()==null){
                if(!methodBuilderMap.containsKey(method.getName())) {
                    throw new RuntimeException("@MethodBuilder need for " + c.getName() + "." + method.getName());
                }
            }
            AbstractMethodBuilder methodBuilder  = mb.value().newInstance();
            if(!method.getName().equals(methodBuilder.getMethodName().equals(method.getName())))
                throw new RuntimeException("method name not match, need"+method.getName()+", but found "
                        +methodBuilder.getMethodName()+" in "+methodBuilder.getClass().getName());
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

        sqlSessionTemplate.getConfiguration().addInterceptor(new SimpleInterceptor());

            Map<String,MapperFactoryBean> beanMap=   event.getApplicationContext().getBeansOfType(MapperFactoryBean.class);

            //Map<String,Mapper> beanMap = event.getApplicationContext().getBeansOfType(Mapper.class);
            if(beanMap != null || beanMap.size() > 0){
                  for (Map.Entry<String,MapperFactoryBean> e : beanMap.entrySet()) {
                    Class mapperClass=e.getValue().getObjectType();
                    add(mapperClass);

                }
            }

    }

    public void add(Class mapperClass){
        String namespace=mapperClass.getName();
        Class entityClass=  Utils.getGenericInterfaces(mapperClass,0,0);
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        String resource = namespace.replace('.', '/') + ".java (best guess)";
        MapperBuilderAssistant assistant =
                new MapperBuilderAssistant(sqlSessionTemplate.getConfiguration(), resource);
        assistant.setCurrentNamespace(namespace);
        sqlSessionTemplate.getConfiguration().setMapUnderscoreToCamelCase(true);
        for(Method method:mapperClass.getMethods()){
            //AbstractMethodBuilder methodBuilder= methodBuilderMap.get(method.getName());
            //if(methodBuilder!=null) methodBuilder.add(assistant,mapperClass,entityClass,entityAnnotation);

            MappedStatement ms= sqlSessionTemplate.getConfiguration().getMappedStatement(namespace+"."+method.getName());
            if(ms!=null){
                if(ms.getSqlCommandType().equals(SqlCommandType.INSERT)) {
                    Utils.setFieldValue(ms,"keyProperties",entityAnnotation.getIdPropertyNames().toArray(new String[0]));
                    Utils.setFieldValue(ms,"keyColumns",entityAnnotation.getIdColumnNames().toArray(new String[0]));
                    Utils.setFieldValue(ms,"keyGenerator", Jdbc3KeyGenerator.INSTANCE);

                }

            }


        }
    }


}

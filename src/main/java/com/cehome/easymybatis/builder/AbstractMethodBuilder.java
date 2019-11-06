package com.cehome.easymybatis.builder;

import com.cehome.easymybatis.EntityAnnotation;
import org.apache.ibatis.annotations.Lang;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;

/**
 * coolma 2019/10/25
 **/
public abstract class AbstractMethodBuilder {
    protected Configuration configuration;
    protected LanguageDriver languageDriver;
    protected MapperBuilderAssistant builderAssistant;
    public void add(MapperBuilderAssistant builderAssistant,
                      Class<?> mapperClass, Class<?> entityClass, EntityAnnotation  entityAnnotation) {
        this.configuration = builderAssistant.getConfiguration();
        this.builderAssistant = builderAssistant;
        this.languageDriver = configuration.getDefaultScriptingLanguageInstance();
        /* 注入自定义方法 */
        doAdd(mapperClass, entityClass, entityAnnotation);
    }


    protected KeyGenerator getKeyGenerator(KeyGenerator keyGenerator){
        if(keyGenerator!=null) return keyGenerator;
        if(configuration.isUseGeneratedKeys()) return Jdbc3KeyGenerator.INSTANCE;
        return   Jdbc3KeyGenerator.INSTANCE;// NoKeyGenerator.INSTANCE;

    }


    protected MappedStatement addInsertMappedStatement(Class<?> mapperClass, Class<?> parameterType, String methodName,
                                                       SqlSource sqlSource, KeyGenerator keyGenerator,
                                                       String keyProperty, String keyColumn) {
        return addMappedStatement(mapperClass, methodName, sqlSource, SqlCommandType.INSERT, parameterType, null,
                Integer.class, keyGenerator, keyProperty, keyColumn);
    }

    protected MappedStatement addUpdateMappedStatement(Class<?> mapperClass, Class<?> parameterType, String methodName,
                                                       SqlSource sqlSource) {
        return addMappedStatement(mapperClass, methodName, sqlSource, SqlCommandType.UPDATE, parameterType, null,
                Integer.class, new NoKeyGenerator(), null, null);
    }



    /**
     * 添加 MappedStatement 到 Mybatis 容器
     */
    protected MappedStatement addMappedStatement(Class<?> mapperClass, String methodName, SqlSource sqlSource,
                                                 SqlCommandType sqlCommandType, Class<?> parameterType,
                                                 String resultMap, Class<?> resultType, KeyGenerator keyGenerator,
                                                 String keyProperty, String keyColumn) {
        String statementName = mapperClass.getName() + "." + methodName;
        if (hasMappedStatement(statementName)) {
            //logger.warn(LEFT_SQ_BRACKET + statementName + "] Has been loaded by XML or SqlProvider or Mybatis's Annotation, so ignoring this injection for [" + getClass() + RIGHT_SQ_BRACKET);
            return null;
        }

        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;

        return builderAssistant.addMappedStatement(methodName, sqlSource, StatementType.PREPARED, sqlCommandType,
                null, null, null, parameterType, resultMap, resultType,
                null, !isSelect, isSelect, false, keyGenerator, keyProperty, keyColumn,
                configuration.getDatabaseId(), languageDriver, null);
    }
    protected boolean hasMappedStatement(String mappedStatement) {
        return configuration.hasStatement(mappedStatement, false);
    }

    protected  Method getMethod(Class<?> mapperClass,String name){
        for(Method m:mapperClass.getMethods()) if(m.getName().equals(getMethodName())) return m;
        return null;
    }
    protected LanguageDriver getLanguageDriver(Class<?> mapperClass) {
        return getLanguageDriver(getMethod(mapperClass, getMethodName()));
    }
    protected LanguageDriver getLanguageDriver(Method method) {
        Lang lang = method.getAnnotation(Lang.class);
        Class<? extends LanguageDriver> langClass = null;
        if (lang != null) {
            langClass = lang.value();
        }
        return configuration.getLanguageDriver(langClass);
    }

    public abstract String getMethodName();
    public abstract MappedStatement doAdd(Class<?> mapperClass, Class<?> modelClass, EntityAnnotation  entityAnnotation);


}

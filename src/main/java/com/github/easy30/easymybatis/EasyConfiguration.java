package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.annotation.EntitySelectKey;
import com.github.easy30.easymybatis.core.ColumnAnnotation;
import com.github.easy30.easymybatis.core.DefaultInterceptor;
import com.github.easy30.easymybatis.core.DialectFactory;
import com.github.easy30.easymybatis.core.EntityAnnotation;
import com.github.easy30.easymybatis.dialect.Dialect;
import com.github.easy30.easymybatis.utils.ObjectSupport;
import com.github.easy30.easymybatis.utils.Utils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.session.Configuration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class EasyConfiguration extends Configuration {
    private Dialect dialect;
    private String dialectName;
    private Map<String, Generation> generations = new ConcurrentHashMap<>();
    private boolean init = false;

    public EasyConfiguration() {
        //-- default config
        setMapUnderscoreToCamelCase(true);
        setUseGeneratedKeys(true);


    }

    public String getDialectName() {
        return dialectName;
    }

    public void setDialectName(String dialectName) {
        this.dialectName = dialectName;
    }

    public Map<String, Generation> getGenerations() {
        return generations;
    }

    /**
     * put generations
     *
     * @param generations
     */
    public void setGenerations(Map<String, Generation> generations) {
        this.generations.putAll(generations);
    }


    @Override
    public void addMappedStatement(MappedStatement ms) {
        initMappedStatement(ms);
        super.addMappedStatement(ms);
    }


    @SneakyThrows
    protected void initMappedStatement(MappedStatement ms) {
        //-- init  dialect
        if (dialect == null) {
            dialect = DialectFactory.createDialect(dialectName, this);
            addInterceptor(new DefaultInterceptor(dialect));
        }
        //get mapper class
        String id = ms.getId();
        int lastPeriod = ms.getId().lastIndexOf('.');
        String mapperClassName = id.substring(0, lastPeriod);
        Class<?> mapperClass = Class.forName(mapperClassName);
        //-- set dialect
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(mapperClass);
        if(entityAnnotation.getMapperClass()==null) {
            entityAnnotation.setDialect(dialect);
            entityAnnotation.setMapperClass(mapperClass);
        }

        if (ms.getSqlCommandType().equals(SqlCommandType.INSERT)) {
            //-- set auto-key-return
            Class entityClass = entityAnnotation.getEntityClass();
            //get mapper  method
            String mapperMethodName = id.substring(lastPeriod + 1);
            Method mapperMethod = Arrays.stream(mapperClass.getMethods()).filter(m -> m.getName().equals(mapperMethodName)).findFirst().orElse(null);
            doKeyGenerator(mapperClass, entityClass, mapperMethod, ms);
        }

    }

    private void doKeyGenerator(Class mapperClass, Class entityClass, Method method, MappedStatement ms) {
        log.debug("------ mapper:{},entity:{},method:{}", mapperClass, entityClass, method);
        KeyGenerator keyGenerator = ms.getKeyGenerator();
        // SelectKey exists ,so do nothing
        if (keyGenerator != null && keyGenerator instanceof SelectKeyGenerator) return;

        final EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        final EntitySelectKey entitySelectKey = entityAnnotation.getEntitySelectKey();

        List<String> idPropertyNames = entityAnnotation.getIdPropertyNames();
        List<String> idColumnNames = entityAnnotation.getIdColumnNames();
        if (idPropertyNames.size() == 0) return;


        if (entitySelectKey != null) {

            //org.apache.ibatis.builder.annotation.MapperAnnotationBuilder.handleSelectKeyAnnotation
            SelectKey selectKey = new SelectKey() {

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
                    if (!StringUtils.isBlank(entitySelectKey.keyProperty())) return entitySelectKey.keyProperty();
                    if (entityAnnotation.getIdPropertyNames().size() > 1) {

                        throw new RuntimeException("keyProperty can not be empty for multiple columns key");

                    }
                    return Utils.toString(entityAnnotation.getIdPropertyNames(), ",", null);
                }

                @Override
                public String keyColumn() {
                    if (!StringUtils.isBlank(entitySelectKey.keyColumn())) return entitySelectKey.keyColumn();
                    if (!StringUtils.isBlank(entitySelectKey.keyProperty())) {
                        String kc = "";
                        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
                        for (String p : entitySelectKey.keyProperty().split(",")) {
                            if (kc.length() > 0) kc += ",";
                            kc += propertyColumnMap.get(p).getName();
                        }
                        return kc;
                    }
                    return Utils.toString(entityAnnotation.getIdColumnNames(), ",", null);


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

                //@Override  high version use, maybe > 3.5.6
                public String databaseId() {
                    return dialectName;
                }
            };


            MapperAnnotationBuilder builder = new MapperAnnotationBuilder(this, mapperClass);

            //invok private method : handleSelectKeyAnnotation(SelectKey selectKeyAnnotation, String baseStatementId, Class<?> parameterTypeClass, LanguageDriver languageDriver)

            Method handleSelectKeyAnnotationMethod = ObjectSupport.getMethod(MapperAnnotationBuilder.class, "handleSelectKeyAnnotation");

            Method getParameterTypeMethod = ObjectSupport.getMethod(MapperAnnotationBuilder.class, "getParameterType");
            getParameterTypeMethod.setAccessible(true);

            handleSelectKeyAnnotationMethod.setAccessible(true);
            try {
                keyGenerator = (KeyGenerator) handleSelectKeyAnnotationMethod.invoke(builder, selectKey, ms.getId(),
                        getParameterTypeMethod.invoke(builder, method), ms.getLang());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }

        if (entitySelectKey == null) keyGenerator = Jdbc3KeyGenerator.INSTANCE;
        // set private field
        String[] keyProperties = new String[idPropertyNames.size()];
        for (int i = 0; i < idPropertyNames.size(); i++) {
            keyProperties[i] = "e." + idPropertyNames.get(i);
        }

        ObjectSupport.setFieldValue(ms, "keyProperties", keyProperties);
        ObjectSupport.setFieldValue(ms, "keyColumns", idColumnNames.toArray(new String[0]));
        ObjectSupport.setFieldValue(ms, "keyGenerator", keyGenerator);


    }


}

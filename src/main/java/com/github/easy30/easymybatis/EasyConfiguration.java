package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.annotation.EntitySelectKey;
import com.github.easy30.easymybatis.core.*;
import com.github.easy30.easymybatis.dialect.Dialect;
import com.github.easy30.easymybatis.utils.ObjectSupport;
import com.github.easy30.easymybatis.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.builder.annotation.MapperAnnotationBuilder;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.InterceptorChain;
import org.apache.ibatis.session.Configuration;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import javax.persistence.Column;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class EasyConfiguration extends Configuration {
    private Dialect dialect;
    private String dialectName;
    private Map<String, Generation> generations = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private Boolean queryEmptyStringParam;
    private Map<Class, String> entityClassTableMap;//custom entity table;
    private Map<String, Class> tableEntityClassMap;
    private boolean init = false;
    private Map<Class, ResultMap> changeResultMapMap = Collections.synchronizedMap(new HashMap());
    private EntityTypeHandler entityTypeHandler;

    public EasyConfiguration() {
        //-- default config
        setMapUnderscoreToCamelCase(true);
        setUseGeneratedKeys(true);
        this.setQueryEmptyStringParam(false);

    }

    public void setEnvironment(Environment environment) {
        super.setEnvironment(environment);
        //-- init  dialect
        if (dialect == null) {
            dialect = StringUtils.isNotBlank(dialectName) ? DialectFactory.createDialect(dialectName)
                    : DialectFactory.createDialect(this.getEnvironment().getDataSource());
        }
        addInterceptor(new DefaultInterceptor(dialect));

    }

    /**
     * in order to keep DefaultInterceptor run first ( Provider need getDialect()),  move it to last always.
     *
     * @param interceptor
     * @see org.apache.ibatis.plugin.InterceptorChain#pluginAll , return the last one, then invoke the last one first.
     */
    @Override
    public void addInterceptor(Interceptor interceptor) {
        List<Interceptor> interceptors = ObjectSupport.getFieldValue(InterceptorChain.class, interceptorChain, "interceptors");
        //move DefaultInterceptor to last,
        DefaultInterceptor defaultInterceptor = null;
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            if (interceptors.get(i) instanceof DefaultInterceptor) {
                defaultInterceptor = (DefaultInterceptor) interceptors.get(i);
                interceptors.remove(i);
                break;
            }
        }
        interceptors.add(interceptor);
        if (defaultInterceptor != null) interceptors.add(defaultInterceptor);


    }


    public String getDialectName() {
        return dialectName;
    }

    public Dialect getDialect() {
        return dialect;
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
        this.generations = generations;
    }

    public void addGeneration(String name, Generation generation) {
        this.generations.put(name, generation);
    }


    public Map<Class, String> getEntityClassTableMap() {
        return entityClassTableMap;
    }

    public Map<String, Class> getTableEntityClassMap() {
        return tableEntityClassMap;
    }

    public void setEntityClassTableMap(Map<Class, String> entityClassTableMap) {
        this.entityClassTableMap = entityClassTableMap;
        if (entityClassTableMap == null) tableEntityClassMap = null;
        else {
            entityClassTableMap.forEach((k, v) -> tableEntityClassMap.put(v, k));
        }

    }

    public EntityTypeHandler getEntityTypeHandler() {
        return entityTypeHandler;
    }

    public void setEntityTypeHandler(EntityTypeHandler entityTypeHandler) {
        this.entityTypeHandler = entityTypeHandler;
    }

    @Override
    public void addMappedStatement(MappedStatement ms) {
        initMappedStatement(ms);
        super.addMappedStatement(ms);
    }


    //@SneakyThrows
    protected void initMappedStatement(MappedStatement ms) {
        //get mapper class
        String id = ms.getId();
        int lastPeriod = ms.getId().lastIndexOf('.');
        String mapperClassName = id.substring(0, lastPeriod);
        Class<?> mapperClass = null;
        //if maybe from XML , not a  Class
        try {
            mapperClass = Class.forName(mapperClassName);
        } catch (ClassNotFoundException e) {
            return;
        }
        if (Mapper.class.isAssignableFrom(mapperClass)) {
            //-- set dialect/mapper/custom table
            Class entityClass = ObjectSupport.getGenericInterfaces(mapperClass, 0, 0);
            EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceOnly(entityClass);
            if (entityAnnotation == null) {
                entityAnnotation = EntityAnnotation.getInstance(entityClass);
                //entityAnnotation.setDialect(dialect);
                entityAnnotation.setMapperClass(mapperClass);//todo:delete
                if (entityTypeHandler != null) entityTypeHandler.register(this, this.getTypeHandlerRegistry(), entityClass);
                //entityAnnotation.setEasyConfiguration(this);
            /*if(entityClassTableMap!=null){
                String table = entityClassTableMap.get(entityAnnotation.getEntityClass());
                if(table!=null) entityAnnotation.setTable(table);
            }*/
            }

            //Class entityClass = entityAnnotation.getEntityClass();
            if (ms.getSqlCommandType().equals(SqlCommandType.INSERT)) {
                //-- set auto-key-return

                //get mapper  method
                String mapperMethodName = id.substring(lastPeriod + 1);
                Method mapperMethod = Arrays.stream(mapperClass.getMethods()).filter(m -> m.getName().equals(mapperMethodName)).findFirst().orElse(null);
                doKeyGenerator(mapperClass, entityClass, mapperMethod, ms);
            } else if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
                changeResultMaps(ms);
            }
        } else if (CommonMapperImpl.class.isAssignableFrom(mapperClass)) {
            if (ms.getSqlCommandType().equals(SqlCommandType.INSERT)) {
                ObjectSupport.setFieldValue(ms, "keyGenerator", CommonMapperKeyGenerator.INSTANCE);

            }
        }

    }

    /**
     * find @Column(name="ccc"), build prop and column name mapping;
     *
     * @param ms
     */
    private void changeResultMaps(MappedStatement ms) {
        List<ResultMap> resultMaps = ms.getResultMaps();
        if (CollectionUtils.isEmpty(resultMaps)) return;
        List<ResultMap> newResultMaps = new ArrayList<>();
        boolean change = false;
        for (ResultMap resultMap : resultMaps) {
            Class<?> resultClass = resultMap.getType();
            ResultMap newResultMap = changeResultMapMap.get(resultClass);
            if (newResultMap == null) {
                //Build new ResultMapping List
                List<ResultMapping> newResultMappings = getColumnResultMappings(resultClass);
                if (newResultMappings.size() > 0) {
                    //keep old ResultMapping . resultMap.getResultMappings() mostly  empty
                    if (resultMap.getResultMappings() != null) {
                        Set<String> propSet = newResultMappings.stream().map(ResultMapping::getProperty).collect(Collectors.toSet());
                        for (ResultMapping oldResultMapping : resultMap.getResultMappings()) {
                            if (!propSet.contains(oldResultMapping.getProperty())) {
                                newResultMappings.add(oldResultMapping);
                            }
                        }
                    }

                    //Build new ResultMap
                    ResultMap.Builder newResultMapBuilder = new ResultMap.Builder(this, resultMap.getId(), resultMap.getType(), newResultMappings, resultMap.getAutoMapping());
                    newResultMap = newResultMapBuilder.build();
                    changeResultMapMap.put(resultClass, newResultMap);

                }
            }

            //add ResultMap to newResultMaps
            if (newResultMap == null) { //add old
                newResultMaps.add(resultMap);
            } else { //add new
                change = true;
                newResultMaps.add(newResultMap);
            }
        }
        if (change) {
            ObjectSupport.setField(ms, "resultMaps", newResultMaps);
        }


    }

    private List<ResultMapping> getColumnResultMappings(Class<?> resultClass) {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(resultClass);
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        for (PropertyDescriptor pd : propertyDescriptors) {
            Column column = ObjectSupport.getAnnotation(Column.class, resultClass, pd);
            if (column == null || StringUtils.isBlank(column.name())) continue;
            ResultMapping.Builder builder = new ResultMapping.Builder(this, pd.getName(), column.name(), pd.getPropertyType());
            resultMappings.add(builder.build());
        }
        return resultMappings;
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
        String entityParamName = getParamName(method);
        // set private field
        String[] keyProperties = new String[idPropertyNames.size()];
        for (int i = 0; i < idPropertyNames.size(); i++) {
            keyProperties[i] = (entityParamName==null?"":entityParamName+".") + idPropertyNames.get(i);
        }

        ObjectSupport.setFieldValue(ms, "keyProperties", keyProperties);
        ObjectSupport.setFieldValue(ms, "keyColumns", idColumnNames.toArray(new String[0]));
        ObjectSupport.setFieldValue(ms, "keyGenerator", keyGenerator);


    }

    private String getParamName(Method method) {
        for (Parameter p : method.getParameters()) {
            Param annotation = p.getAnnotation(Param.class);
            if (annotation != null) {
                if (Const.ENTITY.equals(annotation.value()) || Const.ENTITY_LIST.equals(annotation.value()))
                    return annotation.value();
            }
        }
        return null;
    }


}

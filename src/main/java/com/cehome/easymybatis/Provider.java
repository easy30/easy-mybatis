package com.cehome.easymybatis;

import com.cehome.easymybatis.core.*;
import com.cehome.easymybatis.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.Map;

/**
 * coolma 2019/11/4
 **/
public class Provider<E> {
    public String insert(ProviderContext context,E entity) {
        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        LineBuilder sql = new LineBuilder();
        sql.append("<script>")
                .append("{}")
                .append("insert into {} ")
                .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">{} </trim>")
                .append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">{}</trim>")
                .append("</script>");

        LineBuilder selectKeys = new LineBuilder();
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        Map<String, ColumnAnnotation> columnMap = entityAnnotation.getPropertyColumnMap();


        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();

            if (!columnAnnotation.isInsertable()) continue;
            String prop = e.getKey();
            int valueType = 0;//0: none  1 value 2:dialect value

            // entity value
            Object value = entityAnnotation.getProperty(entity, prop);
            if (value != null) {
                valueType = 1;

            }
            // dialect value
            else {

                value = entityAnnotation.getDialectValue(entity, prop);
                if (value != null) {
                    valueType = 2;

                }
            }

            // generator value
            if (value == null) {

                Generation generation = columnAnnotation.getGeneration();
                if (generation != null) {
                    value = generation.generate(entity, entityAnnotation.getTable(), prop,
                            columnAnnotation.getGeneratorArg());
                    if (value != null) {

                        entityAnnotation.setProperty(entity, prop, value);
                        valueType = 1;

                    }
                }
            }


            // default value
            if (value == null) {
                value = columnAnnotation.getColumnInsertDefault();
                if (value != null) {
                    valueType = 2;
                }
            }

            if (valueType == 1) {
                columnBuilder.append(columnAnnotation.getName() + ",");
                valueBuilder.append(Utils.format("#{{}},", prop));
            } else if (valueType == 2) {
                columnBuilder.append(Utils.format("{},", columnAnnotation.getName()));
                valueBuilder.append(Utils.format("{},", value));

            }

        }

        return Utils.format(sql.toString(), selectKeys, entityAnnotation.getTable(), columnBuilder, valueBuilder);

    }

    public String update(ProviderContext context,E entity) {
        if (entity == null) throw new RuntimeException("entity can not be null");
        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        String set = ProviderSupport.sqlSetValues(entity, "");
        String where= ProviderSupport.sqlWhereById(entity, entityAnnotation);
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_UPDATE);
        queryDefine.setWhere(where);
        queryDefine.setSet(set);
        queryDefine.setTables(entityAnnotation.getTable());
        return queryDefine.toSQL();
    }

   /* public String delete(E entity) {
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        return Utils.format(ProviderSupport.SQL_DELETE, "", entityAnnotation.getTable(),
                ProviderSupport.sqlWhereById(entity, entityAnnotation));
    }*/

    public String updateByParams(ProviderContext context,@Param(Const.ENTITY) E entity, @Param(Const.PARAMS) Object params,@Param(Const.PARAM_NAEMS) String... paramNames) {
        if(ArrayUtils.isEmpty(paramNames)) throw new MapperException("paramNames can not be empty");
        //Class entityClass = entity.getClass();
        if (entity == null || params == null) throw new RuntimeException("entity or params can not be null");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //String sql = ProviderSupport.SQL_UPDATE;

        String set = ProviderSupport.sqlSetValues(entity, Const.ENTITY);


        QueryDefine result= ProviderSupport.parseParams(entityAnnotation,params, paramNames,Global.SQL_TYPE_UPDATE,"",null,Const.PARAMS );
        result.setSet(set);
        return result.toSQL();
        /*LineBuilder whereBuilder = new LineBuilder();
        SimpleProperties sp=SimpleProperties.create(params);
        for(String prop:sp.getProperties()){
            Object value=sp.getValue(prop);
            String fullProp = Const.PARAMS + "." + prop;
            if (value != null) {
                whereBuilder.append(Utils.format(Const.SQL_AND, entityAnnotation.getColumnName(prop), fullProp));
            } else {
                value = entityAnnotation.getDialectParam(params, prop);
                if (value != null) {
                    whereBuilder.append(Utils.format(Const.SQL_AND_DIALECT, entityAnnotation.getColumnName(prop), value));
                }

            }
        }

        String where = whereBuilder.toString();
        if (StringUtils.isBlank(where))
            throw new RuntimeException("params for update can not be empty (safely update!!!)");*/


        //return Utils.format(sql, result[1], set, result[2]);


    }


    public String updateByCondition(ProviderContext context,@Param(Const.ENTITY) E entity, @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params) {

        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String set = ProviderSupport.sqlSetValues(entity, Const.ENTITY);

        if (StringUtils.isBlank(condition))
            throw new RuntimeException("because of safety, where condition can not be blank. (set where to * for updating all records)");
        if (condition.equals("*")) condition = "";//update all
        if (condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation);
        }
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_UPDATE);
        queryDefine.setCondition( entityAnnotation.getDialect().addWhereIfNeed(condition));
        queryDefine.setSet(set);
        queryDefine.setTables(entityAnnotation.getTable());
        return queryDefine.toSQL();

    }


    public String getById(ProviderContext context, @Param(Const.ID) Object id, @Param(Const.COLUMNS) String selectColumns) {
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {
            EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
            selectColumns = ProviderSupport.convertPropsToColumns(selectColumns, entityAnnotation);

        }
        return ProviderSupport.sqlById(context, id, Global.SQL_TYPE_SELECT, selectColumns);
    }

    public String deleteById(ProviderContext context, Object id) {
        return ProviderSupport.sqlById(context, id, Global.SQL_TYPE_DELETE, "");
    }


    /*public Class getCurrentMapperClass(){
        MappedStatement mappedStatement= DefaultInterceptor.getCurrentMappedStatement();
        ProviderSqlSource sqlSource=(ProviderSqlSource)mappedStatement.getSqlSource();
        Method method= ObjectSupport.getFieldValue(ProviderSqlSource.class,sqlSource,"mapperMethod");
        return method.getDeclaringClass();

    }
    public EntityAnnotation getCurrentEntityAnnotation(){
        return EntityAnnotation.getInstanceByMapper(getCurrentMapperClass());
    }*/

    public String getByParams(ProviderContext context,@Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,@Param(Const.COLUMNS) String selectColumns) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        /*if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } *//*else {

            selectColumns = ProviderSupport.convertColumns(selectColumns, entityAnnotation.getPropertyColumnMap());

        }*/
        return ProviderSupport.sqlByParams(entityAnnotation,params,null, Global.SQL_TYPE_SELECT, selectColumns, orderBy, Const.PARAMS);

    }

    public String getValueByParams(ProviderContext context,@Param(Const.PARAMS) Object params,  @Param(Const.ORDER) String orderBy,@Param(Const.COLUMN) String column) {
        //Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation =  EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
      /*  if (StringUtils.isBlank(column)) {
            column = "*";
        }*/
        return ProviderSupport.sqlByParams(entityAnnotation,params,null, Global.SQL_TYPE_SELECT, column, orderBy, Const.PARAMS);

    }

    public String listByParams(ProviderContext context,@Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,
                               @Param(Const.COLUMNS) String selectColumns) {
        //Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation =  EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
       /* if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        }*//* else {
            selectColumns = ProviderSupport.convertColumns(selectColumns, propertyColumnMap);
        }*/

        return ProviderSupport.sqlByParams(entityAnnotation,params, null, Global.SQL_TYPE_SELECT, selectColumns, orderBy, Const.PARAMS);

    }

    public String pageByParams(ProviderContext context,@Param(Const.PARAMS) Object params, @Param(Const.PAGE) Page page,
                               @Param(Const.ORDER) String orderBy, @Param(Const.COLUMNS) String selectColumns) {
        return listByParams(context,params, orderBy, selectColumns);

    }

    public String deleteByParams(ProviderContext context, @Param(Const.PARAMS) Object params,@Param(Const.PARAM_NAEMS) String... paramNames) {
        if(ArrayUtils.isEmpty(paramNames)) throw new MapperException("paramNames can not be empty");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        return ProviderSupport.sqlByParams(entityAnnotation,params,paramNames, Global.SQL_TYPE_DELETE, "", null,  Const.PARAMS);

    }


    public String deleteByCondition(ProviderContext context,
                                    @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();

        if (StringUtils.isBlank(condition))
            throw new RuntimeException("For safety, WHERE condition can not be blank. (set condition to * for deleting all records)");
        if (condition.equals("*")) condition = "";
        if (condition != null && condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation);
        }
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_DELETE);
        queryDefine.setTables(entityAnnotation.getTable());
        queryDefine.setCondition( entityAnnotation.getDialect().addWhereIfNeed(condition));
        return queryDefine.toSQL();

    }

    public String getValueByCondition(ProviderContext context, @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params,
                                      @Param(Const.COLUMN) String column) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        //String sql = ProviderSupport.SQL_SELECT;

        column = ProviderSupport.convertColumn(column, propertyColumnMap);
        if(condition==null) condition="";
        if (condition.length() > 0) {
            condition =ProviderSupport.sqlConvert(condition, entityAnnotation);

        }
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_SELECT);
        queryDefine.setColumns(column);
        queryDefine.setTables(entityAnnotation.getTable());
        queryDefine.setCondition(entityAnnotation.getDialect().addWhereIfNeed(condition));
        return queryDefine.toSQL();


    }

    public String listBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params) {


        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //String sql=ProviderSupport.SQL_UPDATE;
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        //String set = ProviderSupport.getSetValues(propertyColumnMap,"e");

        if (sql != null && sql.length() > 0) {

            sql = ProviderSupport.sqlConvert(sql, entityAnnotation);
            sql = ProviderSupport.sqlComplete(sql, entityAnnotation);
        }

        return sql;


    }

    public String pageBySQL(ProviderContext context, @Param(Const.SQL) String sql,
                            @Param(Const.PARAMS) Object params, @Param(Const.PAGE) Page page) {
        return listBySQL(context, sql, params);

    }
    public String getValueBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params) {
        return listBySQL(context, sql, params);
    }
}

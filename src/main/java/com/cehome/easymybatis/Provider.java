package com.cehome.easymybatis;

import com.cehome.easymybatis.core.*;
import com.cehome.easymybatis.dialect.Dialect;
import com.cehome.easymybatis.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * coolma 2019/11/4
 **/
public class Provider<E> {
    private static Logger logger = LoggerFactory.getLogger(Provider.class);
    public String insert(ProviderContext context,@Param(Const.ENTITY) E entity,@Param(Const.OPTIONS) UpdateOption... options) {
        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Dialect dialect=entityAnnotation.getDialect();
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

        Set ignoreColumnSet= MapperOptionSupport.getIgnoreColumnSet(options);
        Map<String,String> extraColVals= MapperOptionSupport.getExtraColVals(options);
        String table= MapperOptionSupport.getTable(entityAnnotation,options);

        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();

            if (!columnAnnotation.isInsertable()) continue;
            String prop = e.getKey();


            int valueType = 0;//0: none  1 value 2:dialect value

            Object value= MapperOptionSupport.getAndRemove(extraColVals,prop,columnAnnotation.getName());

            if(ignoreColumnSet!=null && (ignoreColumnSet.contains(prop) || ignoreColumnSet.contains(columnAnnotation.getName())))continue;


            if(value!=null){
                valueType = 2;
            }

            // entity value
            if(value==null) {
                value = entityAnnotation.getProperty(entity, prop);
                if (value != null) {
                    valueType = 1;

                }
            }

            // dialect value
            if(value==null)  {

                value = entityAnnotation.getDialectValue(entity, prop);
                if (value != null) {
                    valueType = 2;

                }
            }

            // generator value
            if (value == null) {

                Generation generation = columnAnnotation.getGeneration();
                if (generation != null) {
                    value = generation.generate(entity, table, prop,
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
                columnBuilder.append( dialect.getQuotedColumn(columnAnnotation.getName()) + ",");
                valueBuilder.append(Utils.format("#{{}},", Const.ENTITY+"."+prop));
            } else if (valueType == 2) {
                columnBuilder.append(Utils.format("{},", dialect.getQuotedColumn(columnAnnotation.getName())));
                valueBuilder.append(Utils.format("{},", value));

            }

        }

        //-- 剩下的
        if(extraColVals!=null){
            extraColVals.forEach((k,v)->{
                columnBuilder.append(Utils.format("{},", ProviderSupport.convertColumn(k,entityAnnotation)));
                valueBuilder.append(Utils.format("{},", v));
            });

        }

        String result= Utils.format(sql.toString(), selectKeys, dialect.getQuotedColumn(table), columnBuilder, valueBuilder);
        logger.debug("provider sql= {}",result);
        return result;
    }

    public String update(ProviderContext context,@Param(Const.ENTITY)E entity,@Param(Const.OPTIONS) UpdateOption... options) {
        if (entity == null) throw new RuntimeException("entity can not be null");
        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        String table= MapperOptionSupport.getTable(entityAnnotation,options);
        String set = ProviderSupport.sqlSetValues(entity, entityAnnotation,Const.ENTITY,options);
        String where= ProviderSupport.sqlWhereById(entity, entityAnnotation,Const.ENTITY);
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_UPDATE);
        queryDefine.setWhere(where);
        queryDefine.setSet(set);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        return queryDefine.toSQL();
    }

   /* public String delete(E entity) {
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        return Utils.format(ProviderSupport.SQL_DELETE, "", entityAnnotation.getTable(),
                ProviderSupport.sqlWhereById(entity, entityAnnotation));
    }*/

    public String updateByParams(ProviderContext context,@Param(Const.ENTITY) E entity, @Param(Const.PARAMS) Object params,
                                 @Param(Const.PARAM_NAEMS) String paramNames,@Param(Const.OPTIONS) UpdateOption... options) {
        if(StringUtils.isBlank(paramNames)) throw new MapperException("paramNames can not be empty");
        //Class entityClass = entity.getClass();
        if (entity == null || params == null) throw new RuntimeException("entity or params can not be null");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //String sql = ProviderSupport.SQL_UPDATE;

        String set = ProviderSupport.sqlSetValues(entity, entityAnnotation,Const.ENTITY,options);


        QueryDefine result= ProviderSupport.parseParams(entityAnnotation,params, paramNames.split("[,\\s]+"),Global.SQL_TYPE_UPDATE,"",null,Const.PARAMS ,options);
        result.setSet(set);
        return result.toSQL();

    }


    public String updateByCondition(ProviderContext context,@Param(Const.ENTITY) E entity, @Param(Const.CONDITION) String condition,
                                    @Param(Const.PARAMS) Object params,@Param(Const.OPTIONS) UpdateOption... options) {

        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String set = ProviderSupport.sqlSetValues(entity,entityAnnotation, Const.ENTITY,options);
        String table= MapperOptionSupport.getTable(entityAnnotation,options);
        if (StringUtils.isBlank(condition))
            throw new RuntimeException("because of safety, where condition can not be blank. (set where to * for updating all records)");
        if (condition.equals("*")) condition = "";//update all
        if (condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation,table);
        }

        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_UPDATE);
        queryDefine.setCondition( entityAnnotation.getDialect().addWhereIfNeed(condition));
        queryDefine.setSet(set);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(MapperOptionSupport.getTable(entityAnnotation,options)));
        return queryDefine.toSQL();

    }


    public String getById(ProviderContext context, @Param(Const.ID) Object id, @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        String table= MapperOptionSupport.getTable(entityAnnotation,options);
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {

            selectColumns = ProviderSupport.convertPropsToColumns(selectColumns, entityAnnotation,table);

        }
        return ProviderSupport.sqlById(entityAnnotation, id, Global.SQL_TYPE_SELECT, selectColumns,table);
    }

    public String deleteById(ProviderContext context, @Param(Const.ID)Object id,@Param(Const.OPTIONS)DeleteOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        String table= MapperOptionSupport.getTable(entityAnnotation,options);
        return ProviderSupport.sqlById(entityAnnotation, id, Global.SQL_TYPE_DELETE, "",table);
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

    public String getByParams(ProviderContext context,@Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,
                              @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        /*if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } *//*else {

            selectColumns = ProviderSupport.convertColumns(selectColumns, entityAnnotation.getPropertyColumnMap());

        }*/
        return ProviderSupport.sqlByParams(entityAnnotation,params,null, Global.SQL_TYPE_SELECT, selectColumns, orderBy, Const.PARAMS,options);

    }

    public String getValueByParams(ProviderContext context,@Param(Const.PARAMS) Object params,  @Param(Const.ORDER) String orderBy,
                                   @Param(Const.COLUMN) String column,@Param(Const.OPTIONS) SelectOption... options) {
        //Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation =  EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
      /*  if (StringUtils.isBlank(column)) {
            column = "*";
        }*/
        return ProviderSupport.sqlByParams(entityAnnotation,params,null, Global.SQL_TYPE_SELECT, column, orderBy, Const.PARAMS,options);

    }

    public String listByParams(ProviderContext context,@Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,
                               @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options) {
        //Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation =  EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
       /* if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        }*//* else {
            selectColumns = ProviderSupport.convertColumns(selectColumns, propertyColumnMap);
        }*/

        return ProviderSupport.sqlByParams(entityAnnotation,params, null, Global.SQL_TYPE_SELECT, selectColumns, orderBy, Const.PARAMS,options);

    }

    public String pageByParams(ProviderContext context,@Param(Const.PARAMS) Object params, @Param(Const.PAGE) Page page,
                               @Param(Const.ORDER) String orderBy, @Param(Const.COLUMNS) String selectColumns,@Param(Const.OPTIONS) SelectOption... options) {
        return listByParams(context,params, orderBy, selectColumns);

    }

    public String deleteByParams(ProviderContext context, @Param(Const.PARAMS) Object params,@Param(Const.PARAM_NAEMS) String paramNames,@Param(Const.OPTIONS)DeleteOption... options) {
        if(StringUtils.isBlank(paramNames)) throw new MapperException("paramNames can not be empty");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        return ProviderSupport.sqlByParams(entityAnnotation,params,paramNames.split("[,\\s]+"), Global.SQL_TYPE_DELETE, "", null,  Const.PARAMS,options);

    }


    public String deleteByCondition(ProviderContext context,
                                    @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params,@Param(Const.OPTIONS)DeleteOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();

        if (StringUtils.isBlank(condition))
            throw new RuntimeException("For safety, WHERE condition can not be blank. (set condition to * for deleting all records)");
        String table= MapperOptionSupport.getTable(entityAnnotation,options);

        if (condition.equals("*")) condition = "";
        if (condition != null && condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation,table);
        }
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_DELETE);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        queryDefine.setCondition( entityAnnotation.getDialect().addWhereIfNeed(condition));
        return queryDefine.toSQL();

    }

    public String getValueByCondition(ProviderContext context, @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params,
                                      @Param(Const.COLUMN) String column,@Param(Const.OPTIONS)SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        //String sql = ProviderSupport.SQL_SELECT;
        String table= MapperOptionSupport.getTable(entityAnnotation,options);
        column = ProviderSupport.convertColumn(column, entityAnnotation);
        if(condition==null) condition="";
        if (condition.length() > 0) {
            condition =ProviderSupport.sqlConvert(condition, entityAnnotation,table);

        }
        QueryDefine queryDefine=new QueryDefine(Global.SQL_TYPE_SELECT);
        queryDefine.setColumns(column);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        queryDefine.setCondition(entityAnnotation.getDialect().addWhereIfNeed(condition));
        return queryDefine.toSQL();


    }

    public String listBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params) {


        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //String sql=ProviderSupport.SQL_UPDATE;
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        //String set = ProviderSupport.getSetValues(propertyColumnMap,"e");

        if (sql != null && sql.length() > 0) {

            sql = ProviderSupport.sqlConvert(sql, entityAnnotation,entityAnnotation.getTable());
            sql = ProviderSupport.sqlComplete(sql, entityAnnotation,null);
        }
        logger.debug("provider sql= {}",sql);
        return sql;


    }

    public String pageBySQL(ProviderContext context, @Param(Const.SQL) String sql,
                            @Param(Const.PARAMS) Object params, @Param(Const.PAGE) Page page) {
        return listBySQL(context, sql, params);

    }
    public String getValueBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params) {
        return listBySQL(context, sql, params);
    }

    public String  list(Map params){
        return params.get("@@sql").toString();
    }
}

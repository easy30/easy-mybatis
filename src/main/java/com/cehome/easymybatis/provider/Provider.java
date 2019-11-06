package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.ColumnAnnotation;
import com.cehome.easymybatis.EntityAnnotation;
import com.cehome.easymybatis.Page;
import com.cehome.easymybatis.utils.Const;
import com.cehome.easymybatis.utils.LineBuilder;
import com.cehome.easymybatis.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;

import java.util.Map;

/**
 * coolma 2019/11/4
 **/
public class Provider<E> {
    public String insert(E entity) {
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        LineBuilder sql = new LineBuilder();
        sql.append("<script>")
                .append("insert into {} ")
                .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">{} </trim>")
                .append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">{}</trim>")
                .append("</script>");

        LineBuilder s1 = new LineBuilder();
        LineBuilder s2 = new LineBuilder();
        Map<String, ColumnAnnotation> columnMap = entityAnnotation.getPropertyColumnMap();


        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            String prop = e.getKey();
            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isInsertable()) continue;
            Object value = entityAnnotation.getProperty(entity, prop);
            if (value != null) {
                s1.append(columnAnnotation.getName() + ",");
                s2.append(Utils.format("#{{}},", prop));
            } else {
                value = entityAnnotation.getDialectProperty(entity, prop);
                if (value == null && columnAnnotation.getColumnInsertDefault() != null) {
                    value = columnAnnotation.getColumnInsertDefault();
                }
                if (value != null) {
                    s1.append(Utils.format("{},", columnAnnotation.getName()));
                    s2.append(Utils.format("{},", value));
                }
            }

        }

        return Utils.format(sql.toString(), entityAnnotation.getTable(), s1, s2);

    }

    public String update(E entity) {
        if(entity==null) throw new RuntimeException("entity can not be null");
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        String set = ProviderSupport.sqlSetValues(entity, "");
        return Utils.format(ProviderSupport.SQL_UPDATE, entityAnnotation.getTable(), set,
                ProviderSupport.sqlWhereById(entity, entityAnnotation));
    }

    public String delete(E entity) {
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        return Utils.format(ProviderSupport.SQL_DELETE, "", entityAnnotation.getTable(),
                ProviderSupport.sqlWhereById(entity, entityAnnotation));
    }

    public String updateByEntity(@Param(Const.ENTITY) E entity, @Param(Const.PARAMS) E params) {
        Class entityClass = entity.getClass();
        if(entity==null || params==null) throw new RuntimeException("entity or params can not be null");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        String sql = ProviderSupport.SQL_UPDATE;

        String set = ProviderSupport.sqlSetValues(entity, Const.ENTITY);


        LineBuilder whereBuilder = new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e : entityAnnotation.getPropertyColumnMap().entrySet()) {
            String prop = e.getKey();
            ColumnAnnotation columnAnnotation = e.getValue();
            String fullProp = Const.PARAMS + "." + prop;
            Object value = entityAnnotation.getProperty(params, prop);
            if (value != null) {
                whereBuilder.append(Utils.format(Const.SQL_AND, columnAnnotation.getName(), fullProp));
            } else {
                value = entityAnnotation.getDialectProperty(params, prop);
                if (value != null) {
                    whereBuilder.append(Utils.format(Const.SQL_AND_DIALECT, columnAnnotation.getName(), value));
                }

            }

        }
        String where=whereBuilder.toString();
        if(StringUtils.isBlank(where)) throw new RuntimeException("params for update can not be empty (safely update!!!)");


        return Utils.format(sql, entityAnnotation.getTable(), set, where);


    }


    public String updateByWhere(@Param(Const.ENTITY) E entity, @Param(Const.WHERE) String where, @Param(Const.PARAMS) Object params) {

        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        String sql = ProviderSupport.SQL_UPDATE;
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String set = ProviderSupport.sqlSetValues(entity, Const.ENTITY);

        if (StringUtils.isBlank(where)) throw new RuntimeException("because of safety, where condition can not be blank. (set where to * for updating all records)");
        if(where.equals("*")) where="";//update all
        if (where.trim().startsWith("where")) {
            where = where.trim().substring(5);

        }
        if (where.length() > 0) {
            where = ProviderSupport.sqlFixColumnsAndParams(where, propertyColumnMap);
        }

        return Utils.format(sql, entityAnnotation.getTable(), set, where);


    }


    public String getById(ProviderContext context, @Param(Const.ID) Object id, @Param(Const.COLUMNS) String selectColumns) {
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {
            EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
            selectColumns = ProviderSupport.propertiesToColumns(selectColumns, entityAnnotation.getPropertyColumnMap());

        }
        return ProviderSupport.sqlById(context, id, ProviderSupport.SQL_SELECT, selectColumns);
    }

    public String deleteById(ProviderContext context, Object id) {
        return ProviderSupport.sqlById(context, id, ProviderSupport.SQL_DELETE, "");
    }


    public String getByEntity(@Param(Const.PARAMS) E params, @Param(Const.COLUMNS) String selectColumns) {
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {
            EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(params.getClass());
            selectColumns = ProviderSupport.propertiesToColumns(selectColumns, entityAnnotation.getPropertyColumnMap());

        }
        return ProviderSupport.sqlByEntity(params, ProviderSupport.SQL_SELECT, true,selectColumns, null, Const.PARAMS);

    }

    public String getValueByEntity(@Param(Const.PARAMS) E params, @Param(Const.COLUMN) String column) {
        Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();

        return ProviderSupport.sqlByEntity(params, ProviderSupport.SQL_SELECT, true, ProviderSupport.propertyToColumn(column, propertyColumnMap), null, Const.PARAMS);

    }

    public String listByEntity(@Param(Const.PARAMS) E params, @Param(Const.ORDER) String orderBy,
                               @Param(Const.COLUMNS) String selectColumns) {
        Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {
            selectColumns = ProviderSupport.propertiesToColumns(selectColumns, propertyColumnMap);
        }

        return ProviderSupport.sqlByEntity(params, ProviderSupport.SQL_SELECT,true, selectColumns, orderBy, Const.PARAMS);

    }

    public String pageByEntity(@Param(Const.PARAMS) E params, @Param(Const.PAGE) Page page,
                               @Param(Const.ORDER) String orderBy, @Param(Const.COLUMNS) String selectColumns) {
        return listByEntity(params, orderBy, selectColumns);

    }


    public String deleteByEntity(E params) {
        return ProviderSupport.sqlByEntity(params, ProviderSupport.SQL_DELETE,false, "", null, "");

    }


    public String deleteByWhere(ProviderContext context,
                                @Param(Const.WHERE) String where, @Param(Const.PARAMS) Object params) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String sql = ProviderSupport.SQL_DELETE;

        if (StringUtils.isBlank(where)) throw new RuntimeException("because of safety, WHERE condition can not be blank. (set where to * for deleting all records)");
        if(where.equals("*")) where="";
        if (where != null && where.length() > 0) {
            where = ProviderSupport.sqlFixColumnsAndParams(where, propertyColumnMap);
        }

        return Utils.format(sql, "", entityAnnotation.getTable(), where);

    }

    public String getValueByWhere(ProviderContext context, @Param(Const.WHERE) String where, @Param(Const.PARAMS) Object params,
                                  @Param(Const.COLUMN) String column) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String sql = ProviderSupport.SQL_SELECT;

        column = ProviderSupport.propertyToColumn(column, propertyColumnMap);
        if (where != null && where.length() > 0) {
            where = ProviderSupport.sqlFixColumnsAndParams(where, propertyColumnMap);

        }
        //SQL_SELECT="<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
        return Utils.format(sql, column, entityAnnotation.getTable(), where);

    }

    public String listBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params) {


        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        //String sql=ProviderSupport.SQL_UPDATE;
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        //String set = ProviderSupport.getSetValues(propertyColumnMap,"e");

        if (sql != null && sql.length() > 0) {

            sql = ProviderSupport.sqlFixColumnsAndParams(sql, propertyColumnMap);
            sql = ProviderSupport.sqlAddPrefix(sql, entityAnnotation);
        }

        return sql;


    }

    public String pageBySQL(ProviderContext context, @Param(Const.SQL) String sql,
                            @Param(Const.PARAMS) Object params,@Param(Const.PAGE) Page page) {
        return listBySQL(context, sql, params);

    }
}

package com.cehome.easymybatis.core;

import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.annotation.OperatorEnum;
import com.cehome.easymybatis.annotation.Query;
import com.cehome.easymybatis.annotation.QueryProperty;
import com.cehome.easymybatis.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * coolma 2019/10/30
 **/
public class ProviderSupport {
    private static Logger logger = LoggerFactory.getLogger(ProviderSupport.class);
    public static String SQL_UPDATE = "<script>\r\n update {} <set>{}</set> \r\n <where>{}</where> \r\n </script> ";
    public static String SQL_SELECT = "<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
    public static String SQL_DELETE = "<script>\r\n delete {} from {} <where>{}</where>\r\n</script>";
    public static String SQL_SELECT_KEY = "<selectKey keyProperty='{}' resultType='{}' order='{}'>{}</selectKey>";
    public static int SQL_TYPE_INSERT = 1;
    public static int SQL_TYPE_UPDATE = 2;
    public static int SQL_TYPE_DELETE = 3;
    public static int SQL_TYPE_SELECT = 4;


    public static String sqlSetValues(Object entity, String prefix) {
        LineBuilder s1 = new LineBuilder();
        if (prefix == null) prefix = "";
        if (prefix.length() > 0) prefix += ".";
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);

        for (Map.Entry<String, ColumnAnnotation> e : entityAnnotation.getPropertyColumnMap().entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isUpdatable()) continue;
            String prop = e.getKey();
            //-- entity value
            Object value = entityAnnotation.getProperty(entity, prop);
            if (value != null) {
                s1.append(Utils.format(" {}=#{{}}, ", columnAnnotation.getName(), prefix + prop));
            } else {
                //-- dialect value
                value = entityAnnotation.getDialectValue(entity, prop);
                //-- default value
                if (value == null && columnAnnotation.getColumnUpdateDefault() != null) {
                    value = columnAnnotation.getColumnUpdateDefault();
                }
                if (value != null) {
                    s1.append(Utils.format(" {}={}, ", columnAnnotation.getName(), value));

                }
            }

        }
        String result = s1.toString();
        if (StringUtils.isEmpty(result))
            throw new RuntimeException("entity for update is empty. You need set some values");
        return result;
    }

    @Deprecated
    public static String sqlSetValues(Class entityClass, Map<String, ColumnAnnotation> propertyColumnMap, String prefix) {
        LineBuilder s1 = new LineBuilder();
        if (prefix == null) prefix = "";
        if (prefix.length() > 0) prefix += ".";
        boolean isDialect = DialectEntity.class.isAssignableFrom(entityClass);
        LineBuilder dialectSql = new LineBuilder();
        for (Map.Entry<String, ColumnAnnotation> e : propertyColumnMap.entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isUpdatable()) continue;
            String prop = prefix + e.getKey();
            //-- default value
            if (columnAnnotation.getColumnUpdateDefault() != null) {

                s1.append(Utils.format("<if test='{} == null'> {}={}, </if>", prop, columnAnnotation.getName(), columnAnnotation.getColumnUpdateDefault()));

            }
            //-- entity value
            s1.append(Utils.format("<if test='{} != null'> {}=#{{}}, </if>", prop, columnAnnotation.getName(), prop));

            if (isDialect) {
                String mapProp = Const.VALUE_MAP + "." + prop;
                s1.append(Utils.format("<if test='{} != null and {} != null'> {}=${{}}, </if>", Const.VALUE_MAP, mapProp, columnAnnotation.getName(), mapProp));

            }

        }
        return s1.toString();

    }

    public static String sqlWhereById(Object entity, EntityAnnotation entityAnnotation) {

        String where = "";
        List<String> props = entityAnnotation.getIdPropertyNames();
        List<String> columns = entityAnnotation.getIdColumnNames();
        if (props.size() == 0) throw new RuntimeException("no primary keys found");
        for (int i = 0; i < props.size(); i++) {
            if (i > 0) where += ",";
            String prop = props.get(i);
            Object value = entityAnnotation.getProperty(entity, prop);
            if (value != null) {
                where += columns.get(i) + " = #{" + props.get(i) + "}";
            } else {
                value = entityAnnotation.getDialectParam(entity, prop);
                if (value == null) throw new RuntimeException("property " + prop + " can not be null");
                where += columns.get(i) + " = " + value;
            }


        }
        return where;

    }

    public static String convertSqlColumns(String sql, EntityAnnotation entityAnnotation) {
        //  regex= ([^#\$]\{|^\{)(\w+)\} .  "{id}" or  "  {id}" ,but not #{id} ${id}
        RegularReplace rr = new RegularReplace(sql, "([^#\\$]\\{|^\\{)(\\w+)\\}");
        //  [^#\$]\{\w+\}|^\{(\w+)\}
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        while (rr.find()) {
            String prop = rr.group(2);
            if ("TABLE".equalsIgnoreCase(prop)) {
                rr.replace(entityAnnotation.getTable());
                continue;
            }
            ColumnAnnotation ca = propertyColumnMap.get(prop);
            String column = null;
            if (ca != null) {
                column = ca.getName();
                if (rr.group().charAt(1) == '{') column = rr.group().charAt(0) + column;
            } else {
                column = prop;
            }
            rr.replace(column);

        }
        return rr.getResult();

    }

    /**
     * convert prop to column name ,such as:  {aF} desc , bF asc, cF  ==>  a_f desc , b_f asc , c_f
     *
     * @param columns
     * @param entityAnnotation
     * @return
     */
    public static String convertColumns(String columns, EntityAnnotation entityAnnotation) {
        String result = "";
        if (columns != null && columns.length() > 0) {

            // for complex columns such as "substring(prop1,1,3),prop2", it's hard to parse.
            // so must use {} , that is "substring({prop1},1,3),{prop2}", and use convertSqlColumns() to parse easily
            if (columns.indexOf('(') >= 0) {
                return convertSqlColumns(columns, entityAnnotation);
            }
            Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
            if (columns.trim().equals("*")) return columns;
            String[] items = columns.split(",");
            for (String s : items) {
                s = s.trim();
                if (result.length() > 0) result += " , ";
                int n = indexOfWhitespace(s);
                String prop = n == -1 ? s : s.substring(0, n);
                prop = trimBrace(prop);

                ColumnAnnotation ca = propertyColumnMap.get(prop);
                if (ca == null) {
                    logger.warn("can't find prop {} in entity ", prop);
                }
                String column = ca != null ? ca.getName() : prop;
                if (n == -1)
                    result += column + " ";
                else
                    result += column + s.substring(n);

            }

        }
        return result;
    }

    public static String convertColumn(String propertyOrColumn, Map<String, ColumnAnnotation> propertyColumnMap) {
        propertyOrColumn = trimBrace(propertyOrColumn);
        ColumnAnnotation ca = propertyColumnMap.get(propertyOrColumn);
        return (ca != null) ? ca.getName() : propertyOrColumn;
    }


    public static String sqlComplete(String sql, EntityAnnotation entityAnnotation) {
        if (sql == null) sql = "";
        String sql2 = sql.length() == 0 ? "" : sql.trim().toLowerCase();
        if (sql2.startsWith("select ")) {
            return sql;
        } else if (sql2.startsWith("from ")) {
            return "select * " + sql;
        } else {
            if (entityAnnotation == null) return sql;

            String table = entityAnnotation.getTable();
            if (table == null) return sql;
            if (sql2.length() == 0 || sql2.startsWith("where ") || sql2.startsWith("order ") || sql2.startsWith("group ") || sql2.startsWith("limit ")) {
                return "select * from " + table + " " + sql;
            } else {
                return "select * from " + table + " where " + sql;
            }
        }
    }

    public static String sqlAddParamPrefix(String sql, String prefix) {
        return Utils.regularReplace(sql, "[#\\$]\\{(\\w+)\\}", "#'{'" + prefix + ".{1}'}'");
    }


    private static String trimBrace(String prop) {
        if (prop == null) return null;
        if (prop.startsWith("{")) prop = prop.substring(1);
        if (prop.endsWith("}")) prop = prop.substring(0, prop.length() - 1);
        return prop;
    }

    private static int indexOfWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) <= 32) return i;
        }
        return -1;
    }

    private static String findNotBlank(String... args) {
        String s = "";
        for (String a : args) {
            if (StringUtils.isNotBlank(a)) {
                s = a;
                break;
            }
        }
        return s;
    }

    public static String[] parseParams(EntityAnnotation entityAnnotation, Object params, int sqlType, String columns, String orderBy, String prefix) {


        String tables = entityAnnotation.getTable();
        String conditions = "";
        OperatorEnum innerOperator = OperatorEnum.AND;
        OperatorEnum outerOperator = OperatorEnum.AND;
        boolean bSelect = sqlType == SQL_TYPE_SELECT;
        if (params != null) {
            Query query = params.getClass().getAnnotation(Query.class);
            if (query != null) {
                //-- overwrite columns
                if (bSelect) {
                    columns = findNotBlank(columns, query.columns());
                    orderBy = findNotBlank(orderBy, query.orderBy());

                }

                //-- overwrite default tables
                if (!StringUtils.isBlank(query.tables())) {
                    tables = query.tables();
                }

                //-- set base conditions
                conditions = Utils.toString(query.conditions(), System.lineSeparator(), null);

                //-- do not need
                outerOperator = query.queryPropertyOuterOperator();


                //-- set    innerOperator
                innerOperator = query.queryPropertyInnerOperator();

            }

        }

        if (innerOperator.equals(OperatorEnum.NONE)) innerOperator = OperatorEnum.AND;


        if (StringUtils.isBlank(columns) && bSelect) {
            columns = "*";
        }

        columns = ProviderSupport.convertColumns(columns, entityAnnotation);


        LineBuilder propertyConditions = new LineBuilder();
        if (!outerOperator.equals(OperatorEnum.NONE) && params != null) {

            SimpleProperties sp = SimpleProperties.create(params);
            for (String prop : sp.getProperties()) {
                Object value = sp.getValue(prop);
                if (value != null) {
                    String fullProp = prefix == null || prefix.length() == 0 ? prop : prefix + "." + prop;
                    if (params instanceof Map) {
                        propertyConditions.append(Utils.format(Const.SQL_AND, entityAnnotation.getColumnName(prop), fullProp));
                    } else {

                        String condition = "";
                        QueryProperty queryProperty = ObjectSupport.getAnnotation(QueryProperty.class, params.getClass(), prop);
                        if (queryProperty != null) {
                            condition = Utils.toString(queryProperty.value(), System.lineSeparator(), null);
                        }

                        if (StringUtils.isBlank(condition)) {
                            condition = entityAnnotation.getColumnName(prop) + "=#{" + fullProp + "} ";
                        }
                        if (propertyConditions.length() > 0)
                            propertyConditions.append(" " + innerOperator + " " + condition + " ");
                        else propertyConditions.append(condition);

                        //propertyConditions.append(Utils.format(Const.SQL_AND, entityAnnotation.getColumnName(prop), fullProp));
                    }

                } else { //@Deprecated 使用@QueryCondition后，此功能可以去掉
                    value = entityAnnotation.getDialectParam(params, prop);
                    if (value != null) {
                        propertyConditions.append(Utils.format(Const.SQL_AND_DIALECT, entityAnnotation.getColumnName(prop), value));
                    }
                }
            }

        }

        if (StringUtils.isBlank(conditions)) {
            if (propertyConditions.length() > 0) {
                conditions = propertyConditions.toString();
            }
        } else {
            if (propertyConditions.length() > 0) {
                conditions = conditions + " " + outerOperator + " ( " + propertyConditions + " ) ";
            }
        }


        if (!bSelect && StringUtils.isBlank(conditions))
            throw new RuntimeException(" 'Where' conditions can not be null( Safety!!! ). params need.");

        if (bSelect) {
            String order = convertColumns(orderBy, entityAnnotation);
            if (order.length() > 0) conditions += (" order by " + order);
        }


        return new String[]{columns, tables, conditions};
        //SQL_SELECT="<script>\r\n select {} from {} <propertyConditions>{}</propertyConditions>\r\n</script>";
        //return Utils.format(sqlFormat,columns,tables,propertyConditions);

    }

    public static String sqlByParams(EntityAnnotation entityAnnotation, Object params, String sqlFormat, int sqlType, String columns, String orderBy, String prefix) {

        String[] result=parseParams(entityAnnotation,params,sqlType,columns,orderBy,prefix);

        //SQL_SELECT="<script>\r\n select {} from {} <where>{}</where>\r\n</script>";
        return Utils.format(sqlFormat, result[0], result[1], result[2]);

    }

    public static String convertSql(String sql, EntityAnnotation entityAnnotation) {
        sql = convertSqlColumns(sql, entityAnnotation);
        //convert  #{id}==> #{params.id}
        sql = sqlAddParamPrefix(sql, Const.PARAMS);
        return sql;
    }

    public static String sqlById(ProviderContext context, Object id, String sql, String select) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        List<String> props = entityAnnotation.getIdPropertyNames();
        List<String> columns = entityAnnotation.getIdColumnNames();
        if (props.size() == 0) throw new RuntimeException("primary key not found");
        if (props.size() > 1) throw new RuntimeException("multi primary keys not supported for GetById");

        String where = columns.get(0) + " = #{" + props.get(0) + "}";

        return Utils.format(sql, select, entityAnnotation.getTable(), where);

    }
}

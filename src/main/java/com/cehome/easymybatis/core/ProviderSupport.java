package com.cehome.easymybatis.core;

import com.cehome.easymybatis.Const;
import com.cehome.easymybatis.DialectEntity;
import com.cehome.easymybatis.MapperException;
import com.cehome.easymybatis.annotation.*;
import com.cehome.easymybatis.enums.RelatedOperator;
import com.cehome.easymybatis.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * coolma 2019/10/30
 **/
public class ProviderSupport {
    private static Logger logger = LoggerFactory.getLogger(ProviderSupport.class);
    //public static String SQL_SELECT_KEY = "<selectKey keyProperty='{}' resultType='{}' order='{}'>{}</selectKey>";
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
            throw new MapperException("entity for update is empty. You need set some values");
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
        if (props.size() == 0) throw new MapperException("no primary keys found");
        for (int i = 0; i < props.size(); i++) {
            if (i > 0) where += ",";
            String prop = props.get(i);
            Object value = entityAnnotation.getProperty(entity, prop);
            if (value != null) {
                where += columns.get(i) + " = #{" + props.get(i) + "}";
            } else {
                value = entityAnnotation.getDialectParam(entity, prop);
                if (value == null) throw new MapperException("property " + prop + " can not be null");
                where += columns.get(i) + " = " + value;
            }


        }
        return where;

    }

    /**
     * convert
     *
     * @param sql
     * @param entityAnnotation
     * @return
     */
    public static String convertSqlPropsToColumns(String sql, EntityAnnotation entityAnnotation) {
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
    public static String convertPropsToColumns(String columns, EntityAnnotation entityAnnotation) {
        String result = "";
        if (columns != null && columns.length() > 0) {

            // for complex columns such as "substring(prop1,1,3),prop2", it's hard to parse.
            // so must use {} , that is "substring({prop1},1,3),{prop2}", and use convertSqlColumns() to parse easily
            if (columns.indexOf('(') >= 0) {
                return convertSqlPropsToColumns(columns, entityAnnotation);
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
        if (Utils.startWithTokens(sql2,"select")) {
            return sql;
        } else if (Utils.startWithTokens(sql2,"from")) {
            return "select * " + sql;
        } else {
            if (entityAnnotation == null) return sql;

            String table = entityAnnotation.getTable();
            if (table == null) return sql;
            return "select * from " + table + " " + entityAnnotation.getDialect().addWhereIfNeed(sql);

        }
    }

    public static String convertSqlAddParamPrefix(String sql, String prefix) {
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

    private static String findFirstNotBlank(String... args) {
        String s = "";
        for (String a : args) {
            if (StringUtils.isNotBlank(a)) {
                s = a;
                break;
            }
        }
        return s;
    }

    public static QueryDefine parseParams(EntityAnnotation entityAnnotation, Object params, String[] paramNames, int sqlType, String columns, String orderBy, String prefix) {


        QueryDefine queryDefine=new QueryDefine(sqlType);
        String tables = entityAnnotation.getTable();
        String where="";
        String groupBy="";

        String other="";
        RelatedOperator innerOperator = RelatedOperator.AND;
        RelatedOperator outerOperator = RelatedOperator.AND;
        boolean bSelect = sqlType == Global.SQL_TYPE_SELECT;
        boolean queryPropertyEnable = true;
        //-- load @Query Anno
        if (params != null) {
            Query query = params.getClass().getAnnotation(Query.class);
            //-- Query found
            if (query != null) {
                //-- overwrite columns
                if (bSelect) {
                    // if columns is null then use query.columns. args columns > query.columns
                    columns = findFirstNotBlank(columns, query.columns());

                }

                //-- overwrite default tables. query.tables() > @Table on entity
                if (!StringUtils.isBlank(query.tables())) {
                    tables = query.tables();
                }

                //-- set base conditions
                where = sqlConvert(arrayToString(query.where()), entityAnnotation);
                groupBy=convertPropsToColumns(arrayToString(query.groupBy()), entityAnnotation);
                orderBy = findFirstNotBlank(orderBy, arrayToString(query.orderBy()));
                other= sqlConvert(arrayToString(query.other()), entityAnnotation);

                queryPropertyEnable = query.queryPropertyEnable();
                if (queryPropertyEnable) {
                    //--
                    outerOperator = query.queryPropertyOuterOperator();
                    //-- set    innerOperator
                    innerOperator = query.queryPropertyInnerOperator();
                }


            }

        }

        //if (innerOperator.equals(RelatedOperator.NONE)) innerOperator = RelatedOperator.AND;


        if (StringUtils.isBlank(columns) && bSelect) {
            columns = "*";
        }

        columns = ProviderSupport.convertPropsToColumns(columns, entityAnnotation);


        LineBuilder propertyConditions = new LineBuilder();
        if (queryPropertyEnable && params!=null)  {
            SimpleProperties sp = SimpleProperties.create(params);
            //for updateByParams deleteByParams
            boolean needValue= ArrayUtils.isNotEmpty(paramNames);
            boolean paramsIsMap=params instanceof Map;
            String[] props=needValue?paramNames:sp.getProperties();
            for (String prop : props) {
                Object value = sp.getValue(prop);

                if (value != null) {
                    String fullProp = prefix == null || prefix.length() == 0 ? prop : prefix + "." + prop;
                    // map params
                    if (paramsIsMap) {
                        addCondition(propertyConditions,innerOperator,Utils.format(Global.SQL_AND, entityAnnotation.getColumnName(prop), fullProp));
                    } else { // object params
                        String condition = "";

                        QueryItem queryItem = ObjectSupport.getAnnotation(QueryItem.class, params.getClass(), prop);
                        //-- use queryItem
                        if (queryItem != null) {
                            if(queryItem.ignore()) {
                                continue;
                            }
                            String queryItemValue=Utils.toString(queryItem.value(), System.lineSeparator(), null);
                            if(StringUtils.isBlank(queryItemValue)){
                                condition =doColumnDefault( entityAnnotation, prop, fullProp, value);
                            }else{
                                condition = sqlConvert(Utils.toString(queryItem.value(), System.lineSeparator(), null), entityAnnotation);
                            }


                        } else {
                            //Class valueType= ((ObjectProperties)sp).getType(prop);
                            //-- user QueryColumn
                            QueryColumn queryColumn = ObjectSupport.getAnnotation(QueryColumn.class, params.getClass(), prop);
                            if (queryColumn != null) {
                                String propOrColumn = StringUtils.isNotBlank(queryColumn.column()) ? queryColumn.column() : prop;
                                String column=convertColumn(propOrColumn,entityAnnotation.getPropertyColumnMap());
                                condition = QueryColumnSupport.doQueryColumn(entityAnnotation, column, queryColumn.operator(), fullProp, value);

                            }
                            //-- use default: a=b   or in []
                            else {
                                condition =doColumnDefault( entityAnnotation, prop, fullProp, value);

                            }
                        }

                        if(StringUtils.isNotBlank(condition)) {
                            addCondition(propertyConditions,innerOperator,condition);
                        }
                        //propertyConditions.append(Utils.format(Const.SQL_AND, entityAnnotation.getColumnName(prop), fullProp));
                    }

                } else { //@Deprecated 使用@QueryItem，此功能可以去掉
                    value = entityAnnotation.getDialectParam(params, prop);
                    if (value != null) {
                        addCondition(propertyConditions,innerOperator,Utils.format(Global.SQL_AND_DIALECT, entityAnnotation.getColumnName(prop), value));
                    }
                }
                if(needValue && value==null) throw new MapperException("property "+prop+" of params can not be null");
            }

        }


        if (propertyConditions.length() > 0) {
            if (StringUtils.isBlank(where)) {
                where= propertyConditions.toString();
            }else{
                where+= " "+ outerOperator + " ( " + propertyConditions  + " ) ";
            }
        }


        if (!bSelect && StringUtils.isBlank(where))
            throw new MapperException(" 'Where' conditions can not be null( Safety!!! ). params need.");


        orderBy = convertPropsToColumns(orderBy, entityAnnotation);


        queryDefine.setColumns(columns);
        queryDefine.setTables(tables);
        queryDefine.setWhere(where);
        queryDefine.setGroupBy(groupBy);
        queryDefine.setOrderBy(orderBy);
        queryDefine.setOther(other);


        return queryDefine;//new String[]{columns, tables, conditions};
        //SQL_SELECT="<script>\r\n select {} from {} <propertyConditions>{}</propertyConditions>\r\n</script>";
        //return Utils.format(sqlFormat,columns,tables,propertyConditions);

    }
    private static void addCondition( LineBuilder lineBuilder,RelatedOperator oper,String condition){
        if (lineBuilder.length() > 0) {
            lineBuilder.append(" " + oper + " ");
        }
        lineBuilder.append(condition);
    }
    private static String doColumnDefault(EntityAnnotation entityAnnotation, String prop, String fullProp, Object value){
        String column=entityAnnotation.getColumnName(prop);
        if(StringUtils.isBlank(column)) throw  new MapperException("can not find column name for property "+prop+". You can use @QueryColumn on property");
        return QueryColumnSupport.doQueryColumn(entityAnnotation, entityAnnotation.getColumnName(prop), null, fullProp, value);

    }

    private static String arrayToString(String[] ss){
      return   Utils.toString(ss, System.lineSeparator(), null);
    }


    public static String sqlByParams(EntityAnnotation entityAnnotation, Object params, String[] paramNames,int sqlType, String columns, String orderBy, String prefix) {

        QueryDefine define = parseParams(entityAnnotation, params,paramNames, sqlType, columns, orderBy, prefix);
        return define.toSQL();


    }

    public static String sqlConvert(String sql, EntityAnnotation entityAnnotation) {
        if (StringUtils.isBlank(sql)) return sql;
        sql = convertSqlPropsToColumns(sql, entityAnnotation);
        //convert  #{id}==> #{params.id}
        sql = convertSqlAddParamPrefix(sql, Const.PARAMS);
        return sql;
    }

    public static String sqlById(ProviderContext context, Object id, int sqlType, String select) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        List<String> props = entityAnnotation.getIdPropertyNames();
        List<String> columns = entityAnnotation.getIdColumnNames();
        if (props.size() == 0) throw new MapperException("primary key not found");
        if (props.size() > 1) throw new MapperException("multi primary keys not supported for GetById");

        String where = columns.get(0) + " = #{" + props.get(0) + "}";

         QueryDefine queryDefine=new QueryDefine(sqlType);
        queryDefine.setColumns(select);
        queryDefine.setTables(entityAnnotation.getTable());
        queryDefine.setWhere(where);
        return queryDefine.toSQL();

    }
}

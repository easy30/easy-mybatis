package com.github.easy30.easymybatis.core;

import com.github.easy30.easymybatis.*;
import com.github.easy30.easymybatis.annotation.ColumnGeneration;
import com.github.easy30.easymybatis.annotation.Query;
import com.github.easy30.easymybatis.annotation.QueryColumn;
import com.github.easy30.easymybatis.annotation.QueryExp;
import com.github.easy30.easymybatis.dialect.Dialect;
import com.github.easy30.easymybatis.enums.RelatedOperator;
import com.github.easy30.easymybatis.utils.*;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * coolma 2019/10/30
 **/
public class ProviderSupport {
    private static Logger logger = LoggerFactory.getLogger(ProviderSupport.class);

    //public static String SQL_SELECT_KEY = "<selectKey keyProperty='{}' resultType='{}' order='{}'>{}</selectKey>";
    @SneakyThrows
    public static String sqlSetValues(String table, Object entity, EntityAnnotation entityAnnotation, String prefix, UpdateOption updateOption) {
        LineBuilder lb = new LineBuilder();
        if (prefix == null) prefix = "";
        if (prefix.length() > 0) prefix += ".";
        //Class entityClass = entity.getClass();
        Dialect dialect = entityAnnotation.getDialect();
        Set ignoreColumnSet = MapperOptionSupport.getIgnoreColumnSet(updateOption);
        Map<String, String> extraColVals = MapperOptionSupport.getExtraColVals(updateOption);
        Map<String, ColumnAnnotation> columnMap = entityAnnotation.getPropertyColumnMap();
        //cglib proxy objects
        Set<String> changedProperties = EntityProxyFactory.getChangedProperties(entity);

        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {

            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isUpdatable()) continue;
            if(columnAnnotation.isTransient()) continue;
            String prop = e.getKey();

            if (ignoreColumnSet != null && (ignoreColumnSet.contains(prop) || ignoreColumnSet.contains(columnAnnotation.getName()))) continue;


            int valueType = 0;//0: none  1 value 2:native dialect value
            //-- option优先级比较高(原始sql赋值)
            Object value = MapperOptionSupport.getAndRemove(extraColVals, prop, columnAnnotation.getName());
            if (value != null) {
                valueType = 2;
            }


            //-- entity value
            if (value == null) {

                if (changedProperties == null || changedProperties.contains(prop)) {
                    value = entityAnnotation.getProperty(entity, prop);
                }
                if (value != null) {
                    valueType = 1;

                }
            }

            //-- dialect value  实体继承DialectEntity,可以设置原始sql值,不建议使用,建议从UpdateOption中设置
            if (value == null) {
                value = entityAnnotation.getDialectValue(entity, prop);
                if (value != null) {
                    valueType = 2;
                }
            }

            // generator value
            if (value == null) {

                ColumnGenerationHandler columnGenerationHandler = columnAnnotation.getColumnGenerationHandler();
                if (columnGenerationHandler != null) {
                    value=columnGenerationHandler.getUpdateValue(table, entity, prop,columnAnnotation.getPropType());
                    if (value != null) {
                        entityAnnotation.setProperty(entity, prop, value);
                        valueType = 1;

                    }
                }
            }

            //-- default 注解定义的缺省值
            if (value == null) {
                value = columnAnnotation.getColumnUpdateDefault();
                if (value != null) {
                    valueType = 2;
                }
            }

            if (valueType == 1) {
                lb.append(Utils.format(" {}=#{{}}, ", dialect.getQuotedColumn(columnAnnotation.getName()), prefix + prop));
            } else if (valueType == 2) {
                lb.append(Utils.format(" {}={}, ", dialect.getQuotedColumn(columnAnnotation.getName()), value));
            }
            //valueType==0的情况, 如果Option允许插入null,则插入null值
            else if (updateOption != null && updateOption.isWithNullColumns()) {
                lb.append(Utils.format(" {}= null, ", dialect.getQuotedColumn(columnAnnotation.getName())));
            }

        }

        //-- 剩下的
        if (extraColVals != null) {
            extraColVals.forEach((k, v) -> {
                lb.append(Utils.format(" {}={}, ", ProviderSupport.convertColumn(k, entityAnnotation), v));
            });

        }

        String result = lb.toString();
        if (StringUtils.isEmpty(result))
            throw new MapperException("entity for update is empty. You need set some values");
        return result;
    }

    @Deprecated
    private static String sqlSetValues(Class entityClass, Map<String, ColumnAnnotation> propertyColumnMap, String prefix) {
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

    public static String sqlWhereById(Object entity, EntityAnnotation entityAnnotation, String prefix) {
        if (prefix == null) prefix = "";
        if (prefix.length() > 0) prefix += ".";
        String where = "";
        Dialect dialect = entityAnnotation.getDialect();
        List<String> props = entityAnnotation.getIdPropertyNames();
        List<String> columns = entityAnnotation.getIdColumnNames();
        if (props.size() == 0) throw new MapperException("no primary keys found");
        for (int i = 0; i < props.size(); i++) {
            if (i > 0) where += ",";
            String prop = props.get(i);
            Object value = entityAnnotation.getProperty(entity, prop);
            if (value != null) {
                where += dialect.getQuotedColumn(columns.get(i)) + " = #{" + prefix + prop + "}";
            } else {
                value = entityAnnotation.getDialectParam(entity, prop);
                if (value == null) throw new MapperException("property " + prop + " can not be null");
                where += dialect.getQuotedColumn(columns.get(i)) + " = " + value;
            }


        }
        return where;

    }

    /**
     * convert  prop "{myField}" to  column "my_field"
     *
     * @param sql
     * @param entityAnnotation
     * @return
     */
    public static String convertSqlPropsToColumns(String sql, EntityAnnotation entityAnnotation, String table) {
        //  regex= ([^#\$]\{|^\{)(\w+)\} .  "{id}" or  "  {id}" ,but not #{id} ${id}
        RegularReplace rr = new RegularReplace(sql, "([^#\\$]\\{|^\\{)(\\w+)\\}");
        //  [^#\$]\{\w+\}|^\{(\w+)\}
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        while (rr.find()) {
            String prop = rr.group(2);
            if ("TABLE".equalsIgnoreCase(prop)) {
                rr.replace(entityAnnotation.getDialect().getQuotedColumn(table));
                continue;
            }else if (prop.startsWith("TABLE:")){
                String className=prop.substring("TABLE:".length()).trim();
                String entityTable= null;
                try {
                    entityTable = EntityAnnotation.getInstance(Class.forName(className)).getTable();
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("not entity class found: "+className+"("+prop+")");
                }
                rr.replace(entityAnnotation.getDialect().getQuotedColumn(entityTable));
                continue;
            }
            ColumnAnnotation ca = propertyColumnMap.get(prop);
            String column = null;
            if (ca != null) {
                column = entityAnnotation.getDialect().getQuotedColumn(ca.getName());
                if (rr.group().charAt(1) == '{') column = rr.group().charAt(0) + column;//todo: what's this?
            } else {
                column = prop;
            }
            rr.replace(column);

        }
        return rr.getResult();

    }

    /**
     * convert prop to column name ,such as:  {aF} desc , bF asc, cF  ==>  a_f desc , b_f asc , c_f
     * 1.select columns
     * 2.order by columns
     *
     * @param columns
     * @param entityAnnotation
     * @return
     */
    private static String[] selectKeyWords={"ALL","DISTINCT","DISTINCTROW","HIGH_PRIORITY"};
    private static Set<String> selectKeyWordSet=new HashSet(Arrays.asList(selectKeyWords));
    public static String convertPropsToColumns(String columns, EntityAnnotation entityAnnotation, String table,String tableAlias) {
        if (StringUtils.isBlank(columns) || columns.trim().equals("*")) return columns;
        //StringBuilder result = new StringBuilder("");
        StringJoiner columnJoiner = new StringJoiner(" , ");

        String keyword="";//distinct
        Set<String> excludeColumns = columns.startsWith("!") ? new HashSet<>() : null;
        // exclude columns  (only support simple column/prop such as column1,column2,  not max(column1) )
        if (columns.startsWith("!")) {
            columns = columns.substring(1);
        }
        // include columns
        else {
            // for complex columns such as "substring(prop1,1,3),prop2", it's hard to parse.
            // so must use {} , that is "substring({prop1},1,3),{prop2}", and use convertSqlColumns() to parse easily
            if (columns.indexOf('(') >= 0) {
                return convertSqlPropsToColumns(columns, entityAnnotation, table);
            }
            // remove "distinct"...  , such as select "distinct aaa as bbb" from table1,
            int n = indexOfWhitespace(columns);
            if(n>=0 && selectKeyWordSet.contains(columns.substring(0,n).toUpperCase())){
                keyword=columns.substring(0,n)+" ";
                columns=columns.substring(n).trim();
                if (StringUtils.isBlank(columns) || columns.trim().equals("*")) return columns;

            }
        }

        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String[] items = columns.split(",");
        for (String s : items) {
            s = s.trim();
            //if (result.length() > 0) result.append( " , ");

            int n = indexOfWhitespace(s);
            //example:  t.f1 desc ,   t.f1 as fff
            String prefix = ""; //t
            String prop = ""; //f1
            String suffix = ""; //desc
            if (n == -1) {
                prop = s;
            } else {
                prop = s.substring(0, n);
                suffix = s.substring(n);
            }
            n= prop.indexOf('.');
            if(n>0){
                prefix=prop.substring(0,n+1);//t1.
                prop=prop.substring(n+1);
            }
            if(StringUtils.isBlank(prefix) && StringUtils.isNotBlank(tableAlias)) {
                prefix=tableAlias+".";
            }

            if (prop.equals("*")) {
                columnJoiner.add(prefix+prop);
                continue;
            }
            prop = trimBrace(prop);

            ColumnAnnotation ca = propertyColumnMap.get(prop);
            if (ca == null) {
                logger.debug("can't find prop {} in entity ", prop);
            }
            String column = ca != null ? ca.getName() : prop;
            if (excludeColumns == null) {
                column = entityAnnotation.getDialect().getQuotedColumn(column);
                columnJoiner.add(prefix + column + suffix);
            } else {
                excludeColumns.add(column);
            }

        }

        if (excludeColumns == null) {
            return keyword+columnJoiner.toString();
        }
        //exclude columns
        else {
            return entityAnnotation.getColumnMap().keySet().stream().filter(e -> !excludeColumns.contains(e))
                    .map(e -> entityAnnotation.getDialect().getQuotedColumn(e))
                    .collect(Collectors.joining(","));
        }

    }

    /**
     * @param propertyOrColumn
     * @param entityAnnotation
     * @return column with quote
     */
    public static String convertColumn(String propertyOrColumn, EntityAnnotation entityAnnotation) {
        propertyOrColumn = trimBrace(propertyOrColumn);
        ColumnAnnotation ca = entityAnnotation.getPropertyColumnMap().get(propertyOrColumn);
        if (ca != null) {
            return entityAnnotation.getDialect().getQuotedColumn(ca.getName());
        }
        if (entityAnnotation.getColumnMap().containsKey(propertyOrColumn)) {
            return entityAnnotation.getDialect().getQuotedColumn(propertyOrColumn);
        }
        return propertyOrColumn;
    }


    public static String sqlComplete(String sql, EntityAnnotation entityAnnotation, MapperOption options) {
        if (sql == null) sql = "";
        String sql2 = sql.length() == 0 ? "" : sql.trim().toLowerCase();
        if (Utils.startWithTokens(sql2, "select")) {
            return sql;
        } else if (Utils.startWithTokens(sql2, "from")) {
            return "select * " + sql;
        } else {
            if (entityAnnotation == null) return sql;

            String table = MapperOptionSupport.getTable(entityAnnotation, options);
            if (table == null) return sql;
            return "select * from " + table + " " + entityAnnotation.getDialect().addWhereIfNeed(sql);

        }
    }

    public static String convertSqlAddParamPrefix(String sql, String prefix) {
         sql=  Utils.regularReplace(sql,  "#\\{(\\w+)\\}", "#'{'" + prefix + ".{1}'}'");
        return Utils.regularReplace(sql,"\\$\\{(\\w+)\\}", "$'{'" + prefix + ".{1}'}'");
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

    public static QueryDefine parseParams(EntityAnnotation entityAnnotation, Object params, String[] paramNames, int sqlType,
                                          String columns, String orderBy, String prefix, MapperOption mapperOption) {

        Dialect dialect = entityAnnotation.getDialect();
        QueryDefine queryDefine = new QueryDefine(sqlType);
        String defaultTable= dialect.getQuotedColumn(MapperOptionSupport.getTable(entityAnnotation, mapperOption));
        String tables = defaultTable;
        String where = "";
        String groupBy = "";

        String other = "";
        RelatedOperator innerOperator = RelatedOperator.AND;
        RelatedOperator outerOperator = RelatedOperator.AND;
        boolean bSelect = sqlType == Global.SQL_TYPE_SELECT;
        boolean queryPropertyEnable = true;

        String tableAlias = null;
        //-- load @Query Anno
        if (params != null) {
            Query query = params.getClass().getAnnotation(Query.class);
            boolean ignoreQuery = mapperOption != null && mapperOption.isIgnoreQueryAnnotation();
            //-- Query found
            if (query != null && !ignoreQuery) {
                //-- overwrite columns
                if (bSelect) {
                    // if columns is null then use query.columns. args columns > query.columns
                    columns = findFirstNotBlank(columns, query.columns());
                }

                tableAlias = query.tableAlias() ;
                if (StringUtils.isNotBlank(tableAlias)) tables = tables + " " + tableAlias;

                //-- overwrite default tables. query.tables() > @Table on entity
                String queryTables = arrayToString(query.tables());
                if (!StringUtils.isBlank(queryTables)) {
                    tables = queryTables;
                }

                //-- set base conditions
                where = sqlConvert(arrayToString(query.where()), entityAnnotation, defaultTable);
                groupBy = convertPropsToColumns(arrayToString(query.groupBy()), entityAnnotation, defaultTable,tableAlias);
                orderBy = findFirstNotBlank(orderBy, arrayToString(query.orderBy()));
                other = sqlConvert(arrayToString(query.other()), entityAnnotation, defaultTable);

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

        columns = ProviderSupport.convertPropsToColumns(columns, entityAnnotation, defaultTable,tableAlias);


        LineBuilder propertyConditions = new LineBuilder();
        if (queryPropertyEnable && params != null) {
            SimpleProperties sp = SimpleProperties.create(params);
            //for updateByParams deleteByParams
            boolean needValue = ArrayUtils.isNotEmpty(paramNames);
            boolean paramsIsMap = params instanceof Map;
            String[] props = needValue ? paramNames : sp.getProperties();
            for (String prop : props) {
                prop = prop.trim();
                Object value = sp.getValue(prop);

                if (value != null) {
                    String fullProp = prefix == null || prefix.length() == 0 ? prop : prefix + "." + prop;
                    // map params
                    if (paramsIsMap) {
                        addCondition(propertyConditions, innerOperator, Utils.format(Global.SQL_EQ, entityAnnotation.getColumnName(prop), fullProp));
                    } else { // object params
                        String condition = "";

                        QueryExp queryExp = ObjectSupport.getAnnotation(QueryExp.class, params.getClass(), prop);
                        //-- use queryItem
                        if (queryExp != null) {
                            if (queryExp.ignore()) {
                                continue;
                            }
                            String queryItemValue = Utils.toString(queryExp.value(), System.lineSeparator(), null);
                            if (StringUtils.isBlank(queryItemValue)) {
                                condition = doColumnDefault(entityAnnotation, tableAlias, prop, fullProp, value);
                            } else {
                                condition = sqlConvert(queryItemValue, entityAnnotation, defaultTable);
                            }


                        } else {
                            //Class valueType= ((ObjectProperties)sp).getType(prop);
                            //-- user QueryColumn
                            QueryColumn queryColumn = ObjectSupport.getAnnotation(QueryColumn.class, params.getClass(), prop);
                            if (queryColumn != null) {
                                String propOrColumn = StringUtils.isNotBlank(queryColumn.column()) ? queryColumn.column() : prop;
                                String column = convertColumn(propOrColumn, entityAnnotation);
                                String tableOfColumn = findFirstNotBlank(queryColumn.table(), tableAlias);
                                if (StringUtils.isNotBlank(tableOfColumn)) column = tableOfColumn + "." + column;// add tablename
                                condition = QueryColumnSupport.doQueryColumn(entityAnnotation, column, queryColumn.operator(), fullProp, value);

                            }
                            //-- use default: a=b   or in []
                            else {
                                condition = doColumnDefault(entityAnnotation, tableAlias, prop, fullProp, value);

                            }
                        }

                        if (StringUtils.isNotBlank(condition)) {
                            addCondition(propertyConditions, innerOperator, condition);
                        }
                        //propertyConditions.append(Utils.format(Const.SQL_AND, entityAnnotation.getColumnName(prop), fullProp));
                    }

                } else { //@Deprecated 使用@QueryItem，此功能可以去掉
                    value = entityAnnotation.getDialectParam(params, prop);
                    if (value != null) {
                        addCondition(propertyConditions, innerOperator, Utils.format(Global.SQL_EQ_DIALECT, dialect.getQuotedColumn(entityAnnotation.getColumnName(prop)), value));
                    }
                }
                if (needValue && value == null) throw new MapperException("property " + prop + " of params can not be null");
            }

        }


        if (propertyConditions.length() > 0) {
            if (StringUtils.isBlank(where)) {
                where = propertyConditions.toString();
            } else {
                where += " " + outerOperator + " ( " + propertyConditions + " ) ";
            }
        }


        if (!bSelect && StringUtils.isBlank(where))
            throw new MapperException(" 'Where' conditions can not be null( Safety!!! ). params need.");


        orderBy = convertPropsToColumns(orderBy, entityAnnotation, defaultTable,tableAlias);
        //convert {TABLE} {TABLE:com.xx.User}
        tables = convertSqlPropsToColumns(tables, entityAnnotation, defaultTable);

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

    private static void addCondition(LineBuilder lineBuilder, RelatedOperator oper, String condition) {
        if (lineBuilder.length() > 0) {
            lineBuilder.append(" " + oper + " ");
        }
        lineBuilder.append(condition);
    }

    private static String doColumnDefault(EntityAnnotation entityAnnotation, String tableName, String prop, String fullProp, Object value) {
        String column = entityAnnotation.getColumnName(prop);
        if (StringUtils.isBlank(column)) throw new MapperException("can not find the column name for property '" + prop + "'. You can use @QueryColumn set a column name.");
        column = entityAnnotation.getDialect().getQuotedColumn(column);
        if (StringUtils.isNotBlank(tableName)) column = tableName + "." + column;
        return QueryColumnSupport.doQueryColumn(entityAnnotation, entityAnnotation.getDialect().getQuotedColumn(column), null, fullProp, value);

    }

    private static String arrayToString(String[] ss) {
        return Utils.toString(ss, System.lineSeparator(), null);
    }


    public static String sqlByParams(EntityAnnotation entityAnnotation, Object params, String[] paramNames, int sqlType, String columns, String orderBy, String prefix, MapperOption mapperOption) {

        QueryDefine define = parseParams(entityAnnotation, params, paramNames, sqlType, columns, orderBy, prefix, mapperOption);
        String sql = define.toSQL();
        return sql;

    }

    public static String sqlConvert(String sql, EntityAnnotation entityAnnotation, String table) {
        if (StringUtils.isBlank(sql)) return sql;
        sql = convertSqlPropsToColumns(sql, entityAnnotation, table);
        //convert  #{id}==> #{params.id}
        sql = convertSqlAddParamPrefix(sql, Const.PARAMS);
        return sql;
    }

    public static String sqlById(EntityAnnotation entityAnnotation, Object id, int sqlType, String select, String table) {
        //EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        List<String> props = entityAnnotation.getIdPropertyNames();
        List<String> columns = entityAnnotation.getIdColumnNames();
        if (props.size() == 0) throw new MapperException("primary key not found");
        if (props.size() > 1) throw new MapperException("multi primary keys not supported for GetById");

        String where = entityAnnotation.getDialect().getQuotedColumn(columns.get(0)) + " = #{" + Const.ID + "}";

        QueryDefine queryDefine = new QueryDefine(sqlType);
        queryDefine.setColumns(select);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        queryDefine.setWhere(where);
        return queryDefine.toSQL();

    }

    public static String sqlByIds(EntityAnnotation entityAnnotation, Object[] ids, int sqlType, String select, String table) {
        List<String> props = entityAnnotation.getIdPropertyNames();
        List<String> columns = entityAnnotation.getIdColumnNames();
        if (props.size() == 0) throw new MapperException("primary key not found");
        if (props.size() > 1) throw new MapperException("multi primary keys not supported for GetById");

        String where = entityAnnotation.getDialect().getQuotedColumn(columns.get(0)) + " in ";
        where+= "<foreach collection=\"" +  Const.IDS + "\" separator=\",\" item=\"item\" open=\" (\" close=\") \">#{item}</foreach>";
        QueryDefine queryDefine = new QueryDefine(sqlType);
        queryDefine.setColumns(select);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        queryDefine.setWhere(where);
        return queryDefine.toSQL();

    }

}

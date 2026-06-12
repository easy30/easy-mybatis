package com.github.easy30.easymybatis;


import com.github.easy30.easymybatis.core.*;
import com.github.easy30.easymybatis.dialect.Dialect;
import com.github.easy30.easymybatis.utils.EntityProxyFactory;
import com.github.easy30.easymybatis.utils.LineBuilder;
import com.github.easy30.easymybatis.utils.Utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * coolma 2019/11/4
 **/
public class Provider<E> {
    private static Logger logger = LoggerFactory.getLogger(Provider.class);

    @SneakyThrows
    public String insert(ProviderContext context, @Param(Const.ENTITY) E entity, @Param(Const.OPTIONS) UpdateOption... options) {
        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Dialect dialect = entityAnnotation.getDialect();
        LineBuilder sql = new LineBuilder();
        sql.append("<script>")
                .append("{}")
                .append("insert into {} ")
                .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">{} </trim>")
                .append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">{}</trim>")
                .append("</script>");

        LineBuilder selectKeys = new LineBuilder();
        UpdateOption option = merge(options);
        Set ignoreColumnSet = MapperOptionSupport.getIgnoreColumnSet(option);
        Map<String, String> extraColVals = MapperOptionSupport.getExtraColVals(option);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);

        Map<String, String> insertColumnMap = new LinkedHashMap<>();
        Map<String, String> valueMap = new LinkedHashMap<>();
        resolveInsertColumns(entity, entityAnnotation, dialect, option, ignoreColumnSet, extraColVals, table, insertColumnMap, valueMap);

        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();
        insertColumnMap.forEach((prop, column) -> {
            columnBuilder.append(column + ",");
            String value = valueMap.get(prop);
            if (value == null) {
                valueBuilder.append(Utils.format("#{{}},", Const.ENTITY + "." + prop));
            } else {
                valueBuilder.append(Utils.format("{},", value));
            }
        });

        String result = Utils.format(sql.toString(), selectKeys, dialect.getQuotedColumn(table), columnBuilder, valueBuilder);
        logger.debug("provider sql= {}", result);
        return result;
    }

    /**
     * 计算单个实体的插入列与值（insert/insertList/upsertList 共用）。
     * <p>按优先级取值：option原始值 → 实体值 → dialect值 → generator → insert默认值 → null(withNullColumns)。</p>
     * @param insertColumnMap 累积的 prop -> 带引号列名（跨多行累积列集合，保持顺序）
     * @param valueMap        本行 prop -> 值：null 表示取实体变量，非null 表示常量/原始SQL
     */
    @SneakyThrows
    private void resolveInsertColumns(E entity, EntityAnnotation entityAnnotation, Dialect dialect,
                                      UpdateOption option, Set ignoreColumnSet, Map<String, String> extraColVals,
                                      String table, Map<String, String> insertColumnMap, Map<String, String> valueMap) {
        Map<String, ColumnAnnotation> columnMap = entityAnnotation.getPropertyColumnMap();
        //cglib proxy objects
        Set<String> changedProperties = EntityProxyFactory.getChangedProperties(entity);
        for (Map.Entry<String, ColumnAnnotation> e : columnMap.entrySet()) {
            ColumnAnnotation columnAnnotation = e.getValue();
            if (!columnAnnotation.isInsertable()) continue;
            if (columnAnnotation.isTransient()) continue;
            String prop = e.getKey();
            if (ignoreColumnSet != null && (ignoreColumnSet.contains(prop) || ignoreColumnSet.contains(columnAnnotation.getName()))) continue;

            int valueType = 0;//0: none  1 value 2:dialect value
            Object value = MapperOptionSupport.getAndRemove(extraColVals, prop, columnAnnotation.getName());
            if (value != null) {
                valueType = 2;
                value = ProviderSupport.convertSqlAddParamPrefix(value.toString(), Const.OPTIONS_PARAMS);
            }
            // entity value
            if (valueType == 0) {
                if (changedProperties == null) {
                    value = entityAnnotation.getProperty(entity, prop);
                    if (value != null) valueType = 1;
                } else if (changedProperties.contains(prop)) {// support null
                    value = entityAnnotation.getProperty(entity, prop);
                    valueType = 1;
                }
            }
            // dialect value
            if (valueType == 0) {
                value = entityAnnotation.getDialectValue(entity, prop);
                if (value != null) valueType = 2;
            }
            // generator value
            if (valueType == 0) {
                ColumnGenerationHandler columnGenerationHandler = columnAnnotation.getColumnGenerationHandler();
                if (columnGenerationHandler != null) {
                    value = columnGenerationHandler.getInsertValue(table, entity, prop, columnAnnotation.getPropType());
                    if (value != null) {
                        entityAnnotation.setProperty(entity, prop, value);
                        valueType = 1;
                    }
                }
            }
            // default value
            if (valueType == 0) {
                value = columnAnnotation.getColumnInsertDefault();
                if (value != null) valueType = 2;
            }
            if (valueType == 0 && option != null && option.isWithNullColumns()) {
                value = "null";
                valueType = 2;
            }

            if (valueType == 1) {
                insertColumnMap.put(prop, dialect.getQuotedColumn(columnAnnotation.getName()));
                valueMap.put(prop, null);//从变量中获取值
            } else if (valueType == 2) {
                insertColumnMap.put(prop, dialect.getQuotedColumn(columnAnnotation.getName()));
                valueMap.put(prop, value.toString());//常量值
            }
        }

        //-- 剩下的(未匹配任何属性/列的额外列)
        if (extraColVals != null) {
            extraColVals.forEach((k, v) -> {
                v = ProviderSupport.convertSqlAddParamPrefix(v, Const.OPTIONS + "[0].params.");
                insertColumnMap.put(k, ProviderSupport.convertColumn(k, entityAnnotation));
                valueMap.put(k, v);//常量值
            });
        }
    }


    @SneakyThrows
    public String insertList(ProviderContext context, @Param(Const.ENTITY_LIST) List<E> entityList, @Param(Const.OPTIONS) UpdateOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Dialect dialect = entityAnnotation.getDialect();
        LineBuilder sql = new LineBuilder();
        //INSERT INTO users (name, email) VALUES
        //('Alice', 'alice@example.com'),
        //('Bob', 'bob@example.com'),
        //('Charlie', 'charlie@example.com');
        sql.append("<script> \n insert into {} ({}) values \n {} \n </script>");

        Map<String, String> insertColumnMap = new LinkedHashMap();
        List<Map<String, String>> valueMapList = new ArrayList<>();
        UpdateOption option = merge(options);
        Set ignoreColumnSet = MapperOptionSupport.getIgnoreColumnSet(option);
        Map<String, String> extraColVals = MapperOptionSupport.getExtraColVals(option);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        for (E entity : entityList) {
            Map<String, String> valueMap = new LinkedHashMap<>();
            valueMapList.add(valueMap);
            resolveInsertColumns(entity, entityAnnotation, dialect, option, ignoreColumnSet, extraColVals, table, insertColumnMap, valueMap);
        }

        String insertColumns = insertColumnMap.values().stream().collect(Collectors.joining(","));
        StringJoiner valuesList = new StringJoiner(",");
        for (int i = 0; i < valueMapList.size(); i++) {
            int index = i;
            Map<String, String> valueMap = valueMapList.get(i);
            StringJoiner values = new StringJoiner(",");
            insertColumnMap.forEach((prop, column) -> {
                String value = valueMap.get(prop);
                if (value == null) {
                    values.add("#{" + Const.ENTITY_LIST + "[" + index + "]." + prop + "}");
                } else {
                    values.add(value);
                }

            });
            valuesList.add("(" + values + ")");
        }

        String result = Utils.format(sql.toString(), dialect.getQuotedColumn(table), insertColumns, valuesList);
        logger.debug("provider sql= {}", result);
        return result;
    }

    @SneakyThrows
    public String upsertList(ProviderContext context,
                             @Param(Const.ENTITY_LIST) List<E> entityList,
                             @Param(Const.KEY_COLUMNS) String[] keyColumns,
                             @Param(Const.UPDATE_COLUMNS) String[] updateColumns,
                             @Param(Const.OPTIONS) UpdateOption... options) {
        if (entityList == null || entityList.isEmpty()) throw new RuntimeException("entityList can not be empty");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Dialect dialect = entityAnnotation.getDialect();
        Map<String, ColumnAnnotation> columnMap = entityAnnotation.getPropertyColumnMap();
        UpdateOption option = merge(options);
        Set ignoreColumnSet = MapperOptionSupport.getIgnoreColumnSet(option);
        Map<String, String> extraColVals = MapperOptionSupport.getExtraColVals(option);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);

        // 计算插入列与每行的值表达式（与 insert/insertList 同规则）
        Map<String, String> insertColumnMap = new LinkedHashMap();// prop -> quotedColumn
        List<Map<String, String>> valueMapList = new ArrayList<>();// 每行 prop -> 常量值(null 表示取实体变量)
        for (E entity : entityList) {
            Map<String, String> valueMap = new LinkedHashMap<>();
            valueMapList.add(valueMap);
            resolveInsertColumns(entity, entityAnnotation, dialect, option, ignoreColumnSet, extraColVals, table, insertColumnMap, valueMap);
        }

        List<String> insertColumns = new ArrayList<>(insertColumnMap.values());
        List<List<String>> valueRows = new ArrayList<>();
        for (int i = 0; i < valueMapList.size(); i++) {
            Map<String, String> valueMap = valueMapList.get(i);
            List<String> row = new ArrayList<>();
            for (Map.Entry<String, String> ce : insertColumnMap.entrySet()) {
                String v = valueMap.get(ce.getKey());
                row.add(v == null ? "#{" + Const.ENTITY_LIST + "[" + i + "]." + ce.getKey() + "}" : v);
            }
            valueRows.add(row);
        }

        // 归一化冲突列为带引号列名
        List<String> keyQuotedList = new ArrayList<>();
        Set<String> keyQuotedSet = new HashSet<>();
        if (keyColumns != null) {
            for (String k : keyColumns) {
                String quoted = ProviderSupport.convertColumn(k, entityAnnotation);
                keyQuotedList.add(quoted);
                keyQuotedSet.add(quoted);
            }
        }

        // 计算冲突更新列：upsertIgnore 优先；否则按 updateColumns（null=全量）
        boolean ignore = option != null && option.isUpsertIgnore();
        LinkedHashMap<String, String> updateColumnMap = new LinkedHashMap<>();// quotedColumn -> 更新值表达式(null=引用新插入值)
        if (!ignore) {
            if (updateColumns == null || updateColumns.length == 0) {
                // 全量更新：插入列中，可更新、非冲突列、非 insert-only 列（如 create_time）
                for (Map.Entry<String, String> ce : insertColumnMap.entrySet()) {
                    String prop = ce.getKey();
                    String quoted = ce.getValue();
                    if (keyQuotedSet.contains(quoted)) continue;
                    ColumnAnnotation ca = columnMap.get(prop);
                    if (ca == null || !ca.isUpdatable()) continue;
                    boolean insertOnly = ca.getColumnInsertDefault() != null && ca.getColumnUpdateDefault() == null;
                    if (insertOnly) continue;
                    updateColumnMap.put(quoted, ca.getColumnUpdateDefault());
                }
            } else {
                // 指定列更新
                for (String u : updateColumns) {
                    String prop = trimToProp(u, entityAnnotation);
                    ColumnAnnotation ca = columnMap.get(prop);
                    // 显式指定了不可更新列则静默跳过（与 JPA @Column(updatable=false) 行为一致）
                    if (ca != null && !ca.isUpdatable()) continue;
                    String quoted = ProviderSupport.convertColumn(u, entityAnnotation);
                    updateColumnMap.put(quoted, ca != null ? ca.getColumnUpdateDefault() : null);
                }
            }
            if (updateColumnMap.isEmpty()) ignore = true;// 没有可更新列则退化为忽略
        }

        String result = dialect.getUpsertSql(dialect.getQuotedColumn(table),
                insertColumns, valueRows, keyQuotedList, updateColumnMap, ignore);
        result = "<script>\n" + result + "\n</script>";
        logger.debug("provider upsert sql= {}", result);
        return result;
    }

    // 将属性名或列名解析为属性名（用于取 ColumnAnnotation）；无法解析时原样返回
    private String trimToProp(String propOrColumn, EntityAnnotation entityAnnotation) {
        if (entityAnnotation.getPropertyColumnMap().containsKey(propOrColumn)) return propOrColumn;
        for (Map.Entry<String, ColumnAnnotation> e : entityAnnotation.getPropertyColumnMap().entrySet()) {
            if (e.getValue().getName().equals(propOrColumn)) return e.getKey();
        }
        return propOrColumn;
    }

    public String update(ProviderContext context, @Param(Const.ENTITY) E entity, @Param(Const.OPTIONS) UpdateOption... options) {
        if (entity == null) throw new RuntimeException("entity can not be null");
        //Class entityClass = entity.getClass();
        UpdateOption option = merge(options);
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        String set = ProviderSupport.sqlSetValues(table, entity, entityAnnotation, Const.ENTITY, option, true);
        String where = ProviderSupport.sqlWhereById(entity, entityAnnotation, Const.ENTITY);
        QueryDefine queryDefine = new QueryDefine(Global.SQL_TYPE_UPDATE);
        queryDefine.setWhere(where);
        queryDefine.setSet(set);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        return queryDefine.toSQL();
    }

    public String save(ProviderContext context, @Param(Const.ENTITY) E entity, @Param(Const.OPTIONS) UpdateOption... options) {
        //如果包含了
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        List<String> idPropertyNames = entityAnnotation.getIdPropertyNames();
        boolean insert = true;
        if (idPropertyNames.size() > 0) {
            insert = false;
            for (String p : idPropertyNames) {
                Object value = entityAnnotation.getProperty(entity, p);
                if (value == null) {
                    insert = true;
                    break;
                }
            }

        }
        return insert ? insert(context, entity, options) : update(context, entity, options);

    }

   /* public String delete(E entity) {
        Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        return Utils.format(ProviderSupport.SQL_DELETE, "", entityAnnotation.getTable(),
                ProviderSupport.sqlWhereById(entity, entityAnnotation));
    }*/

    public String updateByParams(ProviderContext context, @Param(Const.ENTITY) E entity, @Param(Const.PARAMS) Object params,
                                 @Param(Const.PARAM_NAMES) String paramNames, @Param(Const.OPTIONS) UpdateOption... options) {
        if (StringUtils.isBlank(paramNames)) throw new MapperException("paramNames can not be empty");
        //Class entityClass = entity.getClass();
        if (entity == null || params == null) throw new RuntimeException("entity or params can not be null");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        UpdateOption option = merge(options);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        //String sql = ProviderSupport.SQL_UPDATE;
        String set = ProviderSupport.sqlSetValues(table, entity, entityAnnotation, Const.ENTITY, option, false);
        QueryDefine result = ProviderSupport.parseParams(entityAnnotation, params, paramNames.split("[,\\s]+"), Global.SQL_TYPE_UPDATE, "", null, Const.PARAMS, merge(options));
        result.setSet(set);
        return result.toSQL();

    }

    private <T> T merge(T... options) {
        return MapperOptionSupport.merge(options);
    }


    public String updateByCondition(ProviderContext context, @Param(Const.ENTITY) E entity, @Param(Const.CONDITION) String condition,
                                    @Param(Const.PARAMS) Object params, @Param(Const.OPTIONS) UpdateOption... options) {

        //Class entityClass = entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        UpdateOption option = merge(options);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        String set = ProviderSupport.sqlSetValues(table, entity, entityAnnotation, Const.ENTITY, option, false);
        if (StringUtils.isBlank(condition))
            throw new RuntimeException("because of safety, where condition can not be blank. (set where to * for updating all records)");
        if (condition.equals("*")) condition = "";//update all
        if (condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation, table);
        }

        QueryDefine queryDefine = new QueryDefine(Global.SQL_TYPE_UPDATE);
        queryDefine.setCondition(entityAnnotation.getDialect().addWhereIfNeed(condition));
        queryDefine.setSet(set);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(MapperOptionSupport.getTable(entityAnnotation, option)));
        return queryDefine.toSQL();

    }


    public String get(ProviderContext context, @Param(Const.ID) Object id, @Param(Const.COLUMNS) String selectColumns, @Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        SelectOption option = merge(options);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {
            selectColumns = ProviderSupport.convertPropsToColumns(selectColumns, entityAnnotation, table, null);
        }
        return ProviderSupport.sqlById(entityAnnotation, id, Global.SQL_TYPE_SELECT, selectColumns, table);
    }

    public String getValue(ProviderContext context, @Param(Const.ID) Object id, @Param(Const.COLUMN) String column, @Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        SelectOption option = merge(options);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        if (StringUtils.isBlank(column)) {
            throw new MapperException("column name need");
        } else {
            column = ProviderSupport.convertPropsToColumns(column, entityAnnotation, table, null);
        }
        return ProviderSupport.sqlById(entityAnnotation, id, Global.SQL_TYPE_SELECT, column, table);
    }

    public String listByIds(ProviderContext context, @Param(Const.IDS) Object[] ids, @Param(Const.COLUMNS) String selectColumns, @Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        SelectOption option = merge(options);
        String table = MapperOptionSupport.getTable(entityAnnotation, option);
        if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } else {
            selectColumns = ProviderSupport.convertPropsToColumns(selectColumns, entityAnnotation, table, null);
        }
        return ProviderSupport.sqlByIds(entityAnnotation, ids, Global.SQL_TYPE_SELECT, selectColumns, table);
    }

    public String deleteById(ProviderContext context, @Param(Const.ID) Object id, @Param(Const.OPTIONS) DeleteOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());

        String table = MapperOptionSupport.getTable(entityAnnotation, merge(options));
        return ProviderSupport.sqlById(entityAnnotation, id, Global.SQL_TYPE_DELETE, "", table);
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

    public String getByParams(ProviderContext context, @Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,
                              @Param(Const.COLUMNS) String selectColumns, @Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        /*if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        } *//*else {

            selectColumns = ProviderSupport.convertColumns(selectColumns, entityAnnotation.getPropertyColumnMap());

        }*/
        return ProviderSupport.sqlByParams(entityAnnotation, params, null, Global.SQL_TYPE_SELECT, selectColumns, orderBy, Const.PARAMS, merge(options));

    }

    public String getValueByParams(ProviderContext context, @Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,
                                   @Param(Const.COLUMN) String column, @Param(Const.OPTIONS) SelectOption... options) {
        //Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
      /*  if (StringUtils.isBlank(column)) {
            column = "*";
        }*/
        return ProviderSupport.sqlByParams(entityAnnotation, params, null, Global.SQL_TYPE_SELECT, column, orderBy, Const.PARAMS, merge(options));

    }

    public String listByParams(ProviderContext context, @Param(Const.PARAMS) Object params, @Param(Const.ORDER) String orderBy,
                               @Param(Const.COLUMNS) String selectColumns, @Param(Const.OPTIONS) SelectOption... options) {
        //Class entityClass = params.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        //Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
       /* if (StringUtils.isBlank(selectColumns)) {
            selectColumns = "*";
        }*//* else {
            selectColumns = ProviderSupport.convertColumns(selectColumns, propertyColumnMap);
        }*/

        return ProviderSupport.sqlByParams(entityAnnotation, params, null, Global.SQL_TYPE_SELECT, selectColumns, orderBy, Const.PARAMS, merge(options));

    }

    public String pageByParams(ProviderContext context, @Param(Const.PARAMS) Object params, @Param(Const.PAGE) Page page,
                               @Param(Const.ORDER) String orderBy, @Param(Const.COLUMNS) String selectColumns, @Param(Const.OPTIONS) SelectOption... options) {
        return listByParams(context, params, orderBy, selectColumns, merge(options));

    }

    public String deleteByParams(ProviderContext context, @Param(Const.PARAMS) Object params, @Param(Const.PARAM_NAMES) String paramNames, @Param(Const.OPTIONS) DeleteOption... options) {
        if (StringUtils.isBlank(paramNames)) throw new MapperException("paramNames can not be empty");
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        return ProviderSupport.sqlByParams(entityAnnotation, params, paramNames.split("[,\\s]+"), Global.SQL_TYPE_DELETE, "", null, Const.PARAMS, merge(options));

    }


    public String deleteByCondition(ProviderContext context,
                                    @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params, @Param(Const.OPTIONS) DeleteOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        DeleteOption option = merge(options);
        if (StringUtils.isBlank(condition))
            throw new RuntimeException("For safety, WHERE condition can not be blank. (set condition to * for deleting all records)");
        String table = MapperOptionSupport.getTable(entityAnnotation, option);

        if (condition.equals("*")) condition = "";
        if (condition != null && condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation, table);
        }
        QueryDefine queryDefine = new QueryDefine(Global.SQL_TYPE_DELETE);
        queryDefine.setTables(entityAnnotation.getDialect().getQuotedColumn(table));
        queryDefine.setCondition(entityAnnotation.getDialect().addWhereIfNeed(condition));
        return queryDefine.toSQL();

    }

    public String getValueByCondition(ProviderContext context, @Param(Const.CONDITION) String condition, @Param(Const.PARAMS) Object params,
                                      @Param(Const.COLUMN) String column, @Param(Const.OPTIONS) SelectOption... options) {
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstanceByMapper(context.getMapperType());
        Map<String, ColumnAnnotation> propertyColumnMap = entityAnnotation.getPropertyColumnMap();
        //String sql = ProviderSupport.SQL_SELECT;
        String table = MapperOptionSupport.getTable(entityAnnotation, merge(options));
        column = ProviderSupport.convertColumn(column, entityAnnotation);
        if (condition == null) condition = "";
        if (condition.length() > 0) {
            condition = ProviderSupport.sqlConvert(condition, entityAnnotation, table);

        }
        QueryDefine queryDefine = new QueryDefine(Global.SQL_TYPE_SELECT);
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

            sql = ProviderSupport.sqlConvert(sql, entityAnnotation, entityAnnotation.getTable());
            sql = ProviderSupport.sqlComplete(sql, entityAnnotation, null);
        }
        logger.debug("provider sql= {}", sql);
        return sql;


    }

    public String pageBySQL(ProviderContext context, @Param(Const.SQL) String sql,
                            @Param(Const.PARAMS) Object params, @Param(Const.PAGE) Page page) {
        return listBySQL(context, sql, params);

    }

    public String getValueBySQL(ProviderContext context, @Param(Const.SQL) String sql, @Param(Const.PARAMS) Object params) {
        return listBySQL(context, sql, params);
    }

   /* public String  list(Map params){
        return params.get("@@sql").toString();
    }*/
}

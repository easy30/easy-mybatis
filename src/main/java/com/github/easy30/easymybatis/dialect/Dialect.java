package com.github.easy30.easymybatis.dialect;

import com.github.easy30.easymybatis.enums.ColumnOperator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.LinkedHashMap;
import java.util.List;

public interface Dialect {
    String getCountSql(String sql);
    List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source);
    String getPageSql(String sql);
    //for Operator define
    String[] getColumnOperatorValue(ColumnOperator columnOperator);
    String addWhereIfNeed(String condition);
    // name ==> `name` in mysql
    String getQuotedColumn(String column);

    /**
     * 生成批量 upsert（插入或冲突更新）语句。各数据库语法差异在此下沉。
     *
     * @param table         表名（已加引号）
     * @param insertColumns 插入列（已加引号），与每行 valueRows 的元素一一对应
     * @param valueRows     每行的值表达式列表（占位符如 #{es[0].name} 或常量如 now()），与 insertColumns 对齐
     * @param keyColumns    冲突判定列（已加引号）。PG/SQLite/Oracle/SQLServer 必需，为空时这些方言应抛异常；MySQL/H2 可为 null
     * @param updateColumns 冲突时更新的列（已加引号）-> 更新值表达式。
     *                      值为 null 表示引用本次插入的新值（由方言决定写法，如 VALUES(col)/EXCLUDED.col/src.col）；
     *                      值非 null 表示字面 SQL（如 now()）。仅在 ignore=false 时有效，且调用方保证非空。
     * @param ignore        true 表示冲突忽略（DO NOTHING / INSERT IGNORE），此时忽略 updateColumns
     * @return 完整可执行 SQL
     */
    default String getUpsertSql(String table,
                                List<String> insertColumns,
                                List<List<String>> valueRows,
                                List<String> keyColumns,
                                LinkedHashMap<String, String> updateColumns,
                                boolean ignore) {
        throw new UnsupportedOperationException("当前方言不支持 upsert: " + getClass().getSimpleName());
    }
}

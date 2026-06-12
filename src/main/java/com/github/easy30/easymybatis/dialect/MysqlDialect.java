package com.github.easy30.easymybatis.dialect;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringJoiner;

/**
 * coolma 2019/11/11
 **/
public class MysqlDialect extends AbstractDialect {

    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source){
        List<ParameterMapping> result = new ArrayList<ParameterMapping>();
        result.addAll(source);
        result.add(new ParameterMapping.Builder(configuration, "page.pageSize", Integer.TYPE).build());
        result.add(new ParameterMapping.Builder(configuration, "page.recordStart", Integer.TYPE).build());
        return result;

    }

    public String getPageSql(String sql) {

        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" limit ? offset ? ");
        return sb.toString();
    }

    @Override
    public String getQuotedColumn(String column) {
        if(column!=null && column.indexOf('`')>=0) return column;
        return "`"+column+"`";
    }

    /**
     * MySQL：INSERT [IGNORE] INTO t (cols) VALUES (..),(..) [ON DUPLICATE KEY UPDATE col=VALUES(col)]
     * MySQL 靠任意唯一/主键触发冲突，keyColumns 可为空。
     */
    @Override
    public String getUpsertSql(String table, List<String> insertColumns, List<List<String>> valueRows,
                               List<String> keyColumns, LinkedHashMap<String, String> updateColumns, boolean ignore) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert ").append(ignore ? "ignore " : "").append("into ").append(table).append(" (");
        sb.append(String.join(",", insertColumns)).append(") values ");
        StringJoiner rows = new StringJoiner(",");
        for (List<String> row : valueRows) {
            rows.add("(" + String.join(",", row) + ")");
        }
        sb.append(rows);
        if (!ignore) {
            StringJoiner sets = new StringJoiner(",");
            for (java.util.Map.Entry<String, String> e : updateColumns.entrySet()) {
                String col = e.getKey();
                String expr = e.getValue();// null 表示引用新插入值
                sets.add(col + "=" + (expr != null ? expr : "values(" + col + ")"));
            }
            sb.append(" on duplicate key update ").append(sets);
        }
        return sb.toString();
    }
}

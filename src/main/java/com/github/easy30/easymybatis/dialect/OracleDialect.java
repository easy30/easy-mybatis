package com.github.easy30.easymybatis.dialect;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * coolma 2019/11/11
 **/
public class OracleDialect extends AbstractDialect {
    @Override
    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source) {
        List<ParameterMapping> result = new ArrayList<ParameterMapping>();
        result.addAll(source);
        result.add(new ParameterMapping.Builder(configuration, "page.recordEnd", Integer.TYPE).build());
        result.add(new ParameterMapping.Builder(configuration, "page.recordStart", Integer.TYPE).build());
        return result;
    }

    @Override
    public String getPageSql(String sql) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ( ");
        sb.append("SELECT PAGE_TABLE_SOURCE.*,ROWNUM ROW_ID FROM ( ");
        sb.append(sql);
        sb.append(" ) PAGE_TABLE_SOURCE WHERE ROWNUM <= ? ");
        sb.append(" ) WHERE ROW_ID > ? ");
        return sb.toString();
    }

    @Override
    public String getQuotedColumn(String column) {
        if(column!=null && column.indexOf('"')>=0) return column;
        return "\""+column.toUpperCase()+"\"";
    }

    /**
     * Oracle：MERGE INTO ... USING (SELECT v1 c1,.. FROM dual UNION ALL ..) src ON (..)
     * WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...
     * keyColumns 必需。
     */
    @Override
    public String getUpsertSql(String table, List<String> insertColumns, List<List<String>> valueRows,
                               List<String> keyColumns, LinkedHashMap<String, String> updateColumns, boolean ignore) {
        return buildMergeSql(table, insertColumns, valueRows, keyColumns, updateColumns,
                ignore, false, "dual", false);
    }


}

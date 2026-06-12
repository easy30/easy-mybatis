package com.github.easy30.easymybatis.dialect;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * coolma 2019/11/11
 **/
public class SqliteDialect extends MysqlDialect{

    /**
     * SQLite：INSERT ... ON CONFLICT (keys) DO NOTHING / DO UPDATE SET col=excluded.col
     * keyColumns 必需。
     */
    @Override
    public String getUpsertSql(String table, List<String> insertColumns, List<List<String>> valueRows,
                               List<String> keyColumns, LinkedHashMap<String, String> updateColumns, boolean ignore) {
        return buildInsertValues(table, insertColumns, valueRows)
                + buildOnConflict(keyColumns, updateColumns, ignore, "excluded");
    }
}

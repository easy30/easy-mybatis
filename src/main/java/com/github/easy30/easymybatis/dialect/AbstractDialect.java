package com.github.easy30.easymybatis.dialect;

import com.github.easy30.easymybatis.enums.ColumnOperator;
import com.github.easy30.easymybatis.utils.Utils;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * coolma 2019/11/11
 **/
public abstract class AbstractDialect implements Dialect {
    @Override
    public String getCountSql(String sql) {

        sql = sql.trim();
        if (sql.startsWith("(") && sql.endsWith(")")) sql = sql.substring(1, sql.length() - 1);
        //sql = StringUtils.trimLeft(sql, new char[] { '(' }, true);
        //sql = StringUtils.trimRight(sql, new char[] { ')' }, true);
        int nLevel = 1;
        int nFrom = -1;
        int nOrderBy = -1;
        boolean all = false;
        final String WITH = "with";
        final String AS = "as";
        final String FROM = "from";
        final String GROUP = "group";
        final String ORDER = "order";
        final String BY = "by";

        String s = sql.toLowerCase();

        if (s.matches("(?s).*with\\s+.*as.*")) { //with as , only simple match
            all = true;
        }

        for (int i = 0; i < s.length(); i++) {

            //todo: ignore () or keyword in strings such as ' (order by) '
            if (s.charAt(i) == '(')
                nLevel++;
            else if (s.charAt(i) == ')')
                nLevel--;
            else if (isToken(s, FROM, i) && nLevel == 1)
                nFrom = i;
            else if (isToken(s, GROUP, i) && nLevel == 1 && isToken(s.substring(i + GROUP.length() + 1).trim(), BY, 0)) {
                all = true;
            } else if (isToken(s, ORDER, i) && nLevel == 1 && isToken(s.substring(i + ORDER.length() + 1).trim(), BY, 0)) {
                nOrderBy = i;
                break;
            }

        }
        if (nOrderBy != -1)
            sql = sql.substring(0, nOrderBy);
        if (!all)
            return "select count(*) " + sql.substring(nFrom);
        else
            return "select count(*) from ( " + sql + " ) t_table_count ";

    }

    private boolean isToken(String sql, String token, int offset) {
        if (offset > 0 && sql.charAt(offset - 1) > 32) return false;
        int end = offset + token.length();
        if (end < sql.length() && sql.charAt(end) > 32) return false;
        return sql.startsWith(token, offset);

    }

    @Override
    public String[] getColumnOperatorValue(ColumnOperator columnOperator) {
        return columnOperator.getValue();
    }

    public String addWhereIfNeed(String condition) {
        if (condition.length() == 0 || Utils.startWithTokens(condition, "where") || Utils.startWithTokens(condition, "order", "by")
                || Utils.startWithTokens(condition, "group", "by") || Utils.startWithTokens(condition, "limit")) {
            return condition;
        } else {
            return " where " + condition;
        }
    }

    @Override
    public String getQuotedColumn(String column) {
        return column;
    }

    public abstract List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source);

    public abstract String getPageSql(String sql);

    /** 拼接 insert 列与 values 多行：insert into t (c1,c2) values (..),(..) */
    protected String buildInsertValues(String table, List<String> insertColumns, List<List<String>> valueRows) {
        StringBuilder sb = new StringBuilder();
        sb.append("insert into ").append(table).append(" (");
        sb.append(String.join(",", insertColumns)).append(") values ");
        StringJoiner rows = new StringJoiner(",");
        for (List<String> row : valueRows) {
            rows.add("(" + String.join(",", row) + ")");
        }
        sb.append(rows);
        return sb.toString();
    }

    /**
     * 生成 SQL 标准的 ON CONFLICT 子句体（供 PostgreSQL/SQLite 共用）。
     * @param excludedPrefix 引用新插入值的前缀，如 "excluded"
     */
    protected String buildOnConflict(List<String> keyColumns, LinkedHashMap<String, String> updateColumns,
                                     boolean ignore, String excludedPrefix) {
        if (keyColumns == null || keyColumns.isEmpty()) {
            throw new IllegalArgumentException("当前方言 upsert 必须指定 keyColumns（冲突判定列）");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" on conflict (").append(String.join(",", keyColumns)).append(") ");
        if (ignore) {
            sb.append("do nothing");
        } else {
            StringJoiner sets = new StringJoiner(",");
            for (Map.Entry<String, String> e : updateColumns.entrySet()) {
                String col = e.getKey();
                String expr = e.getValue();
                sets.add(col + "=" + (expr != null ? expr : excludedPrefix + "." + col));
            }
            sb.append("do update set ").append(sets);
        }
        return sb.toString();
    }

    /**
     * 生成 MERGE 语句（供 Oracle/SQLServer 共用），源行构造方式由 useValuesClause 决定：
     * - true（SQLServer 2016+）：USING (VALUES (..),(..)) src (c1,c2)
     * - false（Oracle）：USING (SELECT v1 c1,v2 c2 FROM dual UNION ALL ...) src
     *
     * @param dual            Oracle 的 from 源（"dual"）；SQLServer 传 null
     * @param endSemicolon    语句末尾是否加分号（SQLServer 需要）
     */
    protected String buildMergeSql(String table, List<String> insertColumns, List<List<String>> valueRows,
                                   List<String> keyColumns, LinkedHashMap<String, String> updateColumns,
                                   boolean ignore, boolean useValuesClause, String dual, boolean endSemicolon) {
        if (keyColumns == null || keyColumns.isEmpty()) {
            throw new IllegalArgumentException("当前方言 upsert 必须指定 keyColumns（冲突判定列）");
        }
        String src = "src";
        StringBuilder sb = new StringBuilder();
        sb.append("merge into ").append(table).append(" tgt using (");
        if (useValuesClause) {
            // VALUES (..),(..)  AS src (c1,c2)
            StringJoiner rows = new StringJoiner(",");
            for (List<String> row : valueRows) rows.add("(" + String.join(",", row) + ")");
            sb.append("values ").append(rows).append(") ").append(src).append(" (")
              .append(String.join(",", insertColumns)).append(")");
        } else {
            // SELECT v1 c1, v2 c2 FROM dual UNION ALL ...
            StringJoiner selects = new StringJoiner(" union all ");
            for (List<String> row : valueRows) {
                StringJoiner cols = new StringJoiner(",");
                for (int i = 0; i < insertColumns.size(); i++) {
                    cols.add(row.get(i) + " " + insertColumns.get(i));
                }
                selects.add("select " + cols + " from " + dual);
            }
            sb.append(selects).append(") ").append(src);
        }
        // on (tgt.k = src.k and ...)
        StringJoiner on = new StringJoiner(" and ");
        for (String k : keyColumns) on.add("tgt." + k + "=" + src + "." + k);
        sb.append(" on (").append(on).append(")");
        // when matched then update
        if (!ignore) {
            StringJoiner sets = new StringJoiner(",");
            for (Map.Entry<String, String> e : updateColumns.entrySet()) {
                String col = e.getKey();
                String expr = e.getValue();
                sets.add("tgt." + col + "=" + (expr != null ? expr : src + "." + col));
            }
            sb.append(" when matched then update set ").append(sets);
        }
        // when not matched then insert
        StringJoiner insCols = new StringJoiner(",");
        StringJoiner insVals = new StringJoiner(",");
        for (String c : insertColumns) {
            insCols.add(c);
            insVals.add(src + "." + c);
        }
        sb.append(" when not matched then insert (").append(insCols).append(") values (").append(insVals).append(")");
        if (endSemicolon) sb.append(";");
        return sb.toString();
    }


}

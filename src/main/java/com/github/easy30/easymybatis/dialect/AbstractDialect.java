package com.github.easy30.easymybatis.dialect;

import com.github.easy30.easymybatis.enums.ColumnOperator;
import com.github.easy30.easymybatis.utils.Utils;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;

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
        if (s.matches(".*with\\s+.*as.*")) { //with as , only simple match
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


}

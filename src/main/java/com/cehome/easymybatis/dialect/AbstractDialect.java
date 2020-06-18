package com.cehome.easymybatis.dialect;

import com.cehome.easymybatis.enums.ColumnOperator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;

/**
 * coolma 2019/11/11
 **/
public abstract class AbstractDialect implements Dialect{
    @Override
    public String getCountSql(String sql)
    {

        sql=sql.trim();
        if(sql.startsWith("(") &&sql.endsWith(")")) sql=sql.substring(1,sql.length()-1);
        //sql = StringUtils.trimLeft(sql, new char[] { '(' }, true);
        //sql = StringUtils.trimRight(sql, new char[] { ')' }, true);
        int nLevel = 1;
        int nFrom = -1;
        int nOrderBy = -1;
        boolean groupBy = false;

        String s = sql.toLowerCase();
        for (int i = 0; i < s.length(); i++)
        {
            //todo: () in strings
            if (s.charAt(i) == '(')
                nLevel++;
            else if (s.charAt(i) == ')')
                nLevel--;
            else if (s.startsWith(" from ", i) && nLevel == 1)
                nFrom = i;
            else if (s.startsWith(" group ", i) && nLevel == 1 && s.substring(i + 7).trim().startsWith("by "))
            {
                groupBy = true;
            }
            else if (s.startsWith(" order ", i) && nLevel == 1 && s.substring(i + 7).trim().startsWith("by "))
            {
                nOrderBy = i;
                break;
            }

        }
        if (nOrderBy != -1)
            sql = sql.substring(0, nOrderBy);
        if (!groupBy)
            return "select count(*) " + sql.substring(nFrom);
        else
            return "select count(*) from ( " + sql + " ) t_table_count ";

    }

    @Override
    public String[] getColumnOperatorValue(ColumnOperator columnOperator){
        return columnOperator.getValue();
    }

    public abstract List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source);
    public abstract String getPageSql(String sql);

}

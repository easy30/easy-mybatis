package com.cehome.easymybatis.dialect;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * coolma 2019/11/11
 **/
public class OracleDialect extends AbstractDialect {
    @Override
    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source) {
        List<ParameterMapping> result = new ArrayList<ParameterMapping>();
        result.addAll(source);
        result.add(new ParameterMapping.Builder(configuration, "page.pageOffsetEnd", Integer.TYPE).build());
        result.add(new ParameterMapping.Builder(configuration, "page.pageOffset", Integer.TYPE).build());
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
}

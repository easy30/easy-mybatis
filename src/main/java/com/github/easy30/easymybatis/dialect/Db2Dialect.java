package com.github.easy30.easymybatis.dialect;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * coolma 2019/11/11
 **/
public class Db2Dialect extends AbstractDialect {

    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source){
        List<ParameterMapping> result = new ArrayList<ParameterMapping>();
        result.addAll(source);
        result.add(new ParameterMapping.Builder(configuration, "page.recordStart", Integer.TYPE).build());
        result.add(new ParameterMapping.Builder(configuration, "page.recordEnd", Integer.TYPE).build());
        return result;

    }

    public String getPageSql(String sql) {
        // offset from 1 not 0
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM (SELECT PAGE_TABLE_SOURCE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ");
        sb.append(sql);
        sb.append(" ) AS PAGE_TABLE_E) PAGE_TABLE_SOURCE WHERE ROW_ID BETWEEN 1+? AND 1+?");
        return sb.toString();
    }
}

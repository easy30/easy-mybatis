package com.cehome.easymybatis.dialect;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

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
}

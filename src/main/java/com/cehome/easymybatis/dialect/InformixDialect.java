package com.cehome.easymybatis.dialect;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * coolma 2019/11/11
 **/
public class InformixDialect extends AbstractDialect {

    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source){
        List<ParameterMapping> result = new ArrayList<ParameterMapping>();
        result.addAll(source);
        result.add(new ParameterMapping.Builder(configuration, "page.pageOffset", Integer.TYPE).build());
        result.add(new ParameterMapping.Builder(configuration, "page.pageSize", Integer.TYPE).build());
        return result;

    }

    public String getPageSql(String sql) {
        int n=sql.toLowerCase().indexOf("select")+6;
        return sql.substring(0,n)+" skip ? first ? "+sql.substring(6);

    }
}

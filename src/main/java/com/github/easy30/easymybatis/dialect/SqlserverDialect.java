package com.github.easy30.easymybatis.dialect;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * coolma 2019/11/11
 **/
public class SqlserverDialect extends AbstractDialect {

    public List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source){
        List<ParameterMapping> result = new ArrayList<ParameterMapping>();
        result.addAll(source);
        result.add(new ParameterMapping.Builder(configuration, "page.recordStart", Integer.TYPE).build());
        result.add(new ParameterMapping.Builder(configuration, "page.pageSize", Integer.TYPE).build());
        return result;

    }

    public String getPageSql(String sql) {

        StringBuilder sb = new StringBuilder();
        sb.append(sql);
        sb.append(" OFFSET ? ");
        sb.append(" ROWS FETCH NEXT ? ROWS ONLY ");
        return sb.toString();
    }
}

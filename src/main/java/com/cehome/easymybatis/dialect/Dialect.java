package com.cehome.easymybatis.dialect;

import com.cehome.easymybatis.enums.ColumnOperator;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;

import java.util.List;

public interface Dialect {
    String getCountSql(String sql);
    List<ParameterMapping> getPageParameterMapping(Configuration configuration, List<ParameterMapping> source);
    String getPageSql(String sql);
    String[] getColumnOperatorValue(ColumnOperator columnOperator);
    String addWhereIfNeed(String condition);
}

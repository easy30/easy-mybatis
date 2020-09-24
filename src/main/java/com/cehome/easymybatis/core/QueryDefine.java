package com.cehome.easymybatis.core;

import com.cehome.easymybatis.MapperException;
import com.cehome.easymybatis.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  condition= where +groupBy+ orderBy +other
 */
public class QueryDefine {
    private static Logger logger = LoggerFactory.getLogger(QueryDefine.class);
    private String columns;
    private String tables;
    private String condition;
    private String where;
    private String groupBy;
    private String orderBy;
    private String other;
    private String set;
    private int sqlType;
    public QueryDefine(int sqlType){
     this.sqlType=sqlType;
    }
    public int getSqlType(){
        return this.sqlType;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    private String toSelect(){
        String condition=StringUtils.trimToEmpty(this.condition);
        if(StringUtils.isBlank(condition)) {
            if (StringUtils.isNotBlank(getWhere())) {
                condition += " where " + getWhere();
            }
            if (StringUtils.isNotBlank(getGroupBy())) {
                condition += " group by " + getGroupBy();
            }
            if (StringUtils.isNotBlank(getOrderBy())) {
                condition += " order by " + getOrderBy();
            }
            if (StringUtils.isNotBlank(getOther())) {
                condition += " " + getOther();
            }
        }
        return Utils.format(Global.SQL_SELECT, getColumns(), getTables(), condition);
    }
    private String toDelete(){
        String condition=StringUtils.trimToEmpty(this.condition);
        if(StringUtils.isBlank(condition)) {
            if (StringUtils.isNotBlank(getWhere())) {
                condition += " where " + getWhere();
            }
        }

     /*   if(StringUtils.isNotBlank(getOrderBy())){
            condition+=" order by "+getOrderBy();
        }
        if(StringUtils.isNotBlank(getOther())){
            condition+=" "+getOther();
        }*/
        return Utils.format(Global.SQL_DELETE, "", getTables(), condition);
    }
    private String toUpdate(){
        String condition=StringUtils.trimToEmpty(this.condition);
        if(StringUtils.isBlank(condition)) {
            if (StringUtils.isNotBlank(getWhere())) {
                condition += " where " + getWhere();
            }
        }
        if(StringUtils.isBlank(set)) throw new MapperException("set values condition need");

        return Utils.format(Global.SQL_UPDATE, getTables(), getSet(),condition);
    }

    public String toSQL(){
        if(sqlType== Global.SQL_TYPE_SELECT){
            String sql= toSelect();
            logger.debug("provider sql= {}",sql);
            return sql;
        }else if(sqlType== Global.SQL_TYPE_UPDATE){
            String sql= toUpdate();
            logger.debug("provider sql= {}",sql);
            return sql;

        }else if(sqlType== Global.SQL_TYPE_DELETE){
            String sql= toDelete();
            logger.debug("provider sql= {}",sql);
            return sql;
        }

        throw new MapperException("not support sqlType="+sqlType);
    }
}

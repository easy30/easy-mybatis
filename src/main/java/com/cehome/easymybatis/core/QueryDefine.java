package com.cehome.easymybatis.core;

import com.cehome.easymybatis.MapperException;
import com.cehome.easymybatis.utils.Utils;
import org.apache.commons.lang3.StringUtils;

public class QueryDefine {
    private String columns;
    private String tables;
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
        String condition="";
        if(StringUtils.isNotBlank(getWhere())){
            condition+=" where "+getWhere();
        }
        if(StringUtils.isNotBlank(getGroupBy())){
            condition+=" group by "+getGroupBy();
        }
        if(StringUtils.isNotBlank(getOrderBy())){
            condition+=" order by "+getOrderBy();
        }
        if(StringUtils.isNotBlank(getOther())){
            condition+=" "+getOther();
        }
        return Utils.format(Global.SQL_SELECT, getColumns(), getTables(), condition);
    }
    private String toDelete(){
        String condition="";
        if(StringUtils.isNotBlank(getWhere())){
            condition+=" where "+getWhere();
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
        String condition="";
        if(StringUtils.isNotBlank(getWhere())){
            condition+=" where "+getWhere();
        }
        if(StringUtils.isBlank(set)) throw new MapperException("set values condition need");

        return Utils.format(Global.SQL_UPDATE, getTables(), getSet(),condition);
    }

    public String toSQL(){
        if(sqlType== Global.SQL_TYPE_SELECT){
            return toSelect();
        }else if(sqlType== Global.SQL_TYPE_UPDATE){
            return toUpdate();

        }else if(sqlType== Global.SQL_TYPE_DELETE){
            return toDelete();
        }
        throw new MapperException("not support sqlType="+sqlType);
    }
}

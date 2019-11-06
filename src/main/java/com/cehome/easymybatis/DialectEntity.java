package com.cehome.easymybatis;

/**
 * coolma 2019/11/1
 **/

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class DialectEntity implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;


    @Transient
    private Map<String, String> dialectMap = null;


   /* public Map<String, String> getDialectMap() {
        return dialectMap;
    }

    public void setDialectMap(Map<String, String> dialectMap) {
        this.dialectMap = dialectMap;
    }*/

    public DialectEntity(){


    }

    /**
     * 利用sql语句值（函数等）给实体类的属性赋值，如 setSqlValue("myTime","sysdate")
     * 调用此方法后，则实体类属性本身的set方法将不起作用。
     *
     * @param property
     * @param dialectValue
     */
    public void put(String property, String dialectValue) {
        if (dialectMap == null) dialectMap = new HashMap<String, String>();
        dialectMap.put(property, dialectValue);

    }

/*

    @Transient
    public String findSqlValue(String property) {
        if (sqlValueMap != null) return sqlValueMap.get(property);
        return null;
    }


    public boolean hasSqlValue() {

        return sqlValueMap != null && sqlValueMap.size() > 0;
    }*/



}

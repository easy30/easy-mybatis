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
     * set value by sql.
     * put("myTime","now()")
     *
     *
     * @param property
     * @param dialectValue
     */
    public void put(String property, String dialectValue) {
        if (dialectMap == null) dialectMap = new HashMap<String, String>();
        dialectMap.put(property, dialectValue);

    }


}

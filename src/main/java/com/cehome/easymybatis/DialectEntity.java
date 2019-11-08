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
    private Map<String, String> valueMap = null;
    @Transient
    private Map<String, String> paramMap = null;


    public DialectEntity(){


    }

    /**
     * set sql value for insert or update
     * put("updateTime","now()")
     *
     *
     * @param property
     * @param dialectValue
     */
    public void setValue(String property, String dialectValue) {
        if (valueMap == null) valueMap = new HashMap<String, String>();
        valueMap.put(property, dialectValue);

    }

    /**
     * set sql param for select
     * @param property
     * @param dialectValue
     */
    public void setParam(String property, String dialectValue) {
        if (paramMap == null) paramMap = new HashMap<String, String>();
        paramMap.put(property, dialectValue);

    }


}

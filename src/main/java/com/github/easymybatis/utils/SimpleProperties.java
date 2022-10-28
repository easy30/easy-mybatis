package com.github.easymybatis.utils;

import java.util.Map;

/**
 * coolma 2019/11/19
 **/
public abstract class SimpleProperties {

    public abstract String[] getProperties();
    public abstract Object getValue(String prop);


    public static SimpleProperties create(Object source){
        if(source instanceof Map) return new MapProperties((Map)source);
        return new ObjectProperties(source);

    }

}

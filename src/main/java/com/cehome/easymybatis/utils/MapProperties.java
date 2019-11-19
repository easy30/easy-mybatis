package com.cehome.easymybatis.utils;

import java.util.Map;

/**
 * coolma 2019/11/19
 **/
public class MapProperties extends SimpleProperties {
    private Map<String,Object> map;
    public MapProperties(Map<String,Object> map){
        this.map=map;
    }

    @Override
    public String[] getProperties() {
        return map.keySet().toArray(new String[0]);
    }

    @Override
    public Object getValue(String prop) {
        return map.get(prop);
    }
}

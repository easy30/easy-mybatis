package com.cehome.easymybatis.core;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MapperOptionSupport {
    public static Set<String> getIgnoreColumnSet(MapperOption... options){
        for(MapperOption option:options){
            if(ArrayUtils.isNotEmpty(option.getIgnoreColumns())){

                return new HashSet<>(Arrays.asList(option.getIgnoreColumns()));
            }
        }
        return null;
    }

    public static Map<String,String> getExtraColVals(MapperOption... options){
        for(MapperOption option:options){
            if(ArrayUtils.isNotEmpty(option.getColumnAndValues())){
                Map<String,String> map=new HashMap<>();
                for(int i = 0; i<option.getColumnAndValues().length; i+=2) {
                    map.put(option.getColumnAndValues()[i],""+option.getColumnAndValues()[i+1]);// ""+null to "null"
                }
                return map;

            }
        }
        return null;
    }

    public static String getAndRemove(Map<String,String> extraColVals, String key1, String key2){
        String value=null;
        if(extraColVals!=null){
            value=extraColVals.get(key1);
            if(value!=null){

                extraColVals.remove(key1);
            }else {
                value = extraColVals.get(key2);
                if(value!=null){
                    extraColVals.remove(key2);
                }
            }
        }
        return value;
    }

    public static String getTable(MapperOption... options){
        if(options!=null)
        for(MapperOption option:options){
            if(StringUtils.isNotEmpty(option.getTable())){
                return option.getTable();
            }
        }
        return null;
    }


    public static String getTable(EntityAnnotation entityAnnotation, MapperOption... options){
        String table=getTable(options);
        if(table==null) table=entityAnnotation.getTable();
        return table;
    }

}

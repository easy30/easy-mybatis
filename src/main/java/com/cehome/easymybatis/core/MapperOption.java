package com.cehome.easymybatis.core;

public class MapperOption {

    protected String[] extraColVals;
    protected String[] ignoreColumns;
    protected String table;


    protected String[] getExtraColVals(){
        return extraColVals;
    }
    protected String[] getIgnoreColumns(){
        return ignoreColumns;
    }
    protected  String getTable(){
        return table;
    }
}

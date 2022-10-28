package com.github.easy30.easymybatis.core;

public class MapperOption {

    protected String[] columnAndValues;
    protected String[] ignoreColumns;
    protected String table;


    protected String[] getColumnAndValues(){
        return columnAndValues;
    }
    protected String[] getIgnoreColumns(){
        return ignoreColumns;
    }
    protected  String getTable(){
        return table;
    }
}

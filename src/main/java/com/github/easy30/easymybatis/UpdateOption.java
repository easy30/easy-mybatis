package com.github.easy30.easymybatis;

import com.alibaba.fastjson.JSON;
import com.github.easy30.easymybatis.core.MapperOption;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import static com.alibaba.fastjson.parser.Feature.SupportNonPublicField;

@Getter
public class UpdateOption extends MapperOption {

    private String[] columnAndValues;
    private String[] ignoreColumns;
    private boolean  withNullColumns;

    public static UpdateOption create(){
        return new UpdateOption();
    }

    public UpdateOption columnAndValues(String... columnAndValues){
        this.columnAndValues=columnAndValues;
        return this;
    }
    public UpdateOption ignoreColumns(String... ignoreColumns){

        this.ignoreColumns=ignoreColumns;

        return this;
    }

    public UpdateOption table(String table){

        this.table=table;

        return this;
    }

    public UpdateOption withNullColumns(boolean withNullColumns){
        this.withNullColumns=withNullColumns;
        return this;
    }

    public UpdateOption ignoreQueryAnnotation(boolean ignoreQueryAnnotation){
        this.ignoreQueryAnnotation=ignoreQueryAnnotation;
        return this;
    }


    public static UpdateOption parse(String s){
        return JSON.parseObject(s,UpdateOption.class,SupportNonPublicField); //private field;
    }

    /*public static void main(String[] args) {
        UpdateOption option=new UpdateOption();
        option.table("table1");
        option.ignoreColumns("c1","c2");
        System.out.println(option.toString());
        String t=option.toString();
        System.out.println(JSON.parseObject(t,UpdateOption.class,SupportNonPublicField).getTable());
    }*/


}

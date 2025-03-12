package com.github.easy30.easymybatis;

import com.alibaba.fastjson.JSON;
import com.github.easy30.easymybatis.core.MapperOption;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.alibaba.fastjson.parser.Feature.SupportNonPublicField;

@Getter
public class UpdateOption extends MapperOption {

    private String[] columnAndValues;
    private String[] ignoreColumns;
    private boolean  withNullColumns;
    private Map params;

    public static UpdateOption create(){
        return new UpdateOption();
    }

    /**
     * set multi columns(or property) and values (sql text)
     * example:
     * 1)columnAndValues("create_time" , "now()", "user_name" ,null) , means "set create_time=now(), user_name =null" ;
     * 2)columnAndValues("create_time" , "#{ctime}").columnAndValueParams("ctime","2024-4-11");
     * @param columnAndValues  column1 , sql value1, property2 , sql value2, ...
     * @return
     */
    public UpdateOption columnAndValues(String... columnAndValues){
        this.columnAndValues=columnAndValues;
        return this;
    }

    /**
     * params(name and value pairs) for columnAndValues()
     * @param params
     * @return
     */
    public UpdateOption columnAndValueParams(Object... params){
        if(this.params ==null) this.params =new LinkedHashMap();
        for(int i=0;i<params.length;i+=2){
            this.params.put(params[i],params[i+1]);
        }
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

    /**
     * update all columns include null columns (set column =null)
     * @param withNullColumns
     * @return
     */
    public UpdateOption withNullColumns(boolean withNullColumns){
        this.withNullColumns=withNullColumns;
        return this;
    }

    public UpdateOption ignoreQueryAnnotation(boolean ignoreQueryAnnotation){
        this.ignoreQueryAnnotation=ignoreQueryAnnotation;
        return this;
    }

    public UpdateOption queryEmptyStringParam(Boolean queryEmptyStringParam){
        this.queryEmptyStringParam=queryEmptyStringParam;
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

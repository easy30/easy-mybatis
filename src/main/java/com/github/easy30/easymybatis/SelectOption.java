package com.github.easy30.easymybatis;

import com.alibaba.fastjson.JSON;
import com.github.easy30.easymybatis.core.MapperOption;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.fastjson.parser.Feature.SupportNonPublicField;

@Getter
public class SelectOption extends MapperOption  {
    protected Integer foreignColumnThreshold;
    public static SelectOption create(){
        return new SelectOption();
    }
    public SelectOption table(String table){
        this.table=table;
        return this;
    }

    public SelectOption ignoreQueryAnnotation(boolean ignoreQueryAnnotation){
        this.ignoreQueryAnnotation=ignoreQueryAnnotation;
        return this;
    }

    public SelectOption ignoreForeignColumn(Integer foreignColumnThreshold){
        this.foreignColumnThreshold=foreignColumnThreshold;
        return this;
    }

    public static SelectOption parse(String s){
        return JSON.parseObject(s,SelectOption.class,SupportNonPublicField); //private field;
    }


}

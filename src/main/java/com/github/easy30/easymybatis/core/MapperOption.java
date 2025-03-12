package com.github.easy30.easymybatis.core;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.Getter;

@Getter
public class MapperOption {
    protected String table;
    protected boolean  ignoreQueryAnnotation;
    protected Boolean queryEmptyStringParam;

    @Override
    public String toString(){
        return JSON.toJSONString(this);
    }

}

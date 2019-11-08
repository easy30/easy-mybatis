package com.cehome.easymybatis.generator;

import com.cehome.easymybatis.Generation;

/**
 * coolma 2019/11/6
 **/
public class OracleSQLValueGeneration implements Generation {


    @Override
    public Type getType() {
        return Type.SQL_VALUE;
    }

    @Override
    public Object generate(Object entity, String table, String property,String arg) {
       return arg+".nextval";
    }
}

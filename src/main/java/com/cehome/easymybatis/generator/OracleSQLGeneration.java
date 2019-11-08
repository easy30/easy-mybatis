package com.cehome.easymybatis.generator;

import com.cehome.easymybatis.Generation;

/**
 * coolma 2019/11/6
 **/
public class OracleSQLGeneration implements Generation {


    @Override
    public Type getType() {
        return Type.SELECT_KEY_SQL;
    }

    @Override
    public Object generate(Object entity, String table, String property,String arg) {
        return "select "+arg+".nextval from dual";
    }
}

package com.cehome.easymybatis.generation;

import com.cehome.easymybatis.Generation;

import java.util.UUID;

/**
 * coolma 2019/11/6
 **/
public class UUIDGeneration implements Generation {




    @Override
    public Object generate(Object entity, String table, String property,String arg) {
        return  UUID.randomUUID().toString();
    }
}

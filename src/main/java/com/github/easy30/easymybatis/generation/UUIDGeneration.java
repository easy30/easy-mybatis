package com.github.easy30.easymybatis.generation;

import com.github.easy30.easymybatis.Generation;

import java.util.UUID;

/**
 * coolma 2019/11/6
 * you need to create a spring bean.
 **/
public class UUIDGeneration implements Generation {

    @Override
    public Object generate(Object entity, String table, String property,String arg) {
        return  UUID.randomUUID().toString();
    }
}

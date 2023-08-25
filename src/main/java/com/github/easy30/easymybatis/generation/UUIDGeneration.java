package com.github.easy30.easymybatis.generation;

import com.github.easy30.easymybatis.Generation;
import com.github.easy30.easymybatis.GenerationContext;

import java.util.UUID;

/**
 * coolma 2019/11/6
 * you need to create a spring bean.
 **/
public class UUIDGeneration implements Generation {

    @Override
    public Object generate(GenerationContext context) {
        return  UUID.randomUUID().toString();
    }

}

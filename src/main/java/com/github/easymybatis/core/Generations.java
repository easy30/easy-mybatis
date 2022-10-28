package com.github.easymybatis.core;

import com.github.easymybatis.Generation;

import java.util.HashMap;
import java.util.Map;

/**
 * coolma 2019/11/7
 **/
public class Generations {

    private static Generations instance=new Generations();
    private Map<String, Generation> map=new HashMap();

    public static Generations getInstance(){
        return instance;
    }

    public void put(String name , Generation generation){
        map.put(name, generation);
    }
    public void putAll( Map<String, Generation> generatorMap){
        map.putAll( generatorMap);
    }

    public Generation get(String name){
        return map.get(name);
    }


}

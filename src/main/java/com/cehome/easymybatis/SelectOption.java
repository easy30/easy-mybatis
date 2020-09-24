package com.cehome.easymybatis;

import com.cehome.easymybatis.core.MapperOption;

public class SelectOption extends MapperOption {

    /**
     * change to another table
     * @param table
     * @return
     */
    public static SelectOption table(String table){

        SelectOption option=   new SelectOption();
        option.table=table;
        return option;
    }


}

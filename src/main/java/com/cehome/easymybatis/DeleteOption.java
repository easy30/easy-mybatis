package com.cehome.easymybatis;

import com.cehome.easymybatis.core.MapperOption;

public class DeleteOption extends MapperOption {

    /**
     * change to another table
     * @param table
     * @return
     */
    public static DeleteOption table(String table){

        DeleteOption option=   new DeleteOption();
        option.table=table;
        return option;
    }




}

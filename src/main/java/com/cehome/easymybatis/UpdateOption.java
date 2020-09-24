package com.cehome.easymybatis;

import com.cehome.easymybatis.core.MapperOption;

public class UpdateOption extends MapperOption {



   public static UpdateOption addColumnValues(String... columnAndValues){

       UpdateOption option=   new UpdateOption();
       option.columnAndValues =columnAndValues;
       return option;
    }

    public static UpdateOption ignoreColumns(String... ignoreColumns){

        UpdateOption option=   new UpdateOption();
        option.ignoreColumns=ignoreColumns;
        return option;
    }

    /**
     * change to another table
     * @param table
     * @return
     */
    public static UpdateOption table(String table){

        UpdateOption option=   new UpdateOption();
        option.table=table;
        return option;
    }



}

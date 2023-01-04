package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.core.MapperOption;
import lombok.Data;

@Data
public class UpdateOption extends MapperOption {

    private String[] columnAndValues;
    private String[] ignoreColumns;
    private boolean  withNullColumns;

    public static UpdateOption create(){
        return new UpdateOption();
    }

    public UpdateOption columnAndValues(String... columnAndValues){
        this.setColumnAndValues(columnAndValues);
        return this;
    }
    public UpdateOption ignoreColumns(String... ignoreColumns){

        this.ignoreColumns=ignoreColumns;

        return this;
    }

    public UpdateOption table(String table){

        this.table=table;

        return this;
    }

    public UpdateOption withNullColumns(boolean withNullColumns){
        this.withNullColumns=withNullColumns;
        return this;
    }






}

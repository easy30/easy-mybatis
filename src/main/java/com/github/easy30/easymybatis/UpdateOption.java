package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.core.MapperOption;

import java.util.ArrayList;
import java.util.List;

public class UpdateOption extends MapperOption {

    public static class Builder{
        private List<UpdateOption> options=new ArrayList();
        public Builder addColumnValues(String... columnAndValues){
            UpdateOption option=   new UpdateOption();
            option.columnAndValues =columnAndValues;
            options.add(option);
            return this;
        }
        public Builder ignoreColumns(String... ignoreColumns){
            UpdateOption option=   new UpdateOption();
            option.ignoreColumns=ignoreColumns;
            options.add(option);
            return this;
        }
        /**
         * change to another table
         * @param table
         * @return
         */
        public Builder table(String table){
            UpdateOption option=   new UpdateOption();
            option.table=table;
            options.add(option);
            return this;
        }
        public UpdateOption[] build(){
            return options.toArray(new UpdateOption[0]);
        }
    }
    public static Builder builder(){
        return new Builder();
    }





}

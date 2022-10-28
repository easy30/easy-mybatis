package com.github.easymybatis;

import com.github.easymybatis.core.MapperOption;

import java.util.ArrayList;
import java.util.List;

public class DeleteOption extends MapperOption {

    public static class Builder{
        private List<DeleteOption> options=new ArrayList();

        /**
         * change to another table
         * @param table
         * @return
         */
        public Builder table(String table){
            DeleteOption option=   new DeleteOption();
            option.table=table;
            options.add(option);
            return this;
        }
        public DeleteOption[] build(){
            return options.toArray(new DeleteOption[0]);
        }
    }
    public static Builder builder(){
        return new Builder();
    }


}

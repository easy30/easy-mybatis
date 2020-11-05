package com.cehome.easymybatis;

import com.cehome.easymybatis.core.MapperOption;

import java.util.ArrayList;
import java.util.List;

public class SelectOption extends MapperOption {


    public static class Builder{
        private List<SelectOption> options=new ArrayList();
        
        /**
         * change to another table
         * @param table
         * @return
         */
        public Builder table(String table){
            SelectOption option=   new SelectOption();
            option.table=table;
            options.add(option);
            return this;
        }
        public SelectOption[] build(){
            return options.toArray(new SelectOption[0]);
        }
    }
    public static Builder builder(){
        return new Builder();
    }
    


}

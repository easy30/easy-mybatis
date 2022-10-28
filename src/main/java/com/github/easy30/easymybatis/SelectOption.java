package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.core.MapperOption;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data

public class SelectOption extends MapperOption  {


    public static SelectOption create(){
        return new SelectOption();
    }
    public SelectOption table(String table){
        this.table=table;
        return this;
    }

}

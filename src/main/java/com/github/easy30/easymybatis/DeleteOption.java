package com.github.easy30.easymybatis;

import com.github.easy30.easymybatis.core.MapperOption;

import java.util.ArrayList;
import java.util.List;

public class DeleteOption extends MapperOption {


    public static DeleteOption create(){
        return new DeleteOption();
    }
    public DeleteOption table(String table){

        this.table=table;

        return this;
    }


}

package com.github.easy30.easymybatis.core;

import lombok.Data;
import lombok.Getter;

@Getter
public class MapperOption {
    protected String table;
    protected boolean  ignoreQueryAnnotation;

}

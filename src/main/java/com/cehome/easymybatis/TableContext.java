package com.cehome.easymybatis;

import com.cehome.easymybatis.core.EntityAnnotation;

import java.io.Closeable;
import java.io.IOException;

public class TableContext implements Closeable {
    EntityAnnotation entityAnnotation;
    public  TableContext(EntityAnnotation entityAnnotation){
        this.entityAnnotation=entityAnnotation;
    }


    @Override
    public void close() throws IOException {
        entityAnnotation.removeContextTable();
    }
}

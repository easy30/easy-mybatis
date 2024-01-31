package com.github.easy30.easymybatis.core;

import com.github.easy30.easymybatis.Generation;
import com.github.easy30.easymybatis.GenerationContext;
import com.github.easy30.easymybatis.annotation.ColumnGeneration;
import com.github.easy30.easymybatis.utils.ObjectSupport;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ColumnGenerationHandler {
    private   ColumnGeneration columnGeneration;
    private EntityAnnotation entityAnnotation;
    private boolean init;
    private Generation generation;
    private Generation insertGeneration;
    private Generation updateGeneration;
    public ColumnGenerationHandler(ColumnGeneration columnGeneration,EntityAnnotation entityAnnotation){
        this.columnGeneration=  columnGeneration;
        this.entityAnnotation=entityAnnotation;
    }

    public Generation getGeneration() {
        return generation;
    }

    public void setGeneration(Generation generation) {
        this.generation = generation;
    }

    public Generation getInsertGeneration() {
        return insertGeneration;
    }

    public void setInsertGeneration(Generation insertGeneration) {
        this.insertGeneration = insertGeneration;
    }

    public Generation getUpdateGeneration() {
        return updateGeneration;
    }

    public void setUpdateGeneration(Generation updateGeneration) {
        this.updateGeneration = updateGeneration;
    }

    @SneakyThrows
    public Object getInsertValue(String table, Object entity, String property,Class propertyType){
        if(!init)doInit();
        if (columnGeneration != null) {
            Generation generation = getInsertGeneration();
            if (generation != null) {
                Method method = ObjectSupport.getMethod(generation.getClass(), columnGeneration.insertMethod(), GenerationContext.class);
                return method.invoke(generation, new GenerationContext(table, entity, property, propertyType, columnGeneration.insertArg()));

            } else {
                generation = getGeneration();
                if (generation != null) {
                    Method method = ObjectSupport.getMethod(generation.getClass(), columnGeneration.method(), GenerationContext.class);
                    return method.invoke(generation, new GenerationContext(table, entity, property, propertyType, columnGeneration.arg()));

                }
            }
        }
        return null;
    }

    @SneakyThrows
    public Object getUpdateValue(String table, Object entity, String property,Class propertyType){
        if(!init)doInit();
        Generation generation =  getUpdateGeneration();
        if(generation!=null) {
            Method method = ObjectSupport.getMethod(generation.getClass(), columnGeneration.updateMethod(), GenerationContext.class);
           return method.invoke(generation,new GenerationContext(table, entity, property,propertyType,  columnGeneration.updateArg()));
        }else {
            generation = getGeneration();
            if(generation!=null){
                Method method = ObjectSupport.getMethod(generation.getClass(), columnGeneration.method(), GenerationContext.class);
               return method.invoke(generation,new GenerationContext(table, entity, property, propertyType, columnGeneration.arg()));
            }
        }

        return null;

    }

    private synchronized void doInit() {
        if(init) return;
        if (columnGeneration != null) {

            Map<String, Generation> generations =entityAnnotation.getEasyConfiguration()!=null?
                    entityAnnotation.getEasyConfiguration().getGenerations():
                    entityAnnotation.getMapperFactory().getGenerations();
            String generatorName = columnGeneration.generation();
            if(StringUtils.isNotBlank(generatorName)) {
                Generation generation = generations.get(generatorName);
                if (generation == null) {
                    throw new RuntimeException("Generator bean '" + generatorName + "' not found for entity class " + entityAnnotation.getEntityClass());
                }
                 setGeneration(generation);
            }

            generatorName = columnGeneration.insertGeneration();
            if(StringUtils.isNotBlank(generatorName)) {
                Generation generation = generations.get(generatorName);
                if (generation == null) {
                    throw new RuntimeException("Insert Generator bean '" + generatorName + "' not found for entity class " + entityAnnotation.getEntityClass());
                }
                setInsertGeneration(generation);
            }

            generatorName = columnGeneration.updateGeneration();
            if(StringUtils.isNotBlank(generatorName)) {
                Generation generation = generations.get(generatorName);
                if (generation == null) {
                    throw new RuntimeException("Update Generator bean '" + generatorName + "' not found for entity class " + entityAnnotation.getEntityClass());
                }
                setUpdateGeneration(generation);
            }

        }
        init=true;
    }
}

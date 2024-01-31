package com.github.easy30.easymybatis;

public class GenerationContext {
    private String table;
    private Object entity;
    private String property;
    private Class propertyType;
    private String arg;
    public GenerationContext(String table, Object entity, String property,Class propertyType, String arg) {
        this.table = table;
        this.entity = entity;
        this.property = property;
        this.propertyType=propertyType;
        this.arg = arg;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public Class getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Class propertyType) {
        this.propertyType = propertyType;
    }
}

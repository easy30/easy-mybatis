package com.github.easy30.easymybatis;

public class EntityAndParams<E,P> {
   private E entity;
   private P params;

    public E getEntity() {
        return entity;
    }

    public void setEntity(E entity) {
        this.entity = entity;
    }

    public P getParams() {
        return params;
    }

    public void setParams(P params) {
        this.params = params;
    }
}

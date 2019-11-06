package com.cehome.easymybatis.provider;

import com.cehome.easymybatis.EntityAnnotation;
import com.cehome.easymybatis.utils.Utils;

/**
 * coolma 2019/10/28
 **/
public class EntityIdProvider<E> {

    public String delete(E entity) {
        Class entityClass=entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        return Utils.format(ProviderSupport.SQL_DELETE,"",entityAnnotation.getTable(),
                ProviderSupport.sqlWhereById(entity,entityAnnotation));
    }
    public String updateById(E entity) {
        Class entityClass=entity.getClass();
        EntityAnnotation entityAnnotation = EntityAnnotation.getInstance(entityClass);
        String set = ProviderSupport.sqlSetValues(entityClass,entityAnnotation.getPropertyColumnMap(),"");
        return Utils.format(ProviderSupport.SQL_UPDATE,entityAnnotation.getTable(),set,
                ProviderSupport.sqlWhereById(entity,entityAnnotation));
    }

}
